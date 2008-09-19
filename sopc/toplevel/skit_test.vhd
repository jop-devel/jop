--megafunction wizard: %Altera SOPC Builder%
--GENERATION: STANDARD
--VERSION: WM1.0


--Legal Notice: (C)2007 Altera Corporation. All rights reserved.  Your
--use of Altera Corporation's design tools, logic functions and other
--software and tools, and its AMPP partner logic functions, and any
--output files any of the foregoing (including device programming or
--simulation files), and any associated documentation or information are
--expressly subject to the terms and conditions of the Altera Program
--License Subscription Agreement or other applicable license agreement,
--including, without limitation, that your use is for the sole purpose
--of programming logic devices manufactured by Altera and sold by Altera
--or its authorized distributors.  Please refer to the applicable
--agreement for further details.


-- turn off superfluous VHDL processor warnings 
-- altera message_level Level1 
-- altera message_off 10034 10035 10036 10037 10230 10240 10030 

library altera;
use altera.altera_europa_support_lib.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

library std;
use std.textio.all;

entity jop_avalon_inst_avalon_master_arbitrator is 
        port (
              -- inputs:
                 signal clk : IN STD_LOGIC;
                 signal d1_sdram_s1_end_xfer : IN STD_LOGIC;
                 signal jop_avalon_inst_avalon_master_address : IN STD_LOGIC_VECTOR (25 DOWNTO 0);
                 signal jop_avalon_inst_avalon_master_read : IN STD_LOGIC;
                 signal jop_avalon_inst_avalon_master_write : IN STD_LOGIC;
                 signal jop_avalon_inst_avalon_master_writedata : IN STD_LOGIC_VECTOR (31 DOWNTO 0);
                 signal jop_avalon_inst_byteenable_sdram_s1 : IN STD_LOGIC_VECTOR (1 DOWNTO 0);
                 signal jop_avalon_inst_granted_sdram_s1 : IN STD_LOGIC;
                 signal jop_avalon_inst_qualified_request_sdram_s1 : IN STD_LOGIC;
                 signal jop_avalon_inst_read_data_valid_sdram_s1 : IN STD_LOGIC;
                 signal jop_avalon_inst_read_data_valid_sdram_s1_shift_register : IN STD_LOGIC;
                 signal jop_avalon_inst_requests_sdram_s1 : IN STD_LOGIC;
                 signal reset_n : IN STD_LOGIC;
                 signal sdram_s1_readdata_from_sa : IN STD_LOGIC_VECTOR (15 DOWNTO 0);
                 signal sdram_s1_waitrequest_from_sa : IN STD_LOGIC;

              -- outputs:
                 signal jop_avalon_inst_avalon_master_address_to_slave : OUT STD_LOGIC_VECTOR (25 DOWNTO 0);
                 signal jop_avalon_inst_avalon_master_readdata : OUT STD_LOGIC_VECTOR (31 DOWNTO 0);
                 signal jop_avalon_inst_avalon_master_waitrequest : OUT STD_LOGIC;
                 signal jop_avalon_inst_dbs_address : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                 signal jop_avalon_inst_dbs_write_16 : OUT STD_LOGIC_VECTOR (15 DOWNTO 0)
              );
attribute auto_dissolve : boolean;
attribute auto_dissolve of jop_avalon_inst_avalon_master_arbitrator : entity is FALSE;
end entity jop_avalon_inst_avalon_master_arbitrator;


architecture europa of jop_avalon_inst_avalon_master_arbitrator is
                signal active_and_waiting_last_time :  STD_LOGIC;
                signal dbs_16_reg_segment_0 :  STD_LOGIC_VECTOR (15 DOWNTO 0);
                signal dbs_count_enable :  STD_LOGIC;
                signal dbs_counter_overflow :  STD_LOGIC;
                signal internal_jop_avalon_inst_avalon_master_address_to_slave :  STD_LOGIC_VECTOR (25 DOWNTO 0);
                signal internal_jop_avalon_inst_avalon_master_waitrequest :  STD_LOGIC;
                signal internal_jop_avalon_inst_dbs_address :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal jop_avalon_inst_avalon_master_address_last_time :  STD_LOGIC_VECTOR (25 DOWNTO 0);
                signal jop_avalon_inst_avalon_master_dbs_increment :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal jop_avalon_inst_avalon_master_read_last_time :  STD_LOGIC;
                signal jop_avalon_inst_avalon_master_run :  STD_LOGIC;
                signal jop_avalon_inst_avalon_master_write_last_time :  STD_LOGIC;
                signal jop_avalon_inst_avalon_master_writedata_last_time :  STD_LOGIC_VECTOR (31 DOWNTO 0);
                signal next_dbs_address :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal p1_dbs_16_reg_segment_0 :  STD_LOGIC_VECTOR (15 DOWNTO 0);
                signal pre_dbs_count_enable :  STD_LOGIC;
                signal r_0 :  STD_LOGIC;

