jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\jbc\cyctal.jbc
jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\jbc\cyctal.jbc
..\down ejip_Main.jop COM2
java -cp ../java/pc/dist/lib/jop-pc.jar udp.Flash ..\java\target\dist\bin\tal_Tal.jop 192.168.0.123
java -cp ../java/pc/dist/lib/jop-pc.jar udp.Flash ..\ttf\cyctal.ttf 192.168.0.123
java -cp ../java/pc/dist/lib/jop-pc.jar udp.Bgid 192.168.0.123
jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\jbc\cyc_conf.jbc
