--
--	sc_lego.vhd
--
--	Motor and sensor interface for LEGO MindStorms
--	
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--		address map:
--
--			0	motor output, sensor input
--			1	not used
--			2	not used
--			3	not used
--
--	2005-12-22	adapted for SimpCon interface
--
--	todo:
--
--

--
--	lesens
--
--	sigma delta AD converter and power switch
--	for the LEGO sensor.
--	


--	TODO: write a general sigma delta ADC
--		But this one is special as the senso gets powered!
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity lesens is

generic (clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;

	dout	: out std_logic_vector(8 downto 0);

	sp		: out std_logic;
	sdi		: in std_logic;
	sdo		: out std_logic
);
end lesens ;

architecture rtl of lesens is

	signal clkint		: unsigned(8 downto 0);			-- 9 bit ADC
	signal val			: unsigned(8 downto 0);

	signal rx_d			: std_logic;
	signal serdata		: std_logic;

	signal spike		: std_logic_vector(2 downto 0);	-- sync in, filter

	signal pow_cnt		: unsigned(2 downto 0);

	signal clksd		: std_logic;
	signal prescale		: unsigned(7 downto 0);
	constant sd_clk_cnt	: integer := ((clk_freq+1000000)/2000000)-1;

begin


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
		dout <= (others => '0');
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

--
--	integrate
--

			if serdata='0' then		-- 'invert' value
				val <= val+1;
			end if;

			if clkint=0 then		-- 256 us
				if pow_cnt=1 then
					dout <= std_logic_vector(val);
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


--
--	lego io
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity sc_lego is
generic (addr_bits : integer;
	clk_freq : integer);

port (
	clk		: in std_logic;
	reset	: in std_logic;

	-- SimpCon interface

	address		: in std_logic_vector(addr_bits-1 downto 0);
	wr_data		: in std_logic_vector(31 downto 0);
	rd, wr		: in std_logic;
	rd_data		: out std_logic_vector(31 downto 0);
	rdy_cnt		: out unsigned(1 downto 0);

	-- motor stuff

	ma_en		: out std_logic;
	ma_l1		: out std_logic;
	ma_l2		: out std_logic;
	ma_l1_sdi	: in std_logic;
	ma_l1_sdo	: out std_logic;
	ma_l2_sdi	: in std_logic;
	ma_l2_sdo	: out std_logic;

	mb_en		: out std_logic;
	mb_l1		: out std_logic;
	mb_l2		: out std_logic;

	-- sensor stuff

	s1_pow		: out std_logic;
	s1_sdi		: in std_logic;
	s1_sdo		: out std_logic

);
end sc_lego;

architecture rtl of sc_lego is

	signal m_out		: std_logic_vector(5 downto 0);
	signal sensor		: std_logic_vector(8 downto 0);

	signal xyz			: std_logic_vector(31 downto 0);

begin

	rdy_cnt <= "00";	-- no wait states

--
--	The registered MUX is all we need for a SimpCon read.
--	The read data is stored in registered rd_data.
--
process(clk, reset)
begin

	if (reset='1') then
		rd_data <= (others => '0');
	elsif rising_edge(clk) then

		if rd='1' then
			-- that's our very simple address decoder
			if address(0)='0' then
				rd_data(8 downto 0) <= sensor;
				rd_data(31 downto 9) <= (others => '0');
			else
				rd_data <= xyz;
			end if;
		end if;
	end if;

end process;


--
--	SimpCon write is very simple
--
process(clk, reset)

begin

	if (reset='1') then
		xyz <= (others => '0');

		m_out <= (others => '0');

	elsif rising_edge(clk) then

		if wr='1' then
			xyz <= wr_data;
			m_out <= wr_data(5 downto 0);
		end if;

	end if;

end process;

	-- simple motor out
	mb_en <= m_out(5);
	mb_l1 <= m_out(4);
	mb_l2 <= m_out(3);
	ma_en <= m_out(2);
	ma_l1 <= m_out(1);
	ma_l2 <= m_out(0);

	ma_l1_sdo <= '0';
	ma_l2_sdo <= '0';

	-- sensor adc
	cmp_sens: entity work.lesens generic map (
			clk_freq => clk_freq
		)
		port map(
			clk => clk,
			reset => reset,

			dout => sensor,

			sp => s1_pow,
			sdi => s1_sdi,
			sdo => s1_sdo
		);

end rtl;
