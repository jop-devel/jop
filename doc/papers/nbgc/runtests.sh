#! /bin/sh

TEST_TIME=900

# set up system level classes
cp	java/target/src/common/com/jopdesign/sys/GC.java.$1 \
	java/target/src/common/com/jopdesign/sys/GC.java
cp	java/target/src/common/com/jopdesign/sys/RtThreadImpl.java.$1 \
	java/target/src/common/com/jopdesign/sys/RtThreadImpl.java

for SET in "hslg" "hpclg" "hpcslg"; do

ARRAY_SIZE=1024

# set up test
cp	java/target/src/test/gcinc/Latency.java.template \
	java/target/src/test/gcinc/Latency.java

sed -i 's/__TASK_SET__/"'$SET'"/;
		s/__TEST_TIME__/'$TEST_TIME'/;
		s/__ARRAY_SIZE__/'$ARRAY_SIZE'/' \
	java/target/src/test/gcinc/Latency.java

echo "TEST RUN: $SET $ARRAY_SIZE $TEST_TIME"

# run test in hardware
ant -Dp1=test -Dp2=gcinc -Dp3=Latency tools java-app && echo "********" | sudo -S ant config-usb && ant -Dp1=test -Dp2=gcinc -Dp3=Latency download | tee "results/test_"$1"_"$SET"_"$ARRAY_SIZE"_"$TEST_TIME".log"

done

for ARRAY_SIZE in 64 128 256 512 2048; do

SET="hpclg"

# set up test
cp	java/target/src/test/gcinc/Latency.java.template \
	java/target/src/test/gcinc/Latency.java

sed -i 's/__TASK_SET__/"'$SET'"/;
		s/__TEST_TIME__/'$TEST_TIME'/;
		s/__ARRAY_SIZE__/'$ARRAY_SIZE'/' \
	java/target/src/test/gcinc/Latency.java

echo "TEST RUN: $SET $ARRAY_SIZE $TEST_TIME"

# run test in hardware
ant -Dp1=test -Dp2=gcinc -Dp3=Latency tools java-app && echo "********" | sudo -S ant config-usb && ant -Dp1=test -Dp2=gcinc -Dp3=Latency download | tee "results/test_"$1"_"$SET"_"$ARRAY_SIZE"_"$TEST_TIME".log"

done

for ARRAY_SIZE in 2048 4096 8192 16384; do

SET="hqclg"

# set up test
cp	java/target/src/test/gcinc/Latency.java.template \
	java/target/src/test/gcinc/Latency.java

sed -i 's/__TASK_SET__/"'$SET'"/;
		s/__TEST_TIME__/'$TEST_TIME'/;
		s/__ARRAY_SIZE__/'$ARRAY_SIZE'/' \
	java/target/src/test/gcinc/Latency.java

echo "TEST RUN: $SET $ARRAY_SIZE $TEST_TIME"

# run test in hardware
ant -Dp1=test -Dp2=gcinc -Dp3=Latency tools java-app && echo "********" | sudo -S ant config-usb && ant -Dp1=test -Dp2=gcinc -Dp3=Latency download | tee "results/test_"$1"_"$SET"_"$ARRAY_SIZE"_"$TEST_TIME".log"

done
