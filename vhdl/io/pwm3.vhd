--
--	pwm3.vhd
--
--	PWM 6 Bit DA converter plus inverted output
--	with 'recovery' time betwenn low-side on and
--	high side on.
--	
--		
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--	resources on ACEX1K30-3
--
--		xx LCs, max xx MHz
--
--
--	todo:
--
--
--	2003-09-27	creation
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity pwm3 is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	sdo		: out std_logic_vector(6 downto 1)
);
end pwm3;

architecture rtl of pwm3 is

	signal clksd		: unsigned(5 downto 0);

	signal clkint		: unsigned(6 downto 0);

	signal val_a		: unsigned(6 downto 0);
	signal val_b		: unsigned(6 downto 0);
	signal val_c		: unsigned(6 downto 0);

	signal sdo_a		: std_logic;
	signal sdo_b		: std_logic;
	signal sdo_c		: std_logic;
	signal dly_a		: std_logic_vector(1 downto 0);
	signal dly_b		: std_logic_vector(1 downto 0);
	signal dly_c		: std_logic_vector(1 downto 0);
	signal dly_na		: std_logic_vector(1 downto 0);
	signal dly_nb		: std_logic_vector(1 downto 0);
	signal dly_nc		: std_logic_vector(1 downto 0);


begin

--
--	io write processing
--
process(clk, reset, wr, addr, din)

begin
	if (reset='1') then

		val_a <= (others => '0');
		val_b <= (others => '0');
		val_c <= (others => '0');

	elsif rising_edge(clk) then

		if addr=std_logic_vector(to_unsigned(io_addr, 4)) and wr='1' then
			val_a <= unsigned(din(6 downto 0));
		elsif addr=std_logic_vector(to_unsigned(io_addr+1, 4)) and wr='1' then
			val_b <= unsigned(din(6 downto 0));
		elsif addr=std_logic_vector(to_unsigned(io_addr+2, 4)) and wr='1' then
			val_c <= unsigned(din(6 downto 0));
		end if;

	end if;
end process;


	dout <= (others => 'Z');

--
--	sigma delta converter
--
process(clk, reset)

begin
	if (reset='1') then
		clksd <= "000000";
		clkint <= (others => '0');
		sdo <= (others => '0');

	elsif rising_edge(clk) then

		clksd <= clksd+1;

		if clksd="00000" then		-- with 20 MHz => 625 kHz

			if clkint<val_a then
				sdo_a <= '1';
			else
				sdo_a <= '0';
			end if;

			if clkint<val_b then
				sdo_b <= '1';
			else
				sdo_b <= '0';
			end if;

			if clkint<val_c then
				sdo_c <= '1';
			else
				sdo_c <= '0';
			end if;

			clkint <= clkint+1;		-- free running counter

			dly_a(0) <= sdo_a;
			dly_b(0) <= sdo_b;
			dly_c(0) <= sdo_c;
			dly_na(0) <= not sdo_a;
			dly_nb(0) <= not sdo_b;
			dly_nc(0) <= not sdo_c;

			dly_a(1) <= dly_a(0);
			dly_b(1) <= dly_b(0);
			dly_c(1) <= dly_c(0);
			dly_na(1) <= dly_na(0);
			dly_nb(1) <= dly_nb(0);
			dly_nc(1) <= dly_nc(0);

			if sdo_a='1' and dly_a(1)='1' then
				sdo(1) <= '1';
			else
				sdo(1) <= '0';
			end if;
			if sdo_b='1' and dly_b(1)='1' then
				sdo(2) <= '1';
			else
				sdo(2) <= '0';
			end if;
			if sdo_c='1' and dly_c(1)='1' then
				sdo(3) <= '1';
			else
				sdo(3) <= '0';
			end if;

			if sdo_a='0' and dly_na(1)='1' then
				sdo(4) <= '1';
			else
				sdo(4) <= '0';
			end if;
			if sdo_b='0' and dly_nb(1)='1' then
				sdo(5) <= '1';
			else
				sdo(5) <= '0';
			end if;
			if sdo_c='0' and dly_nc(1)='1' then
				sdo(6) <= '1';
			else
				sdo(6) <= '0';
			end if;

		end if;
	end if;

end process;


end rtl;
