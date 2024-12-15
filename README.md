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

Upon first running the Kafka cluster, the `kafka-connect` service will fail due to missing topics. Follow these steps to resolve the issue:

1. Access the Kafka Control Center using the IP of the machine running the broker.
2. Create the following topics:
   - `brain_data`
   - `kafka_connect_offset`
   - `kafka_connect_status`
   - `kafka_connect_config`

3. Set the retention period for the Kafka Connect topics to `compact`. The retention period itself is not critical.
4. Set the `Number of partitions` for these topics to 1.

Once the topics are configured, restart the Kafka cluster. The `kafka-connect` container should start successfully.

### Simulation Script
To utilize the data simulation script:

1. Connect to the `kafka-connect` container:
   ```bash
   docker exec -it kafka-connect /bin/bash
   ```

2. Run the `publish.sh` script:
   ```bash
   cd /tmp/full_dataset
   /tmp/full_dataset/publish.sh
   ```

If no specific file is specified, the script will iterate through all dataset files which in our example contain only a small portion of the subjects from subjects 1 and 2.

---

## Additional Notes
This project is a practical implementation of data streaming and visualization. Feel free to explore and modify it to fit your use case!
