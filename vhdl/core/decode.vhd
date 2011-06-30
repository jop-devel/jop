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
--	decode.vhd
--
--	cpu decode of JOP3
--	
--	generate control for pc and stack
--
--
--	resources on ACEX1K30-3
--	
--		xxx LCs, 42.0 MHz	
--
--	todo:
--
--
--	2001-07-03	extracted from core.vhd
--	2001-10-28	ldjpc, stjpc
--	2001-10-31	stbc
--	2001-12-04	cp removed
--	2001-12-05	sel_rda, sel_wra: logic from ir (not registered)
--				sel_smux: logic from ir (not registered, no mix addr/rd-ex: nop befor stsp)
--	2001-12-06	moved ir from decode to fetch, jbr unregistered
--	2001-12-07	moved jbr decode to bcfetch, ldi loads consts from ram (addr > 31), jp removed
--	2001-12-08	instruction set changed to 8 bit
--	2002-03-24	new shift instructions
--	2002-12-02	wait instruction for memory
--	2003-02-12	added instruction ld_opd_8u/16u
--	2004-10-07	new alu selection with sel_sub, sel_amux and ena_a
--	2004-10-08	moved bsy/pcwait from decode to fetch
--	2006-01-12	new ar for local memory addressing: star, ldmi, stmi
--				stioa, stiod, ldiod removed
--	2007-08-31	use addr_width for signal dir
--	2007-09-01	use ram_width from jop_config instead of parameter
--	2005-09-05	use new branch and jmp instructions (offset part of instruction)
--	2009-11-22	move MMU decode from jopcpu (extension) to here
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.numeric_std.all ;

use work.jop_config.all;
use work.jop_types.all;

entity decode is
generic (
	i_width		: integer		-- instruction width
);

port (
	clk, reset	: in std_logic;

	instr		: in std_logic_vector(i_width-1 downto 0);
	zf, nf		: in std_logic;		-- nf, eq and lt not used (only brz, brnz)
	eq, lt		: in std_logic;

	bcopd		: in std_logic_vector(15 downto 0);	-- index for mmu

	br			: out std_logic;
	jmp			: out std_logic;
	jbr			: out std_logic;

	mem_in		: out mem_in_type;
	mmu_instr	: out std_logic_vector(MMU_WIDTH-1 downto 0);
	mul_wr		: out std_logic;
	wr_dly		: out std_logic;

	dir			: out std_logic_vector(ram_width-1 downto 0);

	sel_sub		: out std_logic;						-- 0..add, 1..sub
	sel_amux	: out std_logic;						-- 0..sum, 1..lmux
	ena_a		: out std_logic;						-- 1..store new value
	sel_bmux	: out std_logic;						-- 0..a, 1..mem
	sel_log		: out std_logic_vector(1 downto 0);		-- pop/st, and, or, xor
	sel_shf		: out std_logic_vector(1 downto 0);		-- sr, sl, sra, (sr)
	sel_lmux	: out std_logic_vector(2 downto 0);		-- log, shift, mem, io, reg
	sel_imux	: out std_logic_vector(1 downto 0);		-- java opds
	sel_rmux	: out std_logic_vector(1 downto 0);		-- sp, vp, jpc
	sel_smux	: out std_logic_vector(1 downto 0);		-- sp, a, sp-1, sp+1

	sel_mmux	: out std_logic;						-- 0..a, 1..b
	sel_rda		: out std_logic_vector(2 downto 0);		-- 
	sel_wra		: out std_logic_vector(2 downto 0);		-- 

	wr_ena		: out std_logic;

	ena_b		: out std_logic;
	ena_vp		: out std_logic;
	ena_jpc		: out std_logic;
	ena_ar		: out std_logic
);
end decode;

architecture rtl of decode is

--
--	Signals
--

--
-- intruction register, shortcut
--
--	signal ir		: std_logic_vector(i_width-1 downto 0);		-- Xilinx and ModelSim don't like this... instruction register
	signal ir		: std_logic_vector(9 downto 0);		-- instruction register
	signal is_push	: std_logic;
	signal is_pop	: std_logic;

begin

	ir <= instr;		-- registered in fetch

	mmu_instr <= ir(MMU_WIDTH-1 downto 0);	-- address for extension select

--
--	branch, jbranch
--

