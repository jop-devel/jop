rem
rem usage: dosim pkg MainClass
rem
ren java jop_java

javac pcsim/com/jopdesign/sys/*.java
javac pcsim/javax/joprt/*.java
javac pcsim/util/*.java

del %1\*.class
del util\*.class
del sms\*.class
del tcpip\*.class
del test\*.class

javac %1/%2.java
java -cp pcsim;. %1.%2
ren jop_java java
