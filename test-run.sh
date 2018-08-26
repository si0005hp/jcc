#!/bin/bash


TEST_DIR=${1:-.test}
IS_COMPILE_ONLY=${2:-f}

TEST_LIST="test.list"
FAILS_LIST="fails.list"


(
	cd $TEST_DIR || exit 1
	rm -f "$FAILS_LIST"

	cat "$TEST_LIST" | grep -v "@Fail$" | while IFS="	" read f type answer
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

		if [[ "$IS_COMPILE_ONLY" == "f" ]]; then
			if [[ "$type" == "d" ]]; then
				# Test by output using 'd'iff
				./$elfName > "${elfName}.result"
				diff "${elfName}.result" "$answer" > /dev/null; res="$?"
				if [[ "$res" != "0" ]]; then
					echo "Test failed: ${asmName}	Expected -> ${answer} Actual result -> ${elfName}.result"
					echo "${asmName}	d" >> "$FAILS_LIST"
				fi
			else
				# Test by 'e'xit value or 's'ysout
				if [[ "$type" == "e" ]]; then
					./$elfName; res="$?"
				else 
					res=$(./$elfName)
				fi
				if [ "$res" != "$answer" ]; then
					echo "Test failed: ${asmName}	${answer} expected but got ${res}"
					echo "${asmName}	t" >> "$FAILS_LIST"
	  			fi
			fi
		fi
	done

	if [ -f "$FAILS_LIST" ]; then
		n=$(wc -l "$FAILS_LIST" | awk '{print $1}')
		echo "-------------------"
		echo "[${n} tests failed]"
		while read e; do echo "$e"; done < "$FAILS_LIST" 
		echo "-------------------"
	else
		n=$(wc -l "$TEST_LIST" | awk '{print $1}')
		echo "-----------------------"
		echo "[All ${n} tests passed]"
		echo "-----------------------"
	fi
)