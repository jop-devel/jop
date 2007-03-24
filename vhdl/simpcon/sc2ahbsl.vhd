--
--	sc2ahbsl.vhd
--
--	SimpCon to AMBA bridge
--
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--	2007-03-16	first version
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;

library grlib;
use grlib.amba.all;
--use grlib.tech.all;
library gaisler;
use gaisler.memctrl.all;
--use gaisler.pads.all; -- used for I/O pads
--use gaisler.misc.all;

entity sc2ahbsl is

port (

	clk, reset	: in std_logic;

--	SimpCon memory interface
	scmo		: in sc_mem_out_type;
	scmi		: out sc_in_type;

-- AMBA slave interface
    ahbsi  		: out  ahb_slv_in_type;
    ahbso  		: in ahb_slv_out_type
);
end sc2ahbsl;

architecture rtl of sc2ahbsl is

	type state_type		is (idl, rd, rdw, wr, wrw);
	signal state 		: state_type;
	signal next_state	: state_type;

	signal reg_addr		: std_logic_vector(MEM_ADDR_SIZE-1 downto 0);
	signal reg_wr_data	: std_logic_vector(31 downto 0);
	signal reg_rd_data	: std_logic_vector(31 downto 0);

	signal reg_rd		: std_logic;
	signal reg_wr		: std_logic;

begin

--
--	some defaults
--
	ahbsi.hsel(1 to NAHBSLV-1) <= (others => '0');	-- we use only slave 0
	ahbsi.hsel(0) <= scmo.rd or scmo.wr;			-- slave select
	-- do we need to store the addrsss in a register?
	ahbsi.haddr(MEM_ADDR_SIZE-1+2 downto 2) <= scmo.address;	-- address bus (byte)
	ahbsi.haddr(1 downto 0) <= (others => '0');
	ahbsi.haddr(31 downto MEM_ADDR_SIZE+2) <= (others => '0');
	ahbsi.hwrite <= scmo.wr;						-- read/write
	ahbsi.htrans <= HTRANS_NONSEQ;					-- transfer type
	ahbsi.hsize <= "010";							-- transfer size 32 bits
	ahbsi.hburst <= HBURST_SINGLE;					-- burst type
	ahbsi.hwdata <= reg_wr_data;					-- write data bus
	ahbsi.hprot <= "0000";		-- ? protection control
	ahbsi.hready <= '1';		-- ? transer done 
	ahbsi.hmaster <= "0000";						-- current master
	ahbsi.hmastlock <= '0';		-- locked access
	ahbsi.hmbsel(0) <= '0';							-- memory bank select
	ahbsi.hmbsel(1) <= '1';							-- second is SRAM
	ahbsi.hmbsel(2 to NAHBAMR-1) <= (others => '0');
	ahbsi.hcache <= '1';							-- cacheable
	ahbsi.hirq <= (others => '0');					-- interrupt result bus


-- depends on state....
	scmi.rd_data <= reg_rd_data;


--
--	Register memory address, write data and read data
--	do we need all those for amba?
--
process(clk, reset)
begin
	if reset='1' then

		reg_addr <= (others => '0');
		reg_wr_data <= (others => '0');

	elsif rising_edge(clk) then

		if scmo.rd='1' or scmo.wr='1' then
			reg_addr <= scmo.address;
		end if;
		if scmo.wr='1' then
			reg_wr_data <= scmo.wr_data;
		end if;

	end if;
end process;


----
----	The address MUX slightly violates the Avalon
----	specification. The address changes from the sc_address
----	to the registerd address in the second cycle. However,
----	as both registers contain the same value there should be
----	no real glitch. For synchronous peripherals this is not
----	an issue. For asynchronous peripherals (SRAM) the possible
----	glitch should be short enough to be not seen on the output
----	pins.
----
--process(scmo.rd, scmo.w, sc_address, reg_addr)
--begin
--	if sc_rd='1' or sc_wr='1' then
--		av_address(addr_bits-1+2 downto 2) <= sc_address;
--	else
--		av_address(addr_bits-1+2 downto 2) <= reg_addr;
--	end if;
--end process;
--
----	Same game for the write data and write/read control
--process(sc_wr, sc_wr_data, reg_wr_data)
--begin
--	if sc_wr='1' then
--		av_writedata <= sc_wr_data;
--	else
--		av_writedata <= reg_wr_data;
--	end if;
--end process;
--		
--	av_write <= sc_wr or reg_wr;
--	av_read <= sc_rd or reg_rd;
--
--
--
--
--	next state logic
--
--	At the moment we do not support back to back read
--	or write. We don't need it for JOP, right?
--	If needed just copy the idl code to rd and wr.
--
process(state, scmo, ahbso.hready)

begin

	next_state <= state;

	case state is

		when idl =>
			if scmo.rd='1' then
				next_state <= rdw;
			elsif scmo.wr='1' then
				next_state <= wrw;
			end if;

		when rdw =>
			if ahbso.hready='1' then
				next_state <= rd;
			end if;

		when rd =>
			next_state <= idl;

		when wrw =>
			if ahbso.hready='1' then
				next_state <= wr;
			end if;

		when wr =>
			next_state <= idl;


	end case;
				
end process;

--
--	state machine register
--	and output register
--
process(clk, reset)

begin
	if (reset='1') then
		state <= idl;
		reg_rd_data <= (others => '0');
		scmi.rdy_cnt <= "00";
		reg_rd <= '0';
		reg_wr <= '0';

	elsif rising_edge(clk) then

		state <= next_state;
		scmi.rdy_cnt <= "00";
		reg_rd <= '0';
		reg_wr <= '0';

		case next_state is

			when idl =>

			when rdw =>
				scmi.rdy_cnt <= "11";
				reg_rd <= '1';

			when rd =>
				reg_rd_data <= ahbso.hrdata;

			when wrw =>
				scmi.rdy_cnt <= "11";
				reg_wr <= '1';

			when wr =>

		end case;
					
	end if;
end process;

end rtl;
