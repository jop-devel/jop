gcc -x c -E -C -P src\jvm.asm > generated\jvmser.asm
call build ..\generated\jvmser
