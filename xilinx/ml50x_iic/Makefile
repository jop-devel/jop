NAME=ml50x_iic
PART=xc5vfx70t-1-ff1136

all:
	rm -rf out
	mkdir out out/tmp || exit 1
	xst -intstyle ise -ifn ${NAME}.xst \
        -ofn out/${NAME}.syr || exit 1
	ngdbuild -intstyle ise -dd out/_ngo -sd ./ipcore_dir -uc ${NAME}.ucf \
        -p ${PART} out/${NAME}.ngc out/${NAME}.ngd || exit 1
	map -intstyle ise -p ${PART} -cm area -pr b -c 100 -tx off \
        -o out/${NAME}_map.ncd out/${NAME}.ngd out/${NAME}.pcf || exit 1
	par -w -intstyle ise -ol std  out/${NAME}_map.ncd \
        out/${NAME}.ncd out/${NAME}.pcf || exit 1
	trce -intstyle ise -s 1 -v 3 -l 3 -xml out/${NAME} \
        out/${NAME}.ncd -o out/${NAME}.twr out/${NAME}.pcf || exit 1
	bitgen -intstyle ise -f ${NAME}.ut out/${NAME}.ncd || exit 1
	cp out/${NAME}.bit . || exit 1

tr:
	trce -intstyle ise -s 1 -l 3 -v 3 -xml out/${NAME} \
        out/${NAME}.ncd -o out/${NAME}.twr out/${NAME}.pcf || exit 1

config:
	impact -batch impact.in

clean:
	rm -f *.ise
	rm -f *.xrpt
	rm -f *.bit
	rm -f *.log
	rm -f *.twr
	rm -f *.xml
	rm -rf xlnx_auto*
	rm -rf out
	rm -rf _xmsgs
