rem javac -classpath ..\java ..\java\com\jopdesign\tools\*.java
java -cp ..\java\tools\dist\lib\jop-tools.jar com.jopdesign.tools.Jopa %1.asm
copy *.vhd ..\vhdl
