rem not complete just for the start
rem jbi32 -dDO_PROGRAM=1 -aPROGRAM jbc\je_init.jbc
rem jbi32 -dDO_PROGRAM=1 -aCONFIGURE jbc\je_init.jbc
down java\tcpip\Main.bin COM2
java -cp java/pc Flash java\tcpip\cyc.html
java -cp java/pc Flash java\tcpip\Main.bin
java -cp java/pc Flash ttf\jopcyc.ttf
rem jbi32 -dDO_PROGRAM=1 -aPROGRAM jbc\je_confacx.jbc
