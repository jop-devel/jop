--
--	stack.vhd
--
--	Stack/Alu for JOP3
--
--	resources on ACEX1K30-3
--
--	only stack:
--		268 LCs, max 4x.x MHz with hirarchy flatten results in an adder with inverter for sub
--		373 LCs, max 52.6 MHz (with preserve hirarchy to generate add and sub with amux!)
--	with ram and register:
--		464 LCs, max 46.3 MHz (with preserve hirarchy to generate add and sub with amux!)
--					crit. path vp - ram
-- befor 9.8.2001
--		515 LCs, max 51.0 MHz (with preserve hirarchy to generate add and sub with amux!)
--
--		669 LCs, max 52.0 MHz (with preserve hirarchy to generate add and sub with amux!)
--								and cutoff mem wr/rd path
--		654 LCs, 37.7 MHz	24.3.2002
--		883 LCs, 35.8 MHz	24.3.2002 with shift.
--
--
--	todo:
--
--
--	2001-06-30	first version (adapted from alu.vhd)
--	2001-07-18	components add, sub in own file for Xilinx
--	2001-10-28	ldjpc, stjpc
--	2001-10-31	init cp and vp with 0
--	2001-12-04	cp removed
--	2001-12-06	sp is 0 after reset, must be set in sw
--	2001-12-07	removed imm. values
--	2002-03-24	barrel shifter
--	2003-02-12	added mux for 8 and 16 bit unsigned bytecode operand
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity stack is

generic (
	width		: integer := 32;	-- one data word
	addr_width	: integer := 8;		-- address bits of internal ram (sp,...)
	jpc_width	: integer := 10		-- address bits of java byte code pc
);
port (
	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);
	dir			: in std_logic_vector(addr_width-1 downto 0);
	opd			: in std_logic_vector(15 downto 0);		-- index for vp load opd
	jpc			: in std_logic_vector(jpc_width-1 downto 0);	-- jpc read

	sel_amux	: in std_logic_vector(1 downto 0);		-- a, lmux, sum, diff
	sel_bmux	: in std_logic;							-- 0..a, 1..mem
	sel_log		: in std_logic_vector(1 downto 0);		-- pop/st, and, or, xor
	sel_shf		: in std_logic_vector(1 downto 0);		-- sr, sl, sra, (sr)
	sel_lmux	: in std_logic_vector(2 downto 0);		-- log, shift, mem, din, reg
	sel_imux	: in std_logic_vector(1 downto 0);		-- java opds
	sel_rmux	: in std_logic_vector(1 downto 0);		-- sp, vp, jpc
	sel_smux	: in std_logic_vector(1 downto 0);		-- sp, a, sp-1, sp+1

	sel_mmux	: in std_logic;							-- 0..a, 1..b
	sel_rda		: in std_logic_vector(2 downto 0);		-- 
	sel_wra		: in std_logic_vector(2 downto 0);		-- 

	wr_ena		: in std_logic;

	ena_b		: in std_logic;
	ena_vp		: in std_logic;

	zf			: out std_logic;
	nf			: out std_logic;
	eq			: out std_logic;
	lt			: out std_logic;
	dout		: out std_logic_vector(width-1 downto 0)
);
end stack;

architecture rtl of stack is

component shift is
generic (width : integer);
port (
	din			: in std_logic_vector(width-1 downto 0);
	off			: in std_logic_vector(4 downto 0);
	shtyp		: in std_logic_vector(1 downto 0);
	dout		: out std_logic_vector(width-1 downto 0)
);
end component shift;

--
--	ram component (use technology specific vhdl-file (aram/xram))
--
--	registered  and delayed wraddress, wren
--	registered din
--	registered rdaddress
--	unregistered dout
--
--		=> read during write on same address
--
component ram is
generic (width : integer; addr_width : integer);
port (
	data		: in std_logic_vector(width-1 downto 0);
	wraddress	: in std_logic_vector(addr_width-1 downto 0);
	rdaddress	: in std_logic_vector(addr_width-1 downto 0);
	wren		: in std_logic;
	clock		: in std_logic;

	q			: out std_logic_vector(width-1 downto 0)
);
end component;

	signal a, b			: std_logic_vector(width-1 downto 0);
	signal ram_dout		: std_logic_vector(width-1 downto 0);

	signal sp, spp, spm	: std_logic_vector(addr_width-1 downto 0);
	signal vp0, vp1, vp2, vp3
						: std_logic_vector(addr_width-1 downto 0);

	signal sum, diff	: std_logic_vector(width-1 downto 0);
	signal sout			: std_logic_vector(width-1 downto 0);
	signal log			: std_logic_vector(width-1 downto 0);
	signal immval		: std_logic_vector(width-1 downto 0);
	signal opddly		: std_logic_vector(15 downto 0);

	signal amux		: std_logic_vector(width-1 downto 0);
	signal lmux		: std_logic_vector(width-1 downto 0);
	signal imux		: std_logic_vector(width-1 downto 0);
	signal mmux		: std_logic_vector(width-1 downto 0);

	signal rmux		: std_logic_vector(jpc_width-1 downto 0);
	signal smux		: std_logic_vector(addr_width-1 downto 0);
	signal vpadd	: std_logic_vector(addr_width-1 downto 0);
	signal wraddr	: std_logic_vector(addr_width-1 downto 0);
	signal rdaddr	: std_logic_vector(addr_width-1 downto 0);

begin

	cmp_shf: shift generic map (width) port map (b, a(4 downto 0), sel_shf, sout);

	cmp_ram: ram generic map(width, addr_width)
			port map(mmux, wraddr, rdaddr, wr_ena, clk, ram_dout);

	sum <= std_logic_vector(signed(a) + signed(b));
	diff <= std_logic_vector(signed(b) - signed(a));

