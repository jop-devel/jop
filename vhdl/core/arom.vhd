--
--	arom.vhd
--
--	microinstruction memory for JOP3
--	Version for Altera
--
--	emulate asynch address with address register on negativ clock edge
--
--

Library IEEE ;
use IEEE.std_logic_1164.all ;
use IEEE.std_logic_arith.all ;
use IEEE.std_logic_unsigned.all ;

entity rom is
generic (width : integer; addr_width : integer);
port (
	clk		: in std_logic;

	address	: in std_logic_vector(addr_width-1 downto 0);

	q		: out std_logic_vector(width-1 downto 0)
);
end rom;

architecture rtl of rom is

	COMPONENT lpm_rom
	GENERIC (LPM_WIDTH: POSITIVE;
		LPM_TYPE: STRING := "LPM_ROM";
		LPM_WIDTHAD: POSITIVE;
		LPM_NUMWORDS: NATURAL := 0;
		LPM_FILE: STRING;
		LPM_ADDRESS_CONTROL: STRING := "REGISTERED";
		LPM_OUTDATA: STRING := "REGISTERED";
		LPM_HINT: STRING := "UNUSED");
	PORT (address: IN STD_LOGIC_VECTOR(LPM_WIDTHAD-1 DOWNTO 0);
		inclock: IN STD_LOGIC := '0';
		outclock: IN STD_LOGIC := '0';
		memenab: IN STD_LOGIC := '1';
		q: OUT STD_LOGIC_VECTOR(LPM_WIDTH-1 DOWNTO 0));
	END COMPONENT;

begin

	cmp_rom: lpm_rom
			generic map (
				LPM_WIDTH => width, 
				LPM_TYPE => "LPM_ROM",
				LPM_WIDTHAD =>	addr_width, 
				LPM_NUMWORDS =>	2**addr_width,
				LPM_FILE => "../../asm/generated/rom.mif", 
				LPM_ADDRESS_CONTROL => "REGISTERED",
				LPM_OUTDATA => "REGISTERED",
				LPM_HINT => "USE_EAB=ON")
			port map (
				address => address,
				inclock => not clk,
				outclock => clk,
				q => q
			); 

end rtl;
