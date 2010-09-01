SOURCEPATH=../../java/target/src
SP1=$(SOURCEPATH)/common
SP2=$(SOURCEPATH)/bench
SP3=$(SOURCEPATH)/jdk_base
SP4=$(SOURCEPATH)/jdk11
SOURCE=$(SP1)\;$(SP2)\;$(SP3)\;$(SP4)
PACKAGES=util:ejip:com:joprt:jbe:java
#EXCLUDE=ejip.jtcpip
EXCLUDE=""

all: jdoc 
	cd appendix && make
	pdflatex handbook
	bibtex handbook
	makeindex handbook
	pdflatex handbook
	pdflatex handbook

# bib is a local target for my machine, so we're not
# including it in all
bib:
	cd ../bib && make

# that was the original one
# -docletpath ../lib/texdoclet.jar  -doclet org.wonderly.doclets.TexDoclet \
# we use the patched version from:
#http://www.infosun.fim.uni-passau.de/br/lehrstuhl/Kurse/sep_ws0405/doclet.html

jdoc:
	javadoc 								\
		-docletpath ../lib/doclet.jar -doclet com.c2_tech.doclets.TexDoclet \
		-sourcepath $(SOURCE)				\
		-subpackages $(PACKAGES) -exclude $(EXCLUDE)
	tail +84 docs.tex | sed '$$,$$ d' > classes/classes.tex
	rm docs.tex

htmldoc: javadoc
	javadoc 								\
		-d ../javadoc 						\
		-sourcepath $(SOURCE)				\
		-subpackages $(PACKAGES) -exclude $(EXCLUDE)

javadoc:
	-mkdir ../javadoc

clean:
	-rm *.aux
	-rm *.gz
	-rm *.log
	-rm *.lof
	-rm *.lol
	-rm *.lot
	-rm *.out
	-rm *.blg
	-rm *.bbl
	-rm *.bak
	-rm */*.bak
	-rm *.toc
	-rm *.pdf
	-rm *.idx
	-rm *.ilg
	-rm *.ind
	cd appendix && make clean
	-rm -r ../javadoc
	-rm classes/classes.tex
