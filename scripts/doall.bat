rem use Makefile instead
set qu_proj=cycmin
set projpath=quartus\%qu_proj%
set p1=test
set p2=vmtest
set p3=DoAll

cd java
cd tools
call build
cd ..
cd .. 
cd asm
call jopser
cd ..
quartus_map %projpath%\jop
quartus_fit %projpath%\jop
quartus_asm %projpath%\jop
quartus_tan %projpath%\jop
rem jbi32 -dDO_PROGRAM=1 -aPROGRAM jbc\cycmin.jbc
cd %projpath%
quartus_pgm -c ByteBlasterMV -m JTAG jop.cdf
cd ..\..
cd java\target
call build %p1% %p2% %p3%
rem start ping -n 100 192.168.0.123
..\..\down -e dist\bin\%project%.jop COM1
cd ..\..
