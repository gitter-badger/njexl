#!/bin/bash

echo "after call start"
args=( "$@" )

for i in `seq 0  $((${#args[@]} -1))`; do
    echo "Arg $i = ${args[$i]}"
done
echo "after call end"
