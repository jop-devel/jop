rmdir /Q /S generated
mkdir generated
gcc -x c -E -C -P -DFLASH src\jvm.asm > generated\jvmflash.asm
call build ..\generated\jvmflash
