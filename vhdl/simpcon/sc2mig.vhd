--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2011, Oleg Belousov (belousov.oleg@gmail.com)
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

--	SimpCon/Xilinx MIG bridge
--
--	Author: Oleg Belousov	belousov.oleg@gmail.com

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;

entity sc2mig is
    generic (
	HADDR_WIDTH         	: natural := 23        -- host-side address width
    );
    port (
	clk, reset		: in std_logic;

	-- SimpCon memory interface

	sc_mem_out		: in sc_out_type;
	sc_mem_in		: out sc_in_type;

	-- MIG interface

        mig_cmd_en		: out std_logic;
        mig_cmd_instr		: out std_logic_vector(2 downto 0);
        mig_cmd_byte_addr	: out std_logic_vector(29 downto 0);
        mig_cmd_empty		: in std_logic;
        mig_cmd_bl		: out std_logic_vector(5 downto 0);
        
        mig_wr_en		: out std_logic;
        mig_wr_data		: out std_logic_vector(31 downto 0);
        mig_wr_empty		: in std_logic;
        mig_wr_count		: in std_logic_vector(6 downto 0);

        mig_rd_en		: out std_logic;
        mig_rd_data		: in std_logic_vector(31 downto 0);
        mig_rd_empty		: in std_logic;
        mig_rd_count		: in std_logic_vector(6 downto 0)
    );
end sc2mig;

architecture rtl of sc2mig is

    type state_type is (idl, rd1, rd2, wr1, wr2, wr3);

    signal state		: state_type;
    signal next_state		: state_type;

    signal reg_wr_data		: std_logic_vector(31 downto 0);

    signal reg_addr		: std_logic_vector(HADDR_WIDTH-1 downto 0);
    signal reg_rd_data		: std_logic_vector(31 downto 0);

    signal cmd_en		: std_logic;
    signal rd_en		: std_logic;
    signal wr_en		: std_logic;

    signal cnt			: unsigned(1 downto 0);

begin

    reg_addr <= sc_mem_out.address;

    mig_cmd_byte_addr(HADDR_WIDTH-1+2 downto 2) <= reg_addr;
    mig_cmd_byte_addr(29 downto HADDR_WIDTH+2) <= (others => '0');
    mig_cmd_byte_addr(1 downto 0) <= (others => '0');

    mig_wr_data <= reg_wr_data;

    sc_mem_in.rd_data <= reg_rd_data;
    sc_mem_in.rdy_cnt <= cnt;

    rd_en <= not mig_rd_empty;
    cmd_en <= sc_mem_out.wr or sc_mem_out.rd;

    mig_cmd_instr <=
	"000" when sc_mem_out.wr = '1' else
	"001" when sc_mem_out.rd = '1' else "000";

    mig_cmd_en <= cmd_en;
    mig_rd_en <= rd_en;
    mig_wr_en <= wr_en;
    mig_cmd_bl <= "000000";
    
    -- Register write data
    
    process(clk, reset) begin
        if reset = '1' then
    	    reg_wr_data <= (others => '0');
        elsif rising_edge(clk) then
            if sc_mem_out.wr = '1' then
        	reg_wr_data <= sc_mem_out.wr_data;
            end if;
        end if;
    end process;

    -- Next state logic

    process(state) begin
	next_state <= state;
	cnt <= "00";
	wr_en <= '0';

	case state is
	    when idl =>
		if sc_mem_out.rd = '1' then
		    next_state <= rd1;
		    cnt <= "11";
		elsif sc_mem_out.wr = '1' then
		    next_state <= wr1;
		    cnt <= "11";
		else
		    cnt <= "00";
		end if;
	
	    -- Read
	
	    when rd1 =>
		if mig_rd_empty = '0' then
		    next_state <= rd2;
		    cnt <= "01";
		else
		    cnt <= "11";
		end if;

	    when rd2 =>
		if sc_mem_out.rd = '1' then
		    next_state <= rd1;
		    cnt <= "11";
		elsif sc_mem_out.wr = '1' then
		    next_state <= wr1;
		    cnt <= "11";
		else
		    cnt <= "00";
		    next_state <= idl;
		end if;


	    -- Write

	    when wr1 =>
		cnt <= "11";
		wr_en <= '1';
		next_state <= wr2;
		
	    when wr2 =>
		cnt <= "11";
		if mig_wr_empty = '0' then
		    next_state <= wr3;
		end if;
		
	    when wr3 =>
		if mig_wr_empty = '1' then
		    next_state <= idl;
		    cnt <= "00";
		else
		    cnt <= "11";
		end if;

	end case;
    end process;

    -- State machine register
    
    process(clk, reset) begin
	if reset = '1' then
	    state <= idl;
    	    reg_rd_data <= (others => '0');
	elsif rising_edge(clk) then
	    state <= next_state;
	    
	    case next_state is
		when idl =>
		
		when rd1 =>

		when rd2 =>
		    reg_rd_data <= mig_rd_data;
		
		when wr1 =>
		when wr2 =>
		when wr3 =>
	    end case;
	end if;
    end process;

end rtl;
