rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\jbc\acxtal.jbc
rem jbi32 -dDO_PROGRAM=1 -aCONFIGURE ..\jbc\acxtal.jbc
rem ..\down ejip_Main.jop COM2
java -cp ../java/pc/dist/lib/jop-pc.jar udp.Flash ..\java\target\dist\bin\tal_Lift.jop 192.168.0.123
rem java -cp ../java/pc/dist/lib/jop-pc.jar udp.Flash tal_Lift.jop 192.168.0.123
rem java -cp ../java/pc/dist/lib/jop-pc.jar udp.Flash test_Clock.jop 192.168.0.123
rem java -cp ../java/pc/dist/lib/jop-pc.jar udp.Flash ..\ttf\acxtal.ttf 192.168.0.123
rem java -cp ../java/pc/dist/lib/jop-pc.jar udp.Bgid 192.168.0.123
rem jbi32 -dDO_PROGRAM=1 -aPROGRAM ..\jbc\acx_conf.jbc
