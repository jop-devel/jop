rem version for bb
rem usage: doit Mast/Zentrale
rem
rem javac -classpath .;jcc.jar runtime\*.java

rem javac com\jopdesign\sys\*.java

del kfl\*.class
javac kfl/%1.java


rem this for kfl: no util, no com.jopdesign.*
java -cp jcc.jar;. JavaCodeCompact -nq -arch JOP -o kfl/%1.bin kfl/*.class java/lang/*.class
rem ..\down kfl\%1.bin
