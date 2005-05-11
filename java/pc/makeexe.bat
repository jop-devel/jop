setlocal
rmdir /Q /S dist
mkdir dist\lib
mkdir dist\exe
call \usr2\gcj_win\setp.bat
rem gcj --main=%1 src/udp/*.java -o dist/exe/%1.exe
rc jop.rc
windres jop.res jop.o
gcj --main=test.%1 src/udp/*.java src/ui/Connection.java \usr2\eclipse\workspace\SWTBuilder\test\%1.java -mwindows jop.o -o %1.exe
endlocal
