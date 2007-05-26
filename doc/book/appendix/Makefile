all:
	cd tools && javac *.java
	java -cp tools Csv2Latex microcode.csv > microcode.tex
	pdfLatex timing

clean:
	-cd tools && rm *.class
	-rm microcode.tex
	-rm *.aux
	-rm *.log
	-rm *.out
	-rm *.bak
	-rm *.pdf
