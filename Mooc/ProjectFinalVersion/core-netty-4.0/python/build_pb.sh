#!/bin/bash
#
# creates the python classes for our .proto
#

project_base="/Users/poojasrinivas/Desktop/275/Project/core-netty-4.0"

rm ${project_base}/python/src/comm_pb2.py

protoc -I=${project_base}/resources --python_out=./python/src ${project_base}/resources/comm.proto
