--
--	sigdel_bb.vhd
--
--	sigma delta AD converter for BB temperature measure.
--	
--	without external comperator:
--		input threshhold of Acex is used as comperator
--		(not very exact but only 3 external components)
--
--
--            100k
--            ___
--    sdo o--|___|--+
--                  |
--            100k  |
--            ___   |
--    uin o--|___|--o----------o sdi
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
--	2002-02-23	first working version
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity sigdel is

port (
	clk		: in std_logic;
	reset	: in std_logic;

	dout	: out std_logic_vector(15 downto 0);

	sdi		: in std_logic;
	sdo		: out std_logic
);
end sigdel ;

architecture rtl of sigdel is

	signal clksd		: unsigned(3 downto 0);

	signal clkint		: unsigned(15 downto 0);
	signal val			: unsigned(15 downto 0);

	signal rx_d			: std_logic;
	signal serdata		: std_logic;

	signal spike		: std_logic_vector(2 downto 0);	-- sync in, filter

begin

	sdo <= serdata;


process(clk, reset)

begin
	if (reset='1') then
		clksd <= "0000";
		spike <= "000";
		dout <= (others => '0');
		val <= (others => '0');
		clkint <= to_unsigned(460*100, 16);
		serdata <= '0';

	elsif rising_edge(clk) then

		clksd <= clksd+1;

		if clksd="0000" then		-- with 7.4xx MHz => 460800 Hz

--
--	delay
--
			spike(0) <= sdi;
			spike(2 downto 1) <= spike(1 downto 0);
--			serdata <= rx_d;		-- no inverter, using an invert. comperator
			serdata <= not rx_d;	-- without comperator

--
--	integrate
--

			if serdata='0' then		-- 'invert' value
				val <= val+1;
			end if;

			if clkint=0 then		-- one ms (0.99957)
				dout <= std_logic_vector(val);
				val <= (others => '0');
				clkint <= to_unsigned(460*100, 16);
			else
				clkint <= clkint-1;
			end if;

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
