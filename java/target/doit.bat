call build %1 %2 %3

java -cp ../tools/dist/lib/jop-tools.jar com.jopdesign.tools.jop2dat dist\bin\%project%.jop 
copy *.dat ..\..\modelsim
del *.dat

pause Compilation ok? Start download?

rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\..\jbc\cycmin.jbc
jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\..\jbc\cycmin.jbc
..\..\down -e dist\bin\%project%.jop COM1

rem java -cp ../pc/dist/lib/jop-pc.jar udp.Flash dist\bin\%project%.jop 192.168.1.2
rem ..\..\e COM2
java -cp ../tools/dist/lib/jop-tools.jar -Dlog="false" com.jopdesign.tools.JopSim dist\bin\%project%.jop
rem ..\..\jopc\jopvm %project%.jop
