#! /bin/bash

# Usage:
#  typeset.sh    # builds main thesis.pdf file
#  typeset.sh 2  # builds PDF of chapter 2 only
#  typeset.sh a  # builds PDF of appendices only



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
fi


echo "Typesetting $THESISDIR/$SUBDIR/$FILENAME"


# double-check that the file exists
cd "$THESISDIR/$SUBDIR"
if [ ! -f "$FILENAME.tex" -o ! -r "$FILENAME.tex" ]
then
	(>&2 echo "File $THESISDIR/$SUBDIR/$FILENAME.tex not readable.")
	exit 1
fi


# clean biblatex cache (if desired)
CLEAN=1
function cleancache {
	(( "$CLEAN" )) || return
	echo "Cleanup:"
#	rm -fR `biber --cache`
	rm -vf "$FILENAME.aux"
	rm -vf "$FILENAME.bbl"
	rm -vf "$FILENAME.bcf"
	rm -vf "$FILENAME.lof"
	rm -vf "$FILENAME.lot"
	rm -vf "$FILENAME.out"
	rm -vf "$FILENAME.run.xml"
	rm -vf "$FILENAME.toc"
}
#cleancache

# typeset TeX file
xelatex --file-line-error "$FILENAME.tex"
biber "$FILENAME"
mv "$FILENAME.blg" "$FILENAME.biber.log"
xelatex --file-line-error "$FILENAME.tex"
biber "$FILENAME"  # cross-references
cat "$FILENAME.blg" >> "$FILENAME.biber.log" && rm -f "$FILENAME.blg"
#xelatex --file-line-error "$FILENAME.tex"
xelatex --file-line-error "$FILENAME.tex" | tee "$FILENAME.stdout.log"
cleancache

# the 4th xelatex run might be superfluous


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
