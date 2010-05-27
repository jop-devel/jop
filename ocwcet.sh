#! /bin/sh

RESULT_DIR=ocache_eval

mkdir -p ${RESULT_DIR}
SRAM_OPTIONS="-dataflow-analysis -object-cache-analysis -jop-ocache-access-cycles 2"
SRAM_KEY="_2_0"
SDRAM_OPTIONS="-dataflow-analysis -object-cache-analysis -jop-ocache-access-cycles 2 -jop-ocache-access-delay 10 -jop-ocache-max-burst 4"
SDRAM_KEY="_2_10_4"

# The lift benchmark

if [ $RUNLIFT ] ; then
echo "Running Lift: '#{RUNLIFT}'"

# Configuration SRAM (2*w)
make java_app wcet P1=test P2=wcet P3=StartLift WCET_METHOD=measure WCET_OPTIONS="${SRAM_OPTIONS}"
cp java/target/wcet/wcet.StartLift_measure/ocache_eval.txt ${RESULT_DIR}/lift${SRAM_KEY}.txt

# Configuration SDRAM (10+2*w) with max_burst = 4 words
make java_app wcet P1=test P2=wcet P3=StartLift WCET_METHOD=measure WCET_OPTIONS="${SDRAM_OPTIONS}"
cp java/target/wcet/wcet.StartLift_measure/ocache_eval.txt ${RESULT_DIR}/lift${SDRAM_KEY}.txt
fi


# The trading benchmark

# checkForTrade
if [ $RUNORDER ] ; then
echo "Running Order Manager: '${RUNORDER}'"

RESULT_FILE=java/target/wcet/com.sun.oss.trader.Main_com.sun.oss.trader.tradingengine.OrderManager.checkForTrade_Lcom_sun_oss_trader_data_OrderEntry_D_Z/ocache_eval.txt
WCET_METHOD="com.sun.oss.trader.tradingengine.OrderManager.checkForTrade(Lcom/sun/oss/trader/data/OrderEntry;D)Z"

make java_app wcet P1=paper/trading/plain P2=com/sun/oss/trader P3=Main TARGET_JDK=jdk16mod \
    WCET_METHOD=${WCET_METHOD} WCET_OPTIONS="${SRAM_OPTIONS}"
cp ${RESULT_FILE} ${RESULT_DIR}/order_manager${SRAM_KEY}.txt

make java_app wcet P1=paper/trading/plain P2=com/sun/oss/trader P3=Main TARGET_JDK=jdk16mod \
    WCET_METHOD=${WCET_METHOD} WCET_OPTIONS="${SDRAM_OPTIONS}"
cp ${RESULT_FILE} ${RESULT_DIR}/order_manager${SDRAM_KEY}.txt

fi

# The TTPA benchmark
if [ $RUNTTPA ] ; then
echo "Running TTPA"
WCET_METHOD=ttpa.protocol.Node.doMpSlotAction
make java_app wcet P1=common P2=ttpa/demo P3=Main \
    WCET_METHOD=${WCET_METHOD} WCET_OPTIONS="${SRAM_OPTIONS}"
fi

exit

# The CDx (micro) benchmark
#make java_app wcet P1=bench P2=scd_micro P3=Main WCET_METHOD="scd_micro.Motion.findIntersection(Lscd_micro/Motion)" \
make java_app wcet P1=bench P2=scd_micro P3=Main WCET_METHOD="measure" \
  TARGET_JDK=jdk16mod \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis"

exit;

# The jolden benchmarks
# jolden is not in the source

#MST
make java_app wcet P1=bench P2=jolden/mst P3=MST WCET_METHOD=main \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis -object-cache-fields"
make java_app wcet P1=bench P2=jolden/mst P3=MST WCET_METHOD=main \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis"


exit;

# Synthetic Benchmarks

make java_app wcet P1=test P2=wcet/devel P3=Simple WCET_METHOD=measure3 \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis -jop-ocache-line-size 1"

exit;


make java_app wcet P1=paper/trading/plain P2=com/sun/oss/trader P3=Main WCET_METHOD="com.sun.oss.trader.tradingengine.OrderManager.checkForTrade(Lcom/sun/oss/trader/data/OrderEntry;D)Z" \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis"


# jolden is not in the source
make java_app wcet P1=test P2=wcet P3=StartLift WCET_METHOD=measure \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis -jop-ocache-line-size 1"

exit;

make java_app wcet P1=bench P2=scd_micro P3=Main WCET_METHOD="scd_micro.Motion.findIntersection(Lscd_micro/Motion)" \
  TARGET_JDK?=jdk16mod \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis"


make java_app wcet P1=test P2=wcet P3=StartLift WCET_METHOD=measure \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis"


make java_app wcet P1=common P2=ttpa/demo P3=Main WCET_METHOD=ttpa.protocol.Node.doMpSlotAction \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis"


make java_app wcet P1=paper/trading/plain P2=com/sun/oss/trader P3=Main WCET_METHOD="com.sun.oss.trader.tradingengine.OrderManager.checkForTrade(Lcom/sun/oss/trader/data/OrderEntry;D)Z" \
  WCET_OPTIONS="-dataflow-analysis -object-cache-analysis"




