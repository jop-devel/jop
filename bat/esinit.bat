jbi32 -dDO_PROGRAM=1 -aPROGRAM jbc\es_init.jbc
jbi32 -dDO_PROGRAM=1 -aCONFIGURE jbc\es_init.jbc
down java\tcpip\Main.bin COM2
java -cp java/pc Flash java\tcpip\test.html
rem java -cp java/pc Flash java\oebb\Main.bin
rem derweilen in tcpip.Main
java -cp java/pc Flash java\tcpip\Main.bin
java -cp java/pc Flash ttf\es_jvmflash.ttf
jbi32 -dDO_PROGRAM=1 -aPROGRAM jbc\es_confacx.jbc
