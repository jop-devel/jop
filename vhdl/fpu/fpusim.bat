set REL= .\test_bench

vlib work

vcom fpupack.vhd
vcom pre_norm_addsub.vhd
vcom addsub_28.vhd
vcom post_norm_addsub.vhd
vcom pre_norm_mul.vhd
vcom mul_24.vhd
vcom serial_mul.vhd
vcom post_norm_mul.vhd
vcom pre_norm_div.vhd
vcom serial_div.vhd
vcom post_norm_div.vhd
vcom pre_norm_sqrt.vhd
vcom sqrt.vhd
vcom post_norm_sqrt.vhd
vcom comppack.vhd
vcom fpu.vhd

rem *** compile FPU II only for testing.
rem ***Usselmann's FPU is used here
vlog %REL%\FPU_II\pre_norm.v
vlog %REL%\FPU_II\pre_norm_fmul.v
vlog %REL%\FPU_II\primitives.v
vlog %REL%\FPU_II\except.v
vlog %REL%\FPU_II\post_norm.v
vlog %REL%\FPU_II\fpu.v

vcom %REL%\tb_fpu.vhd



pause Start simulation?


vsim -do fpu_wave.do tb_fpu


