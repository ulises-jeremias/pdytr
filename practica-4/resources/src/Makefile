all: clean
	mkdir ex1/classes ex2/classes ex3/classes
	javac -classpath lib/jade.jar -d ex1/classes ex1/Agent1.java
	javac -classpath lib/jade.jar -d ex2/classes ex2/Agent2.java
	javac -classpath lib/jade.jar -d ex3/classes ex3/Agent3.java

start-gui:
	java -cp lib/jade.jar:classes jade.Boot -gui

run-ex1:
	cd ex1 && java -cp ../lib/jade.jar:classes jade.Boot -gui -container -host localhost -agents mol:Agent1

run-ex2:
	cd ex2 && java -cp ../lib/jade.jar:classes jade.Boot -gui -container -host localhost -agents mol:Agent2

clean:
	- rm -r APDescription.txt MTPs-Main-Container.txt ex1/classes ex2/classes ex3/classes
