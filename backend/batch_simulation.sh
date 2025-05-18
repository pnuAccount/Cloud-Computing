#!/bin/bash

URL="http://172.29.0.1:8080"
COUNT=3

echo "Sending $COUNT persistent curl requests to $URL"

for i in $(seq 1 $COUNT)
do
    visitor_id=$(uuidgen)
    echo "Request $i with visitorId: $visitor_id"
    curl --cookie "visitorId=$visitor_id" --max-time 15 --limit-rate 1 --no-buffer $URL &
done

wait
echo "All requests done."


