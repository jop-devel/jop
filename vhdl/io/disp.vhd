--
--	disp.vhd
--
--	8-N-1 serial interface for Noritake display
--	
--	Author: Martin Schoeberl	martin@good-ear.com
--
--
--	resources on ACEX1K30-3
--
--		xx LCs, max xx MHz
--
--	offset 0:	rd not bsy, wr reset display
--	offset 1:	wr display data
--
--	2003-06-21	first working version
--	2003-07-05	new IO standard
--	2003-09-12	one more stop bit (datasheet talks about parity bit 'non parity'.
--				Use ncts and tdrf for handshake with SW (paranoid).
--				However, the display does NOT deliver a correct busy signal.
--				Delay (min. 3 ms) is neccessary in sw!
--	2003-09-19	sync ncts in!!! Thats the solution to above problem.
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity disp is

generic (io_addr : integer; clk_freq : integer;
	baud_rate : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	resout	: out std_logic;
	txd		: out std_logic;
	ncts	: in std_logic
);
end disp ;

architecture rtl of disp is

	signal ua_wr, tdre			: std_logic;

	type disp_tx_state_type		is (s0, s1);
	signal disp_tx_state 		: disp_tx_state_type;

	type disp_tdr_state_type	is (s0, s1);
	signal disp_tdr_state 		: disp_tdr_state_type;

	signal tdr			: std_logic_vector(7 downto 0); -- tx buffer
	signal tsr			: std_logic_vector(9 downto 0); -- tx shift register
	signal tdrf			: std_logic;					-- tdr has valid data
	signal tdr_rd		: std_logic;					-- tdr was read

	signal tx_clk		: std_logic;

	constant clk16_cnt	: integer := (clk_freq/baud_rate+8)/16-1;

	signal ncts_buf		: std_logic_vector(2 downto 0);	-- sync in

begin

--
--	io handling
--
process(addr, tdre)

begin
	if addr=std_logic_vector(to_unsigned(io_addr, 4)) then
		dout <= std_logic_vector(to_unsigned(0, 31)) & tdre;
	else
		dout <= (others => 'Z');
	end if;

end process;


process(wr, addr)

begin
	ua_wr <= '0';
	if addr=std_logic_vector(to_unsigned(io_addr+1, 4))
		and wr='1' then
		ua_wr <= '1';
	end if;
end process;

process(clk, reset, wr, addr)

begin
	if (reset='1') then

		resout <= '0';

	elsif rising_edge(clk) then

		if addr=std_logic_vector(to_unsigned(io_addr, 4))
			and wr='1' then
			resout <= din(0);
		end if;

	end if;
end process;

--
--
--	serial clock
--
process(clk, reset)

	variable clk16		: integer range 0 to clk16_cnt;
	variable clktx		: unsigned(3 downto 0);

begin
	if (reset='1') then
		clk16 := 0;
		clktx := "0000";
		tx_clk <= '0';

	elsif rising_edge(clk) then

		if (clk16=clk16_cnt) then		-- 16 x serial clock
			clk16 := 0;
--
--	tx clock
--
			clktx := clktx + 1;
			if (clktx="0000") then
				tx_clk <= '1';
			else
				tx_clk <= '0';
			end if;
		else
			clk16 := clk16 + 1;
			tx_clk <= '0';
		end if;


	end if;

end process;

--
--	state machine for tdr
--
process(clk, reset)

begin

	if (reset='1') then
		disp_tdr_state <= s0;
		tdrf <= '0';
		tdr <= "00000000";
	elsif rising_edge(clk) then

		case disp_tdr_state is

			when s0 =>
				if (ua_wr='1') then
					tdr <= din(7 downto 0);
					tdrf <= '1';
					disp_tdr_state <= s1;
				end if;

			when s1 =>
				if (tdr_rd='1') then
					tdrf <= '0';
					disp_tdr_state <= s0;
				end if;

		end case;
	end if;

end process;


--
--	state machine for actual shift out
--
process(clk, reset)

	variable i : integer range 0 to 15;

begin

	if (reset='1') then
		disp_tx_state <= s0;
		tsr <= "1111111111";
		tdr_rd <= '0';
		ncts_buf <= "111";

	elsif rising_edge(clk) then

		ncts_buf(0) <= ncts;
		ncts_buf(2 downto 1) <= ncts_buf(1 downto 0);

		case disp_tx_state is

			when s0 =>
				i := 0;
				if (tdrf='1' and ncts_buf(2)='0') then
					disp_tx_state <= s1;
					tsr <= tdr & '0' & '1';
					tdr_rd <= '1';
				end if;

			when s1 =>
				tdr_rd <= '0';
				if (tx_clk='1') then
					tsr(9) <= '1';
					tsr(8 downto 0) <= tsr(9 downto 1);
					i := i+1;
					if (i=11) then
						disp_tx_state <= s0;
					end if;
				end if;
				
		end case;
	end if;

end process;

	txd <= not tsr(0);		-- inverted with digital transistor
	tdre <= not tdrf;

end rtl;
