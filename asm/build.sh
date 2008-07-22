export project=$1
export blocksize=4096
#rem generated is used by jop[ser|flash].bat
#rem rmdir /Q /S generated
#rem mkdir generated
java -cp ../java/tools/dist/lib/jop-tools.jar com.jopdesign.tools.Jopa -s src -d generated $project.asm
#rem java -cp ../java/tools/dist/lib/jop-tools.jar BlockGen -b $blocksize -pd -d 1024 -w 8 -m xjbc_block -o generated
java -cp ../java/tools/dist/lib/jop-tools.jar BlockGen -b $blocksize -pd -m xram_block generated/ram.mif generated/xram_block.vhd
java -cp ../java/tools/dist/lib/jop-tools.jar BlockGen -b 16384 -pd -m xram_block generated/ram.mif generated/xv4ram_block.vhd
#rem java -cp ../java/tools/dist/lib/jop-tools.jar BlockGen -b $blocksize -pd -m xrom_block generated/rom.mif generated/xrom_block.vhd
java -cp ../java/tools/dist/lib/jop-tools.jar GenAsynROM -m actelram_initrom generated/ram.mif generated/actelram_initrom.vhd
cp generated/*.vhd ../vhdl

cp generated/*.dat ../modelsim
