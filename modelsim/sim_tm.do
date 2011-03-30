set BreakOnAssertion 1
set NumericStdNoWarnings 1
#view *
view wave
do wave_tm.do
#nolog -all
#when -label start_logging {$now == @6.0 ms} {echo "Start logging " ; log -r *;}
run 25 ms
