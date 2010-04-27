#! /bin/sh

for b in allocHandles allocBlocks; do
	mkdir $b
# 	ant -Dp1=bench -Dp2=jolden/mst -Dp3=MST -Dwcet-method="main" -Dwcet-processor=$b tools java-app wcet
#	cp -r java/target/wcet/* $b
# 	ant -Dp1=bench -Dp2=jolden/em3d -Dp3=Em3d -Dwcet-method="main" -Dwcet-processor=$b tools java-app wcet
# 	cp -r java/target/wcet/* $b
# 	ant -Dp1=bench -Dp2=jolden/bh -Dp3=BH -Dwcet-method="jolden.bh.Tree.createTestData(I)V" -Dwcet-processor=$b tools java-app wcet
# 	cp -r java/target/wcet/* $b
# 	ant -Dp1=paper/trading/plain -Dp2=com/sun/oss/trader -Dp3=Main -Dwcet-method="com.sun.oss.trader.tradingengine.OrderManager.checkForTrade(Lcom/sun/oss/trader/data/OrderEntry;D)Z" -Dwcet-processor=$b tools java-app wcet
# 	cp -r java/target/wcet/* $b
 	ant -Dp1=paper/trading/plain -Dp2=com/sun/oss/trader -Dp3=Main -Dwcet-method="com.sun.oss.trader.tradingengine.MarketManager.onMessage(Ljava/lang/String;)V" -Dwcet-processor=$b tools java-app wcet
 	cp -r java/target/wcet/* $b
#	ant -Dp1=bench -Dp2=scd_micro -Dp3=Main -Dwcet-method="run()V" -Dwcet-processor=$b tools java-app wcet
#	cp -r java/target/wcet/* $b
done	