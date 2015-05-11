#!/bin/bash

echo "before call start"
args=( "$@" )

for i in `seq 0  $((${#args[@]} -1))`; do
    echo "Arg $i = ${args[$i]}"
done
echo "before call end"
