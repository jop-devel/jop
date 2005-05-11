call lejosc embjbench\%1.java
call lejoslink -o %1.bin embjbench\%1
pause
call lejosdl %1.bin
del embjbench\*.class
del %1.bin
