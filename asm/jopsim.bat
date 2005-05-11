rmdir /Q /S generated
mkdir generated
gcc -x c -E -C -P -DSIMULATION src\jvm.asm > generated\jvmsim.asm
call build ..\generated\jvmsim
