rmdir /Q /S dist
mkdir dist\lib
mkdir dist\classes
dir /b /s src\*.java > .sourcefiles
javac -g -d dist\classes -classpath jcc.jar -sourcepath .;../target/src/common @.sourcefiles
del .sourcefiles
cd dist\classes
jar cf ..\lib\jop-pc.jar *
cd ..\..
