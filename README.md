# Kafka to Elastic with Logstash and Kibana

## Project Overview
This project demonstrates how to:
- Post data from Kafka to Elastic using Logstash.
- Dynamically create dashboards in Kibana to visualize the data.
- Integrate an external Java component for additional processing.

### Dataset
The dataset used for this project contains EEG data from human limb movement, sourced from the following paper: [EEG Data on Mendeley](https://data.mendeley.com/datasets/x8psbz3f6x/2).

A small subset of the original dataset is included in this project to simulate data streaming while retaining the original file format.

### Data Simulation
To simulate data arrival in Kafka, a custom script dynamically modifies dataset records by adding additional fields, extracted from the dataset's file naming conventions. These fields include:
- An incremental number
- The subject number
- The performed task
- Whether the task was imaginary or motor
- The current timestamp

The script, `publish.sh`, is located under the `full_dataset` folder and appends the current Linux machine timestamp to each published record. An artificial delay of 1 second is added to prevent multiple records from sharing the same timestamp, as many records are published within the same millisecond.

Original record example:
-80.00041198730469,-87.9839096069336,440.9456787109375,162.37144470214844,-89.1994857788086,-86.63407135009766,-65.12501525878906,213.51199340820312,672.4793090820312,-117.2621841430664,-283.0644226074219,-84.1676254272461,1196.9293212890625,-163.21888732910156,615.9774780273438,537.8173217773438

Published record example:
0,BEO,I,1,1,-80.00041198730469,-87.9839096069336,440.9456787109375,162.37144470214844,-89.1994857788086,-86.63407135009766,-65.12501525878906,213.51199340820312,672.4793090820312,-117.2621841430664,-283.0644226074219,-84.1676254272461,1196.9293212890625,-163.21888732910156,615.9774780273438,537.8173217773438,1734220322317

Refer to the linked paper for more details on the dataset file naming conventions.

---

## Setup

### Kafka
Make sure to replace the IP of the broker container from 192.168.1.76 to the IP of your VM/machine that the docker compose is running on.

Upon first running the Kafka cluster
```bash
docker compose up -d
```
the `kafka-connect` service will fail due to missing topics.
Follow these steps to resolve the issue:

1. Access the Kafka Control Center using the IP and port specified in the compose file of the machine running the broker.
2. Create the following topics:
   - `brain_data`
   - `kafka_connect_offset`
   - `kafka_connect_status`
   - `kafka_connect_config`

3. Set the retention period for the Kafka Connect topics to `compact`. The retention period itself is not critical.
4. Set the `Number of partitions` for these topics to 1.

Once the topics are configured, build the kafka-connect image 
```bash
docker compose up -d kafka-connect
```
 and the container should start successfully.

### Simulation Script
To utilize the data simulation script:

1. Connect to the `kafka-connect` container:
   ```bash
   docker exec -it kafka-connect /bin/bash
   ```

2. Run the `publish.sh` script:
   ```bash
   cd /tmp/full_dataset
   ./publish.sh
   ```

If no specific file is specified, the script will iterate through all dataset files which in our example contain only a small portion of the subjects from subjects 1 and 2.

---

### ELK

The basic ELK setup has been sourced from the repository over [here](https://github.com/deviantony/docker-elk/tree/tls), specifically the tls branch.

For the initial setup the instructions can be found also the docker-elk subfolder or the original repository.

If you choose to setup ELK from the original repository there are a number of updates that would need to do in order for the ELK to operate with the ELKInterface and kafka.

#### Kibana

In order to setup Kibana to operate through https with the use of the self-signed certificates from the tls setup you need to update kibana.yml:
```bash
server.ssl.enabled: true
```

#### Logstash
In order for Logstash to be able to communicate with Kafka add the following in the logstash/pipeline/logstash.yml "input" field:
```bash
kafka {
        bootstrap_servers => "192.168.1.76:9092"
        topics => ["brain_data"]
        group_id => "logstash-consumer-group"
}
```
Replace 192.168.1.76 with the IP of the Kafka broker.

The "filter" to convert the input from kafka to seperate fields and their correct type for querying purposes:
```bash
filter {
    csv {
        separator => "," # Set the separator to comma
        columns => ["ID","Task Identifier","Activity Type","Repetition","Subject","Electrode 1","Electrode 2","Electrode 3","Electrode 4","Electrode 5","Electrode 6","Electrode 7","Electrode 8","Electrode 9","Electrode 10","Electrode 11","Electrode 12","Electrode 13","Electrode 14","Electrode 15","Electrode 16","ts"]
        source => "message" # Specify the source field
        convert => {
                "ID" => "integer"
		"Repetition" => "integer"
		"Subject" => "integer"
		"Electrode 1" => "float"
		"Electrode 2" => "float"
		"Electrode 3" => "float"
		"Electrode 4" => "float"
		"Electrode 5" => "float"
		"Electrode 6" => "float"
		"Electrode 7" => "float"
		"Electrode 8" => "float"
		"Electrode 9" => "float"
		"Electrode 10" => "float"
		"Electrode 11" => "float"
		"Electrode 12" => "float"
		"Electrode 13" => "float"
		"Electrode 14" => "float"
		"Electrode 15" => "float"
		"Electrode 16" => "float"
        }
    }
    
    # Parse timestamp from the CSV data if available
    date {
        match => ["timestamp", "dd-MM-yyyy HH:mm:ss"] # Adjust format as needed
        target => "timestamp"
    }
}
```

Once Kafka and ELK are running, run the publish.sh script in order to publish some data to kafka and these should be visible through Kibana.

#### Kibana Dashboard Setup
Kibana does not provide a proper API for dashboard creation like it is done on their serverless offering.
For this reason the logics applied in order to interact with Kibana in a dynamic manner progrmmatically are specific to this project and the way they were developed is the following:

1. Due to the number of electrodes that were present in the dataset 16 template dashboards were created with an applied time filter. These dashboards display a combination of electrodes, from 1 up to 16 altogether.
```bash
curl -k -X GET "https://localhost:5601/api/saved_objects/_find?type=dashboard&per_page=100" -u elastic:${elastic_password}
```
3. The "saved_objects/_find" API was used in order to extract information about the dashboard unique identifier in Kibana
4. The "saved_objects/_export" API was used to store each dashboard to a specific template file. You can find these dashboards under the ELKInterface and import them to your local Kibana using the "saved_objects/_import" API although it's not needed:
```bash
curl -k -X POST https://localhost:5601/api/saved_objects/_import?createNewCopies=true -H "kbn-xsrf: true" --form file=@dashboard.ndjson -u elastic:${elastic_password}
```

After exporting a few dashboards and experimenting on the exported objects a pattern was identified, in which new dashboards could be created with an alternate filter and alternative electrodes to the original ones.

So changing the filter to point to the past 15 days instead of the 30 days of the template was possible and also changing the dahsboard to display values of electrodes 6,3,7 instead of 1,2,3 was again possible.

Issues arose when trying to create dashboards that would display more electrodes than the original template from the exported file though, hence why 16 template dashboards were created.

### ELKInterface

ELKInterafce is build using maven and as described above serves the purpose of acting as an intermediate between the user and Kibana, providing to the user the ability to create dashboards dynamically and retrieve them as images locally.

To run ELKInterface you need to setup a config.json file similar to the one in the resources folder of the project and pass its path as an argument to the executable jar, otherwise a file named config.json is attempted to be loaded from the local folder that the executable is run.

In the executable you need to specify the connection properties of the Kibana server:
```bash
{
	"server":
	{
		"ip": "192.168.1.81",
		"port": 5601,
		"ssl_enabled": true,
		"timeout": 10,
		"credentials":
		{
			"username": "elastic",
			"password": "ATwI9nzPKj-t7=k*uDF*"
		}
	}
}
```

Once the configuration is loaded the user is requested to input the electrodes, a timeframe on which to filter upon, a title for the dashboard and the local folder where the dashboard will be saved:
![image](https://github.com/user-attachments/assets/f3f5fbaf-246a-4fb7-ac61-0f888ba1eb44)

With an output similar to:
![image](https://github.com/user-attachments/assets/51198d25-c8c4-4060-ba61-712b5d6968e8)

This is achived by following the below processes:
1. Based on the number of electrodes the user inputs the appropriate template file is selected and updated
2. A request is made to Kibana to create the dashboard based on the updated template file
3. Once the dashboard is updated a request is made to retrieve the the list of all the dashboards from Kibana and the correct dashboardID is selected based on the description of the dashboard.
4. Using the ID retrieved above a request is made to Kibana to generate a PND report.
   This API was not created from Kibana with the purpose of being programmatically generated or utilized. For this reason I have taken the endpoint that was generated from Kibana using their UI and update the dashboardID as needed.
   This path might need to be modified from user to user as it contains information about the Kibana version and also timezone of the browser that the initial endpoint was received upon. For the purposes of this demonstration it is hardcoded. As a response we receive an endpoint which can be utilized to download the produced report.
5. The report requires about 10-15 seconds to be created so an waiting period of 20 seconds has been added. Once the report is ready, its downloaded and saved locally to the path specified in the user input and the png is displayed on a seperate pop-up window after which the whole process is restarted.

### Possible improvements:
1. Add possibility for more filters about the remaining fields (subject, task etc.)
2. Delete the created reports/dashboards after the execution
3. Setup the dashboard to display all the available records and not be restrained to the latest 500 timestamps
4. Make the generate report API template universal


## Additional Notes
This project is a practical implementation of data streaming and visualization. Feel free to explore and modify it to fit your use case!
