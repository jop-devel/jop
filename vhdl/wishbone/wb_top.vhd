--
--	wb_top.vhd
--
--	The top level for wishbone devices connected to JOP.
--	Do the address decoding here for the various slaves.
--	
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--          
--          WISHBONE DATA SHEET
--          
--          Revision Level: B.3, Released: September 7, 2002
--          Type: MASTER
--          
--          Signals: record and address size is defines in wb_pack.vhd
--          
--          Port    Width   Direction   Description
--          ------------------------------------------------------------------------
--          clk       1     Input       Master clock, see JOP top level
--          reset     1     Input       Reset, see JOP top level
--          dat_o    32     Output      Data from JOP
--          adr_o     8     Output      Address bits for the slaves, see wb_pack.vhd
--          we_o      1     Output      Write enable output
--          cyc_o     1     Output      Valid bus cycle output
--          stb_o     1     Output      Strobe signal output
--          dat_i    32     Input       Data from the slaves to JOP
--          ack_i     1     Input       Bus cycle acknowledge input
--          
--          Port size: 32-bit
--          Port granularity: 32-bit
--          Maximum operand size: 32-bit
--          Data transfer ordering: BIG/LITTLE ENDIAN
--          Sequence of data transfer: UNDEFINED
--          
--          
--
--	2005-05-30	first version with two simple test slaves
--
--	todo:
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.wb_pack.all;

entity wb_top is

port (
	clk		: in std_logic;
	reset	: in std_logic;
	wb_out	: in wb_master_out_type;
	wb_in	: out wb_master_in_type
);
end wb_top;

architecture rtl of wb_top is

	constant SLAVE_NR : integer := 2;

	type wbs_in_array is array(0 to SLAVE_NR-1) of wb_slave_in_type;
	signal wbs_in		: wbs_in_array;
	type wbs_out_array is array(0 to SLAVE_NR-1) of wb_slave_out_type;
	signal wbs_out		: wbs_out_array;


begin

--	this is a simple point to point connection
--	wb_connect(wb_in, wb_out,
--		wb_s_in, wb_s_out);

	--
	-- two simple test slaves
	--
	gsl: for i in 0 to SLAVE_NR-1 generate
		wbsl: entity work.wb_test_slave port map(
			clk => clk,
			reset => reset,
			wb_in => wbs_in(i),
			wb_out => wbs_out(i)
		);
		wbs_in(i).dat_i <= wb_out.dat_o;
		wbs_in(i).we_i <= wb_out.we_o;
		wbs_in(i).adr_i <= wb_out.adr_o(S_ADDR_SIZE-1 downto 0);
		wbs_in(i).cyc_i <= wb_out.cyc_o;
	end generate;

--
--	This is the address decoding and the data muxer.
--
process(wb_out)
begin

	if wb_out.adr_o(S_ADDR_SIZE)='0' then
		wbs_in(0).stb_i <= wb_out.stb_o;
		wbs_in(1).stb_i <= '0';
		wb_in.dat_i <= wbs_out(0).dat_o;
		wb_in.ack_i <= wbs_out(0).ack_o;
	else
		wbs_in(0).stb_i <= '0';
		wbs_in(1).stb_i <= wb_out.stb_o;
		wb_in.dat_i <= wbs_out(1).dat_o;
		wb_in.ack_i <= wbs_out(1).ack_o;
	end if;

end process;

end rtl;
