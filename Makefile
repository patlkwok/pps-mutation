compile:
	javac mutation/sim/*.java

run:
	java mutation.sim.Simulator -p g8 -c config/g8/mutationtest.cfg

gui:
	java mutation.sim.Simulator -p g8 -c config/g8/mutationtest.cfg -g
