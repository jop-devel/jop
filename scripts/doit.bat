make pld_init -e BLASTER_TYPE=USB-Blaster
pause
make japp -e BLASTER_TYPE=USB-Blaster COM_PORT=COM18 P1=common P2=util P3=EraseFlash
