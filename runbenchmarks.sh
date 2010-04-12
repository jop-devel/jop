#! /bin/sh

for b in allocObjs allocHandles allocHeaders allocBlocks; do
	mkdir $b
 	ant -Dp1=app/trading/plain -Dp2=com/sun/oss/trader -Dp3=Main -Dwcet-method="com.sun.oss.trader.tradingengine.OrderManager.checkForTrade(Lcom/sun/oss/trader/data/OrderEntry;D)Z" -Dwcet-processor=$b tools java-app wcet
 	cp -r java/target/wcet/* $b
 	ant -Dp1=app/trading/plain -Dp2=com/sun/oss/trader -Dp3=Main -Dwcet-method="com.sun.oss.trader.tradingengine.MarketManager.onMessage(Ljava/lang/String;)V" -Dwcet-processor=$b tools java-app wcet
 	cp -r java/target/wcet/* $b
	ant -Dp1=bench -Dp2=scd_micro -Dp3=Main -Dwcet-method="run()V" -Dwcet-processor=$b tools java-app wcet
	cp -r java/target/wcet/* $b
done	