process(clk, reset)
begin
	if (reset='1') then
		br <= '0';
		jmp <= '0';
	elsif rising_edge(clk) then

		br <= '0';
		jmp <= '0';
		if((ir(9 downto 6)="0110" and zf='1') or		-- bz
			(ir(9 downto 6)="0111" and zf='0')) then	-- bnz
			br <= '1';
		end if;
		if (ir(9)='1') then								-- jmp
			jmp <= '1';
		end if;

	end if;
end process;

--	wait is decoded direct in fetch.vhd!

process(ir)
begin

	jbr <= '0';
	if ir="0100000010" then		-- jbr: goto and if_xxx
		jbr <= '1';
	end if;

end process;

--
--	addr, read stage:
--		decode from ir (only logic, no register)
--
process(ir, is_pop, is_push)
begin

	is_pop <= '0';
	is_push <= '0';

	case ir(9 downto 6) is

		when "0000" =>			-- POP
				is_pop <= '1';
		when "0001" =>			-- POP
				is_pop <= '1';
		when "0010" =>			-- PUSH
				is_push <= '1';
		when "0011" =>			-- PUSH
				is_push <= '1';
		when "0100" =>			-- NOP
		when "0101" =>			-- null
		when "0110" =>			-- POP
				is_pop <= '1';
		when "0111" =>			-- POP
				is_pop <= '1';
		when "1000" =>			-- NOP
		when "1001" =>			-- null
		when "1010" =>			-- null
		when "1011" =>			-- null
		when "1100" =>			-- null
		when "1101" =>			-- null
		when "1110" =>			-- null
		when "1111" =>			-- null


		when others =>
			null;
	end case;



-- ram wraddress and wrena are registered

	wr_ena <= '0';
	if (is_push='1' or						-- push instructions
		ir(9 downto 5)="00001" or			-- stm
		ir(9 downto 3)="0000010") then		-- st, stn, stmi

		wr_ena <= '1';
	end if;

	sel_imux <= ir(1 downto 0);			-- ld opd_x

-- select for rd/wr address muxes

	dir <= std_logic_vector(to_unsigned(0, ram_width-5)) & ir(4 downto 0);

	sel_rda <= "110";					-- sp
	if (ir(9 downto 3)="0011101") then	-- ld, ldn, ldmi
		sel_rda <= ir(2 downto 0);
	end if;
	if (ir(9 downto 5)="00101") then	-- ldm
		sel_rda <= "111";
	end if;
	if (ir(9 downto 5)="00110") then	-- ldi
		sel_rda <= "111";
		dir <= std_logic_vector(to_unsigned(1, ram_width-5)) & 
			ir(4 downto 0);				-- addr > 31 constants
	end if;

	sel_wra <= "110";					-- spp
	if ir(9 downto 3)="0000010" then	-- st, stn, stmi
		sel_wra <= ir(2 downto 0);
	end if;
	if ir(9 downto 5)="00001" then		-- stm
		sel_wra <= "111";
	end if;

-- select for sp update

	sel_smux <= "00";				-- sp = sp
	if is_pop='1' then				-- 'pop' instruction
		sel_smux <= "01";			-- --sp
	end if;
	if is_push='1' then				-- 'push' instruction
		sel_smux <= "10";			-- ++sp
	end if;
	if ir="0000011011" then			-- st sp
		sel_smux <= "11";			-- sp = a
	end if;
			
end process;

--
--	ex stage (ALU, stack)
--

process(clk, reset)
begin

	if (reset='1') then
		sel_sub <= '0';
		sel_amux <= '0';
		ena_a <= '0';
		sel_bmux <= '0';
		sel_log <= "00";
		sel_shf <= "00";
		sel_lmux <= "000";
		sel_rmux <= "00";
		sel_mmux <= '0';
		ena_b <= '0';
		ena_vp <= '0';
		ena_jpc <= '0';
		ena_ar <= '0';

	elsif rising_edge(clk) then

		sel_log <= "00";						-- default is pop path
		if (ir(9 downto 2)="00000000") then		-- pop, and, or, xor
			sel_log <= ir(1 downto 0);
		end if;

		sel_shf <= ir(1 downto 0);

		sel_sub <= '1';			-- default is subtract for lt-flag
		sel_amux <= '1';			-- default is lmux
		ena_a <= '1';			-- default is enable
		ena_vp <= '0';
		ena_jpc <= '0';
		ena_ar <= '0';

		case ir is

			when "0000000000" =>			-- pop
			when "0000000001" =>			-- and
			when "0000000010" =>			-- or
			when "0000000011" =>			-- xor
			when "0000000100" =>			-- add
					sel_sub <= '0';
					sel_amux <= '0';
			when "0000000101" =>			-- sub
					sel_amux <= '0';
			when "0000010000" =>			-- st0
			when "0000010001" =>			-- st1
			when "0000010010" =>			-- st2
			when "0000010011" =>			-- st3
			when "0000010100" =>			-- st
			when "0000010101" =>			-- stmi
			when "0000011000" =>			-- stvp
					ena_vp <= '1';
			when "0000011001" =>			-- stjpc
					ena_jpc <= '1';
			when "0000011010" =>			-- star
					ena_ar <= '1';
			when "0000011011" =>			-- stsp
			when "0000011100" =>			-- ushr
			when "0000011101" =>			-- shl
			when "0000011110" =>			-- shr
