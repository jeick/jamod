#!/bin/bash 

libs=".:lib/*:/usr/share/java/*"
src="net/wimpi/modbus/*.java \
net/wimpi/modbus/cmd/*.java \
net/wimpi/modbus/facade/*.java \
net/wimpi/modbus/io/*.java \
net/wimpi/modbus/msg/*.java \
net/wimpi/modbus/net/*.java \
net/wimpi/modbus/procimg/*.java \
net/wimpi/modbus/util/*.java"
outputDir="output/"
distDir="dist/"
outputDistRoot="output/net/"
projectName="jamod-1.2mod"

java="java"
javac="javac"
jar="jar"
compileErrorsFile="errors.txt"
compileArgs="-cp $libs -d $outputDir"

COMMAND="$1"

cleanup() {
	echo "Cleaning..."
	rm -rf $outputDir
	mkdir -p $outputDir
}

dist() {
	echo "Packaging..."
	rm -rf $distDir
	mkdir -p $distDir
  jar cf $distDir$projectName.jar -C $outputDir .
}


if [ "$COMMAND" = "d" ] || [ "$COMMAND" = "clean" ]; then
	cleanup;
elif [ "$COMMAND" = "c" ] || [ "$COMMAND" = "compile" ]; then
	cleanup;
	echo "Compiling...."
	$javac $compileArgs $src &> $compileErrorsFile
	if [ $? = 0 ] ; then
		dist;
	fi
elif [ "$COMMAND" = "r" ] || [ "$COMMAND" = "run" ]; then
	echo "No running of this project..."
fi

