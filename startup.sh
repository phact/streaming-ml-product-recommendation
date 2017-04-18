#!/bin/bash
#check for dsefs
PORT_CHECK=$(lsof -i:5598|grep LISTEN|wc -l)
if [ "$PORT_CHECK" -eq 0 ]; then
  echo "dsefs needs to be running";
  exit 1;
fi

#push file to dsefs
dse fs "put ./sales_observations sales_observations"

#build and run spark job
mvn package && dse spark-submit --deploy-mode cluster --supervise  --class com.datastax.powertools.analytics.SparkMLProductRecommendationServeJDBC ./target/StreamingMLProductRecommendations-0.1.jar localhost 9999 &> nohup.out &

echo "now other stuff"

i=1
#kick off nc sockets and add data
while [ true ];
do
    echo "while $i";
    while read -r line; sleep 1; do echo $line | awk -F'\t' -v OFS='\t' '{print $0 $1 $2+$i}'; done < ./sales_observations | nc -lk 9999
    i=$((i+1));
done

#standup the docs
git submodule update --init
git submodule sync
hugo server ./content&
