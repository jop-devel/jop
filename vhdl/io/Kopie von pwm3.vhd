--
--	pwm3.vhd
--
--	PWM DA converter
--	
--
--                   
--            100k   
--            ___    
--    sdo o--|___|--o----------o uout
--                  |
--                 ---
--                 ---  100n
--                  |
--                  |
--                 ---
--                  -
--
--		
--	Author: Martin Schoeberl	martin@good-ear.com
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

	signal clksd		: unsigned(3 downto 0);

	signal clkint		: unsigned(7 downto 0);

	signal val_a		: unsigned(7 downto 0);
	signal val_b		: unsigned(7 downto 0);
	signal val_c		: unsigned(7 downto 0);


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
			val_a <= unsigned(din(7 downto 0));
		elsif addr=std_logic_vector(to_unsigned(io_addr+1, 4)) then
			val_b <= unsigned(din(7 downto 0));
		elsif addr=std_logic_vector(to_unsigned(io_addr+2, 4)) then
			val_c <= unsigned(din(7 downto 0));
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
		clksd <= "0000";
		clkint <= (others => '0');
		sdo <= (others => '0');

	elsif rising_edge(clk) then

		clksd <= clksd+1;

		if clksd="0000" then		-- with 20 MHz => 1.25 MHz

			if clkint<val_a then
				sdo(1) <= '1';
				sdo(4) <= '0';
			else
				sdo(1) <= '0';
				sdo(4) <= '1';
			end if;

			if clkint<val_b then
				sdo(2) <= '1';
				sdo(5) <= '0';
			else
				sdo(2) <= '0';
				sdo(5) <= '1';
			end if;

			if clkint<val_c then
				sdo(3) <= '1';
				sdo(6) <= '0';
			else
				sdo(3) <= '0';
				sdo(6) <= '1';
			end if;

			clkint <= clkint+1;		-- free running counter

		end if;
	end if;

end process;


end rtl;
