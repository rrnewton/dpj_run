#!/bin/bash

set -e 

# source this file

if ! [ -f dpjbin.tar.gz ]; then
  wget http://dpj.cs.uiuc.edu/DPJ/Download_files/dpjbin.tar.gz
fi 
if ! [ -f ./dpjbin/Implementation/bin/dpj ]; then
  tar xzvf dpjbin.tar.gz
fi

# export DPJ_ROOT=`pwd`/dpjbin/build/dpjbin
export DPJ_ROOT=`pwd`/dpjbin/
export PATH=$PATH:$DPJ_ROOT/Implementation/bin

echo "Patching the Harness.java file..."
cp -v Harness.java dpjbin/Benchmarks/Kernels/dpj/Harness.java

# cabal sandbox init
cabal install --only-dependencies -j 
cabal build
