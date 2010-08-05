#! /bin/sh
rm -rf dist/
mkdir dist
javac -Xlint:all -sourcepath .. model/Main.java -d dist
stty -F /dev/ttyS0 115200 raw -echo
tee input.log < /dev/ttyS0 | java -cp dist cruiser/model/Main 2> test.log | tee output.log > /dev/ttyS0
