rmdir /Q /S dist
mkdir dist\lib
mkdir dist\classes
dir /b /s ..\target\src\common\com\jopdesign\sys\Const.java > .sourcefiles
dir /b /s src\*.java >> .sourcefiles
set java_lib=../tools/dist/lib/jop-tools.jar;../lib/bcel-5.1.jar;../lib/jakarta-regexp-1.3.jar;jcc.jar
javac -g -d dist\classes -classpath %java_lib% @.sourcefiles
del .sourcefiles
cd dist\classes
jar cf ..\lib\jop-tools.jar *
cd ..\..
