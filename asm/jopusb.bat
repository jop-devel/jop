rmdir /Q /S generated
mkdir generated
gcc -x c -E -C -P -DUSB %GCC_PARAMS% src\jvm.asm > generated\jvmusb.asm
call build ..\generated\jvmusb
