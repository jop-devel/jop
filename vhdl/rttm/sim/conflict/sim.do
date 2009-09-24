# Value Range: 0 (note), 1 (warning), 2 (error), 3 (failure), 4 (fatal)
set BreakOnAssertion 1

view wave
do wave.do

set NumericStdNoWarnings 1
run 10 ns
set NumericStdNoWarnings 0
run -all

wave zoomfull
