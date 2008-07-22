rm -rf generated
mkdir generated || exit 1
gcc -x c -E -C -P src/jvm.asm > generated/jvmser.asm || exit 1
./build.sh ../generated/jvmser
