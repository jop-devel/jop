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
--	fetch.vhd
--
--	jop instrcution fetch and branch
--
--
--	resources on ACEX1K30-3
--
--		132 LCs, max ca. 50 MHz
--
--	todo:
--		5 stage pipeline (jtbl/rom)
--		relativ address for jp, br
--		load pc instead of addres mux befor rom!
--
--	2001-07-04	first version
--	2001-07-18	component pc_inc in own file for Xilinx
--	2001-10-24	added 2 delays for br address (address is now in br opcode!)
--	2001-10-28	ldjpc, stjpc
--	2001-10-31	stbc (write content of jbc)
--	2001-11-13	added jtbl (jtbl and rom in one pipline stage!)
--	2001-11-14	change jbc to 1024 bytes
--	2001-11-16	split to fetch and bcfetch
--	2001-12-06	ir from decode to rom, (one brdly removed)
--				mux befor rom removed, unregistered jfetch conrols imput to
--				pc, jpaddr unregistered!
--	2001-12-07	branch relativ
--	2001-12-08	use table for branch offsets
--	2001-12-08	instruction set changed to 8 bit, pc to 10 bits
--	2002-12-02	wait instruction for memory
--	2003-08-15	move bcfetch to core
--	2004-04-06	nxt and opd are in rom. rom address from jpc_mux and with
--				positiv edge rdaddr. unregistered output in rom.
--	2004-10-08	moved bsy/pcwait from decode to fetch
--	2005-09-05	use new branch and jmp instructions (offset part of instruction)
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity fetch is

generic (
	pc_width	: integer;	-- address bits of internal instruction rom
	i_width		: integer	-- instruction width
);
port (
	clk, reset	: in std_logic;

	nxt, opd	: out std_logic;	-- jfetch and jopdfetch from table

	br			: in std_logic;
	jmp			: in std_logic;
	bsy			: in std_logic;		-- direct from the memory module
	jpaddr		: in std_logic_vector(pc_width-1 downto 0);

	dout		: out std_logic_vector(i_width-1 downto 0)		-- internal instruction (rom)
);
end fetch;

architecture rtl of fetch is

--
--	rom component (use technology specific vhdl-file (arom/xrom))
--		or generic rom.vhd
--
--	rom registered address, unregisterd out
--
component rom is
generic (width : integer; addr_width : integer);
port (
	clk			: in std_logic;

	address		: in std_logic_vector(pc_width-1 downto 0);

	q			: out std_logic_vector(i_width+1 downto 0)
);
end component;

	signal pc_mux		: std_logic_vector(pc_width-1 downto 0);
	signal pc_inc		: std_logic_vector(pc_width-1 downto 0);
	signal pc			: std_logic_vector(pc_width-1 downto 0);
	signal brdly		: std_logic_vector(pc_width-1 downto 0);
	signal jpdly		: std_logic_vector(pc_width-1 downto 0);

	signal jfetch		: std_logic;		-- fetch next byte code as opcode
	signal jopdfetch	: std_logic;		-- fetch next byte code as operand

	signal rom_data		: std_logic_vector(i_width+1 downto 0);		-- output from ROM
	signal ir			: std_logic_vector(i_width-1 downto 0);		-- instruction register
signal pcwait : std_logic;

begin


--
--	pc_mux is 1 during reset!
--		=> first instruction from ROM gets NEVER executed.
--
	mcrom: rom generic map (i_width+2, pc_width) port map(clk, pc_mux, rom_data);
	jfetch <= rom_data(i_width+1);
	jopdfetch <= rom_data(i_width);

	dout <= ir;
	nxt <= jfetch;
	opd <= jopdfetch;

process(clk)
begin
	if rising_edge(clk) then				-- we don't need a reset
		ir <= rom_data(i_width-1 downto 0);			-- better read (second) instruction from rom
		pcwait <= '0';
		-- decode wait instruction from unregistered rom
		if (rom_data(i_width-1 downto 0)="0100000001") then	-- wait instuction
			pcwait <= '1';
		end if;
	end if;
end process;

process(clk, reset)

begin
	if (reset='1') then
		pc <= (others => '0');
		brdly <= (others => '0');
		jpdly <= (others => '0');
	elsif rising_edge(clk) then
		-- 6 bits as signed branch offset
		brdly <= std_logic_vector(signed(pc) + to_signed(to_integer(signed(ir(5 downto 0))), pc_width));
		-- 9 bits as signed jump offset
		jpdly <= std_logic_vector(signed(pc) + to_signed(to_integer(signed(ir(i_width-2 downto 0))), pc_width));
		pc <= pc_mux;
	end if;
end process;

	pc_inc <= std_logic_vector(unsigned(pc) + 1);

process(jfetch, br, jmp, jpaddr, brdly, jpdly, pc, pc_inc, pcwait, bsy)
begin
	if jfetch='1' then
		pc_mux <= jpaddr;
	else 
		if br='1' then
			pc_mux <= brdly;
		elsif jmp='1' then
			pc_mux <= jpdly;
		else
			-- bsy is too late to register pcwait and bsy
			if (pcwait='1' and bsy='1') then
				pc_mux <= pc;
			else
				pc_mux <= pc_inc;
			end if;
		end if;
	end if;
end process;

-- synthesis translate_off
	-- show microcode mnemonic
	uc: entity work.microcode port map(ir);
-- synthesis translate_on

end rtl;

