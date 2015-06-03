#####################################################################
# Copyright 2015 Nabarun Mondal
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#########################################################################

#!/usr/local/bin/bash


function call_script(){
    arg_array=( "$@" )
    arg=( "${arg_array[@]:1}" )
    echo "Invoking script : $1 [with] data : ${arg[@]}"
    $BASE$1 ${arg[@]}
}

function run_tests(){
    script=$1 
    data_file=$2 
    before=$3 
    after=$4 
    while read data ; do

        if [[ ${data:0:1} == '#' ]] ; then
            # comment line, or header hence 
            continue
        fi
        # call the before handler 
        call_script $before $data 
        if [[ $? != 0 ]]; then 
            echo $before 'failed with :[' $data '] skipping script execution' >&2 
            continue
        fi 
        # call the actual script
        call_script $script $data
        if [[ $? != 0 ]]; then 
            echo  $script 'failed with :['  $data  '] skipping test validation' >&2 
            continue
        fi 
        # call the before handler 
        call_script $after $data 
        if [[ $? != 0 ]] ; then 
            echo $after 'failed with :[' $data '] failing test validation' >&2
        fi
    done < $BASE$data_file

}

function main(){

    while read line ; do
        if [[ ${line:0:1} == '#' ]] ; then
            # comment line, or header hence 
            continue
        fi 
        IFS=$'\t' read -a words <<< "$line"
        run_tests "${words[@]}"   
    done < $BASE$MASTER_FILE
}

if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters : "
    echo "first argument should be the base directory ending in "/", second argument is the master data file"
    exit 255
fi
BASE=$1
MASTER_FILE=$2
main $BASE $MASTER_FILE
