rmdir /Q /S dist
mkdir dist\lib
mkdir dist\classes
dir /b /s src\*.java > .sourcefiles
javac -sourcepath ../target/src;../target/src/app -d dist\classes @.sourcefiles
del .sourcefiles
cd dist\classes
jar cf ..\lib\pc.jar *
cd ..\..