--
--	mux for stack register, alu
--
process(ram_dout, opddly, immval, sout, din, lmux, rmux, sp, vp0, jpc, sum, diff, log, a, b,
		sel_log, sel_shf, sel_rmux, sel_lmux, sel_imux, sel_mmux, sel_amux)

begin

	case sel_log is
		when "00" =>
			log <= b;
		when "01" =>
			log <= a and b;
		when "10" =>
			log <= a or b;
		when "11" =>
			log <= a xor b;
		when others =>
			null;
	end case;

	case sel_rmux is
		when "00" =>
			rmux <= "00" & sp;
		when "01" =>
			rmux <= "00" & vp0;
		when others =>
			rmux <= jpc;
	end case;

	case sel_lmux(2 downto 0) is
		when "000" =>
			lmux <= log;
		when "001" =>
			lmux <= sout;
		when "010" =>
			lmux <= ram_dout;
		when "011" =>
			lmux <= immval;
		when "100" =>
			lmux <= din;
		when others =>
			lmux <= std_logic_vector(to_signed(to_integer(unsigned(rmux)), width));
	end case;

-- TODO: immval could be 16 bit and expand in lmux
	case sel_imux is
		when "00" =>
			imux <= "000000000000000000000000" & opddly(7 downto 0);
		when "01" =>
			imux <= std_logic_vector(to_signed(to_integer(signed(opddly(7 downto 0))), width));
		when "10" =>
			imux <= "0000000000000000" & opddly;
		when others =>
			imux <= std_logic_vector(to_signed(to_integer(signed(opddly)), width));
	end case;

	if (sel_mmux='0') then
		mmux <= a;
	else
		mmux <= b;
	end if;

	case sel_amux is
		when "00" =>
			amux <= a;
		when "01" =>
			amux <= lmux;
		when "10" =>
			amux <= sum;
		when "11" =>
			amux <= diff;
		when others =>
			null;
	end case;

--	if (a = (a'range => '0'))  then		-- Xilinx has problems
	if (a=std_logic_vector(to_unsigned(0, width))) then
		zf <= '1';
	else
		zf <= '0';
	end if;
	nf <= a(width-1);

	if (a=b) then
		eq <= '1';
	else
		eq <= '0';
	end if;
	lt <= diff(width-1);

end process;

process(clk, sel_amux, ena_b) begin
	if rising_edge(clk) then

		a <= amux;

		if (ena_b = '1') then
			if sel_bmux = '0' then
				b <= a;
			else
				b <= ram_dout;
			end if;
		end if;

	end if;
end process;

	dout <= a;

--
--	stack pointer and vp register
--
process(a, sp, spm, spp, sel_smux)

begin

	case sel_smux is
		when "00" =>
			smux <= sp;
		when "01" =>
			smux <= spm;
		when "10" =>
			smux <= spp;
		when "11" =>
			smux <= a(addr_width-1 downto 0);
		when others =>
			null;
	end case;

end process;


--
--	address mux for ram
--
process(sp, spp, vp0, vp1, vp2, vp3, vpadd, dir, sel_rda, sel_wra)

begin


	case sel_rda is
		when "000" =>
			rdaddr <= vp0;
		when "001" =>
			rdaddr <= vp1;
		when "010" =>
			rdaddr <= vp2;
		when "011" =>
			rdaddr <= vp3;
		when "100" =>
			rdaddr <= sp;
		when "101" =>
			rdaddr <= vpadd;
		when others =>
			rdaddr <= dir;
	end case;

	case sel_wra is
		when "000" =>
			wraddr <= vp0;
		when "001" =>
			wraddr <= vp1;
		when "010" =>
			wraddr <= vp2;
		when "011" =>
			wraddr <= vp3;
		when "100" =>
			wraddr <= spp;
		when "101" =>
			wraddr <= vpadd;
		when others =>
			wraddr <= dir;
	end case;

end process;

process(clk, reset)

begin
	if (reset='1') then
--
--	sp is 'strange' (0xfe) after reset, after stsp all is ok
--
		sp <= std_logic_vector(to_unsigned(0, addr_width));		
		spp <= std_logic_vector(to_unsigned(0, addr_width));	-- just for the compiler
		spm <= std_logic_vector(to_unsigned(0, addr_width));	-- just for the compiler
		vp0 <= std_logic_vector(to_unsigned(0, addr_width));
		vp1 <= std_logic_vector(to_unsigned(0, addr_width));
		vp2 <= std_logic_vector(to_unsigned(0, addr_width));
		vp3 <= std_logic_vector(to_unsigned(0, addr_width));
		vpadd <= std_logic_vector(to_unsigned(0, addr_width));	-- just for the compiler
		immval <= std_logic_vector(to_unsigned(0, width));		-- just for the compiler
		opddly <= std_logic_vector(to_unsigned(0, 16));			-- just for the compiler
	elsif rising_edge(clk) then
		spp <= std_logic_vector(unsigned(smux) + 1);
		spm <= std_logic_vector(unsigned(smux) - 1);
		sp <= smux;
		if (ena_vp = '1') then
			vp0 <= a(addr_width-1 downto 0);
			vp1 <= std_logic_vector(unsigned(a(addr_width-1 downto 0)) + 1);
			vp2 <= std_logic_vector(unsigned(a(addr_width-1 downto 0)) + 2);
			vp3 <= std_logic_vector(unsigned(a(addr_width-1 downto 0)) + 3);
		end if;
		vpadd <= std_logic_vector(unsigned(vp0) + unsigned(opd(7 downto 0)));
		opddly <= opd;
		immval <= imux;
	end if;
end process;

end rtl;
