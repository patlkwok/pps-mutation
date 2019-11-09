import subprocess, sys, os, glob, re

def write_to_file(str, path):
    f = open(path, "w")
    f.write(str)
    f.close()

player = "g0"
if len(sys.argv) > 1:
    player = sys.argv[1]

players = ["g" + str(i) for i in range(1, 9)]

config_path = 'config/tournament'
config_files = glob.glob(config_path + '/*.cfg')
config_files.sort()
config_files += ["config/" + p + "/tournament.cfg" for p in players]

config_files.sort()

rep = 5
result_path = 'results/' + player;
summary = ""

for m in [10, 5, 15]:
    for config_file in config_files:
        for tt in range(rep):
            case_name = config_file[7:-4].replace("/", "_").replace("\\", "_") + "_" + str(m) + "_" + str(tt)
            err_path = result_path + "/" + case_name + ".err"
            out_path = result_path + "/" + case_name + ".log"
            # print(case_name)
            output = subprocess.Popen(['java', 'mutation.sim.Simulator', '-p', player, '-v',
                                      '-c', config_file, '-m', str(m), '-tl', '1800000'],
                                      stdout=subprocess.PIPE, stderr=open(err_path, "w"), cwd="./")
            stdout, stderr = output.communicate()

            start_print = False
            result = "Runtime Error"
            run_time = "N/A"
            num_guesses = "N/A"
            num_experiments = "N/A"

            out_string = stdout.decode("utf-8")
            for line in out_string.split('\n'):
                if len(line) > 6 and line[:6] == "Player":
                    start_print = True
                if start_print:
                    if "Correct!" in line:
                        result = "Correct"
                    if "Jaccard score is:" in line:
                        result = re.findall(r'\d+\/\d+', line)[0]
                    if "guesses and" in line:
                        _, num_guesses, num_experiments = re.findall(r'\d+', line)
                    if "Player finished in" in line:
                        run_time = re.findall(r'\d+ms', line)[0]
            write_to_file(out_string, out_path)
            print(case_name + "," + result + "," + run_time + "," + num_guesses + "," + num_experiments)
            summary += case_name + "," + result + "," + run_time + "," + num_guesses + "," + num_experiments + "\n"

write_to_file(summary, result_path + "/summary.csv")
