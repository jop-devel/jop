--
--	bcfetch.vhd
--
--	Java bc fetch and address translation for JVM
--
--	resources on ACEX1K30-3
--
--		xxx LCs, max ca. xx MHz
--
--	todo:
--
--	2001-11-16	split from fetch.vhd, register jpaddr instead of jinstr
--	2001-12-06	unregistered!!! jpaddr, jbr registered (moved from decode)
--	2001-12-07	removed mux befor jbc ram, jbr unregistered selects addr. for jpc
--				decode goto and if_xxx from jinstr
--	2002-03-24	autoincrement of jpc on bc_wr
--	2002-10-21	added if(non)null
--	2003-02-22	registered jbc ram
--	2003-08-14	move wr-addr load and autoincrement to ajbc.vhd (now 32 bit interface)
--	2003-08-15	interrupt handling
--
--	TODO:	use 'running' bit and generate jbr here!
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity bcfetch is

generic (
	jpc_width	: integer;		-- address bits of java byte code pc
	pc_width	: integer		-- address bits of internal instruction rom
);
port (
	clk, reset	: in std_logic;

	jpc_out		: out std_logic_vector(jpc_width-1 downto 0);	-- jpc read
	din			: in std_logic_vector(31 downto 0);				-- A from stack
	jpc_wr		: in std_logic;
	bc_wr		: in std_logic;

	jfetch		: in std_logic;
	jopdfetch	: in std_logic;

	zf, nf		: in std_logic;
	eq, lt		: in std_logic;

	jbr			: in std_logic;

	irq			: in std_logic;			-- interrupt request (positiv edge sensitive)
	irq_ena		: in std_logic;			-- interrupt enable (pendig int is fired on ena)

	jpaddr		: out std_logic_vector(pc_width-1 downto 0);	-- address for JVM
	opd			: out std_logic_vector(15 downto 0)				-- operands
);
end bcfetch;

architecture rtl of bcfetch is

--
--	jbc component (use technology specific vhdl-file (ajbc/xjbc))
--
--	dual port ram
--	wraddr and wrena registered and delayed
--	rdaddr is registered
--	indata registered
--	outdata is unregistered
--
component jbc is
generic (width : integer; addr_width : integer);
port (
	data		: in std_logic_vector(31 downto 0);
	rdaddress	: in std_logic_vector(jpc_width-1 downto 0);
	wr_addr		: in std_logic;									-- load start address (=jpc)
	wren		: in std_logic;
	clock		: in std_logic;

	q			: out std_logic_vector(7 downto 0)
);
end component;
--
--	jtbl component (generated vhdl file from Jopa!)
--
--	logic rom (unregistered)
--
component jtbl is
port (
	bcode	: in std_logic_vector(7 downto 0);
	q		: out std_logic_vector(pc_width-1 downto 0)
);
end component;


	signal jbc_mux	: std_logic_vector(jpc_width-1 downto 0);
	signal jbc_q	: std_logic_vector(7 downto 0);

	signal jpc		: std_logic_vector(jpc_width-1 downto 0);
	signal jpc_br	: std_logic_vector(jpc_width-1 downto 0);

	signal jinstr	: std_logic_vector(7 downto 0);
	signal tp		: std_logic_vector(3 downto 0);
	signal jmp		: std_logic;

	signal jopd		: std_logic_vector(15 downto 0);

--
--	signals for interrupt handling
--
	signal irq_gate		: std_logic;
	signal irq_dly		: std_logic;
	signal trig			: std_logic;
	signal int_pend		: std_logic;
	signal sys_int		: std_logic;

	signal jbc_q_mux	: std_logic_vector(7 downto 0);

begin

--
--	interrupt processing at bytecode fetch level
--
process(clk, reset) begin

	if (reset='1') then
		irq_dly <= '0';
		int_pend <= '0';
	elsif rising_edge(clk) then

		irq_dly <= irq_gate;
		if trig='1' then
			int_pend <= '1';
		elsif sys_int='1' or irq_ena='0' then
			int_pend <= '0';
		end if;
	end if;

end process;

	irq_gate <= irq and irq_ena;
	trig <= irq_gate and not irq_dly;
	sys_int <= int_pend and jfetch;

--
--	bytecode mux on interrupt
--		jpc is one too high after generating sys_int
--		this is corrected in jvm.asm
--
process(sys_int, jbc_q) begin
	jbc_q_mux <= jbc_q;
	if sys_int='1' then
		jbc_q_mux <= "11110000";			-- int bytecode
	end if;
end process;

--
--	java byte code fetch and branch
--

	cmp_jtbl: jtbl port map(jbc_q_mux, jpaddr);
	cmp_jbc: jbc generic map (8, jpc_width) port map(din, jbc_mux, jpc_wr, bc_wr, clk, jbc_q);