begin

  --r_0 master_run cascaded wait assignment, which is an e_assign
  r_0 <= Vector_To_Std_Logic((((std_logic_vector'("00000000000000000000000000000001") AND (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((((jop_avalon_inst_qualified_request_sdram_s1 OR ((jop_avalon_inst_read_data_valid_sdram_s1 AND internal_jop_avalon_inst_dbs_address(1)))) OR (((jop_avalon_inst_avalon_master_write AND NOT(or_reduce(jop_avalon_inst_byteenable_sdram_s1))) AND internal_jop_avalon_inst_dbs_address(1)))) OR NOT jop_avalon_inst_requests_sdram_s1)))))) AND (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR((((NOT jop_avalon_inst_qualified_request_sdram_s1 OR NOT jop_avalon_inst_avalon_master_read) OR (((jop_avalon_inst_read_data_valid_sdram_s1 AND (internal_jop_avalon_inst_dbs_address(1))) AND jop_avalon_inst_avalon_master_read)))))))) AND (((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR((NOT jop_avalon_inst_qualified_request_sdram_s1 OR NOT jop_avalon_inst_avalon_master_write)))) OR ((((std_logic_vector'("00000000000000000000000000000001") AND (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(NOT sdram_s1_waitrequest_from_sa)))) AND (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR((internal_jop_avalon_inst_dbs_address(1)))))) AND (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(jop_avalon_inst_avalon_master_write)))))))));
  --cascaded wait assignment, which is an e_assign
  jop_avalon_inst_avalon_master_run <= r_0;
  --optimize select-logic by passing only those address bits which matter.
  internal_jop_avalon_inst_avalon_master_address_to_slave <= Std_Logic_Vector'(std_logic_vector'("000") & jop_avalon_inst_avalon_master_address(22 DOWNTO 0));
  --pre dbs count enable, which is an e_mux
  pre_dbs_count_enable <= Vector_To_Std_Logic((((((((NOT std_logic_vector'("00000000000000000000000000000000")) AND (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(jop_avalon_inst_requests_sdram_s1)))) AND (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(jop_avalon_inst_avalon_master_write)))) AND (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(NOT(or_reduce(jop_avalon_inst_byteenable_sdram_s1))))))) OR (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(jop_avalon_inst_read_data_valid_sdram_s1)))) OR (((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR((jop_avalon_inst_granted_sdram_s1 AND jop_avalon_inst_avalon_master_write)))) AND std_logic_vector'("00000000000000000000000000000001")) AND std_logic_vector'("00000000000000000000000000000001")) AND (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(NOT sdram_s1_waitrequest_from_sa)))))));
  --input to dbs-16 stored 0, which is an e_mux
  p1_dbs_16_reg_segment_0 <= sdram_s1_readdata_from_sa;
  --dbs register for dbs-16 segment 0, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      dbs_16_reg_segment_0 <= std_logic_vector'("0000000000000000");
    elsif clk'event and clk = '1' then
      if std_logic'((dbs_count_enable AND to_std_logic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR((internal_jop_avalon_inst_dbs_address(1))))) = std_logic_vector'("00000000000000000000000000000000")))))) = '1' then 
        dbs_16_reg_segment_0 <= p1_dbs_16_reg_segment_0;
      end if;
    end if;

  end process;

  --jop_avalon_inst/avalon_master readdata mux, which is an e_mux
  jop_avalon_inst_avalon_master_readdata <= Std_Logic_Vector'(sdram_s1_readdata_from_sa(15 DOWNTO 0) & dbs_16_reg_segment_0);
  --mux write dbs 1, which is an e_mux
  jop_avalon_inst_dbs_write_16 <= A_WE_StdLogicVector((std_logic'((internal_jop_avalon_inst_dbs_address(1))) = '1'), jop_avalon_inst_avalon_master_writedata(31 DOWNTO 16), jop_avalon_inst_avalon_master_writedata(15 DOWNTO 0));
  --actual waitrequest port, which is an e_assign
  internal_jop_avalon_inst_avalon_master_waitrequest <= NOT jop_avalon_inst_avalon_master_run;
  --dbs count increment, which is an e_mux
  jop_avalon_inst_avalon_master_dbs_increment <= A_EXT (A_WE_StdLogicVector((std_logic'((jop_avalon_inst_requests_sdram_s1)) = '1'), std_logic_vector'("00000000000000000000000000000010"), std_logic_vector'("00000000000000000000000000000000")), 2);
  --dbs counter overflow, which is an e_assign
  dbs_counter_overflow <= internal_jop_avalon_inst_dbs_address(1) AND NOT((next_dbs_address(1)));
  --next master address, which is an e_assign
  next_dbs_address <= A_EXT (((std_logic_vector'("0") & (internal_jop_avalon_inst_dbs_address)) + (std_logic_vector'("0") & (jop_avalon_inst_avalon_master_dbs_increment))), 2);
  --dbs count enable, which is an e_mux
  dbs_count_enable <= pre_dbs_count_enable;
  --dbs counter, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      internal_jop_avalon_inst_dbs_address <= std_logic_vector'("00");
    elsif clk'event and clk = '1' then
      if std_logic'(dbs_count_enable) = '1' then 
        internal_jop_avalon_inst_dbs_address <= next_dbs_address;
      end if;
    end if;

  end process;

  --vhdl renameroo for output signals
  jop_avalon_inst_avalon_master_address_to_slave <= internal_jop_avalon_inst_avalon_master_address_to_slave;
  --vhdl renameroo for output signals
  jop_avalon_inst_avalon_master_waitrequest <= internal_jop_avalon_inst_avalon_master_waitrequest;
  --vhdl renameroo for output signals
  jop_avalon_inst_dbs_address <= internal_jop_avalon_inst_dbs_address;
--synthesis translate_off
    --jop_avalon_inst_avalon_master_address check against wait, which is an e_register
    process (clk, reset_n)
    begin
      if reset_n = '0' then
        jop_avalon_inst_avalon_master_address_last_time <= std_logic_vector'("00000000000000000000000000");
      elsif clk'event and clk = '1' then
        if (std_logic_vector'("00000000000000000000000000000001")) /= std_logic_vector'("00000000000000000000000000000000") then 
          jop_avalon_inst_avalon_master_address_last_time <= jop_avalon_inst_avalon_master_address;
        end if;
      end if;

    end process;

    --jop_avalon_inst/avalon_master waited last time, which is an e_register
    process (clk, reset_n)
    begin
      if reset_n = '0' then
        active_and_waiting_last_time <= std_logic'('0');
      elsif clk'event and clk = '1' then
        if (std_logic_vector'("00000000000000000000000000000001")) /= std_logic_vector'("00000000000000000000000000000000") then 
          active_and_waiting_last_time <= internal_jop_avalon_inst_avalon_master_waitrequest AND ((jop_avalon_inst_avalon_master_read OR jop_avalon_inst_avalon_master_write));
        end if;
      end if;

    end process;

    --jop_avalon_inst_avalon_master_address matches last port_name, which is an e_process
    process (active_and_waiting_last_time, jop_avalon_inst_avalon_master_address, jop_avalon_inst_avalon_master_address_last_time)
    VARIABLE write_line : line;
    begin
        if std_logic'((active_and_waiting_last_time AND to_std_logic(((jop_avalon_inst_avalon_master_address /= jop_avalon_inst_avalon_master_address_last_time))))) = '1' then 
          write(write_line, now);
          write(write_line, string'(": "));
          write(write_line, string'("jop_avalon_inst_avalon_master_address did not heed wait!!!"));
          write(output, write_line.all);
          deallocate (write_line);
          assert false report "VHDL STOP" severity failure;
        end if;

    end process;

    --jop_avalon_inst_avalon_master_read check against wait, which is an e_register
    process (clk, reset_n)
    begin
      if reset_n = '0' then
        jop_avalon_inst_avalon_master_read_last_time <= std_logic'('0');
      elsif clk'event and clk = '1' then
        if (std_logic_vector'("00000000000000000000000000000001")) /= std_logic_vector'("00000000000000000000000000000000") then 
          jop_avalon_inst_avalon_master_read_last_time <= jop_avalon_inst_avalon_master_read;
        end if;
      end if;

    end process;

    --jop_avalon_inst_avalon_master_read matches last port_name, which is an e_process
    process (active_and_waiting_last_time, jop_avalon_inst_avalon_master_read, jop_avalon_inst_avalon_master_read_last_time)
    VARIABLE write_line1 : line;
    begin
        if std_logic'((active_and_waiting_last_time AND to_std_logic(((std_logic'(jop_avalon_inst_avalon_master_read) /= std_logic'(jop_avalon_inst_avalon_master_read_last_time)))))) = '1' then 
          write(write_line1, now);
          write(write_line1, string'(": "));
          write(write_line1, string'("jop_avalon_inst_avalon_master_read did not heed wait!!!"));
          write(output, write_line1.all);
          deallocate (write_line1);
          assert false report "VHDL STOP" severity failure;
        end if;

    end process;

    --jop_avalon_inst_avalon_master_write check against wait, which is an e_register
    process (clk, reset_n)
    begin
      if reset_n = '0' then
        jop_avalon_inst_avalon_master_write_last_time <= std_logic'('0');
      elsif clk'event and clk = '1' then
        if (std_logic_vector'("00000000000000000000000000000001")) /= std_logic_vector'("00000000000000000000000000000000") then 
          jop_avalon_inst_avalon_master_write_last_time <= jop_avalon_inst_avalon_master_write;
        end if;
      end if;

    end process;

    --jop_avalon_inst_avalon_master_write matches last port_name, which is an e_process
    process (active_and_waiting_last_time, jop_avalon_inst_avalon_master_write, jop_avalon_inst_avalon_master_write_last_time)
    VARIABLE write_line2 : line;
    begin
        if std_logic'((active_and_waiting_last_time AND to_std_logic(((std_logic'(jop_avalon_inst_avalon_master_write) /= std_logic'(jop_avalon_inst_avalon_master_write_last_time)))))) = '1' then 
          write(write_line2, now);
          write(write_line2, string'(": "));
          write(write_line2, string'("jop_avalon_inst_avalon_master_write did not heed wait!!!"));
          write(output, write_line2.all);
          deallocate (write_line2);
          assert false report "VHDL STOP" severity failure;
        end if;

    end process;

    --jop_avalon_inst_avalon_master_writedata check against wait, which is an e_register
    process (clk, reset_n)
    begin
      if reset_n = '0' then
        jop_avalon_inst_avalon_master_writedata_last_time <= std_logic_vector'("00000000000000000000000000000000");
      elsif clk'event and clk = '1' then
        if (std_logic_vector'("00000000000000000000000000000001")) /= std_logic_vector'("00000000000000000000000000000000") then 
          jop_avalon_inst_avalon_master_writedata_last_time <= jop_avalon_inst_avalon_master_writedata;
        end if;
      end if;

    end process;

    --jop_avalon_inst_avalon_master_writedata matches last port_name, which is an e_process
    process (active_and_waiting_last_time, jop_avalon_inst_avalon_master_write, jop_avalon_inst_avalon_master_writedata, jop_avalon_inst_avalon_master_writedata_last_time)
    VARIABLE write_line3 : line;
    begin
        if std_logic'(((active_and_waiting_last_time AND to_std_logic(((jop_avalon_inst_avalon_master_writedata /= jop_avalon_inst_avalon_master_writedata_last_time)))) AND jop_avalon_inst_avalon_master_write)) = '1' then 
          write(write_line3, now);
          write(write_line3, string'(": "));
          write(write_line3, string'("jop_avalon_inst_avalon_master_writedata did not heed wait!!!"));
          write(output, write_line3.all);
          deallocate (write_line3);
          assert false report "VHDL STOP" severity failure;
        end if;

    end process;

--synthesis translate_on

end europa;



-- turn off superfluous VHDL processor warnings 
-- altera message_level Level1 
-- altera message_off 10034 10035 10036 10037 10230 10240 10030 

library altera;
use altera.altera_europa_support_lib.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

entity rdv_fifo_for_jop_avalon_inst_avalon_master_to_sdram_s1_module is 
        port (
              -- inputs:
                 signal clear_fifo : IN STD_LOGIC;
                 signal clk : IN STD_LOGIC;
                 signal data_in : IN STD_LOGIC;
                 signal read : IN STD_LOGIC;
                 signal reset_n : IN STD_LOGIC;
                 signal sync_reset : IN STD_LOGIC;
                 signal write : IN STD_LOGIC;

              -- outputs:
                 signal data_out : OUT STD_LOGIC;
                 signal empty : OUT STD_LOGIC;
                 signal fifo_contains_ones_n : OUT STD_LOGIC;
                 signal full : OUT STD_LOGIC
              );
end entity rdv_fifo_for_jop_avalon_inst_avalon_master_to_sdram_s1_module;


architecture europa of rdv_fifo_for_jop_avalon_inst_avalon_master_to_sdram_s1_module is
                signal full_0 :  STD_LOGIC;
                signal full_1 :  STD_LOGIC;
                signal full_2 :  STD_LOGIC;
                signal full_3 :  STD_LOGIC;
                signal full_4 :  STD_LOGIC;
                signal full_5 :  STD_LOGIC;
                signal full_6 :  STD_LOGIC;
                signal full_7 :  STD_LOGIC;
                signal how_many_ones :  STD_LOGIC_VECTOR (3 DOWNTO 0);
                signal one_count_minus_one :  STD_LOGIC_VECTOR (3 DOWNTO 0);
                signal one_count_plus_one :  STD_LOGIC_VECTOR (3 DOWNTO 0);
                signal p0_full_0 :  STD_LOGIC;
                signal p0_stage_0 :  STD_LOGIC;
                signal p1_full_1 :  STD_LOGIC;
                signal p1_stage_1 :  STD_LOGIC;
                signal p2_full_2 :  STD_LOGIC;
                signal p2_stage_2 :  STD_LOGIC;
                signal p3_full_3 :  STD_LOGIC;
                signal p3_stage_3 :  STD_LOGIC;
                signal p4_full_4 :  STD_LOGIC;
                signal p4_stage_4 :  STD_LOGIC;
                signal p5_full_5 :  STD_LOGIC;
                signal p5_stage_5 :  STD_LOGIC;
                signal p6_full_6 :  STD_LOGIC;
                signal p6_stage_6 :  STD_LOGIC;
                signal stage_0 :  STD_LOGIC;
                signal stage_1 :  STD_LOGIC;
                signal stage_2 :  STD_LOGIC;
                signal stage_3 :  STD_LOGIC;
                signal stage_4 :  STD_LOGIC;
                signal stage_5 :  STD_LOGIC;
                signal stage_6 :  STD_LOGIC;
                signal updated_one_count :  STD_LOGIC_VECTOR (3 DOWNTO 0);

begin

  data_out <= stage_0;
  full <= full_6;
  empty <= NOT(full_0);
  full_7 <= std_logic'('0');
  --data_6, which is an e_mux
  p6_stage_6 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((full_7 AND NOT clear_fifo))))) = std_logic_vector'("00000000000000000000000000000000"))), data_in, data_in);
  --data_reg_6, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      stage_6 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'((((clear_fifo OR sync_reset) OR read) OR ((write AND NOT(full_6))))) = '1' then 
        if std_logic'(((sync_reset AND full_6) AND NOT((((to_std_logic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(full_7))) = std_logic_vector'("00000000000000000000000000000000")))) AND read) AND write))))) = '1' then 
          stage_6 <= std_logic'('0');
        else
          stage_6 <= p6_stage_6;
        end if;
      end if;
    end if;

  end process;

  --control_6, which is an e_mux
  p6_full_6 <= Vector_To_Std_Logic(A_WE_StdLogicVector((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((read AND NOT(write)))))) = std_logic_vector'("00000000000000000000000000000000"))), (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(full_5))), std_logic_vector'("00000000000000000000000000000000")));
  --control_reg_6, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      full_6 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'(((clear_fifo OR ((read XOR write))) OR ((write AND NOT(full_0))))) = '1' then 
        if std_logic'(clear_fifo) = '1' then 
          full_6 <= std_logic'('0');
        else
          full_6 <= p6_full_6;
        end if;
      end if;
    end if;

  end process;

  --data_5, which is an e_mux
  p5_stage_5 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((full_6 AND NOT clear_fifo))))) = std_logic_vector'("00000000000000000000000000000000"))), data_in, stage_6);
  --data_reg_5, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      stage_5 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'((((clear_fifo OR sync_reset) OR read) OR ((write AND NOT(full_5))))) = '1' then 
        if std_logic'(((sync_reset AND full_5) AND NOT((((to_std_logic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(full_6))) = std_logic_vector'("00000000000000000000000000000000")))) AND read) AND write))))) = '1' then 
          stage_5 <= std_logic'('0');
        else
          stage_5 <= p5_stage_5;
        end if;
      end if;
    end if;

  end process;

  --control_5, which is an e_mux
  p5_full_5 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((read AND NOT(write)))))) = std_logic_vector'("00000000000000000000000000000000"))), full_4, full_6);
  --control_reg_5, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      full_5 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'(((clear_fifo OR ((read XOR write))) OR ((write AND NOT(full_0))))) = '1' then 
        if std_logic'(clear_fifo) = '1' then 
          full_5 <= std_logic'('0');
        else
          full_5 <= p5_full_5;
        end if;
      end if;
    end if;

  end process;

  --data_4, which is an e_mux
  p4_stage_4 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((full_5 AND NOT clear_fifo))))) = std_logic_vector'("00000000000000000000000000000000"))), data_in, stage_5);
  --data_reg_4, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      stage_4 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'((((clear_fifo OR sync_reset) OR read) OR ((write AND NOT(full_4))))) = '1' then 
        if std_logic'(((sync_reset AND full_4) AND NOT((((to_std_logic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(full_5))) = std_logic_vector'("00000000000000000000000000000000")))) AND read) AND write))))) = '1' then 
          stage_4 <= std_logic'('0');
        else
          stage_4 <= p4_stage_4;
        end if;
      end if;
    end if;

  end process;

  --control_4, which is an e_mux
  p4_full_4 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((read AND NOT(write)))))) = std_logic_vector'("00000000000000000000000000000000"))), full_3, full_5);
  --control_reg_4, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      full_4 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'(((clear_fifo OR ((read XOR write))) OR ((write AND NOT(full_0))))) = '1' then 
        if std_logic'(clear_fifo) = '1' then 
          full_4 <= std_logic'('0');
        else
          full_4 <= p4_full_4;
        end if;
      end if;
    end if;

  end process;

  --data_3, which is an e_mux
  p3_stage_3 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((full_4 AND NOT clear_fifo))))) = std_logic_vector'("00000000000000000000000000000000"))), data_in, stage_4);
  --data_reg_3, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      stage_3 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'((((clear_fifo OR sync_reset) OR read) OR ((write AND NOT(full_3))))) = '1' then 
        if std_logic'(((sync_reset AND full_3) AND NOT((((to_std_logic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(full_4))) = std_logic_vector'("00000000000000000000000000000000")))) AND read) AND write))))) = '1' then 
          stage_3 <= std_logic'('0');
        else
          stage_3 <= p3_stage_3;
        end if;
      end if;
    end if;

  end process;

  --control_3, which is an e_mux
  p3_full_3 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((read AND NOT(write)))))) = std_logic_vector'("00000000000000000000000000000000"))), full_2, full_4);
  --control_reg_3, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      full_3 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'(((clear_fifo OR ((read XOR write))) OR ((write AND NOT(full_0))))) = '1' then 
        if std_logic'(clear_fifo) = '1' then 
          full_3 <= std_logic'('0');
        else
          full_3 <= p3_full_3;
        end if;
      end if;
    end if;

  end process;

  --data_2, which is an e_mux
  p2_stage_2 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((full_3 AND NOT clear_fifo))))) = std_logic_vector'("00000000000000000000000000000000"))), data_in, stage_3);
  --data_reg_2, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      stage_2 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'((((clear_fifo OR sync_reset) OR read) OR ((write AND NOT(full_2))))) = '1' then 
        if std_logic'(((sync_reset AND full_2) AND NOT((((to_std_logic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(full_3))) = std_logic_vector'("00000000000000000000000000000000")))) AND read) AND write))))) = '1' then 
          stage_2 <= std_logic'('0');
        else
          stage_2 <= p2_stage_2;
        end if;
      end if;
    end if;

  end process;

  --control_2, which is an e_mux
  p2_full_2 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((read AND NOT(write)))))) = std_logic_vector'("00000000000000000000000000000000"))), full_1, full_3);
  --control_reg_2, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      full_2 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'(((clear_fifo OR ((read XOR write))) OR ((write AND NOT(full_0))))) = '1' then 
        if std_logic'(clear_fifo) = '1' then 
          full_2 <= std_logic'('0');
        else
          full_2 <= p2_full_2;
        end if;
      end if;
    end if;

  end process;

  --data_1, which is an e_mux
  p1_stage_1 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((full_2 AND NOT clear_fifo))))) = std_logic_vector'("00000000000000000000000000000000"))), data_in, stage_2);
  --data_reg_1, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      stage_1 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'((((clear_fifo OR sync_reset) OR read) OR ((write AND NOT(full_1))))) = '1' then 
        if std_logic'(((sync_reset AND full_1) AND NOT((((to_std_logic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(full_2))) = std_logic_vector'("00000000000000000000000000000000")))) AND read) AND write))))) = '1' then 
          stage_1 <= std_logic'('0');
        else
          stage_1 <= p1_stage_1;
        end if;
      end if;
    end if;

  end process;

  --control_1, which is an e_mux
  p1_full_1 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((read AND NOT(write)))))) = std_logic_vector'("00000000000000000000000000000000"))), full_0, full_2);
  --control_reg_1, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      full_1 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'(((clear_fifo OR ((read XOR write))) OR ((write AND NOT(full_0))))) = '1' then 
        if std_logic'(clear_fifo) = '1' then 
          full_1 <= std_logic'('0');
        else
          full_1 <= p1_full_1;
        end if;
      end if;
    end if;

  end process;

  --data_0, which is an e_mux
  p0_stage_0 <= A_WE_StdLogic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((full_1 AND NOT clear_fifo))))) = std_logic_vector'("00000000000000000000000000000000"))), data_in, stage_1);
  --data_reg_0, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      stage_0 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'((((clear_fifo OR sync_reset) OR read) OR ((write AND NOT(full_0))))) = '1' then 
        if std_logic'(((sync_reset AND full_0) AND NOT((((to_std_logic((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(full_1))) = std_logic_vector'("00000000000000000000000000000000")))) AND read) AND write))))) = '1' then 
          stage_0 <= std_logic'('0');
        else
          stage_0 <= p0_stage_0;
        end if;
      end if;
    end if;

  end process;

  --control_0, which is an e_mux
  p0_full_0 <= Vector_To_Std_Logic(A_WE_StdLogicVector((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(((read AND NOT(write)))))) = std_logic_vector'("00000000000000000000000000000000"))), std_logic_vector'("00000000000000000000000000000001"), (std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(full_1)))));
  --control_reg_0, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      full_0 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'(((clear_fifo OR ((read XOR write))) OR ((write AND NOT(full_0))))) = '1' then 
        if std_logic'((clear_fifo AND NOT write)) = '1' then 
          full_0 <= std_logic'('0');
        else
          full_0 <= p0_full_0;
        end if;
      end if;
    end if;

  end process;

  one_count_plus_one <= A_EXT (((std_logic_vector'("00000000000000000000000000000") & (how_many_ones)) + std_logic_vector'("000000000000000000000000000000001")), 4);
  one_count_minus_one <= A_EXT (((std_logic_vector'("00000000000000000000000000000") & (how_many_ones)) - std_logic_vector'("000000000000000000000000000000001")), 4);
  --updated_one_count, which is an e_mux
  updated_one_count <= A_EXT (A_WE_StdLogicVector((std_logic'(((((clear_fifo OR sync_reset)) AND NOT(write)))) = '1'), std_logic_vector'("00000000000000000000000000000000"), (std_logic_vector'("0000000000000000000000000000") & (A_WE_StdLogicVector((std_logic'(((((clear_fifo OR sync_reset)) AND write))) = '1'), (std_logic_vector'("000") & (A_TOSTDLOGICVECTOR(data_in))), A_WE_StdLogicVector((std_logic'(((((read AND (data_in)) AND write) AND (stage_0)))) = '1'), how_many_ones, A_WE_StdLogicVector((std_logic'(((write AND (data_in)))) = '1'), one_count_plus_one, A_WE_StdLogicVector((std_logic'(((read AND (stage_0)))) = '1'), one_count_minus_one, how_many_ones))))))), 4);
  --counts how many ones in the data pipeline, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      how_many_ones <= std_logic_vector'("0000");
    elsif clk'event and clk = '1' then
      if std_logic'((((clear_fifo OR sync_reset) OR read) OR write)) = '1' then 
        how_many_ones <= updated_one_count;
      end if;
    end if;

  end process;

  --this fifo contains ones in the data pipeline, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      fifo_contains_ones_n <= std_logic'('1');
    elsif clk'event and clk = '1' then
      if std_logic'((((clear_fifo OR sync_reset) OR read) OR write)) = '1' then 
        fifo_contains_ones_n <= NOT (or_reduce(updated_one_count));
      end if;
    end if;

  end process;


