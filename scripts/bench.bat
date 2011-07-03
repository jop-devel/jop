
rem make japp -e QPROJ=altde2-70cmp BLASTER_TYPE=USB-Blaster COM_PORT=COM23 TARGET_APP_PATH=../Documents/workspace/jembench/src MAIN_CLASS=jembench/Main P3=Main

rem make -e QPROJ=altde2-70cmp_cache BLASTER_TYPE=USB-Blaster COM_PORT=COM18 TARGET_APP_PATH=../Documents/workspace/jembench/src MAIN_CLASS=jembench/Main P3=Main

rem make japp -e QPROJ=altde2-70cmp_cache BLASTER_TYPE=USB-Blaster COM_PORT=COM18 P1=bench P2=jembench P3=Main

rem make japp -e QPROJ=altde2-70cmp_cache BLASTER_TYPE=USB-Blaster COM_PORT=COM18 P1=test P2=cache P3=TestConcurrent

rem make japp -e USB=true QPROJ=usb100 TARGET_APP_PATH=../Documents/workspace/jembench/src MAIN_CLASS=jembench/Main P3=Main

rem make japp -e USB=true QPROJ=usbmin TARGET_APP_PATH=../Documents/workspace/jembench/src MAIN_CLASS=jembench/Main P3=Main

rem make japp -e USB=true QPROJ=cyccmp TARGET_APP_PATH=../Documents/workspace/jembench/src MAIN_CLASS=jembench/Main P3=Main

rem make japp -e USB=true QPROJ=cyccmp TARGET_APP_PATH=../Documents/workspace/jembench/src MAIN_CLASS=jembench/cmp/EjipBenchCMP P3=EjipBenchCMP

rem make japp -e USB=true QPROJ=cyccmp P1=bench P2=jembench/cmp P3=EjipBenchCMP
rem make japp -e USB=true QPROJ=cyccmp P1=test P2=cmp P3=EjipBenchCMP
rem make -e japp USB=true QPROJ=cyccmp P1=test P2=cache P3=TestConcurrent
rem make -e USB=true QPROJ=cyccmp P1=bench P2=jembench P3=Main
rem make -e USB=true QPROJ=usbmin P1=test P2=test P3=DoAll
rem make -e USB=true QPROJ=cyccmp P1=bench P2=jembench P3=Main

rem make -e USB=true QPROJ=usb100 P1=bench P2=jembench P3=Main COM_PORT=COM4

make japp -e QPROJ=altde2-70 BLASTER_TYPE=USB-Blaster COM_PORT=COM5 P1=bench P2=jembench P3=Main

rem make japp -e USB=true QPROJ=cyccsp P1=paper P2=csp P3=BenchCsp COM_PORT=COM4

rem make japp -e USB=true QPROJ=usbmin P1=bench P2=fixed P3=LoopAes COM_PORT=COM4
