#!/bin/bash

echo 'Installing Maven'
if [ -f /etc/redhat-release ]; then
  yum install maven -y
fi

if [ -f /etc/lsb-release ]; then
  apt-get install maven -y
fi

echo 'Checking for dsefs'
#check for dsefs
PORT_CHECK=$(lsof -i:5598|grep LISTEN|wc -l)
if [ "$PORT_CHECK" -eq 0 ]; then
  echo "dsefs needs to be running";
  exit 1;
fi

echo 'Pushing file to dsefs'
#push file to dsefs
dse fs "put ./sales_observations sales_observations"

echo "standing up docs"
#standup the docs
#install hugo
mkdir /opt
mkdir /opt/hugo
wget https://github.com/spf13/hugo/releases/download/v0.20.2/hugo_0.20.2_Linux-64bit.tar.gz
tar -xvf hugo_0.20.2_Linux-64bit.tar.gz -C /opt/hugo

git submodule update --init
git submodule sync
/opt/hugo/hugo_0.20.2_linux_amd64/hugo_0.20.2_linux_amd64 server ./content --bind=0.0.0.0 --port=80 --disableLiveReload=true&

echo "running streaming job"
#build and run spark job
mvn package && nohup dse spark-submit --deploy-mode cluster --supervise  --class com.datastax.powertools.analytics.SparkMLProductRecommendationServeJDBC ./target/StreamingMLProductRecommendations-0.1.jar localhost 9999&

echo "generating stream"
i=1
#kick off nc sockets and add data
while [ true ];
do
    echo "while $i";
    while read -r line; sleep 1; do echo $line | awk -F'\t' -v OFS='\t' '{print $0 $1 $2+$i}'; done < ./sales_observations | nc -lk 9999
    i=$((i+1));
done