--			when "00001-----" =>			-- stm
			when "0001000000" =>			-- stmul
			when "0001000001" =>			-- stmwa
			when "0001000010" =>			-- stmra
			when "0001000011" =>			-- stmwd
			when "0001000100" =>			-- stald
			when "0001000101" =>			-- stast
			when "0001000110" =>			-- stgf
			when "0001000111" =>			-- stpf
			when "0001001111" =>			-- stpfr/stpsr/stastr
			when "0001001000" =>			-- stcp
			when "0001001001" =>			-- stbcrd
			when "0001001010" =>			-- stidx
			when "0001001011" =>			-- stps
			when "0001001100" =>			-- stmrac
			when "0001001101" =>			-- stmraf
			when "0001001110" =>			-- stmwdf
--			when "00101-----" =>			-- ldm
--			when "00110-----" =>			-- ldi
			when "0011100000" =>			-- ldmrd
			when "0011100001" =>			-- ldmul
			when "0011100010" =>			-- ldbcstart
			when "0011101000" =>			-- ld0
			when "0011101001" =>			-- ld1
			when "0011101010" =>			-- ld2
			when "0011101011" =>			-- ld3
			when "0011101100" =>			-- ld
			when "0011101101" =>			-- ldmi
			when "0011110000" =>			-- ldsp
			when "0011110001" =>			-- ldvp
			when "0011110010" =>			-- ldjpc
			when "0011110100" =>			-- ld_opd_8u
			when "0011110101" =>			-- ld_opd_8s
			when "0011110110" =>			-- ld_opd_16u
			when "0011110111" =>			-- ld_opd_16s
			when "0011111000" =>			-- dup
					ena_a <= '0';
			when "0100000000" =>			-- nop
					ena_a <= '0';
			when "0100000001" =>			-- wait
					ena_a <= '0';
			when "0100000010" =>			-- jbr
					ena_a <= '0';
			when "0100010000" =>			-- stgs
					ena_a <= '0';
			when "0100010001" =>			-- cinval
					ena_a <= '0';
			when "0100010010" =>			-- atmstart
					ena_a <= '0';
			when "0100010011" =>			-- atmend
					ena_a <= '0';
--			when "0110------" =>			-- bz
--			when "0111------" =>			-- bnz
--			when "1---------" =>			-- jmp
--					ena_a <= '0';

			when others =>
				null;
		end case;

		if ir(9)='1' then		-- jmp
			ena_a <= '0';
		end if;

		sel_lmux <= "000";		-- log

		if ir(9 downto 2)="00000111" then				-- ushr, shl, shr
			sel_lmux <= "001";
		end if;

		if ir(9 downto 5)="00101" then				-- ldm
			sel_lmux <= "010";
		end if;
		if ir(9 downto 5)="00110" then				-- ldi
			sel_lmux <= "010";
		end if;

		if ir(9 downto 3)="0011101" then				-- ld, ldn, ldmi
			sel_lmux <= "010";
		end if;

		if ir(9 downto 2)="00111101" then				-- ld_opd_x
			sel_lmux <= "011";
		end if;

		if ir(9 downto 3)="0011100" then				-- ld from mmu/mul
			sel_lmux <= "100";
		end if;

		if ir(9 downto 2)="00111100" then				-- ldsp, ldvp, ldjpc
			sel_lmux <= "101";
		end if;

									-- default 'pop'
		sel_bmux <= '1';			-- mem
		sel_mmux <= '0';			-- a
		if is_pop='0' then			-- 'push' or 'no stack change'
			sel_bmux <= '0';		-- a
			sel_mmux <= '1';		-- b
		end if;

		ena_b <= '1';
		if is_push='0' and is_pop='0' then	-- 'no stack change' (nop, wait, jbr)
			ena_b <= '0';
		end if;

		sel_rmux <= ir(1 downto 0);			-- ldsp, ldvp, ldjpc

	end if;
