#! /bin/sh

RESULT_DIR=ocache_eval

mkdir -p ${RESULT_DIR}
# not used - 4 configurations are encoded in WCETAnalysis.java
WCET_OPTIONS="-dataflow-analysis -object-cache-analysis"
SRAM_OPTIONS="-dataflow-analysis -object-cache-analysis -jop-ocache-access-cycles 2"
SRAM_KEY="_2_0"
SDRAM_OPTIONS="-dataflow-analysis -object-cache-analysis -jop-ocache-access-cycles 2 -jop-ocache-access-delay 10 -jop-ocache-max-burst 4"
SDRAM_KEY="_2_10_4"

# The lift benchmark

if [ $RUNLIFT ] ; then
make java_app wcet P1=test P2=wcet P3=StartLift WCET_METHOD=measure WCET_OPTIONS="${WCET_OPTIONS}"
cp java/target/wcet/wcet.StartLift_measure/ocache_eval.txt ${RESULT_DIR}/lift.txt
fi

# The UdpIp benchmark
if [ $RUNUDPIP ] ; then
make java_app wcet P1=test P2=wcet P3=StartBenchUdpIp WCET_METHOD=measure WCET_OPTIONS="${WCET_OPTIONS}"
cp java/target/wcet/wcet.StartBenchUdpIp_measure/ocache_eval.txt ocache_eval/udpip.txt
fi


# The EjipCmp benchmark

if [ $RUNEJIP ] ; then
make java_app wcet P1=test P2=wcet P3=StartEjipCmp WCET_METHOD=measure WCET_OPTIONS="${WCET_OPTIONS}"
cp java/target/wcet/wcet.StartEjipCmp_measure/ocache_eval.txt ocache_eval/ejip.txt
fi


# The trading benchmark
# checkForTrade
if [ $RUNORDER ] ; then

RESULT_FILE=java/target/wcet/com.sun.oss.trader.Main_com.sun.oss.trader.tradingengine.OrderManager.checkForTrade_Lcom_sun_oss_trader_data_OrderEntry_D_Z/ocache_eval.txt
WCET_METHOD="com.sun.oss.trader.tradingengine.OrderManager.checkForTrade(Lcom/sun/oss/trader/data/OrderEntry;D)Z"

make java_app wcet P1=paper/trading/plain P2=com/sun/oss/trader P3=Main TARGET_JDK=jdk16mod \
    WCET_METHOD=${WCET_METHOD} WCET_OPTIONS="${WCET_OPTIONS}"
cp ${RESULT_FILE} ${RESULT_DIR}/order_manager.txt
fi

# The CDx (micro) benchmark
#make java_app wcet P1=bench P2=scd_micro P3=Main WCET_METHOD="scd_micro.Motion.findIntersection(Lscd_micro/Motion)" \
if [ $RUNCDX ] ; then
make java_app wcet P1=bench P2=scd_micro P3=Main WCET_METHOD="measure" \
  TARGET_JDK=jdk16mod \
  WCET_OPTIONS="${WCET_OPTIONS}"
cp java/target/wcet/scd_micro.Main_measure/ocache_eval.txt ocache_eval/scd_micro.txt
fi

# The TTPA benchmark
if [ $RUNTTPA ] ; then
echo "Running TTPA"
WCET_METHOD=ttpa.protocol.Node.doMpSlotAction
make java_app wcet P1=common P2=ttpa/demo P3=Main \
    WCET_METHOD=${WCET_METHOD} WCET_OPTIONS="${WCET_OPTIONS}"
# TODO: copy result file
fi

exit;

# Synthetic Benchmarks

make java_app wcet P1=test P2=wcet/devel P3=Simple WCET_METHOD=measure3 \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis -jop-ocache-line-size 1"
# TODO: copy result file

exit;

# The jolden benchmarks
# jolden is not in the source

#MST
make java_app wcet P1=bench P2=jolden/mst P3=MST WCET_METHOD=main \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis -object-cache-fields"
make java_app wcet P1=bench P2=jolden/mst P3=MST WCET_METHOD=main \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis"


exit;





