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
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.numeric_std.all ;

entity decode is
generic (
	i_width		: integer := 8;		-- instruction width
	addr_width	: integer := 8;		-- address bits of internal ram (sp,...)
	ioa_width	: integer := 3		-- address bits of internal io (or 5/4)
);

port (
	clk, reset	: in std_logic;

	instr		: in std_logic_vector(i_width-1 downto 0);
	zf, nf		: in std_logic;		-- nf, eq and lt not used (only brz, brnz)
	eq, lt		: in std_logic;
	bsy			: in std_logic;		-- from mem interface

	br			: out std_logic;
	pcwait		: out std_logic;	-- no pc increment 'wait'
	jbr			: out std_logic;

	io_addr		: out std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: out std_logic;

	dir			: out std_logic_vector(addr_width-1 downto 0);

	sel_amux	: out std_logic_vector(1 downto 0);		-- a, lmux, sum, diff
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
	ena_bc		: out std_logic
);
end decode;

architecture rtl of decode is

--
--	Signals
--

--
-- intruction register, shortcut
--
--	signal ir		: std_logic_vector(i_width-1 downto 0);		-- Xilinx does not like this... instruction register
	signal ir		: std_logic_vector(7 downto 0);		-- instruction register

begin

	ir <= instr;		-- registered in fetch

	io_addr <= ir(ioa_width-1 downto 0);

--
--	branch, pcwait, jbranch
--

process(clk, reset, ir, zf)
begin
	if (reset='1') then
		br <= '0';
	elsif rising_edge(clk) then

		br <= '0';
		if((ir(7 downto 5)="010" and zf='1') or		-- bz
			(ir(7 downto 5)="011" and zf='0')) then	-- bnz
			br <= '1';
		end if;

	end if;
end process;

process(ir, bsy)
begin

	pcwait <= '0';
	if (ir="10000001") then		-- wait 
		pcwait <= bsy;
	end if;

end process;

process(ir)
begin

	jbr <= '0';
	if (ir="10000010") then		-- jbr: goto and if_xxx
		jbr <= '1';
	end if;

end process;

--
--	addr, read stage:
--		decode from ir (only logic, no register)
--
process(ir, ir(2 downto 0))
begin

-- ram wraddress and wrena are registered

	wr_ena <= '0';
	if (ir(7 downto 5)="101" or			-- 'push' instructions
		ir(7 downto 5)="110" or
		ir(7 downto 5)="111" or
		ir(7 downto 5)="001" or			-- stm
		ir(7 downto 3)="00010") then	-- st, stn

		wr_ena <= '1';
	end if;

	rd <= '0';
	if ir(7 downto 3)="11100" then		-- ld memio
		rd <= '1';
	end if;
	wr <= '0';
	if ir(7 downto 3)="00001" then		-- st memio
		wr <= '1';
	end if;

	sel_imux <= ir(1 downto 0);			-- ld opd_x

	ena_bc <= '0';

	if ir="00011010" then				-- stbc
		ena_bc <= '1';
	end if;

-- select for rd/wr address muxes

	dir <= "000" & ir(4 downto 0);

	sel_rda <= "100";					-- sp
	if (ir(7 downto 3)="11101") then	-- ld, ldn
		sel_rda <= ir(2 downto 0);
	end if;
	if (ir(7 downto 5)="101") then		-- ldm
		sel_rda <= "111";
	end if;
	if (ir(7 downto 5)="110") then		-- ldi
		sel_rda <= "111";
		dir <= "001" & ir(4 downto 0);	-- addr > 31 constants
	end if;

	sel_wra <= "100";					-- spp
	if ir(7 downto 3)="00010" then		-- st, stn
		sel_wra <= ir(2 downto 0);
	end if;
	if ir(7 downto 5)="001" then		-- stm
		sel_wra <= "111";
	end if;

-- select for sp update

	sel_smux <= "00";				-- sp = sp
	if(ir(7)='0') then			-- 'pop' instruction
		sel_smux <= "01";			-- --sp
	end if;
	if(ir(7 downto 5)="101" or	-- 'push' instruction
		ir(7 downto 5)="110" or
		ir(7 downto 5)="111") then
		sel_smux <= "10";			-- ++sp
	end if;
	if (ir="00011011") then			-- st sp
		sel_smux <= "11";			-- sp = a
	end if;
			
end process;

--
--	ex stage
--

