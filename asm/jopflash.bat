gcc -x c -E -C -P -DFLASH src\jvm.asm > src\jvmflash.asm
call build jvmflash
