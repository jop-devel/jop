jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\jbc\cyctal_init.jbc
jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\jbc\cyctal_init.jbc
..\down ejip_Main.bin COM2
java -cp ../java/pc/dist/lib/jop-pc.jar Flash ejip_Main.bin 192.168.1.2
java -cp ../java/pc/dist/lib/jop-pc.jar Flash ..\ttf\cyctal.ttf 192.168.1.2
jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\jbc\cyc_conf.jbc
