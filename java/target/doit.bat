call build %1 %2 %3

pause

rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\..\jbc\cycbg_init.jbc
rem jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\..\jbc\cycbg_init.jbc
..\..\down -e dist\bin\%project%.jop COM9

rem java -cp ../pc/dist/lib/jop-pc.jar udp.Flash dist\bin\%project%.jop 192.168.1.2
rem ..\..\e COM2

rem ..\..\jopc\jopvm %project%.jop
rem java -cp ../tools/dist/lib/jop-tools.jar com.jopdesign.tools.JopSim dist\bin\%project%.jop 500000
