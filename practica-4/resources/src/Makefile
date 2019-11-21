dirs := Info Sum FTP
conv_dirs := Info Sum

all: clean build

build:
	$(foreach dir, $(dirs), mkdir $(dir)/classes;)
	$(foreach dir, $(conv_dirs), javac -classpath lib/jade.jar -d $(dir)/classes $(dir)/$(dir)Agent.java;)
	javac -classpath lib/jade.jar -d FTP/classes FTP/FTPAgent.java FTP/FTPCommand.java

start:
	java -cp lib/jade.jar:classes jade.Boot -gui

run-info:
	cd Info && java -cp ../lib/jade.jar:classes jade.Boot -gui -container -host localhost -agents mol:InfoAgent

run-sum:
	cd Sum && java -cp ../lib/jade.jar:classes jade.Boot -gui -container -host localhost -agents mol:SumAgent

run-ftp:
	cd FTP && ./FTP/run --help

clean:
	- rm -rf APDescription.txt MTPs-Main-Container.txt
	- $(foreach dir, $(dirs), rm -rf $(dir)/classes;)
