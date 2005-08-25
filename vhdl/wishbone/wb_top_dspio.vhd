--
--	wb_top_guitar.vhd
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
--	2005-06-30	top level for guitar project
--
--	todo:
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.wb_pack.all;

entity wb_top is

port (
	clk		: in std_logic;
	reset	: in std_logic;
	wb_out	: in wb_master_out_type;
	wb_in	: out wb_master_in_type;
	wb_io	: inout io_ports
);
end wb_top;

architecture rtl of wb_top is

	constant SLAVE_NR : integer := 2;

	type wbs_in_array is array(0 to SLAVE_NR-1) of wb_slave_in_type;
	signal wbs_in		: wbs_in_array;
	type wbs_out_array is array(0 to SLAVE_NR-1) of wb_slave_out_type;
	signal wbs_out		: wbs_out_array;


begin

--
--	unused and input pins tri state
--
	wb_io.l(15 downto 1) <= (others => 'Z');
	wb_io.l(20 downto 18) <= (others => 'Z');
	wb_io.r(20 downto 14) <= (others => 'Z');
	wb_io.t <= (others => 'Z');
	wb_io.b(10 downto 3) <= (others => 'Z');

--	this is a simple point to point connection
--	wb_connect(wb_in, wb_out,
--		wb_s_in, wb_s_out);


	wbguit: entity work.wb_guitar port map(
		clk => clk,
		reset => reset,
		wb_in => wbs_in(0),
		wb_out => wbs_out(0),
		adc_in => wb_io.l(17),
		adc_out => wb_io.l(16),
		dac_l => wb_io.b(2),
		dac_r => wb_io.b(1)
	);


	wbusb: entity work.wb_usb port map(
		clk => clk,
		reset => reset,
		wb_in => wbs_in(1),
		wb_out => wbs_out(1),
		data => wb_io.r(8 downto 1),
		nrxf => wb_io.r(9),
		ntxe => wb_io.r(10),
		nrd => wb_io.r(11),
		wr => wb_io.r(12),
		nsi => wb_io.r(13)
	);



	--
	-- two simple test slaves
	--
	gsl: for i in 0 to SLAVE_NR-1 generate
		wbs_in(i).dat_i <= wb_out.dat_o;
		wbs_in(i).we_i <= wb_out.we_o;
		wbs_in(i).adr_i <= wb_out.adr_o(S_ADDR_SIZE-1 downto 0);
		wbs_in(i).cyc_i <= wb_out.cyc_o;
	end generate;

--
--	This is the address decoding and the data muxer.
--
process(wb_out, wbs_out)
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
