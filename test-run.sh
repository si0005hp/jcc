#!/bin/bash


TEST_DIR=${1:-test}
IS_COMPILE_ONLY=${2:-f}

TEST_LIST="test.list"

(
	errors=()

	cd $TEST_DIR
	cat "$TEST_LIST" | while IFS="	" read f answer
	do
		asmName=$(echo "$f" | sed -e 's/\.c/\.s/g')
		elfName=$(echo "$f" | sed -e 's/\.c//g')
		
		# gcc compile
		gcc -o "$elfName" "$asmName"
		if [ $? -ne 0 ]; then
	    	echo "Compile failed: ${asmName}"
	    	errors+=( "$asmName" )
	    	continue
  		fi


		if [[ "$IS_COMPILE_ONLY" != "f" ]]; then
			./$elfName
			res="$?"
			if [ "$res" != "$answer" ]; then
    			echo "Test failed: ${asmName}	${answer} expected but got ${res}"
    			errors+=( "$asmName" )
  			fi
		fi
	done

	if [[ ${#errors[@]} -ne 0 ]]; then
		echo "-----------------"
		echo "Failed tests -> "
		for e in ${errors[@]}; do echo ${e}; done
		echo "-----------------"
	else
		echo "-----------------"
		echo "All tests passed."
		echo "-----------------"
	fi
)