process(clk, reset)
begin

	if (reset='1') then
		sel_amux <= "00";
		sel_bmux <= '0';
		sel_log <= "00";
		sel_shf <= "00";
		sel_lmux <= "000";
		sel_rmux <= "00";
		sel_mmux <= '0';
		ena_b <= '0';
		ena_vp <= '0';
		ena_jpc <= '0';

	elsif rising_edge(clk) then

		sel_log <= "00";
		if (ir(7 downto 2)="000000") then		-- pop, and, or, xor
			sel_log <= ir(1 downto 0);
		end if;

		sel_shf <= ir(1 downto 0);

		sel_amux <= "01";		-- lmux
		ena_vp <= '0';
		ena_jpc <= '0';

		case ir is

			when "00000000" =>				-- pop
			when "00000001" =>				-- and
			when "00000010" =>				-- or
			when "00000011" =>				-- xor
			when "00000100" =>				-- add
					sel_amux <= "10";
			when "00000101" =>				-- sub
					sel_amux <= "11";
			when "00001000" =>				-- stioa
			when "00001001" =>				-- stiod
			when "00001010" =>				-- stmra
			when "00001011" =>				-- stmwa
			when "00001100" =>				-- stmwd
			when "00001101" =>				-- stopa
			when "00001110" =>				-- stopb
			when "00010000" =>				-- st0
			when "00010001" =>				-- st1
			when "00010010" =>				-- st2
			when "00010011" =>				-- st3
			when "00010101" =>				-- st
			when "00011000" =>				-- stvp
					ena_vp <= '1';
			when "00011001" =>				-- stjpc
					ena_jpc <= '1';
			when "00011010" =>				-- stbc
			when "00011011" =>				-- stsp
			when "00011100" =>				-- ushr
			when "00011101" =>				-- shl
			when "00011110" =>				-- shr
--			when "001-----" =>				-- stm
--			when "010-----" =>				-- bz
--			when "011-----" =>				-- bnz
			when "10000000" =>				-- nop
					sel_amux <= "00";	-- a
			when "10000001" =>				-- wait
					sel_amux <= "00";	-- a
			when "10000010" =>				-- jbr
					sel_amux <= "00";	-- a
--			when "101-----" =>				-- ldm
--			when "110-----" =>				-- ldi
			when "11100001" =>				-- ldiod
			when "11100010" =>				-- ldmrd
			when "11100011" =>				-- ldmbsy
			when "11100101" =>				-- ldmul
			when "11101000" =>				-- ld0
			when "11101001" =>				-- ld1
			when "11101010" =>				-- ld2
			when "11101011" =>				-- ld3
			when "11101101" =>				-- ld
			when "11110000" =>				-- ldsp
			when "11110001" =>				-- ldvp
			when "11110010" =>				-- ldjpc
			when "11110100" =>				-- ld_opd_8u
			when "11110101" =>				-- ld_opd_8s
			when "11110110" =>				-- ld_opd_16u
			when "11110111" =>				-- ld_opd_16s
			when "11111000" =>				-- dup
				sel_amux <= "00";	-- a

			when others =>
				null;
		end case;


		sel_lmux <= "000";		-- log

		if ir(7 downto 2)="000111" then				-- ushr, shl, shr
			sel_lmux <= "001";
		end if;

		if ir(7 downto 5)="101" then				-- ldm
			sel_lmux <= "010";
		end if;
		if ir(7 downto 5)="110" then				-- ldi
			sel_lmux <= "010";
		end if;

		if ir(7 downto 3)="11101" then				-- ld, ldn
			sel_lmux <= "010";
		end if;

		if ir(7 downto 2)="111101" then				-- ld_opd_x
			sel_lmux <= "011";
		end if;

		if ir(7 downto 3)="11100" then				-- ld io
			sel_lmux <= "100";
		end if;

		if ir(7 downto 2)="111100" then				-- ldsp, ldvp, ldjpc
			sel_lmux <= "101";
		end if;

									-- default 'pop'
		sel_bmux <= '1';			-- mem
		sel_mmux <= '0';			-- a
		if (ir(7)='1') then		-- 'push' and 'no stack change'
			sel_bmux <= '0';		-- a
			sel_mmux <= '1';		-- b
		end if;

		ena_b <= '1';
		if (ir(7 downto 5)="100") then	-- 'no stack change' (nop, jbr)
			ena_b <= '0';
		end if;

		sel_rmux <= ir(1 downto 0);			-- ldsp, ldvp, ldjpc

	end if;
end process;

end rtl;
