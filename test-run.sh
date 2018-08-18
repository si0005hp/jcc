#!/bin/bash


TEST_DIR=${1:-test}
IS_COMPILE_ONLY=${2:-f}

TEST_LIST="test.list"
FAILS_LIST="fails.list"


(
	cd $TEST_DIR || exit 1
	rm -f "$FAILS_LIST"

	cat "$TEST_LIST" | while IFS="	" read f answer
	do
		asmName=$(echo "$f" | sed -e 's/\.c/\.s/g')
		elfName=$(echo "$f" | sed -e 's/\.c//g')
		
		# gcc compile
		gcc -o "$elfName" "$asmName"
		if [ $? -ne 0 ]; then
	    	echo "Compile failed: ${asmName}"
	    	echo "${asmName}	c" >> "$FAILS_LIST"
 	    	continue
  		fi

		if [[ "$IS_COMPILE_ONLY" != "f" ]]; then
			./$elfName
			res="$?"
			if [ "$res" != "$answer" ]; then
    			echo "Test failed: ${asmName}	${answer} expected but got ${res}"
    			echo "${asmName}	t" >> "$FAILS_LIST"
  			fi
		fi
	done

	if [ -f "$FAILS_LIST" ]; then
		echo "-------------------"
		echo "[Failed tests]"
		while read e; do echo "$e"; done < "$FAILS_LIST" 
		echo "-------------------"
	else
		echo "------------------"
		echo "[All tests passed]"
		echo "------------------"
	fi
)