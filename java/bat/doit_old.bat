rem
rem usage: doit pkg MainClass
rem
rem javac -classpath .;jcc.jar runtime\*.java

javac com\jopdesign\sys\*.java
cd jdk
javac -classpath ..;. java\lang\*.java
jar cfM0 ../classes.zip java/lang/*.class
cd ..
javac joprt\*.java

del %1\*.class
del util\*.class
del ejip\*.class
del sms\*.class
del test\*.class
del oebb\*.class
del ravenscar\*.class
del javax\realtime\*.class

javac %1/%2.java

rem java -cp jcc.jar;. JavaCodeCompact -nq -arch JOP -o %1/%2.bin %1/*.class util/*.class java/lang/*.class com/jopdesign/sys/*.class sms/*.class ejip/*.class test/*.class

rem classes for simple tests
jar ufM0 classes.zip %1/*.class com/jopdesign/sys/*.class util/*.class
jar ufM0 classes.zip joprt/*.class
rem add some packages
jar ufM0 classes.zip ejip/*.class
jar ufM0 classes.zip ravenscar/*.class
jar ufM0 classes.zip javax/realtime/*.class

java -cp jcc.jar;. JavaCodeCompact -nq -arch JOP -o %1/%2.bin classes.zip

rem gen .c just to compare .bin and .c
rem java -cp jcc.jar;. JavaCodeCompact -nq -arch KVM -o %1/%2.c classes.zip

pause

rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\jbc\acxmin_init.jbc
rem jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\jbc\acxmin_init.jbc
rem jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\jbc\acxtal_init.jbc
rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\jbc\bgcyc_init.jbc
jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\jbc\bgcyc_init.jbc
..\down -e %1\%2.bin COM2

rem java -cp pc Flash %1\%2.bin 192.168.1.2
rem ..\jopc\jopsim %1\%2.bin

rem ..\jopc\jopvm %1\%2.bin
rem javac com\jopdesign\tools\JopSim.java
rem java com.jopdesign.tools.JopSim %1\%2.bin
