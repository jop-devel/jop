rmdir /Q /S dist
mkdir dist\lib
mkdir dist\classes
dir /b /s src\*.java > .sourcefiles
javac -sourcepath ../target/src -d dist\classes @.sourcefiles
del .sourcefiles
cd dist\classes
jar cf ..\lib\pc.jar *
cd ..\..