end europa;



-- turn off superfluous VHDL processor warnings 
-- altera message_level Level1 
-- altera message_off 10034 10035 10036 10037 10230 10240 10030 

library altera;
use altera.altera_europa_support_lib.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

entity sdram_s1_arbitrator is 
        port (
              -- inputs:
                 signal clk : IN STD_LOGIC;
                 signal jop_avalon_inst_avalon_master_address_to_slave : IN STD_LOGIC_VECTOR (25 DOWNTO 0);
                 signal jop_avalon_inst_avalon_master_byteenable : IN STD_LOGIC_VECTOR (3 DOWNTO 0);
                 signal jop_avalon_inst_avalon_master_read : IN STD_LOGIC;
                 signal jop_avalon_inst_avalon_master_write : IN STD_LOGIC;
                 signal jop_avalon_inst_dbs_address : IN STD_LOGIC_VECTOR (1 DOWNTO 0);
                 signal jop_avalon_inst_dbs_write_16 : IN STD_LOGIC_VECTOR (15 DOWNTO 0);
                 signal reset_n : IN STD_LOGIC;
                 signal sdram_s1_readdata : IN STD_LOGIC_VECTOR (15 DOWNTO 0);
                 signal sdram_s1_readdatavalid : IN STD_LOGIC;
                 signal sdram_s1_waitrequest : IN STD_LOGIC;

              -- outputs:
                 signal d1_sdram_s1_end_xfer : OUT STD_LOGIC;
                 signal jop_avalon_inst_byteenable_sdram_s1 : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                 signal jop_avalon_inst_granted_sdram_s1 : OUT STD_LOGIC;
                 signal jop_avalon_inst_qualified_request_sdram_s1 : OUT STD_LOGIC;
                 signal jop_avalon_inst_read_data_valid_sdram_s1 : OUT STD_LOGIC;
                 signal jop_avalon_inst_read_data_valid_sdram_s1_shift_register : OUT STD_LOGIC;
                 signal jop_avalon_inst_requests_sdram_s1 : OUT STD_LOGIC;
                 signal sdram_s1_address : OUT STD_LOGIC_VECTOR (21 DOWNTO 0);
                 signal sdram_s1_byteenable_n : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                 signal sdram_s1_chipselect : OUT STD_LOGIC;
                 signal sdram_s1_read_n : OUT STD_LOGIC;
                 signal sdram_s1_readdata_from_sa : OUT STD_LOGIC_VECTOR (15 DOWNTO 0);
                 signal sdram_s1_reset_n : OUT STD_LOGIC;
                 signal sdram_s1_waitrequest_from_sa : OUT STD_LOGIC;
                 signal sdram_s1_write_n : OUT STD_LOGIC;
                 signal sdram_s1_writedata : OUT STD_LOGIC_VECTOR (15 DOWNTO 0)
              );
