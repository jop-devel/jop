echo off
set project=Lizy
set startclass=LizyChess
set appdir=/usr1/lizy
rmdir /Q /S dist
mkdir dist\classes
mkdir dist\lib
mkdir dist\bin
dir /b /s src\jdk\java\*.java > .sourcefiles
dir /b /s src\common\com\jopdesign\sys\*.java >> .sourcefiles
echo on
javac -d dist/classes -sourcepath src/common @.sourcefiles
del .sourcefiles
javac -d dist/classes -sourcepath src/common %appdir%/%startclass%.java
cd dist\classes
jar cf ../lib/classes.zip *
cd ..\..
java -cp ../tools/jcc.jar;../tools/dist/lib/jop-tools.jar -Djop.startclass="%startclass%" JavaCodeCompact -nq -arch JOP -o dist/bin/%project%.jop dist/lib/classes.zip

pause Compilation ok? Start download?

rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\..\jbc\cycmin.jbc
rem jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\..\jbc\cycmin100.jbc
..\..\down -e dist\bin\%project%.jop COM1

rem java -cp ../tools/dist/lib/jop-tools.jar com.jopdesign.tools.JopSim dist\bin\%project%.jop
