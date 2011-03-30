--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
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


--
--	jop_nexys2.vhd
--
--	top level for Digilent nexys2 Spartan-3E kit
--
--	2007-10-12	Adapted from xs3
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config_global.all;
use work.jop_config.all;


entity jop is

generic (
	ram_cnt		: integer := 10;	-- clock cycles for external ram
	jpc_width	: integer := 11;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4;		-- 2*block_bits is number of cache blocks
	spm_width	: integer := 0		-- size of scratchpad RAM (in number of address bits for 32-bit words)
);

port (
	clk		: in std_logic;
--
--	serial interface
--
	RsRx       : in    std_logic; 
	RsTx       : inout std_logic; 

--
--	LED, Flash enable, and PSDRAM signals as named by digilent
--
--	However, the control signals are low active!
--
	led        : out   std_logic_vector (7 downto 0); 
	FlashCS    : out   std_logic; 
	MemAdr     : out   std_logic_vector (23 downto 1); 
	MemDB      : inout std_logic_vector (15 downto 0); 
	RamWait    : in    std_logic; 
	MemOe      : out   std_logic; 
	MemWr      : out   std_logic; 
	RamAdv     : out   std_logic; 
	RamClk     : out   std_logic; 
	RamCre     : out   std_logic; 
	RamCS      : out   std_logic; 
	RamLB      : out   std_logic; 
	RamUB      : out   std_logic
);
end jop;

architecture rtl of jop is

--
--	components:
--

--
--	Signals
--
	signal clk_int			: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	-- attribute altera_attribute : string;
	-- attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";

--
--	jopcpu connections
--
	signal sc_mem_out		: sc_out_type;
	signal sc_mem_in		: sc_in_type;
	signal sc_io_out		: sc_out_type;
	signal sc_io_in			: sc_in_type;
	signal irq_in			: irq_bcf_type;
	signal irq_out			: irq_ack_type;
	signal exc_req			: exception_type;

--
--	IO interface
--
	signal ser_in			: ser_in_type;
	signal ser_out			: ser_out_type;
	signal wd_out			: std_logic;

	-- for generation of internal reset

-- memory interface

	signal ram_dout			: std_logic_vector(15 downto 0);
	signal ram_din			: std_logic_vector(15 downto 0);
	signal ram_dout_en		: std_logic;

-- not available at this board:
	signal ser_ncts			: std_logic;
	signal ser_nrts			: std_logic;
begin

	FlashCS <= '1';
	led(7 downto 1) <= "1111000";	-- just some pattern

	ser_ncts <= '0';
--
--	intern reset
--

process(clk_int)
begin
	if rising_edge(clk_int) then
		if (res_cnt/="111") then
			res_cnt <= res_cnt+1;
		end if;

		int_res <= not res_cnt(0) or not res_cnt(1) or not res_cnt(2);
	end if;
end process;

--
--	components of jop
--
	clk_int <= clk;

	led(0) <= wd_out;

	cpu: entity work.jopcpu
		generic map(
			jpc_width => jpc_width,
			block_bits => block_bits,
			spm_width => spm_width
		)
		port map(clk_int, int_res,
			sc_mem_out, sc_mem_in,
			sc_io_out, sc_io_in,
			irq_in, irq_out, exc_req);

	io: entity work.scio 
		port map (clk_int, int_res,
			sc_io_out, sc_io_in,
			irq_in, irq_out, exc_req,

			txd => RsTx,
			rxd => RsRx,
			ncts => ser_ncts,
			nrts => ser_nrts,
			wd => wd_out,
			l => open,
			r => open,
			t => open,
			b => open
		);

	scm: entity work.sc_mem_if
		generic map (
			ram_ws => ram_cnt-1,
			addr_bits => 21
		)
		port map (clk_int, int_res,
			sc_mem_out, sc_mem_in,

			ram_addr => MemAdr(21 downto 1),
			ram_dout => ram_dout,
			ram_din => ram_din,
			ram_dout_en	=> ram_dout_en,
			ram_ncs => RamCS,
			ram_noe => MemOe,
			ram_nwe => MemWr
		);

	-- a quick hack to avoid SC package change
	-- TODO: we should rethink the SC records
	MemAdr(23 downto 22) <= "00";
	RamAdv <= '0';
	RamClk <= '0';
	RamCre <= '0';
	RamLB <= '0';
	RamUB <= '0';

	process(ram_dout_en, ram_dout)
	begin
		if ram_dout_en='1' then
			MemDB <= ram_dout(15 downto 0);
		else
			MemDB <= (others => 'Z');
		end if;
	end process;

	ram_din <= MemDB;

end rtl;
