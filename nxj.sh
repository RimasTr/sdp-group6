#!/bin/bash
BASE="$(readlink -f "$(dirname $0)")"
export "NXJ_HOME=$BASE/lib/lejos_nxj_91"
export "PATH=$PATH:$NXJ_HOME/bin"
export "LOCALBASE=$LOCALBASE:$BASE/lib"
export "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$BASE/lib/lib"

