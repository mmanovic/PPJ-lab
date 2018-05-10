#!/bin/bash

TEST_FOLDER_PATH=/home/u1/Documents/Faks/PPJ/PPJ-Labos/ppjTestovi/3labos-2012-2012
CLASS_FOLDER_PATH=/home/u1/coding/gitProjects/PPJ/PPJ_Semantic/out/production/PPJ_Semantic
SOURCE_FOLDER_PATH=/home/u1/coding/gitProjects/PPJ/PPJ_Semantic
RESULT="";

passed=0
total=0
for f in $(find $TEST_FOLDER_PATH -name '*.in'); do 
#java -cp $CLASS_FOLDER_PATH SemantickiAnalizator < $f > /dev/null;
java -cp $CLASS_FOLDER_PATH SemantickiAnalizator < ${f: :-3}".in" > $SOURCE_FOLDER_PATH/out.txt 2>&1;
RESULT=$(diff $SOURCE_FOLDER_PATH/out.txt ${f: :-3}".out" -q -b);
if [[ $RESULT = *[!\ ]* ]]; then
  #echo "ERROR: "${f};
  total=$((total + 1));
  
  #break;
else
  #echo "PASSED: "${f::-8};
  total=$((total + 1));
  passed=$((passed + 1));

fi
done

echo total: ${total}, passed: ${passed}, failed: $((total - passed))
