jbi32 -dDO_PROGRAM=1 -aPROGRAM init.jbc
jbi32 -dDO_PROGRAM=1 -aCONFIGURE init.jbc
..\down web_nand.bin COM2
java -cp ../java/pc Flash ..\java\tcpip\cyc.html
java -cp ../java/pc Flash web_nand.bin
java -cp ../java/pc Flash ..\ttf\jopcyc.ttf
jbi32 -dDO_PROGRAM=1 -aPROGRAM conf.jbc