attribute auto_dissolve : boolean;
attribute auto_dissolve of sdram_s1_arbitrator : entity is FALSE;
end entity sdram_s1_arbitrator;


architecture europa of sdram_s1_arbitrator is
component rdv_fifo_for_jop_avalon_inst_avalon_master_to_sdram_s1_module is 
           port (
                 -- inputs:
                    signal clear_fifo : IN STD_LOGIC;
                    signal clk : IN STD_LOGIC;
                    signal data_in : IN STD_LOGIC;
                    signal read : IN STD_LOGIC;
                    signal reset_n : IN STD_LOGIC;
                    signal sync_reset : IN STD_LOGIC;
                    signal write : IN STD_LOGIC;

                 -- outputs:
                    signal data_out : OUT STD_LOGIC;
                    signal empty : OUT STD_LOGIC;
                    signal fifo_contains_ones_n : OUT STD_LOGIC;
                    signal full : OUT STD_LOGIC
                 );
end component rdv_fifo_for_jop_avalon_inst_avalon_master_to_sdram_s1_module;

                signal d1_reasons_to_wait :  STD_LOGIC;
                signal enable_nonzero_assertions :  STD_LOGIC;
                signal end_xfer_arb_share_counter_term_sdram_s1 :  STD_LOGIC;
                signal in_a_read_cycle :  STD_LOGIC;
                signal in_a_write_cycle :  STD_LOGIC;
                signal internal_jop_avalon_inst_byteenable_sdram_s1 :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal internal_jop_avalon_inst_granted_sdram_s1 :  STD_LOGIC;
                signal internal_jop_avalon_inst_qualified_request_sdram_s1 :  STD_LOGIC;
                signal internal_jop_avalon_inst_read_data_valid_sdram_s1_shift_register :  STD_LOGIC;
                signal internal_jop_avalon_inst_requests_sdram_s1 :  STD_LOGIC;
                signal internal_sdram_s1_waitrequest_from_sa :  STD_LOGIC;
                signal jop_avalon_inst_avalon_master_arbiterlock :  STD_LOGIC;
                signal jop_avalon_inst_avalon_master_arbiterlock2 :  STD_LOGIC;
                signal jop_avalon_inst_avalon_master_continuerequest :  STD_LOGIC;
                signal jop_avalon_inst_byteenable_sdram_s1_segment_0 :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal jop_avalon_inst_byteenable_sdram_s1_segment_1 :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal jop_avalon_inst_rdv_fifo_empty_sdram_s1 :  STD_LOGIC;
                signal jop_avalon_inst_rdv_fifo_output_from_sdram_s1 :  STD_LOGIC;
                signal jop_avalon_inst_saved_grant_sdram_s1 :  STD_LOGIC;
                signal module_input :  STD_LOGIC;
                signal module_input1 :  STD_LOGIC;
                signal module_input2 :  STD_LOGIC;
                signal sdram_s1_allgrants :  STD_LOGIC;
                signal sdram_s1_allow_new_arb_cycle :  STD_LOGIC;
                signal sdram_s1_any_bursting_master_saved_grant :  STD_LOGIC;
                signal sdram_s1_any_continuerequest :  STD_LOGIC;
                signal sdram_s1_arb_counter_enable :  STD_LOGIC;
                signal sdram_s1_arb_share_counter :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal sdram_s1_arb_share_counter_next_value :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal sdram_s1_arb_share_set_values :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal sdram_s1_beginbursttransfer_internal :  STD_LOGIC;
                signal sdram_s1_begins_xfer :  STD_LOGIC;
                signal sdram_s1_end_xfer :  STD_LOGIC;
                signal sdram_s1_firsttransfer :  STD_LOGIC;
                signal sdram_s1_grant_vector :  STD_LOGIC;
                signal sdram_s1_in_a_read_cycle :  STD_LOGIC;
                signal sdram_s1_in_a_write_cycle :  STD_LOGIC;
                signal sdram_s1_master_qreq_vector :  STD_LOGIC;
                signal sdram_s1_move_on_to_next_transaction :  STD_LOGIC;
                signal sdram_s1_non_bursting_master_requests :  STD_LOGIC;
                signal sdram_s1_readdatavalid_from_sa :  STD_LOGIC;
                signal sdram_s1_reg_firsttransfer :  STD_LOGIC;
                signal sdram_s1_slavearbiterlockenable :  STD_LOGIC;
                signal sdram_s1_slavearbiterlockenable2 :  STD_LOGIC;
                signal sdram_s1_unreg_firsttransfer :  STD_LOGIC;
                signal sdram_s1_waits_for_read :  STD_LOGIC;
                signal sdram_s1_waits_for_write :  STD_LOGIC;
                signal shifted_address_to_sdram_s1_from_jop_avalon_inst_avalon_master :  STD_LOGIC_VECTOR (25 DOWNTO 0);
                signal wait_for_sdram_s1_counter :  STD_LOGIC;

