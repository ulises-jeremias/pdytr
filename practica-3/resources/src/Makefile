all: compile start

start:
	rmiregistry &
	java StartRemoteObject

stop:
	@echo "Stop server\n"
	- killall rmiregistry

compile:
	javac gnu/getopt/*.java && javac *.java

clean:
	- rm *.class
