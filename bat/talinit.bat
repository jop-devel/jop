jbi32 -dDO_PROGRAM=1 -aPROGRAM jbc\acxtal_init.jbc
jbi32 -dDO_PROGRAM=1 -aCONFIGURE jbc\acxtal_init.jbc
down java\ejip\Main.jop COM2
java -cp java/pc Flash java\ejip\test.html 192.168.0.4
java -cp java/pc Flash java\ejip\Main.jop 192.168.0.4
java -cp java/pc Flash java\ejip\Tal.class 192.168.0.4
java -cp java/pc Flash ttf\acxtal_flash.ttf 192.168.0.4
jbi32 -dDO_PROGRAM=1 -aPROGRAM jbc\acxtal_flash.jbc
