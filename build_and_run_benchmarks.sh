#!/bin/bash

set -x
set -e

# If given an argument, the first is a directory:
if [ "$1" != "" ]; then 
  cd $1
  shift
fi

if ! [ -f dpjbin.tar.gz ]; then
  wget http://dpj.cs.uiuc.edu/DPJ/Download_files/dpjbin.tar.gz
fi 
if ! [ -f ./dpjbin/Implementation/bin/dpj ]; then
  tar xzvf dpjbin.tar.gz
fi

echo "Patching the Harness.java file and Makefile..."
cp -v Harness.java dpjbin/Benchmarks/Kernels/dpj/Harness.java
cp -v tweaked_Makefile dpjbin/Benchmarks/Kernels/Makefile

source envvars.sh
module add intel  # For ICC

cabal sandbox init
# cabal install --only-dependencies -j 
# cabal build
cabal install -j ./ ./HSBencher/

# Simple way:
# ./run_benchmark --trials=5 --server

# Complicated way to run the benchmarks:
# ----------------------------------------
export TRIALS=5

# Parfunc account, registered app in api console:
CID=905767673358.apps.googleusercontent.com
SEC=2a2H57dBggubW1_rqglC7jtK

# LVish doc ID:  
TABID=1YxEmNpeUoGCBptDK0ddtomC_oK2IVH1f2M89IIA
# https://www.google.com/fusiontables/DataSource?docid=1YxEmNpeUoGCBptDK0ddtomC_oK2IVH1f2M89IIA#rows

# RUNIT=./run_benchmark
RUNIT=./.cabal-sandbox/bin/run_benchmark

# Enable upload of benchmarking data to a Google Fusion Table:
# $RUNIT --server --keepgoing --trials=$TRIALS --fusion-upload=$TABID --clientid=$CID --clientsecret=$SEC $*
$RUNIT --server --keepgoing --trials=$TRIALS --fusion-upload=$TABID --clientid=$CID --clientsecret=$SEC $*
