--
--	xjbc_xc2s_xcv.vhd
--
--	byte code memory for JOP3
--	Version for Xilinx Spartan II/IIe and Virtex Families
--
--		wr_addr comes together with address (from A)
--			take start address from data
--		wren comes one clock befor data (from A) ... generated in read/addr stage
--			read 4 byte data and start write with high byte first and
--			address auto increment
--
--	on FPGAs (Cyclone) with different read and write port size
--	write could be a 32 bit single cycle thing, but ACEX does not
--	support it.
--			
--
--	Changes:
--		2003-08-14	load start address with jpc_wr and do autoincrement
--					load 32 bit data and do the 4 byte writes serial
--
--    2003-12-29  EA - modified for Xilinx ISE to use Block SelectRAM+
--

Library IEEE ;
use IEEE.std_logic_1164.all ;
use ieee.numeric_std.all;
use ieee.std_logic_unsigned.all;
library unisim; 
use unisim.vcomponents.all; 

entity jbc is
	generic (width : integer := 8; addr_width : integer := 10);
	port (
		data		: in std_logic_vector(31 downto 0);

		rdaddress	: in std_logic_vector(addr_width-1 downto 0);
		wr_addr		: in std_logic;									-- load start address (=jpc)
		wren		: in std_logic;
		clock		: in std_logic;

		q			: out std_logic_vector(width-1 downto 0)
	);
end jbc ;

--
--	registered and delayed wraddress, wren
--	registered din
--	registered rdaddress
--	unregistered dout
--
architecture rtl of jbc is

	signal dreg			: std_logic_vector(31 downto 0);
	signal wraddr_dly	: std_logic_vector(addr_width-1 downto 0);
	signal wren_dly		: std_logic;
	signal ram_wr		: std_logic;
	signal ram_din		: std_logic_vector(7 downto 0);
	signal cnt			: std_logic_vector(2 downto 0);
	signal sel			: std_logic_vector(1 downto 0);

	COMPONENT xjbc_block
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

--
--
--
	process(clock) begin

		if rising_edge(clock) then
			wren_dly <= wren;				-- wren is one cycle befor data
			if wr_addr='1' then
				wraddr_dly <= data(addr_width-1 downto 0);
			end if;

			if (wren_dly='1') then			-- wren_dly starts 4 cycle write
				dreg <= data;				-- register write data
				cnt <= "100";				-- and start write
				sel <= "00";
			end if;

			if (cnt/="000") then
				wraddr_dly <= std_logic_vector(unsigned(wraddr_dly) + 1);
				cnt <= std_logic_vector(unsigned(cnt) - 1);
				sel <= std_logic_vector(unsigned(sel) + 1);
			end if;
		end if;
	end process;

	process(cnt) begin
		if (cnt/="000") then
			ram_wr <= '1';
		else
			ram_wr <= '0';
		end if;
	end process;

	process(sel) begin
		case sel is
			when "00" =>
				ram_din <= dreg(31 downto 24);
			when "01" =>
				ram_din <= dreg(23 downto 16);
			when "10" =>
				ram_din <= dreg(15 downto 8);
			when "11" =>
				ram_din <= dreg(7 downto 0);
			when others =>
				null;
		end case;
	end process;

	cmp_xjbc_block: xjbc_block PORT MAP(
		a_rst => '0',
		a_clk => clock,
		a_en => '1',
		a_wr => ram_wr,
		a_addr => wraddr_dly,
		a_din => ram_din,
		a_dout => open,
		b_rst => '0',
		b_clk => clock,
		b_en => '1',
		b_wr => '0',
		b_addr => rdaddress,
		b_din => X"00",
		b_dout => q
	);

end rtl;
