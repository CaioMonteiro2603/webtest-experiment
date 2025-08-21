#!/bin/bash

#############################
# How to run
# ./generate-report.sh
#############################

mvn clean test-compile

for model in $(echo "deepseek claudesonnet4")
do
	for ws in $(echo "ws01 ws02 ws03 ws04 ws05 ws06 ws07 ws08 ws09")
	do
		for seq in $(echo "seq01 seq02 seq03 seq04 seq05")
		do
			mvn test -Dtest="**/${model}/${ws}/${seq}/*"
			mvn surefire-report:report-only
			mkdir -p reports/${model}/${ws}
			mv target/reports reports/${model}/${ws}/${seq}
		done
	done
done