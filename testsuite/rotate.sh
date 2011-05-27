# Rotate the build directories
KEEP=14

set -e
LOG_DIR=$1
if [ -z ${LOG_DIR} ]; then
    echo "Usage: rotate LOG_DIR"
    exit 1
fi
if [ ! -e ${LOG_DIR}/current ]; then
   echo "'current' log directory does not exit"
   exit 1
fi
cd ${LOG_DIR}
for n in `seq 1 $((KEEP-1)) | sort -nr` ; do
    if [ -e jop-${n}-days-ago ] ; then
	echo "Rotating jop-$n-days-ago"
	mv jop-$n-days-ago jop-$((n+1))-days-ago
    fi
done

if [ -e jop-$KEEP-days-ago ]; then
    rm -rf jop-$KEEP-days-ago
fi
mv current jop-1-days-ago

