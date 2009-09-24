BATCH=y
PATHS=`ls -p|grep /`

all:
	for p in ${PATHS}; do \
		make -C $$p -e BATCH=${BATCH} || exit; \
	done
 

clean:
	for p in ${PATHS}; do \
		make clean -C $$p || exit; \
	done
