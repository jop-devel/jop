call build %1 %2 %3

pause

jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\..\jbc\cycmin.jbc
rem jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\..\jbc\cycmin100_thesis.jbc
..\..\down -e dist\bin\%project%.jop COM1

rem java -cp ../pc/dist/lib/jop-pc.jar udp.Flash dist\bin\%project%.jop 192.168.1.2
rem ..\..\e COM2

rem java -cp ../tools/dist/lib/jop-tools.jar com.jopdesign.tools.JopSim dist\bin\%project%.jop 500000
rem ..\..\jopc\jopvm %project%.jop
