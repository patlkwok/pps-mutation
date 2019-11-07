import subprocess
import os
import glob

config_path = 'config/g1'
config_files = glob.glob(config_path + '/*.cfg')
config_files.sort()

# output = subprocess.Popen(['javac', 'mutation/sim/*.java'], cwd='../',
#                           stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
# stdout, stderr = output.communicate()
# print(stdout.decode("utf-8"))

for config_file in config_files:
    output = subprocess.Popen(['java', 'mutation.sim.Simulator', '-p', 'g1', '-c', config_file], 
                              stdout=subprocess.PIPE, stderr=subprocess.STDOUT, cwd="./")
    stdout, stderr = output.communicate()

    print("#" * 40)
    print("Running config: " + config_file)
    with open(config_file) as f:
        for line in f:
            print(line, end="")
    print()
    
    start_print = False
    out_string = stdout.decode("utf-8")
    for line in out_string.split('\n'):
        if len(line) > 6 and line[:6] == "Player":
            start_print = True
        if start_print:
            print(line)
    
    print()
