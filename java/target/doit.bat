call build %1 %2 %3

pause Compilation ok? Press Enter to start the download.

rem uncomment your target for the Cyclone board:

rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\..\jbc\cycmin.jbc
rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\..\jbc\cyc_conf.jbc
jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\..\jbc\cycmin.jbc
rem ..\..\down -e dist\bin\%project%.jop COM1
set TMP_PATH=%PATH%
set PATH=..\..;%PATH%
echo %PATH%
java -cp ../tools/dist/lib/jop-tools.jar;../lib/RXTXcomm.jar com.jopdesign.tools.JavaDown -e dist\bin\%project%.jop COM1
set PATH=%TMP_PATH%

rem java -cp ../tools/dist/lib/jop-tools.jar -Dlog="false" -Dhandle="true" com.jopdesign.tools.JopSim dist\bin\%project%.jop
