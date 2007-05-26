all:
	cd ../bib && make
	cd appendix && make
	pdfLatex handbook
#	bibtex handbook
#	pdfLatex handbook
#	pdfLatex handbook

clean:
	-rm *.aux
	-rm *.log
	-rm *.out
	-rm *.blg
	-rm *.bbl
	-rm *.bak
	-rm *.toc
	-rm *.pdf
	cd appendix && make clean
