rmdir /Q /S generated
mkdir generated
gcc -x c -E -C -P %GCC_PARAMS% src\jvm.asm > generated\jvmser.asm
call build ..\generated\jvmser
