jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\jbc\cyctal_init.jbc
jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\jbc\cyctal_init.jbc
..\down ejip_Main.jop COM2
java -cp ../java/pc/dist/lib/jop-pc.jar udp.Flash ejip_Main.jop 192.168.0.123
java -cp ../java/pc/dist/lib/jop-pc.jar udp.Flash ..\ttf\cyctal.ttf 192.168.0.123
jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\jbc\cyc_conf.jbc