begin

  process (clk, reset_n)
  begin
    if reset_n = '0' then
      d1_reasons_to_wait <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if (std_logic_vector'("00000000000000000000000000000001")) /= std_logic_vector'("00000000000000000000000000000000") then 
        d1_reasons_to_wait <= NOT sdram_s1_end_xfer;
      end if;
    end if;

  end process;

  sdram_s1_begins_xfer <= NOT d1_reasons_to_wait AND (internal_jop_avalon_inst_qualified_request_sdram_s1);
  --assign sdram_s1_readdata_from_sa = sdram_s1_readdata so that symbol knows where to group signals which may go to master only, which is an e_assign
  sdram_s1_readdata_from_sa <= sdram_s1_readdata;
  internal_jop_avalon_inst_requests_sdram_s1 <= to_std_logic(((Std_Logic_Vector'(jop_avalon_inst_avalon_master_address_to_slave(25 DOWNTO 23) & std_logic_vector'("00000000000000000000000")) = std_logic_vector'("00000000000000000000000000")))) AND ((jop_avalon_inst_avalon_master_read OR jop_avalon_inst_avalon_master_write));
  --assign sdram_s1_waitrequest_from_sa = sdram_s1_waitrequest so that symbol knows where to group signals which may go to master only, which is an e_assign
  internal_sdram_s1_waitrequest_from_sa <= sdram_s1_waitrequest;
  --assign sdram_s1_readdatavalid_from_sa = sdram_s1_readdatavalid so that symbol knows where to group signals which may go to master only, which is an e_assign
  sdram_s1_readdatavalid_from_sa <= sdram_s1_readdatavalid;
  --sdram_s1_arb_share_counter set values, which is an e_mux
  sdram_s1_arb_share_set_values <= A_EXT (A_WE_StdLogicVector((std_logic'((internal_jop_avalon_inst_granted_sdram_s1)) = '1'), std_logic_vector'("00000000000000000000000000000010"), std_logic_vector'("00000000000000000000000000000001")), 2);
  --sdram_s1_non_bursting_master_requests mux, which is an e_mux
  sdram_s1_non_bursting_master_requests <= internal_jop_avalon_inst_requests_sdram_s1;
  --sdram_s1_any_bursting_master_saved_grant mux, which is an e_mux
  sdram_s1_any_bursting_master_saved_grant <= std_logic'('0');
  --sdram_s1_arb_share_counter_next_value assignment, which is an e_assign
  sdram_s1_arb_share_counter_next_value <= A_EXT (A_WE_StdLogicVector((std_logic'(sdram_s1_firsttransfer) = '1'), (((std_logic_vector'("0000000000000000000000000000000") & (sdram_s1_arb_share_set_values)) - std_logic_vector'("000000000000000000000000000000001"))), A_WE_StdLogicVector((std_logic'(or_reduce(sdram_s1_arb_share_counter)) = '1'), (((std_logic_vector'("0000000000000000000000000000000") & (sdram_s1_arb_share_counter)) - std_logic_vector'("000000000000000000000000000000001"))), std_logic_vector'("000000000000000000000000000000000"))), 2);
  --sdram_s1_allgrants all slave grants, which is an e_mux
  sdram_s1_allgrants <= sdram_s1_grant_vector;
  --sdram_s1_end_xfer assignment, which is an e_assign
  sdram_s1_end_xfer <= NOT ((sdram_s1_waits_for_read OR sdram_s1_waits_for_write));
  --end_xfer_arb_share_counter_term_sdram_s1 arb share counter enable term, which is an e_assign
  end_xfer_arb_share_counter_term_sdram_s1 <= sdram_s1_end_xfer AND (((NOT sdram_s1_any_bursting_master_saved_grant OR in_a_read_cycle) OR in_a_write_cycle));
  --sdram_s1_arb_share_counter arbitration counter enable, which is an e_assign
  sdram_s1_arb_counter_enable <= ((end_xfer_arb_share_counter_term_sdram_s1 AND sdram_s1_allgrants)) OR ((end_xfer_arb_share_counter_term_sdram_s1 AND NOT sdram_s1_non_bursting_master_requests));
  --sdram_s1_arb_share_counter counter, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      sdram_s1_arb_share_counter <= std_logic_vector'("00");
    elsif clk'event and clk = '1' then
      if std_logic'(sdram_s1_arb_counter_enable) = '1' then 
        sdram_s1_arb_share_counter <= sdram_s1_arb_share_counter_next_value;
      end if;
    end if;

  end process;

  --sdram_s1_slavearbiterlockenable slave enables arbiterlock, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      sdram_s1_slavearbiterlockenable <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if std_logic'((((sdram_s1_master_qreq_vector AND end_xfer_arb_share_counter_term_sdram_s1)) OR ((end_xfer_arb_share_counter_term_sdram_s1 AND NOT sdram_s1_non_bursting_master_requests)))) = '1' then 
        sdram_s1_slavearbiterlockenable <= or_reduce(sdram_s1_arb_share_counter_next_value);
      end if;
    end if;

  end process;

  --jop_avalon_inst/avalon_master sdram/s1 arbiterlock, which is an e_assign
  jop_avalon_inst_avalon_master_arbiterlock <= sdram_s1_slavearbiterlockenable AND jop_avalon_inst_avalon_master_continuerequest;
  --sdram_s1_slavearbiterlockenable2 slave enables arbiterlock2, which is an e_assign
  sdram_s1_slavearbiterlockenable2 <= or_reduce(sdram_s1_arb_share_counter_next_value);
  --jop_avalon_inst/avalon_master sdram/s1 arbiterlock2, which is an e_assign
  jop_avalon_inst_avalon_master_arbiterlock2 <= sdram_s1_slavearbiterlockenable2 AND jop_avalon_inst_avalon_master_continuerequest;
  --sdram_s1_any_continuerequest at least one master continues requesting, which is an e_assign
  sdram_s1_any_continuerequest <= std_logic'('1');
  --jop_avalon_inst_avalon_master_continuerequest continued request, which is an e_assign
  jop_avalon_inst_avalon_master_continuerequest <= std_logic'('1');
  internal_jop_avalon_inst_qualified_request_sdram_s1 <= internal_jop_avalon_inst_requests_sdram_s1 AND NOT ((((jop_avalon_inst_avalon_master_read AND (internal_jop_avalon_inst_read_data_valid_sdram_s1_shift_register))) OR (((NOT(or_reduce(internal_jop_avalon_inst_byteenable_sdram_s1))) AND jop_avalon_inst_avalon_master_write))));
  --unique name for sdram_s1_move_on_to_next_transaction, which is an e_assign
  sdram_s1_move_on_to_next_transaction <= sdram_s1_readdatavalid_from_sa;
  --rdv_fifo_for_jop_avalon_inst_avalon_master_to_sdram_s1, which is an e_fifo_with_registered_outputs
  rdv_fifo_for_jop_avalon_inst_avalon_master_to_sdram_s1 : rdv_fifo_for_jop_avalon_inst_avalon_master_to_sdram_s1_module
    port map(
      data_out => jop_avalon_inst_rdv_fifo_output_from_sdram_s1,
      empty => open,
      fifo_contains_ones_n => jop_avalon_inst_rdv_fifo_empty_sdram_s1,
      full => open,
      clear_fifo => module_input,
      clk => clk,
      data_in => internal_jop_avalon_inst_granted_sdram_s1,
      read => sdram_s1_move_on_to_next_transaction,
      reset_n => reset_n,
      sync_reset => module_input1,
      write => module_input2
    );

  module_input <= std_logic'('0');
  module_input1 <= std_logic'('0');
  module_input2 <= in_a_read_cycle AND NOT sdram_s1_waits_for_read;

  internal_jop_avalon_inst_read_data_valid_sdram_s1_shift_register <= NOT jop_avalon_inst_rdv_fifo_empty_sdram_s1;
  --local readdatavalid jop_avalon_inst_read_data_valid_sdram_s1, which is an e_mux
  jop_avalon_inst_read_data_valid_sdram_s1 <= sdram_s1_readdatavalid_from_sa;
  --sdram_s1_writedata mux, which is an e_mux
  sdram_s1_writedata <= jop_avalon_inst_dbs_write_16;
  --master is always granted when requested
  internal_jop_avalon_inst_granted_sdram_s1 <= internal_jop_avalon_inst_qualified_request_sdram_s1;
  --jop_avalon_inst/avalon_master saved-grant sdram/s1, which is an e_assign
  jop_avalon_inst_saved_grant_sdram_s1 <= internal_jop_avalon_inst_requests_sdram_s1;
  --allow new arb cycle for sdram/s1, which is an e_assign
  sdram_s1_allow_new_arb_cycle <= std_logic'('1');
  --placeholder chosen master
  sdram_s1_grant_vector <= std_logic'('1');
  --placeholder vector of master qualified-requests
  sdram_s1_master_qreq_vector <= std_logic'('1');
  --sdram_s1_reset_n assignment, which is an e_assign
  sdram_s1_reset_n <= reset_n;
  sdram_s1_chipselect <= internal_jop_avalon_inst_granted_sdram_s1;
  --sdram_s1_firsttransfer first transaction, which is an e_assign
  sdram_s1_firsttransfer <= A_WE_StdLogic((std_logic'(sdram_s1_begins_xfer) = '1'), sdram_s1_unreg_firsttransfer, sdram_s1_reg_firsttransfer);
  --sdram_s1_unreg_firsttransfer first transaction, which is an e_assign
  sdram_s1_unreg_firsttransfer <= NOT ((sdram_s1_slavearbiterlockenable AND sdram_s1_any_continuerequest));
  --sdram_s1_reg_firsttransfer first transaction, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      sdram_s1_reg_firsttransfer <= std_logic'('1');
    elsif clk'event and clk = '1' then
      if std_logic'(sdram_s1_begins_xfer) = '1' then 
        sdram_s1_reg_firsttransfer <= sdram_s1_unreg_firsttransfer;
      end if;
    end if;

  end process;

  --sdram_s1_beginbursttransfer_internal begin burst transfer, which is an e_assign
  sdram_s1_beginbursttransfer_internal <= sdram_s1_begins_xfer;
  --~sdram_s1_read_n assignment, which is an e_mux
  sdram_s1_read_n <= NOT ((internal_jop_avalon_inst_granted_sdram_s1 AND jop_avalon_inst_avalon_master_read));
  --~sdram_s1_write_n assignment, which is an e_mux
  sdram_s1_write_n <= NOT ((internal_jop_avalon_inst_granted_sdram_s1 AND jop_avalon_inst_avalon_master_write));
  shifted_address_to_sdram_s1_from_jop_avalon_inst_avalon_master <= A_EXT (Std_Logic_Vector'(A_SRL(jop_avalon_inst_avalon_master_address_to_slave,std_logic_vector'("00000000000000000000000000000010")) & A_ToStdLogicVector(jop_avalon_inst_dbs_address(1)) & A_ToStdLogicVector(std_logic'('0'))), 26);
  --sdram_s1_address mux, which is an e_mux
  sdram_s1_address <= A_EXT (A_SRL(shifted_address_to_sdram_s1_from_jop_avalon_inst_avalon_master,std_logic_vector'("00000000000000000000000000000001")), 22);
  --d1_sdram_s1_end_xfer register, which is an e_register
  process (clk, reset_n)
  begin
    if reset_n = '0' then
      d1_sdram_s1_end_xfer <= std_logic'('1');
    elsif clk'event and clk = '1' then
      if (std_logic_vector'("00000000000000000000000000000001")) /= std_logic_vector'("00000000000000000000000000000000") then 
        d1_sdram_s1_end_xfer <= sdram_s1_end_xfer;
      end if;
    end if;

  end process;

  --sdram_s1_waits_for_read in a cycle, which is an e_mux
  sdram_s1_waits_for_read <= sdram_s1_in_a_read_cycle AND internal_sdram_s1_waitrequest_from_sa;
  --sdram_s1_in_a_read_cycle assignment, which is an e_assign
  sdram_s1_in_a_read_cycle <= internal_jop_avalon_inst_granted_sdram_s1 AND jop_avalon_inst_avalon_master_read;
  --in_a_read_cycle assignment, which is an e_mux
  in_a_read_cycle <= sdram_s1_in_a_read_cycle;
  --sdram_s1_waits_for_write in a cycle, which is an e_mux
  sdram_s1_waits_for_write <= sdram_s1_in_a_write_cycle AND internal_sdram_s1_waitrequest_from_sa;
  --sdram_s1_in_a_write_cycle assignment, which is an e_assign
  sdram_s1_in_a_write_cycle <= internal_jop_avalon_inst_granted_sdram_s1 AND jop_avalon_inst_avalon_master_write;
  --in_a_write_cycle assignment, which is an e_mux
  in_a_write_cycle <= sdram_s1_in_a_write_cycle;
  wait_for_sdram_s1_counter <= std_logic'('0');
  --~sdram_s1_byteenable_n byte enable port mux, which is an e_mux
  sdram_s1_byteenable_n <= A_EXT (NOT (A_WE_StdLogicVector((std_logic'((internal_jop_avalon_inst_granted_sdram_s1)) = '1'), (std_logic_vector'("000000000000000000000000000000") & (internal_jop_avalon_inst_byteenable_sdram_s1)), -SIGNED(std_logic_vector'("00000000000000000000000000000001")))), 2);
  (jop_avalon_inst_byteenable_sdram_s1_segment_1(1), jop_avalon_inst_byteenable_sdram_s1_segment_1(0), jop_avalon_inst_byteenable_sdram_s1_segment_0(1), jop_avalon_inst_byteenable_sdram_s1_segment_0(0)) <= jop_avalon_inst_avalon_master_byteenable;
  internal_jop_avalon_inst_byteenable_sdram_s1 <= A_WE_StdLogicVector((((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(jop_avalon_inst_dbs_address(1)))) = std_logic_vector'("00000000000000000000000000000000"))), jop_avalon_inst_byteenable_sdram_s1_segment_0, jop_avalon_inst_byteenable_sdram_s1_segment_1);
  --vhdl renameroo for output signals
  jop_avalon_inst_byteenable_sdram_s1 <= internal_jop_avalon_inst_byteenable_sdram_s1;
  --vhdl renameroo for output signals
  jop_avalon_inst_granted_sdram_s1 <= internal_jop_avalon_inst_granted_sdram_s1;
  --vhdl renameroo for output signals
  jop_avalon_inst_qualified_request_sdram_s1 <= internal_jop_avalon_inst_qualified_request_sdram_s1;
  --vhdl renameroo for output signals
  jop_avalon_inst_read_data_valid_sdram_s1_shift_register <= internal_jop_avalon_inst_read_data_valid_sdram_s1_shift_register;
  --vhdl renameroo for output signals
  jop_avalon_inst_requests_sdram_s1 <= internal_jop_avalon_inst_requests_sdram_s1;
  --vhdl renameroo for output signals
  sdram_s1_waitrequest_from_sa <= internal_sdram_s1_waitrequest_from_sa;
--synthesis translate_off
    --sdram/s1 enable non-zero assertions, which is an e_register
    process (clk, reset_n)
    begin
      if reset_n = '0' then
        enable_nonzero_assertions <= std_logic'('0');
      elsif clk'event and clk = '1' then
        if (std_logic_vector'("00000000000000000000000000000001")) /= std_logic_vector'("00000000000000000000000000000000") then 
          enable_nonzero_assertions <= std_logic'('1');
        end if;
      end if;

    end process;

--synthesis translate_on

end europa;



-- turn off superfluous VHDL processor warnings 
-- altera message_level Level1 
-- altera message_off 10034 10035 10036 10037 10230 10240 10030 

library altera;
use altera.altera_europa_support_lib.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

entity test_reset_clk_domain_synch_module is 
        port (
              -- inputs:
                 signal clk : IN STD_LOGIC;
                 signal data_in : IN STD_LOGIC;
                 signal reset_n : IN STD_LOGIC;

              -- outputs:
                 signal data_out : OUT STD_LOGIC
              );
end entity test_reset_clk_domain_synch_module;


architecture europa of test_reset_clk_domain_synch_module is
                signal data_in_d1 :  STD_LOGIC;
attribute ALTERA_ATTRIBUTE : string;
attribute ALTERA_ATTRIBUTE of data_in_d1 : signal is "MAX_DELAY=100ns ; PRESERVE_REGISTER=ON ; SUPPRESS_DA_RULE_INTERNAL=R101";
attribute ALTERA_ATTRIBUTE of data_out : signal is "PRESERVE_REGISTER=ON ; SUPPRESS_DA_RULE_INTERNAL=R101";

begin

  process (clk, reset_n)
  begin
    if reset_n = '0' then
      data_in_d1 <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if (std_logic_vector'("00000000000000000000000000000001")) /= std_logic_vector'("00000000000000000000000000000000") then 
        data_in_d1 <= data_in;
      end if;
    end if;

  end process;

  process (clk, reset_n)
  begin
    if reset_n = '0' then
      data_out <= std_logic'('0');
    elsif clk'event and clk = '1' then
      if (std_logic_vector'("00000000000000000000000000000001")) /= std_logic_vector'("00000000000000000000000000000000") then 
        data_out <= data_in_d1;
      end if;
    end if;

  end process;


end europa;



-- turn off superfluous VHDL processor warnings 
-- altera message_level Level1 
-- altera message_off 10034 10035 10036 10037 10230 10240 10030 

library altera;
use altera.altera_europa_support_lib.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

entity test is 
        port (
              -- 1) global signals:
                 signal clk : IN STD_LOGIC;
                 signal reset_n : IN STD_LOGIC;

              -- the_jop_avalon_inst
          --       signal clk_to_the_jop_avalon_inst : IN STD_LOGIC;
          --       signal reset_to_the_jop_avalon_inst : IN STD_LOGIC;
                 signal ser_rxd_to_the_jop_avalon_inst : IN STD_LOGIC;
                 signal ser_txd_from_the_jop_avalon_inst : OUT STD_LOGIC;
                 signal wd_from_the_jop_avalon_inst : OUT STD_LOGIC;

              -- the_sdram
                 signal zs_addr_from_the_sdram : OUT STD_LOGIC_VECTOR (11 DOWNTO 0);
                 signal zs_ba_from_the_sdram : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                 signal zs_cas_n_from_the_sdram : OUT STD_LOGIC;
                 signal zs_cke_from_the_sdram : OUT STD_LOGIC;
                 signal zs_cs_n_from_the_sdram : OUT STD_LOGIC;
                 signal zs_dq_to_and_from_the_sdram : INOUT STD_LOGIC_VECTOR (15 DOWNTO 0);
                 signal zs_dqm_from_the_sdram : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                 signal zs_ras_n_from_the_sdram : OUT STD_LOGIC;
                 signal zs_we_n_from_the_sdram : OUT STD_LOGIC
              );
end entity test;


architecture europa of test is
component jop_avalon_inst_avalon_master_arbitrator is 
           port (
                 -- inputs:
                    signal clk : IN STD_LOGIC;
                    signal d1_sdram_s1_end_xfer : IN STD_LOGIC;
                    signal jop_avalon_inst_avalon_master_address : IN STD_LOGIC_VECTOR (25 DOWNTO 0);
                    signal jop_avalon_inst_avalon_master_read : IN STD_LOGIC;
                    signal jop_avalon_inst_avalon_master_write : IN STD_LOGIC;
                    signal jop_avalon_inst_avalon_master_writedata : IN STD_LOGIC_VECTOR (31 DOWNTO 0);
                    signal jop_avalon_inst_byteenable_sdram_s1 : IN STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal jop_avalon_inst_granted_sdram_s1 : IN STD_LOGIC;
                    signal jop_avalon_inst_qualified_request_sdram_s1 : IN STD_LOGIC;
                    signal jop_avalon_inst_read_data_valid_sdram_s1 : IN STD_LOGIC;
                    signal jop_avalon_inst_read_data_valid_sdram_s1_shift_register : IN STD_LOGIC;
                    signal jop_avalon_inst_requests_sdram_s1 : IN STD_LOGIC;
                    signal reset_n : IN STD_LOGIC;
                    signal sdram_s1_readdata_from_sa : IN STD_LOGIC_VECTOR (15 DOWNTO 0);
                    signal sdram_s1_waitrequest_from_sa : IN STD_LOGIC;

                 -- outputs:
                    signal jop_avalon_inst_avalon_master_address_to_slave : OUT STD_LOGIC_VECTOR (25 DOWNTO 0);
                    signal jop_avalon_inst_avalon_master_readdata : OUT STD_LOGIC_VECTOR (31 DOWNTO 0);
                    signal jop_avalon_inst_avalon_master_waitrequest : OUT STD_LOGIC;
                    signal jop_avalon_inst_dbs_address : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal jop_avalon_inst_dbs_write_16 : OUT STD_LOGIC_VECTOR (15 DOWNTO 0)
                 );
end component jop_avalon_inst_avalon_master_arbitrator;

component jop_avalon_inst is 
           port (
                 -- inputs:
                    signal clk : IN STD_LOGIC;
                    signal readdata : IN STD_LOGIC_VECTOR (31 DOWNTO 0);
                    signal reset : IN STD_LOGIC;
                    signal ser_rxd : IN STD_LOGIC;
                    signal waitrequest : IN STD_LOGIC;

                 -- outputs:
                    signal address : OUT STD_LOGIC_VECTOR (25 DOWNTO 0);
                    signal byteenable : OUT STD_LOGIC_VECTOR (3 DOWNTO 0);
                    signal read : OUT STD_LOGIC;
                    signal ser_txd : OUT STD_LOGIC;
                    signal wd : OUT STD_LOGIC;
                    signal write : OUT STD_LOGIC;
                    signal writedata : OUT STD_LOGIC_VECTOR (31 DOWNTO 0)
                 );
end component jop_avalon_inst;

component sdram_s1_arbitrator is 
           port (
                 -- inputs:
                    signal clk : IN STD_LOGIC;
                    signal jop_avalon_inst_avalon_master_address_to_slave : IN STD_LOGIC_VECTOR (25 DOWNTO 0);
                    signal jop_avalon_inst_avalon_master_byteenable : IN STD_LOGIC_VECTOR (3 DOWNTO 0);
                    signal jop_avalon_inst_avalon_master_read : IN STD_LOGIC;
                    signal jop_avalon_inst_avalon_master_write : IN STD_LOGIC;
                    signal jop_avalon_inst_dbs_address : IN STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal jop_avalon_inst_dbs_write_16 : IN STD_LOGIC_VECTOR (15 DOWNTO 0);
                    signal reset_n : IN STD_LOGIC;
                    signal sdram_s1_readdata : IN STD_LOGIC_VECTOR (15 DOWNTO 0);
                    signal sdram_s1_readdatavalid : IN STD_LOGIC;
                    signal sdram_s1_waitrequest : IN STD_LOGIC;

                 -- outputs:
                    signal d1_sdram_s1_end_xfer : OUT STD_LOGIC;
                    signal jop_avalon_inst_byteenable_sdram_s1 : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal jop_avalon_inst_granted_sdram_s1 : OUT STD_LOGIC;
                    signal jop_avalon_inst_qualified_request_sdram_s1 : OUT STD_LOGIC;
                    signal jop_avalon_inst_read_data_valid_sdram_s1 : OUT STD_LOGIC;
                    signal jop_avalon_inst_read_data_valid_sdram_s1_shift_register : OUT STD_LOGIC;
                    signal jop_avalon_inst_requests_sdram_s1 : OUT STD_LOGIC;
                    signal sdram_s1_address : OUT STD_LOGIC_VECTOR (21 DOWNTO 0);
                    signal sdram_s1_byteenable_n : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal sdram_s1_chipselect : OUT STD_LOGIC;
                    signal sdram_s1_read_n : OUT STD_LOGIC;
                    signal sdram_s1_readdata_from_sa : OUT STD_LOGIC_VECTOR (15 DOWNTO 0);
                    signal sdram_s1_reset_n : OUT STD_LOGIC;
                    signal sdram_s1_waitrequest_from_sa : OUT STD_LOGIC;
                    signal sdram_s1_write_n : OUT STD_LOGIC;
                    signal sdram_s1_writedata : OUT STD_LOGIC_VECTOR (15 DOWNTO 0)
                 );
end component sdram_s1_arbitrator;

component sdram is 
           port (
                 -- inputs:
                    signal az_addr : IN STD_LOGIC_VECTOR (21 DOWNTO 0);
                    signal az_be_n : IN STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal az_cs : IN STD_LOGIC;
                    signal az_data : IN STD_LOGIC_VECTOR (15 DOWNTO 0);
                    signal az_rd_n : IN STD_LOGIC;
                    signal az_wr_n : IN STD_LOGIC;
                    signal clk : IN STD_LOGIC;
                    signal reset_n : IN STD_LOGIC;

                 -- outputs:
                    signal za_data : OUT STD_LOGIC_VECTOR (15 DOWNTO 0);
                    signal za_valid : OUT STD_LOGIC;
                    signal za_waitrequest : OUT STD_LOGIC;
                    signal zs_addr : OUT STD_LOGIC_VECTOR (11 DOWNTO 0);
                    signal zs_ba : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal zs_cas_n : OUT STD_LOGIC;
                    signal zs_cke : OUT STD_LOGIC;
                    signal zs_cs_n : OUT STD_LOGIC;
                    signal zs_dq : INOUT STD_LOGIC_VECTOR (15 DOWNTO 0);
                    signal zs_dqm : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal zs_ras_n : OUT STD_LOGIC;
                    signal zs_we_n : OUT STD_LOGIC
                 );
end component sdram;

component test_reset_clk_domain_synch_module is 
           port (
                 -- inputs:
                    signal clk : IN STD_LOGIC;
                    signal data_in : IN STD_LOGIC;
                    signal reset_n : IN STD_LOGIC;

                 -- outputs:
                    signal data_out : OUT STD_LOGIC
                 );
end component test_reset_clk_domain_synch_module;

                signal clk_reset_n :  STD_LOGIC;
                signal d1_sdram_s1_end_xfer :  STD_LOGIC;
                signal internal_ser_txd_from_the_jop_avalon_inst :  STD_LOGIC;
                signal internal_wd_from_the_jop_avalon_inst :  STD_LOGIC;
                signal internal_zs_addr_from_the_sdram :  STD_LOGIC_VECTOR (11 DOWNTO 0);
                signal internal_zs_ba_from_the_sdram :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal internal_zs_cas_n_from_the_sdram :  STD_LOGIC;
                signal internal_zs_cke_from_the_sdram :  STD_LOGIC;
                signal internal_zs_cs_n_from_the_sdram :  STD_LOGIC;
                signal internal_zs_dqm_from_the_sdram :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal internal_zs_ras_n_from_the_sdram :  STD_LOGIC;
                signal internal_zs_we_n_from_the_sdram :  STD_LOGIC;
                signal jop_avalon_inst_avalon_master_address :  STD_LOGIC_VECTOR (25 DOWNTO 0);
                signal jop_avalon_inst_avalon_master_address_to_slave :  STD_LOGIC_VECTOR (25 DOWNTO 0);
                signal jop_avalon_inst_avalon_master_byteenable :  STD_LOGIC_VECTOR (3 DOWNTO 0);
                signal jop_avalon_inst_avalon_master_read :  STD_LOGIC;
                signal jop_avalon_inst_avalon_master_readdata :  STD_LOGIC_VECTOR (31 DOWNTO 0);
                signal jop_avalon_inst_avalon_master_waitrequest :  STD_LOGIC;
                signal jop_avalon_inst_avalon_master_write :  STD_LOGIC;
                signal jop_avalon_inst_avalon_master_writedata :  STD_LOGIC_VECTOR (31 DOWNTO 0);
                signal jop_avalon_inst_byteenable_sdram_s1 :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal jop_avalon_inst_dbs_address :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal jop_avalon_inst_dbs_write_16 :  STD_LOGIC_VECTOR (15 DOWNTO 0);
                signal jop_avalon_inst_granted_sdram_s1 :  STD_LOGIC;
                signal jop_avalon_inst_qualified_request_sdram_s1 :  STD_LOGIC;
                signal jop_avalon_inst_read_data_valid_sdram_s1 :  STD_LOGIC;
                signal jop_avalon_inst_read_data_valid_sdram_s1_shift_register :  STD_LOGIC;
                signal jop_avalon_inst_requests_sdram_s1 :  STD_LOGIC;
                signal module_input3 :  STD_LOGIC;
                signal reset_n_sources :  STD_LOGIC;
                signal sdram_s1_address :  STD_LOGIC_VECTOR (21 DOWNTO 0);
                signal sdram_s1_byteenable_n :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal sdram_s1_chipselect :  STD_LOGIC;
                signal sdram_s1_read_n :  STD_LOGIC;
                signal sdram_s1_readdata :  STD_LOGIC_VECTOR (15 DOWNTO 0);
                signal sdram_s1_readdata_from_sa :  STD_LOGIC_VECTOR (15 DOWNTO 0);
                signal sdram_s1_readdatavalid :  STD_LOGIC;
                signal sdram_s1_reset_n :  STD_LOGIC;
                signal sdram_s1_waitrequest :  STD_LOGIC;
                signal sdram_s1_waitrequest_from_sa :  STD_LOGIC;
                signal sdram_s1_write_n :  STD_LOGIC;
                signal sdram_s1_writedata :  STD_LOGIC_VECTOR (15 DOWNTO 0);

begin

  --the_jop_avalon_inst_avalon_master, which is an e_instance
  the_jop_avalon_inst_avalon_master : jop_avalon_inst_avalon_master_arbitrator
    port map(
      jop_avalon_inst_avalon_master_address_to_slave => jop_avalon_inst_avalon_master_address_to_slave,
      jop_avalon_inst_avalon_master_readdata => jop_avalon_inst_avalon_master_readdata,
      jop_avalon_inst_avalon_master_waitrequest => jop_avalon_inst_avalon_master_waitrequest,
      jop_avalon_inst_dbs_address => jop_avalon_inst_dbs_address,
      jop_avalon_inst_dbs_write_16 => jop_avalon_inst_dbs_write_16,
      clk => clk,
      d1_sdram_s1_end_xfer => d1_sdram_s1_end_xfer,
      jop_avalon_inst_avalon_master_address => jop_avalon_inst_avalon_master_address,
      jop_avalon_inst_avalon_master_read => jop_avalon_inst_avalon_master_read,
      jop_avalon_inst_avalon_master_write => jop_avalon_inst_avalon_master_write,
      jop_avalon_inst_avalon_master_writedata => jop_avalon_inst_avalon_master_writedata,
      jop_avalon_inst_byteenable_sdram_s1 => jop_avalon_inst_byteenable_sdram_s1,
      jop_avalon_inst_granted_sdram_s1 => jop_avalon_inst_granted_sdram_s1,
      jop_avalon_inst_qualified_request_sdram_s1 => jop_avalon_inst_qualified_request_sdram_s1,
      jop_avalon_inst_read_data_valid_sdram_s1 => jop_avalon_inst_read_data_valid_sdram_s1,
      jop_avalon_inst_read_data_valid_sdram_s1_shift_register => jop_avalon_inst_read_data_valid_sdram_s1_shift_register,
      jop_avalon_inst_requests_sdram_s1 => jop_avalon_inst_requests_sdram_s1,
      reset_n => clk_reset_n,
      sdram_s1_readdata_from_sa => sdram_s1_readdata_from_sa,
      sdram_s1_waitrequest_from_sa => sdram_s1_waitrequest_from_sa
    );


  --the_jop_avalon_inst, which is an e_ptf_instance
  the_jop_avalon_inst : jop_avalon_inst
    port map(
      address => jop_avalon_inst_avalon_master_address,
      byteenable => jop_avalon_inst_avalon_master_byteenable,
      read => jop_avalon_inst_avalon_master_read,
      ser_txd => internal_ser_txd_from_the_jop_avalon_inst,
      wd => internal_wd_from_the_jop_avalon_inst,
      write => jop_avalon_inst_avalon_master_write,
      writedata => jop_avalon_inst_avalon_master_writedata,
      --clk => clk_to_the_jop_avalon_inst,
      clk => clk,
      readdata => jop_avalon_inst_avalon_master_readdata,
      --reset => reset_to_the_jop_avalon_inst,
      reset => reset_n,
      ser_rxd => ser_rxd_to_the_jop_avalon_inst,
      waitrequest => jop_avalon_inst_avalon_master_waitrequest
    );


  --the_sdram_s1, which is an e_instance
  the_sdram_s1 : sdram_s1_arbitrator
    port map(
      d1_sdram_s1_end_xfer => d1_sdram_s1_end_xfer,
      jop_avalon_inst_byteenable_sdram_s1 => jop_avalon_inst_byteenable_sdram_s1,
      jop_avalon_inst_granted_sdram_s1 => jop_avalon_inst_granted_sdram_s1,
      jop_avalon_inst_qualified_request_sdram_s1 => jop_avalon_inst_qualified_request_sdram_s1,
      jop_avalon_inst_read_data_valid_sdram_s1 => jop_avalon_inst_read_data_valid_sdram_s1,
      jop_avalon_inst_read_data_valid_sdram_s1_shift_register => jop_avalon_inst_read_data_valid_sdram_s1_shift_register,
      jop_avalon_inst_requests_sdram_s1 => jop_avalon_inst_requests_sdram_s1,
      sdram_s1_address => sdram_s1_address,
      sdram_s1_byteenable_n => sdram_s1_byteenable_n,
      sdram_s1_chipselect => sdram_s1_chipselect,
      sdram_s1_read_n => sdram_s1_read_n,
      sdram_s1_readdata_from_sa => sdram_s1_readdata_from_sa,
      sdram_s1_reset_n => sdram_s1_reset_n,
      sdram_s1_waitrequest_from_sa => sdram_s1_waitrequest_from_sa,
      sdram_s1_write_n => sdram_s1_write_n,
      sdram_s1_writedata => sdram_s1_writedata,
      clk => clk,
      jop_avalon_inst_avalon_master_address_to_slave => jop_avalon_inst_avalon_master_address_to_slave,
      jop_avalon_inst_avalon_master_byteenable => jop_avalon_inst_avalon_master_byteenable,
      jop_avalon_inst_avalon_master_read => jop_avalon_inst_avalon_master_read,
      jop_avalon_inst_avalon_master_write => jop_avalon_inst_avalon_master_write,
      jop_avalon_inst_dbs_address => jop_avalon_inst_dbs_address,
      jop_avalon_inst_dbs_write_16 => jop_avalon_inst_dbs_write_16,
      reset_n => clk_reset_n,
      sdram_s1_readdata => sdram_s1_readdata,
      sdram_s1_readdatavalid => sdram_s1_readdatavalid,
      sdram_s1_waitrequest => sdram_s1_waitrequest
    );


  --the_sdram, which is an e_ptf_instance
  the_sdram : sdram
    port map(
      za_data => sdram_s1_readdata,
      za_valid => sdram_s1_readdatavalid,
      za_waitrequest => sdram_s1_waitrequest,
      zs_addr => internal_zs_addr_from_the_sdram,
      zs_ba => internal_zs_ba_from_the_sdram,
      zs_cas_n => internal_zs_cas_n_from_the_sdram,
      zs_cke => internal_zs_cke_from_the_sdram,
      zs_cs_n => internal_zs_cs_n_from_the_sdram,
      zs_dq => zs_dq_to_and_from_the_sdram,
      zs_dqm => internal_zs_dqm_from_the_sdram,
      zs_ras_n => internal_zs_ras_n_from_the_sdram,
      zs_we_n => internal_zs_we_n_from_the_sdram,
      az_addr => sdram_s1_address,
      az_be_n => sdram_s1_byteenable_n,
      az_cs => sdram_s1_chipselect,
      az_data => sdram_s1_writedata,
      az_rd_n => sdram_s1_read_n,
      az_wr_n => sdram_s1_write_n,
      clk => clk,
      reset_n => sdram_s1_reset_n
    );


  --reset is asserted asynchronously and deasserted synchronously
  test_reset_clk_domain_synch : test_reset_clk_domain_synch_module
    port map(
      data_out => clk_reset_n,
      clk => clk,
      data_in => module_input3,
      reset_n => reset_n_sources
    );

  module_input3 <= std_logic'('1');

  --reset sources mux, which is an e_mux
  reset_n_sources <= Vector_To_Std_Logic(NOT (((std_logic_vector'("0000000000000000000000000000000") & (A_TOSTDLOGICVECTOR(NOT reset_n))) OR std_logic_vector'("00000000000000000000000000000000"))));
  --vhdl renameroo for output signals
  ser_txd_from_the_jop_avalon_inst <= internal_ser_txd_from_the_jop_avalon_inst;
  --vhdl renameroo for output signals
  wd_from_the_jop_avalon_inst <= internal_wd_from_the_jop_avalon_inst;
  --vhdl renameroo for output signals
  zs_addr_from_the_sdram <= internal_zs_addr_from_the_sdram;
  --vhdl renameroo for output signals
  zs_ba_from_the_sdram <= internal_zs_ba_from_the_sdram;
  --vhdl renameroo for output signals
  zs_cas_n_from_the_sdram <= internal_zs_cas_n_from_the_sdram;
  --vhdl renameroo for output signals
  zs_cke_from_the_sdram <= internal_zs_cke_from_the_sdram;
  --vhdl renameroo for output signals
  zs_cs_n_from_the_sdram <= internal_zs_cs_n_from_the_sdram;
  --vhdl renameroo for output signals
  zs_dqm_from_the_sdram <= internal_zs_dqm_from_the_sdram;
  --vhdl renameroo for output signals
  zs_ras_n_from_the_sdram <= internal_zs_ras_n_from_the_sdram;
  --vhdl renameroo for output signals
  zs_we_n_from_the_sdram <= internal_zs_we_n_from_the_sdram;

end europa;


--synthesis translate_off

library altera;
use altera.altera_europa_support_lib.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;



-- <ALTERA_NOTE> CODE INSERTED BETWEEN HERE
--add your libraries here
-- AND HERE WILL BE PRESERVED </ALTERA_NOTE>

entity test_bench is 
end entity test_bench;


architecture europa of test_bench is
component test is 
           port (
                 -- 1) global signals:
                    signal clk : IN STD_LOGIC;
                    signal reset_n : IN STD_LOGIC;

                 -- the_jop_avalon_inst
                    signal clk_to_the_jop_avalon_inst : IN STD_LOGIC;
                    signal reset_to_the_jop_avalon_inst : IN STD_LOGIC;
                    signal ser_rxd_to_the_jop_avalon_inst : IN STD_LOGIC;
                    signal ser_txd_from_the_jop_avalon_inst : OUT STD_LOGIC;
                    signal wd_from_the_jop_avalon_inst : OUT STD_LOGIC;

                 -- the_sdram
                    signal zs_addr_from_the_sdram : OUT STD_LOGIC_VECTOR (11 DOWNTO 0);
                    signal zs_ba_from_the_sdram : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal zs_cas_n_from_the_sdram : OUT STD_LOGIC;
                    signal zs_cke_from_the_sdram : OUT STD_LOGIC;
                    signal zs_cs_n_from_the_sdram : OUT STD_LOGIC;
                    signal zs_dq_to_and_from_the_sdram : INOUT STD_LOGIC_VECTOR (15 DOWNTO 0);
                    signal zs_dqm_from_the_sdram : OUT STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal zs_ras_n_from_the_sdram : OUT STD_LOGIC;
                    signal zs_we_n_from_the_sdram : OUT STD_LOGIC
                 );
end component test;

component sdram_test_component is 
           port (
                 -- inputs:
                    signal clk : IN STD_LOGIC;
                    signal zs_addr : IN STD_LOGIC_VECTOR (11 DOWNTO 0);
                    signal zs_ba : IN STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal zs_cas_n : IN STD_LOGIC;
                    signal zs_cke : IN STD_LOGIC;
                    signal zs_cs_n : IN STD_LOGIC;
                    signal zs_dqm : IN STD_LOGIC_VECTOR (1 DOWNTO 0);
                    signal zs_ras_n : IN STD_LOGIC;
                    signal zs_we_n : IN STD_LOGIC;

                 -- outputs:
                    signal zs_dq : INOUT STD_LOGIC_VECTOR (15 DOWNTO 0)
                 );
end component sdram_test_component;

                signal clk :  STD_LOGIC;
                signal clk_to_the_jop_avalon_inst :  STD_LOGIC;
                signal reset_n :  STD_LOGIC;
                signal reset_to_the_jop_avalon_inst :  STD_LOGIC;
                signal ser_rxd_to_the_jop_avalon_inst :  STD_LOGIC;
                signal ser_txd_from_the_jop_avalon_inst :  STD_LOGIC;
                signal wd_from_the_jop_avalon_inst :  STD_LOGIC;
                signal zs_addr_from_the_sdram :  STD_LOGIC_VECTOR (11 DOWNTO 0);
                signal zs_ba_from_the_sdram :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal zs_cas_n_from_the_sdram :  STD_LOGIC;
                signal zs_cke_from_the_sdram :  STD_LOGIC;
                signal zs_cs_n_from_the_sdram :  STD_LOGIC;
                signal zs_dq_to_and_from_the_sdram :  STD_LOGIC_VECTOR (15 DOWNTO 0);
                signal zs_dqm_from_the_sdram :  STD_LOGIC_VECTOR (1 DOWNTO 0);
                signal zs_ras_n_from_the_sdram :  STD_LOGIC;
                signal zs_we_n_from_the_sdram :  STD_LOGIC;


-- <ALTERA_NOTE> CODE INSERTED BETWEEN HERE
--add your component and signal declaration here
-- AND HERE WILL BE PRESERVED </ALTERA_NOTE>


begin

  --Set us up the Dut
  DUT : test
    port map(
      ser_txd_from_the_jop_avalon_inst => ser_txd_from_the_jop_avalon_inst,
      wd_from_the_jop_avalon_inst => wd_from_the_jop_avalon_inst,
      zs_addr_from_the_sdram => zs_addr_from_the_sdram,
      zs_ba_from_the_sdram => zs_ba_from_the_sdram,
      zs_cas_n_from_the_sdram => zs_cas_n_from_the_sdram,
      zs_cke_from_the_sdram => zs_cke_from_the_sdram,
      zs_cs_n_from_the_sdram => zs_cs_n_from_the_sdram,
      zs_dq_to_and_from_the_sdram => zs_dq_to_and_from_the_sdram,
      zs_dqm_from_the_sdram => zs_dqm_from_the_sdram,
      zs_ras_n_from_the_sdram => zs_ras_n_from_the_sdram,
      zs_we_n_from_the_sdram => zs_we_n_from_the_sdram,
      clk => clk,
      clk_to_the_jop_avalon_inst => clk_to_the_jop_avalon_inst,
      reset_n => reset_n,
      reset_to_the_jop_avalon_inst => reset_to_the_jop_avalon_inst,
      ser_rxd_to_the_jop_avalon_inst => ser_rxd_to_the_jop_avalon_inst
    );


  --the_sdram_test_component, which is an e_instance
  the_sdram_test_component : sdram_test_component
    port map(
      zs_dq => zs_dq_to_and_from_the_sdram,
      clk => clk,
      zs_addr => zs_addr_from_the_sdram,
      zs_ba => zs_ba_from_the_sdram,
      zs_cas_n => zs_cas_n_from_the_sdram,
      zs_cke => zs_cke_from_the_sdram,
      zs_cs_n => zs_cs_n_from_the_sdram,
      zs_dqm => zs_dqm_from_the_sdram,
      zs_ras_n => zs_ras_n_from_the_sdram,
      zs_we_n => zs_we_n_from_the_sdram
    );


  process
  begin
    clk <= '0';
    loop
       wait for 10 ns;
       clk <= not clk;
    end loop;
  end process;
  PROCESS
    BEGIN
       reset_n <= '0';
       wait for 200 ns;
       reset_n <= '1'; 
    WAIT;
  END PROCESS;


-- <ALTERA_NOTE> CODE INSERTED BETWEEN HERE
--add additional architecture here
-- AND HERE WILL BE PRESERVED </ALTERA_NOTE>


end europa;



--synthesis translate_on
