--
--	lemotor.vhd
--
--	PWM for the LEGO DC motor and
--	sigma delta AD converters
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
--	2004-08-11	creation
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity lemotor is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	en1		: out std_logic;
	in1a	: out std_logic;
	in1b	: out std_logic;
	en2		: out std_logic;
	in2a	: out std_logic;
	in2b	: out std_logic;

	sdia	: in std_logic;
	sdib	: in std_logic;
	sdoa	: out std_logic;
	sdob	: out std_logic
);
end lemotor ;

architecture rtl of lemotor is

component lemsd is

generic (clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	dout	: out std_logic_vector(8 downto 0);

	sdi		: in std_logic;
	sdo		: out std_logic
);
end component lemsd;

	signal vala			: std_logic_vector(8 downto 0);			-- 9 bit ADC
	signal valb			: std_logic_vector(8 downto 0);

	signal m_out		: std_logic_vector(5 downto 0);

begin


process(addr, rd, vala, valb)

begin
	if addr=std_logic_vector(to_unsigned(io_addr, 4)) then
		dout <= std_logic_vector(to_unsigned(0, 7)) & valb &
				std_logic_vector(to_unsigned(0, 7)) & vala;
	else
		dout <= (others => 'Z');
	end if;

end process;

	en2 <= m_out(5);
	in2a <= m_out(4);
	in2b <= m_out(3);
	en1 <= m_out(2);
	in1a <= m_out(1);
	in1b <= m_out(0);

process(clk, reset, wr, addr)

begin
	if (reset='1') then

		m_out <= (others => '0');

	elsif rising_edge(clk) then

		if addr=std_logic_vector(to_unsigned(io_addr, 4))
			and wr='1' then
			m_out <= din(5 downto 0);
		end if;

	end if;
end process;


	cmp_sda : lemsd generic map (clk_freq)
				port map(clk, reset,
				vala, sdia, sdoa);
	cmp_sdb : lemsd generic map (clk_freq)
				port map(clk, reset,
				valb, sdib, sdob);

end rtl;
