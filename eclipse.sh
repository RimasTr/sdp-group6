#!/bin/bash

BASE="$(readlink -f "$(dirname $0)")"
export "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$BASE/lib/lib"
/usr/bin/eclipse-4.2 &
