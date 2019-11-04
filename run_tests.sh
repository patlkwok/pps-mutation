#!/bin/bash

# Compile the files in our directory 
javac mutation/g7/*.java

# Set path to tests 
TESTS=config/g7/test_cases/* 
# Read in m from command line 
m=$1

## rm previous output if exists
echo "Running test cases:" > output.txt

# Variable to keep track of successful 
correct=0
# Variable to keep track of total 
total=0
for config_file in $TESTS
do 
    res=$(java mutation.sim.Simulator -p g7 -m $m -c $config_file)
    test=$(echo $config_file | cut -f4 -d/ )
    let "total += 1"
    if [[ $res == *"Failed"* ]]; 
    then
        echo "Failed on test case "$test >> output.txt
        jaccard=$(echo $res | cut -f2 -d: )
        echo "Jaccard score was: "$jaccard >> output.txt
    else
        let "correct += 1"
        echo "Succeeded on test case "$test >> output.txt
    fi
done 

echo "Test cases passed" $correct "/" $total >> output.txt

