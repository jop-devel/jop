set project=%1
set blocksize=4096
rmdir /Q /S generated
mkdir generated
java -cp ..\java\tools\dist\lib\jop-tools.jar com.jopdesign.tools.Jopa -s src -d generated %project%.asm
java -cp ..\java\tools\dist\lib\jop-tools.jar BlockGen -b %blocksize% -pd -d 1024 -w 8 -m xjbc_block -o generated
java -cp ..\java\tools\dist\lib\jop-tools.jar BlockGen -b %blocksize% -pd -m xram_block generated\ram.mif generated\xram_block.vhd
java -cp ..\java\tools\dist\lib\jop-tools.jar BlockGen -b %blocksize% -pd -m xrom_block generated\rom.mif generated\xrom_block.vhd
copy generated\*.vhd ..\vhdl
