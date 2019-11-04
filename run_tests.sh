#!/bin/bash

# Compile the files in our directory 
javac mutation/g7/*.java

# Set path to tests 
TESTS=config/g7/test_cases/* 
# Read in m from command line 
m=$1

# Remove previous output 
rm output.txt 

for config_file in $TESTS
do 
    res=$(java mutation.sim.Simulator -p g7 -m $m -c $config_file)
    test=$(echo $config_file | cut -f4 -d/ )
    if [[ $res == *"Failed"* ]]; 
    then
        echo "Failed on test case "$test >> output.txt
        jaccard=$(echo $res | cut -f2 -d: )
        echo "Jaccard score was: "$jaccard >> output.txt
    else
        echo "Succeeded on test case "$test >> output.txt
    fi
done 

