--
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2007,2008, Christof Pitter
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--




library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.jop_types.all;

entity tb_cmpsync is
end tb_cmpsync;

architecture test of tb_cmpsync is
  
  component cmpsync is
	generic (
			 cpu_cnt	: integer);
	port (
			clk, reset	: in std_logic;			
			sync_in_array  : in sync_in_array_type(0 to cpu_cnt-1);
			sync_out_array : out sync_out_array_type(0 to cpu_cnt-1)
	);
  end component;
	
	signal cpu_cnt : integer := 3;
	
	-- Stimulus Signals
	signal s_clk		: std_logic := '0'; 
	signal s_reset	: std_logic := '0';
	
	signal s_in0		: std_logic;
	signal s_lock0	: std_logic;
	signal s_in1		: std_logic;
	signal s_lock1	: std_logic;
	signal s_in2		: std_logic;
	signal s_lock2	: std_logic;
		
	-- Response Signals
	signal r_out0			: std_logic;
	signal r_release0 : std_logic;
	signal r_out1			: std_logic;
	signal r_release1 : std_logic;
	signal r_out2			: std_logic;
	signal r_release2 : std_logic;
	
begin
	
	cmpsync1: cmpsync 
	generic map (
		cpu_cnt => 3)
	port map(
		clk => s_clk,
		reset => s_reset,
	
		sync_in_array(0).s_in => s_in0,
		sync_in_array(0).lockreq => s_lock0,
		sync_in_array(1).s_in => s_in1,
		sync_in_array(1).lockreq => s_lock1,
		sync_in_array(2).s_in => s_in2,
		sync_in_array(2).lockreq => s_lock2,
			
		sync_out_array(0).s_out => r_out0,
		sync_out_array(0).locked => r_release0,
		sync_out_array(1).s_out => r_out1,
		sync_out_array(1).locked => r_release1,
		sync_out_array(2).s_out => r_out2,
		sync_out_array(2).locked => r_release2
		
    );
 
	s_clk <= not s_clk after 5 ns;
	
	stim : process
	begin

 --Zugriff nacheinander
		s_reset <= '1';
		s_in0 <= '0';
		s_in1 <= '0';
		s_in2 <= '0';
		wait for 10 ns;
		s_reset <= '0';
		wait for 5 ns;
		s_in0 <= '1';
		wait for 10 ns;
		s_lock0 <= '1';
		wait for 10 ns;
		s_lock0 <= '0';
		wait for 10 ns;
		s_lock1 <= '1';
		wait for 10 ns;
		s_lock1 <= '0';
		wait for 10 ns;
		s_lock2 <= '1';
		wait for 10 ns;
		s_lock2 <= '0';
		s_lock0 <= '1';
		s_lock1 <= '1';
		wait for 10 ns;
		wait for 10 ns;
		s_lock0 <= '0';
		s_lock2 <= '1';
		wait for 10 ns;
		wait for 10 ns;
		s_lock1 <= '0';
		wait for 10 ns;
		s_lock2 <= '0';
		wait for 295 ns;
    

	end process stim;
	
end test;



















