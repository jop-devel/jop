--
--	sc_DE2-70_expansionheader.vhd
--
--
--	2012-02-11	created Tórur Biskopstø Strøm
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;
use ieee.std_logic_unsigned.all;
use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;

entity expansionheader is
port (
	clk		: in std_logic;
	reset	: in std_logic;

--
--	SimpCon IO interface
--
	sc_rd		: in std_logic;
	sc_rd_data	: out std_logic_vector(31 downto 0);
	
	sc_wr		: in std_logic;
	sc_wr_data	: in std_logic_vector(31 downto 0);
	
	sc_rdy_cnt	: out unsigned(1 downto 0);

--
-- Expansion header
--
	GPIO_0	: inout std_logic_vector(31 downto 0)
	
 );
end expansionheader;


architecture rtl of expansionheader is
	signal readtemp : std_logic;
	signal cnt : std_logic_vector(31 downto 0);
	signal cntvalue : std_logic_vector(31 downto 0);
begin
	
	process(clk, reset)
	begin
		if(reset='1') then
			GPIO_0(0) <= '0';
			GPIO_0(1) <= '0';
			GPIO_0(2) <= '0';
			GPIO_0(4) <= '0';
			GPIO_0(6) <= '0';
			GPIO_0(8) <= '0';
			GPIO_0(10) <= '0';
			GPIO_0(12) <= '0';
			GPIO_0(16) <= '0';
			GPIO_0(18) <= '0';
			GPIO_0(20) <= '0';
			GPIO_0(22) <= '0';
			GPIO_0(23) <= '0';
			GPIO_0(24) <= '0';
			GPIO_0(25) <= '0';
			GPIO_0(26) <= '0';
			GPIO_0(28) <= '0';
			sc_rd_data <= (others => '0');
			readtemp <= '0';
			cntvalue <= (others => '0');
			cnt <= (others => '0');
		elsif(rising_edge(clk)) then
			
			if(readtemp = '1') then
				if(GPIO_0(27) = '1') then
					cntvalue <= cnt;
					cnt <= (others => '0');
					readtemp <= '0';
				else
					cnt <= cnt + 1;
				end if;
			else
				if(cnt = 1000000) then
					readtemp <= '1';
					cnt <= (others => '0');
				else
					cnt <= cnt + 1;
				end if;
			end if;
			
			if(sc_wr = '1') then
			
				GPIO_0(0) <= sc_wr_data(0); --Motor1 step
				GPIO_0(1) <= sc_wr_data(1); --Motor1 reset
				GPIO_0(2) <= sc_wr_data(2); --Motor1 dir
				GPIO_0(4) <= sc_wr_data(4); --Motor2 reset
				GPIO_0(6) <= sc_wr_data(6); --Motor2 step
				GPIO_0(8) <= sc_wr_data(8); --Motor2 dir
				GPIO_0(10) <= sc_wr_data(10); --Motor3 reset
				GPIO_0(12) <= sc_wr_data(12); --Motor3 step
				GPIO_0(16) <= sc_wr_data(16); --Motor3 dir
				GPIO_0(18) <= sc_wr_data(18); --Motor4 reset
				GPIO_0(20) <= sc_wr_data(20); --Motor4 step
				GPIO_0(22) <= sc_wr_data(22); --Motor4 dir
				GPIO_0(23) <= sc_wr_data(23); --Heater1
				GPIO_0(24) <= sc_wr_data(24); --Motor5 reset
				GPIO_0(25) <= sc_wr_data(25); --Heater2
				GPIO_0(26) <= sc_wr_data(26); --Motor5 step
				GPIO_0(28) <= sc_wr_data(28); --Motor5 dir
				
			elsif(sc_rd = '1') then
--				sc_rd_data(2 downto 0) <= (others => '0');
--				sc_rd_data(3) <= GPIO_0(3); --Endstop1
--				sc_rd_data(4) <= '0';
--				sc_rd_data(5) <= GPIO_0(5); --Endstop2
--				sc_rd_data(6) <= '0';
--				sc_rd_data(7) <= GPIO_0(7); --Endstop3
--				sc_rd_data(26 downto 8) <= (others => '0');
--				sc_rd_data(27) <= GPIO_0(27);
--				sc_rd_data(31 downto 28) <= (others => '0');
				sc_rd_data(31 downto 1) <= cntvalue(30 downto 0);
				sc_rd_data(0) <= readtemp;
			end if;
		end if;
	end process;
	
	GPIO_0(9) <= '0';
	GPIO_0(11) <= '0';
	GPIO_0(13) <= '0';
	GPIO_0(14) <= '0';
	GPIO_0(15) <= '0';
	GPIO_0(17) <= '0';
	GPIO_0(19) <= '0';
	GPIO_0(21) <= '0';
	GPIO_0(30) <= '0';
	GPIO_0(31) <= '0';
	GPIO_0(29) <= '1';
	
	GPIO_0(27) <= '0' when (readtemp = '0') else 'Z';
	

end rtl;

