jbi32 -dDO_PROGRAM=1 -aPROGRAM init.jbc
jbi32 -dDO_PROGRAM=1 -aCONFIGURE init.jbc
..\down web_nand.bin COM2
ping 192.168.0.4
rem should be 198 173 1c0 1c0
java -cp ../java/pc UDPDbg
java -cp ../java/pc Flash ..\java\tcpip\cyc.html
