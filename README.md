The purpose of this project is to showcase the ability to post data from kafka to Elastic through Logstash and dynamically create dashboards in Kibana that utilize those data with an external component written in Java.

For this project the dataset that was chosen contains EEG data from human limb movement that come from the following paper:
https://data.mendeley.com/datasets/x8psbz3f6x/2
A small portion of the files has been included in this project for the purposes of the simulation, retaining the original formatting of the files.

To simulate the arrival of the data to kafka a script was created that dynamically modifies the records of the dataset including additional critical information that originally was present in the name of the files.
The addtional fields that are added are:
  An incremental number
  The number of the subject
  The task that was performed
  If the task was imaginary or motor
  The current timestamp
More information about the above can be retrieved from the linked paper and specifically in the paragraph of the explanation regarding the naming of the dataset files

The simulation script that can be found under the full_dataset folder inputs at the end of each published record the current timestamp of the linux machine. Due to the operation speed it was detected that a lot of records were published within the same millisecond and for this reason an artificial delay of 1 second was addded.

Setup
Kafka
The first time the cluster is run it will result in the failure of kafka-connect due to missing topics on the broker.
To fix this, connect to the control-center using the IP of the machine that is running the broker and create the following topics:
  -brain_data
  -kafka_connect_offset
  -kafka_connect_status
  -kafka_connect_config

For the kafka_connect topics the retention period must be set to 'compact' (the period is not important) and the 'Number of partitions' to 1.
Once this is complete try to rerun the cluster and the kafka-connect container should start.

To utilize the simulation script connect to the kafka-connect container:
  docker exec -it kafka-connect /bin/bash

and run the script:
  cd /tmp/full_dataset
  /tmp/full_dataset/publish.sh
If no specific file is selected then all the files of the dataset are iterated.

