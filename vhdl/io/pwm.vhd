--
--	pwm.vhd
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

entity pwm is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	sdo		: out std_logic;
	nsdo	: out std_logic
);
end pwm;

architecture rtl of pwm is

	signal clksd		: unsigned(3 downto 0);

	signal clkint		: unsigned(7 downto 0);
	signal sd_dout		: std_logic_vector(15 downto 0);

	signal val			: unsigned(7 downto 0);

	signal serdata		: std_logic;


begin

	sdo <= serdata;
	nsdo <= not serdata;

--
--	io write processing
--
process(clk, reset, wr, addr, din)

begin
	if (reset='1') then

		val <= (others => '0');

	elsif rising_edge(clk) then

		if addr=std_logic_vector(to_unsigned(io_addr, 4)) and wr='1' then
			val <= unsigned(din(7 downto 0));
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
		serdata <= '0';

	elsif rising_edge(clk) then

		clksd <= clksd+1;

		if clksd="0000" then		-- with 20 MHz => 1.25 MHz

			if clkint<val then
				serdata <= '1';
			else
				serdata <= '0';
			end if;

			clkint <= clkint+1;		-- free running counter

		end if;
	end if;

end process;


end rtl;
