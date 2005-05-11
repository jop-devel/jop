echo off
set arg3=%3
if defined arg3 (
	set project=%2_%3
	set startclass=%2/%3
	set appdir=%1
) else (
	set project=%1_%2
	set startclass=%1/%2
	set appdir="."
) 
rmdir /Q /S dist
mkdir dist\classes
mkdir dist\lib
mkdir dist\bin
dir /b /s src\jdk\java\*.java > .sourcefiles
dir /b /s src\common\com\jopdesign\sys\*.java >> .sourcefiles
echo on
javac -d dist/classes -sourcepath src/common @.sourcefiles
del .sourcefiles
javac -d dist/classes -sourcepath src/common;src/%appdir% src/%appdir%/%startclass%.java
cd dist\classes
jar cf ../lib/classes.zip *
cd ..\..
java -cp ../tools/jcc.jar;../tools/dist/lib/jop-tools.jar -Djop.startclass="%startclass%" JavaCodeCompact -nq -arch JOP -o dist/bin/%project%.jop dist/lib/classes.zip
rem java -cp ..\tools\dist\lib\jop-tools.jar com.jopdesign.tools.JopBitGen dist\bin\%project%.jop dist\bin\%project%.bit
