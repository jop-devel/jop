#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"
jbe="linefollower lift kfl"
dev="dev_supergraph1 dev_loadonreturn dev_rasmus"
broken="udpip ejipcmp"
benchmarks="${jbe} ${dev}"
for B in ${benchmarks} ; do
    ${DIR}/${B}
done
