--
--	xrom_xc2s_xcv.vhd
--
--	microinstruction memory for JOP3
--	Version for Xilinx Spartan II/IIe and Virtex Families
--
--	emulate asynch address with address register on negativ clock edge
--
--	Changes:
--    2003-12-29  EA - modified for Xilinx ISE to use Block SelectRAM+
--
--

Library IEEE ;
use IEEE.std_logic_1164.all ;
use IEEE.std_logic_arith.all ;
use IEEE.std_logic_unsigned.all ;
library unisim; 
use unisim.vcomponents.all; 

entity rom is
	generic (width : integer; addr_width : integer);
	port (
		clk		: in std_logic;

		address	: in std_logic_vector(9 downto 0);

		q		: out std_logic_vector(7 downto 0)
	);
end rom;

architecture rtl of rom is

	signal rom_ra: std_logic_vector(9 downto 0);

	COMPONENT xrom_block
	PORT(
		a_rst : IN std_logic;
		a_clk : IN std_logic;
		a_en : IN std_logic;
		a_wr : IN std_logic;
		a_addr : IN std_logic_vector(9 downto 0);
		a_din : IN std_logic_vector(7 downto 0);
		b_rst : IN std_logic;
		b_clk : IN std_logic;
		b_en : IN std_logic;
		b_wr : IN std_logic;
		b_addr : IN std_logic_vector(9 downto 0);
		b_din : IN std_logic_vector(7 downto 0);          
		a_dout : OUT std_logic_vector(7 downto 0);
		b_dout : OUT std_logic_vector(7 downto 0)
		);
	END COMPONENT;

begin

	cmp_xrom_block: xrom_block PORT MAP(
		a_rst => '0',
		a_clk => clk,
		a_en => '1',
		a_wr => '0',
		a_addr => rom_ra,
		a_din => X"00",
		a_dout => q,
		b_rst => '0',
		b_clk => clk,
		b_en => '0',
		b_wr =>'0',
		b_addr => "0000000000",
		b_din => X"00",
		b_dout => open
	);

	process(clk) begin
		if (clk'event and clk='0') then
			rom_ra <= address;
		end if;
	end process;
			
end rtl;