--
--	decode if and goto byte codes
--
process(clk, jinstr) begin

	if rising_edge(clk) then
		case jinstr is

--			when "10011001" => tp <= "1001";	-- ifeq
--			when "10011010" => tp <= "1010";	-- ifne
--			when "10011011" => tp <= "1011";	-- iflt
--			when "10011100" => tp <= "1100";	-- ifge
--			when "10011101" => tp <= "1101";	-- ifgt
--			when "10011110" => tp <= "1110";	-- ifle

--			when "10011111" => tp <= "1111";	-- if_icmpeq
--			when "10100000" => tp <= "0000";	-- if_icmpne
--			when "10100001" => tp <= "0001";	-- if_icmplt
--			when "10100010" => tp <= "0010";	-- if_icmpge
--			when "10100011" => tp <= "0011";	-- if_icmpgt
--			when "10100100" => tp <= "0100";	-- if_icmple

			when "10100101" => tp <= "1111";	-- if_acmpeq
			when "10100110" => tp <= "0000";	-- if_acmpne
--			when "10100111" => tp <= "0111";	-- goto

			when "11000110" => tp <= "1001";	-- ifnull
			when "11000111" => tp <= "1010";	-- ifnonnull

			when others => tp <= jinstr(3 downto 0);
		end case;
	end if;

end process;

process(tp, jbr, zf, nf, eq, lt)
begin

	jmp <= '0';
	if (jbr='1') then
		case tp is
			when "1001" =>			-- ifeq, ifnull
				if (zf='1') then
					jmp <= '1';
				end if;
			when "1010" =>			-- ifne, ifnonnull
				if (zf='0') then
					jmp <= '1';
				end if;
			when "1011" =>			-- iflt
				if (nf='1') then
					jmp <= '1';
				end if;
			when "1100" =>			-- ifge
				if (nf='0') then
					jmp <= '1';
				end if;
			when "1101" =>			-- ifgt
				if (zf='0' and nf='0') then
					jmp <= '1';
				end if;
			when "1110" =>			-- ifle
				if (zf='1' or nf='1') then
					jmp <= '1';
				end if;

			when "1111" =>			-- if_icmpeq, if_acmpeq
				if (eq='1') then
					jmp <= '1';
				end if;
			when "0000" =>			-- if_icmpne, if_acmpne
				if (eq='0') then
					jmp <= '1';
				end if;
			when "0001" =>			-- if_icmplt
				if (lt='1') then
					jmp <= '1';
				end if;
			when "0010" =>			-- if_icmpge
				if (lt='0') then
					jmp <= '1';
				end if;
			when "0011" =>			-- if_icmpgt
				if (eq='0' and lt='0') then
					jmp <= '1';
				end if;
			when "0100" =>			-- if_icmple
				if (eq='1' or lt='1') then
					jmp <= '1';
				end if;

			when "0111" =>			-- goto
				jmp <= '1';

			when others =>
				null;
		end case;
	end if;

end process;

--
--	jbc read address and jpc mux (is registered in ram)
--
process(din, jpc, jpc_br, jopd, jpc_wr, jfetch, jopdfetch, jmp)

begin

	if (jpc_wr='1') then
		jbc_mux <= din(jpc_width-1 downto 0);
	elsif (jmp='1') then
		jbc_mux <= std_logic_vector(unsigned(jpc_br) +
			unsigned(jopd(jpc_width-1 downto 0)));
	elsif (jfetch='1' or jopdfetch='1') then
		jbc_mux <= std_logic_vector(unsigned(jpc) + 1);
	else
		jbc_mux <= jpc;
	end if;

end process;

--
--	use same read address mux for jpc
--
process(clk, reset, jbc_mux)

begin
	if (reset='1') then
		jpc <= std_logic_vector(to_unsigned(0, jpc_width));
	elsif rising_edge(clk) then
		jpc <= jbc_mux;
	end if;
end process;

	jpc_out <= jpc;



process(clk, jpc, jfetch)
begin
	if rising_edge(clk) then
		if (jfetch='1') then
			jpc_br <= jpc;		-- save start address of instruction for branch
			jinstr <= jbc_q;
		end if;

	end if;
end process;

process(clk, reset, jbc_q, jopdfetch)

begin
	if (reset='1') then
		jopd <= (others => '0');
	elsif rising_edge(clk) then
		jopd(7 downto 0) <= jbc_q;
		if (jopdfetch='1') then
			jopd(15 downto 8) <= jopd(7 downto 0);
		end if;
	end if;
end process;

	opd <= jopd;

end rtl;

