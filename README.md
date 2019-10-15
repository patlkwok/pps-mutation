# Project 3: Mutation
Course: COMS 4444 Programming and Problem Solving (F2019)  
Uni: Columbia University  
Instructor: Prof. Kenneth Ross   
TAs: Vaibhav Darbari, Chengyu Lin   

# User Guide
* First compile the simulator `javac mutation/sim/*.java`
* Extend `mutation.sim.Player` class to implement your idea.
* Your player will be interacting with `mutation.sim.Console`. There are 3 public methods that you may need to use:
  * `String Mutate(String genome)`: it performs `m` mutations. The string `genome` must have length exactly 1000. If you exceeds the limit of experiments, it will return an empty string.
  * `int getNumberOfMutations()`: returns the number of mutations performed for the last `Mutate(genome)` call.
  * `boolean Guess(Mutagen guess)`: returns `true` if your guess exactly matches the hidden mutagen.
* Running your player with `java mutation.sim.Simulator -p g0` (replace `g0` with your group number). Other useful parameters are:
  * `-c [path] | --cfgpath [path]`: path to the hidden mutagen. Default value is `mutagen.cfg`.
  * `-g | --gui`: enable GUI
  * `--fps [fps]`: set fps for GUI. Default value is 5.
  * `-m [m]`: set the number of mutations per experiment. Default value is 1.
  * `-t [t] | --trials [t]`: set the maximum number of experiments. Default value is 100,000.
  * `-tl [limit] | --timelimit [limit]`: set the time limit in millisecond. Note that the computation within console is also considered. Default value is 60,000.