end process;

--
--	ex stage (MMU, mul)
--		was in former extension.vhd
--
process(clk, reset)
begin
	if (reset='1') then
		mem_in.rd <= '0';
		mem_in.wr <= '0';
		mem_in.addr_wr <= '0';
		mem_in.bc_rd <= '0';
		mem_in.stidx <= '0';
		mem_in.iaload <= '0';
		mem_in.iastore <= '0';
		mem_in.getfield <= '0';
		mem_in.putfield <= '0';
		mem_in.putref <= '0';
		mem_in.getstatic <= '0';
		mem_in.putstatic <= '0';
		mem_in.rdc <= '0';
		mem_in.rdf <= '0';
		mem_in.copy <= '0';
		mem_in.cinval <= '0';
		mem_in.atmstart <= '0';
		mem_in.atmend <= '0';
		mul_wr <= '0';
		wr_dly <= '0';


	elsif rising_edge(clk) then
		mem_in.rd <= '0';
		mem_in.wr <= '0';
		mem_in.addr_wr <= '0';
		mem_in.bc_rd <= '0';
		mem_in.stidx <= '0';
		mem_in.iaload <= '0';
		mem_in.iastore <= '0';
		mem_in.getfield <= '0';
		mem_in.putfield <= '0';
		mem_in.putref <= '0';
		mem_in.getstatic <= '0';
		mem_in.putstatic <= '0';
		mem_in.rdc <= '0';
		mem_in.rdf <= '0';
		mem_in.wrf <= '0';
		mem_in.copy <= '0';
		mem_in.cinval <= '0';
		mem_in.atmstart <= '0';
		mem_in.atmend <= '0';
		mul_wr <= '0';
		wr_dly <= '0';

		if ir(9 downto 4)="000100" then		-- a MMU or mul instruction
			wr_dly <= '1';
			case ir(MMU_WIDTH-1 downto 0) is
				when STMUL =>
					mul_wr <= '1';			-- start multiplier
				when STMWA =>
					mem_in.addr_wr <= '1';	-- store write address
				when STMRA =>
					mem_in.rd <= '1';		-- start memory or io read
				when STMWD =>
					mem_in.wr <= '1';		-- start memory or io write
				when STALD =>
					mem_in.iaload <= '1';	-- start array load
				when STAST =>
					mem_in.iastore <= '1';	-- start array store
				when STGF =>
					mem_in.getfield <= '1';	-- start getfield
				when STPF =>
					mem_in.putfield <= '1';	-- start putfield
				when STPFR =>
					mem_in.putfield <= '1'; -- start putfield reference
					mem_in.putref	<= '1';	
-- 				when STPSR =>
-- 					mem_in.putstatic <= '1'; -- start putstatic reference
-- 					mem_in.putref	<= '1';
-- 				when STASTR => 
-- 					mem_in.iastore <= '1';	-- start reference array store
-- 					mem_in.putref	<= '1';	
				when STCP =>
					mem_in.copy <= '1';		-- start copy
				when STBCR =>
					mem_in.bc_rd <= '1';	-- start bytecode read
				when STIDX =>
					mem_in.stidx <= '1';	-- store index
				when STPS =>
					mem_in.putstatic <= '1';	-- start putstatic
				when STMRAC =>
					mem_in.rdc <= '1';		-- start memory constant read
				when STMRAF =>
					mem_in.rdf <= '1';		-- start memory read through full assoc. cache
				when STMWDF =>
					mem_in.wrf <= '1';		-- start memory write through full assoc. cache
				when others =>
					null;
			end case;
		end if;

		if ir(9 downto 4)="010001" then		-- a MMU instruction, no SP change
			wr_dly <= '1';
			case ir(MMU_WIDTH-1 downto 0) is
				when STGS =>
					mem_in.getstatic <= '1';	-- start getstatic
				when cinval =>
					mem_in.cinval <= '1';		-- invalidate data cache
				when atmstart =>
					mem_in.atmstart <= '1';		-- start atomic arbiter operation
				when atmend =>
					mem_in.atmend <= '1';		-- end atomic arbiter operation
				when others =>
					null;
			end case;
		end if;

	end if;
end process;

	-- route bytcode operand from bcfetch to MMU
	mem_in.bcopd <= bcopd;

end rtl;
