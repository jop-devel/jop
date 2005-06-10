call build %1 %2 %3

java -cp ../tools/dist/lib/jop-tools.jar com.jopdesign.tools.jop2dat dist\bin\%project%.jop 
copy *.dat ..\..\modelsim
del *.dat

rem pause Compilation ok? Start download?

rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\..\jbc\cycmin.jbc
rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\..\jbc\cyc_conf.jbc
rem jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\..\jbc\cycmin100.jbc
rem ..\..\down -e dist\bin\%project%.jop COM1

rem java -cp ../tools/dist/lib/jop-tools.jar -Dlog="false" com.jopdesign.tools.JopSim dist\bin\%project%.jop

java -cp ../tools/dist/lib/jop-tools.jar com.jopdesign.build.JOPizer -cp dist/lib/classes.zip %startclass%

java -cp ../tools/dist/lib/jop-tools.jar -Dlog="false" com.jopdesign.tools.JopSim  /tmp/test.jop
..\..\down -e \tmp\test.jop COM1
