--
--	iadc.vhd
--
--	sigma delta AD converter for I measure
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
--	2002-02-23	first working version
--	2002-06-09	adapted for current measure
--	2002-06-21	3 channels
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity iadc is

generic (clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;

	dout	: out std_logic_vector(31 downto 0);

	sdi		: in std_logic_vector(3 downto 1);
	sdo		: out std_logic_vector(3 downto 1)
);
end iadc ;

architecture rtl of iadc is

	signal sd_clk		: std_logic;

	signal clkint		: unsigned(10 downto 0);
	signal val_a		: unsigned(9 downto 0);
	signal val_b		: unsigned(9 downto 0);
	signal val_c		: unsigned(9 downto 0);

	signal rx_a			: std_logic;
	signal rx_b			: std_logic;
	signal rx_c			: std_logic;
	signal ser_a		: std_logic;
	signal ser_b		: std_logic;
	signal ser_c		: std_logic;

	signal spike_a		: std_logic_vector(2 downto 0);	-- sync in, filter
	signal spike_b		: std_logic_vector(2 downto 0);	-- sync in, filter
	signal spike_c		: std_logic_vector(2 downto 0);	-- sync in, filter

	constant sdclk_cnt	: integer := (clk_freq/(50*1024))-1;	-- 1024 samples in 20 ms

begin

	sdo(1) <= ser_a;
	sdo(2) <= ser_b;
	sdo(3) <= ser_c;

--
--	sigdel clock
--
process(clk, reset)

	variable sddiv		: integer range 0 to sdclk_cnt;

begin
	if (reset='1') then
		sddiv := 0;
		sd_clk <= '0';

	elsif rising_edge(clk) then

		if (sddiv=sdclk_cnt) then		-- 16 x serial clock
			sddiv := 0;
			sd_clk <= '1';
		else
			sddiv := sddiv + 1;
			sd_clk <= '0';
		end if;


	end if;

end process;

process(clk, reset)

begin
	if (reset='1') then
		spike_a <= "000";
		spike_b <= "000";
		spike_c <= "000";
		dout <= (others => '0');
		val_a <= (others => '0');
		val_b <= (others => '0');
		val_c <= (others => '0');
		clkint <= to_unsigned(1024, 11);
		ser_a <= '0';
		ser_b <= '0';
		ser_c <= '0';

	elsif rising_edge(clk) then

		if sd_clk='1' then			-- at 50*1024 Hz

--
--	delay
--
			spike_a(0) <= sdi(1);
			spike_b(0) <= sdi(2);
			spike_c(0) <= sdi(3);
			spike_a(2 downto 1) <= spike_a(1 downto 0);
			spike_b(2 downto 1) <= spike_b(1 downto 0);
			spike_c(2 downto 1) <= spike_c(1 downto 0);

			ser_a <= rx_a;		-- no inverter, using an invert. comperator
			ser_b <= rx_b;
			ser_c <= rx_c;

--
--	integrate
--


			if clkint=0 then		-- 20 ms
				dout <= "00" & std_logic_vector(val_c) & std_logic_vector(val_b) & std_logic_vector(val_a);
				val_a <= (others => '0');
				val_b <= (others => '0');
				val_c <= (others => '0');
				clkint <= to_unsigned(1024, 11);
			else
				if ser_a='0' then		-- 'invert' val_aue
					val_a <= val_a+1;
				end if;
				if ser_b='0' then		-- 'invert' val_aue
					val_b <= val_b+1;
				end if;
				if ser_c='0' then		-- 'invert' val_aue
					val_c <= val_c+1;
				end if;
				clkint <= clkint-1;
			end if;

		end if;
	end if;

end process;


--
--	filter input
--
	with spike_a select
		rx_a <=	'0' when "000",
				'0' when "001",
				'0' when "010",
				'1' when "011",
				'0' when "100",
				'1' when "101",
				'1' when "110",
				'1' when "111",
				'X' when others;

	with spike_b select
		rx_b <=	'0' when "000",
				'0' when "001",
				'0' when "010",
				'1' when "011",
				'0' when "100",
				'1' when "101",
				'1' when "110",
				'1' when "111",
				'X' when others;

	with spike_c select
		rx_c <=	'0' when "000",
				'0' when "001",
				'0' when "010",
				'1' when "011",
				'0' when "100",
				'1' when "101",
				'1' when "110",
				'1' when "111",
				'X' when others;


end rtl;
