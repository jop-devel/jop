<<<<<<< Updated upstream:vhdl/rttm/sim/tm_coordinator/wave.do
onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -format Logic /tb_tm_coordinator/finished
add wave -noupdate -format Logic /tb_tm_coordinator/clk
add wave -noupdate -format Logic /tb_tm_coordinator/reset
add wave -noupdate -format Literal /tb_tm_coordinator/commit_try
add wave -noupdate -format Literal /tb_tm_coordinator/commit_allow
add wave -noupdate -format Logic /tb_tm_coordinator/dut/clk
add wave -noupdate -format Logic /tb_tm_coordinator/dut/reset
add wave -noupdate -format Literal /tb_tm_coordinator/dut/commit_try
add wave -noupdate -format Literal /tb_tm_coordinator/dut/commit_allow
add wave -noupdate -format Literal -radix decimal /tb_tm_coordinator/dut/next_committer
add wave -noupdate -format Literal -radix decimal /tb_tm_coordinator/dut/committer
add wave -noupdate -format Literal -radix decimal /tb_tm_coordinator/dut/commit_race_winner
add wave -noupdate -format Literal /tb_tm_coordinator/dut/next_commit_allow
add wave -noupdate -format Logic /tb_tm_coordinator/dut/next_committing
add wave -noupdate -format Logic /tb_tm_coordinator/dut/committing
add wave -noupdate -format Literal /tb_tm_coordinator/dut/commit_race_result
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {0 ns} 0}
configure wave -namecolwidth 280
configure wave -valuecolwidth 68
configure wave -justifyvalue left
configure wave -signalnamewidth 0
configure wave -snapdistance 10
configure wave -datasetprefix 0
configure wave -rowmargin 4
configure wave -childrowmargin 2
configure wave -gridoffset 0
configure wave -gridperiod 1
configure wave -griddelta 40
configure wave -timeline 0
configure wave -timelineunits ns
update
WaveRestoreZoom {0 ns} {67 ns}
=======
add wave *
add wave /dut/*
>>>>>>> Stashed changes:vhdl/rttm/sim/tm_coordinator/wave.do
