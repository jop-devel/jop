<<<<<<< Updated upstream:vhdl/rttm/sim/sim_batch.do
set NumericStdNoWarnings 1
run -all
quit
=======
# Value Range: 0 (note), 1 (warning), 2 (error), 3 (failure), 4 (fatal)
set BreakOnAssertion 1

set NumericStdNoWarnings 1
set StdArithNoWarnings 1
run 0 ns;
set StdArithNoWarnings 0

run -all
quit
>>>>>>> Stashed changes:vhdl/rttm/sim/sim_batch.do
