rmdir /Q /S dist
mkdir dist\lib
mkdir dist\classes
dir /b /s src\*.java > .sourcefiles
javac -g -d dist\classes -classpath jcc.jar @.sourcefiles
del .sourcefiles
cd dist\classes
jar cf ..\lib\jop-pc.jar *
cd ..\..
