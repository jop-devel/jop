--
--	lemsd.vhd
--
--	sigma delta AD converter for motor feedback
--	
--		
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--	resources on Cyclone
--
--		xx LCs, max xx MHz
--
--
--	todo: should collect all sig-del and provide one generic version.
--
--
--	2004-08-11	creation
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity lemsd is

generic (clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	dout	: out std_logic_vector(8 downto 0);

	sdi		: in std_logic;
	sdo		: out std_logic
);
end lemsd ;

architecture rtl of lemsd is

	signal clkint		: unsigned(8 downto 0);			-- 9 bit ADC
	signal val			: unsigned(8 downto 0);
	signal sd_dout		: std_logic_vector(8 downto 0);

	signal rx_d			: std_logic;
	signal serdata		: std_logic;

	signal spike		: std_logic_vector(2 downto 0);	-- sync in, filter


	signal clksd		: std_logic;
	signal prescale		: unsigned(7 downto 0);
	constant sd_clk_cnt	: integer := ((clk_freq+1000000)/2000000)-1;

begin


	dout <= sd_dout;

	sdo <= serdata;

--
--	prescaler (2 MHz clock)
--
process(clk, reset)

begin
	if (reset='1') then
		prescale <= (others => '0');
		clksd <= '0';
	elsif rising_edge(clk) then
		clksd <= '0';
		prescale <= prescale + 1;
		if prescale = sd_clk_cnt then
			prescale <= (others => '0');
			clksd <= '1';
		end if;
	end if;

end process;

--
--	sigma delta converter
--
process(clk, reset)

begin
	if (reset='1') then
		spike <= "000";
		sd_dout <= (others => '0');
		val <= (others => '0');
		clkint <= (others => '0');
		serdata <= '0';

	elsif rising_edge(clk) then

		if clksd='1' then

--
--	delay
--
			spike(0) <= sdi;
			spike(2 downto 1) <= spike(1 downto 0);
			serdata <= rx_d;		-- no inverter, using an invert. comperator
--			serdata <= not rx_d;	-- without comperator

--
--	integrate
--

			if serdata='0' then		-- 'invert' value
				val <= val+1;
			end if;

			if clkint=0 then		-- 256 us
				sd_dout <= std_logic_vector(val);
				val <= (others => '0');
			end if;

			clkint <= clkint+1;		-- free running counter

		end if;

	end if;

end process;


--
--	filter input
--
	with spike select
		rx_d <=	'0' when "000",
				'0' when "001",
				'0' when "010",
				'1' when "011",
				'0' when "100",
				'1' when "101",
				'1' when "110",
				'1' when "111",
				'X' when others;


end rtl;
