--
--	lesens.vhd
--
--	sigma delta AD converter and power switch
--	for the LEGO sensor.
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
--	todo:
--
--
--	2004-08-09	creation
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity lesens is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	sp		: out std_logic;
	sdi		: in std_logic;
	sdo		: out std_logic
);
end lesens ;

architecture rtl of lesens is

	signal clkint		: unsigned(8 downto 0);			-- 9 bit ADC
	signal val			: unsigned(8 downto 0);
	signal sd_dout		: std_logic_vector(8 downto 0);

	signal rx_d			: std_logic;
	signal serdata		: std_logic;

	signal spike		: std_logic_vector(2 downto 0);	-- sync in, filter

	signal pow_cnt		: unsigned(2 downto 0);

	signal clksd		: std_logic;
	signal prescale		: unsigned(7 downto 0);
	constant sd_clk_cnt	: integer := ((clk_freq+1000000)/2000000)-1;

begin


process(addr, rd, sd_dout)

begin
	if addr=std_logic_vector(to_unsigned(io_addr, 4)) then
		dout <= std_logic_vector(to_unsigned(0, 23)) & sd_dout;
	else
		dout <= (others => 'Z');
	end if;

end process;

--
--	power switch for sensor
--
process(pow_cnt)

begin
	sp <= '1';
	if pow_cnt=0 or pow_cnt=1 then
		sp <= '0';
	end if;
end process;

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
		pow_cnt <= (others => '0');

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
				if pow_cnt=1 then
					sd_dout <= std_logic_vector(val);
				end if;
				val <= (others => '0');
				pow_cnt <= pow_cnt + 1;
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
