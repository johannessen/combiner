#! /bin/bash

# Usage:
#  typeset.sh    # builds main thesis.pdf file
#  typeset.sh 2  # builds PDF of chapter 2 only
#  typeset.sh a  # builds PDF of appendices only
#  (export THOROUGH=0;typeset.sh)  # extra quick run
#  (export THOROUGH=2;export CLEAN=2;typeset.sh)  # extra thorough run


# determine file to typeset
SUBDIR="main"
FILENAME="thesis"

THESISDIR=`dirname "$0"`
if [ -n "$1" ] && (( "$1" >= 1 && "$1" <= 8 ))
then
	SUBDIR="chapter$1"
	FILENAME=`(cd "$THESISDIR/$SUBDIR" && find *.tex) | sed -e 's/\.tex$//'`
	if (( `echo "$FILENAME" | wc -l` != 1 ))
	then
		(>&2 printf "TeX file in $THESISDIR/$SUBDIR/ not identified; found:\n$FILENAME\n")
		exit 1
	fi
elif [ -n "$1" ] && [ "$1" = "A" -o "$1" = "a" ]
then
	SUBDIR="appendices"
	FILENAME="Anhang"
elif [ -n "$1" ] && [ "$1" = "B" -o "$1" = "b" ]
then
	SUBDIR="bibliography"
	FILENAME="Literaturverzeichnis"
fi


# prefs
CLEAN="${CLEAN:=1}"  # 0 nothing, 1 only files (default), 2 files and Biber cache
THOROUGH="${THOROUGH:=1}"  # 0 never, 1 only for main file and bib (default), 2 always
if [ -n "$THOROUGH" ] && [ "$THOROUGH" = 2 -o "$THOROUGH" = 1 -a "$SUBDIR" = "main" -o "$THOROUGH" = 1 -a "$SUBDIR" = "bibliography" ]
then
	SLOW=1
fi


echo "Typesetting $THESISDIR/$SUBDIR/$FILENAME"
echo "(clean=$CLEAN, thorough=$SLOW)"


# double-check that the file exists
cd "$THESISDIR/$SUBDIR"
if [ ! -f "$FILENAME.tex" -o ! -r "$FILENAME.tex" ]
then
	(>&2 echo "File $THESISDIR/$SUBDIR/$FILENAME.tex not readable.")
	exit 1
fi


function cleancache {
	(( "$CLEAN" )) || return
	echo "Cleanup:"
	(( "$CLEAN" == 2 )) && ( echo "Biber cache" ; rm -fR `biber --cache` )
	rm -vf "$FILENAME.aux"
	rm -vf "$FILENAME.bbl"
	rm -vf "$FILENAME.bcf"
	rm -vf "$FILENAME.lof"
	rm -vf "$FILENAME.lot"
	rm -vf "$FILENAME.out"
	rm -vf "$FILENAME.run.xml"
	rm -vf "$FILENAME.toc"
}


# typeset TeX file
#(( "$SLOW" )) && cleancache
if (( "$SLOW" ))
then
	cleancache
	xelatex --file-line-error "$FILENAME.tex"
	biber "$FILENAME"
	mv "$FILENAME.blg" "$FILENAME.biber.log"
	xelatex --file-line-error "$FILENAME.tex"
	biber "$FILENAME"  # cross-references
	cat "$FILENAME.blg" >> "$FILENAME.biber.log" && rm -f "$FILENAME.blg"
#	xelatex --file-line-error "$FILENAME.tex"
	# the extra xelatex run might be superfluous
fi
xelatex --file-line-error "$FILENAME.tex" | tee "$FILENAME.stdout.log"
cleancache


# show license status
if [ "$FILENAME" = "thesis" ]
then
	if find ../*/*_51.png > /dev/null
	then
		printf "\nWarning:\n"
		echo "The resulting file is NOT compatible with CC-BY-SA!"
		echo "Licensed under: CC-BY-ND"
	else
		printf "\nWarning:\n"
		echo "Missing images! See page 2 of main/thesis.pdf for an explanation."
		echo "The resulting file is probably compatible with CC-BY-SA."
	fi
	echo
fi
