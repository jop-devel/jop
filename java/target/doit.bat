call build %1 %2 %3

pause

rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\..\jbc\acxmin_init.jbc
rem jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\..\jbc\acxmin_init.jbc
rem jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\..\jbc\acxtal_init.jbc
rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\..\jbc\bgcyc_init.jbc
rem jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\..\jbc\bgcyc_init.jbc
..\..\down -e dist\bin\%project%.bin COM2

rem java -cp pc Flash %project%.bin 192.168.1.2

rem ..\..\jopc\jopvm %project%.bin
java -cp ../tools/dist/lib/jop-tools.jar com.jopdesign.tools.JopSim dist\bin\%project%.bin
