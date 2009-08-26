--
--
--  This file is a part of the VGA_fb Controller Module 
--
--  Copyright (C) 2009, Matthias Wenzl (e0425388@student.tuwien.ac.at)
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--

--the native resolution is 640*480 @8bit, however due to memory limitations a 
--320*240 @8 bit window is deployed at the center of the screen. Any data written to the shared
-- will be displayed here
-- if you want to change back to the native resolution comment the line between
--the markers START_COMMENTME and END_COMMENTME in the file crt_engine.vhd

--moreover the see the state req_data for further changes achieving native 
--resolution again.

-- **************************************
-- vga_fb architecture
-- **************************************

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use ieee.numeric_std.all;

library WORK;
use WORK.all;

architecture rtl of vga_fb is

-- seee state req data for chang advisories
constant  SCREEN_ADDR_TOP :integer := ((320*240)/4);
  
signal sys_clk :std_logic;
signal locked :std_logic;

component altera_fifo
	PORT
	(
		aclr		: IN STD_LOGIC  := '0';
		data		: IN STD_LOGIC_VECTOR (31 DOWNTO 0);
		rdclk		: IN STD_LOGIC ;
		rdreq		: IN STD_LOGIC ;
		wrclk		: IN STD_LOGIC ;
		wrreq		: IN STD_LOGIC ;
		q		: OUT STD_LOGIC_VECTOR (31 DOWNTO 0);
		rdempty		: OUT STD_LOGIC ;
		wrfull		: OUT STD_LOGIC ;
		wrusedw		: OUT STD_LOGIC_VECTOR (5 DOWNTO 0)
	);
end component;



component crt_engine is
  generic ( 
    DAC_WIDTH :integer := 10;
    WORD_WIDTH :integer := 32;
    ADDR_WIDTH :integer := 32;
    HSC_WIDTH :integer := 10;
    VSC_WIDTH :integer := 10;
    HS_PULSE_TOP :integer := 94; 
    HS_FPORCH_TOP :integer := 16; 
    HS_PIXEL_TOP :integer :=640;
    HS_BPORCH_TOP :integer := 48; 
    VS_PULSE_TOP :integer := 2;
    VS_FPORCH_TOP :integer := 10; 
    VS_LINE_TOP :integer := 480;
    VS_BPORCH_TOP :integer := 33 
    );
  port ( 
    reset :in std_logic;
    async_fifo_reset :in std_logic;
    pixel_clk :in std_logic;
    ld_serializer :in std_logic;
    fifo2crt :in std_logic_vector(WORD_WIDTH-1 downto 0);
    serializer_empty :out std_logic;
    dac_clk :out std_logic;
    v_blank :out std_logic;
    h_blank :out std_logic;
    H_SYNC :out std_logic;
    V_SYNC :out std_logic;
    R :out std_logic_vector(DAC_WIDTH-1 downto 0);
    G :out std_logic_vector(DAC_WIDTH-1 downto 0);
    B :out std_logic_vector(DAC_WIDTH-1 downto 0)
    );
end component;
  
  signal ld_serializer,pop,push :std_logic;
  signal fifo2crt,fifo_dout : std_logic_vector(31 downto 0);
  signal serializer_empty_n :std_logic;
  signal v_blank, h_blank :std_logic;

	
	constant FIFO_MAX :std_logic_vector(5 downto 0) := B"11" & X"f";
  
  signal fifo_empty,fifo_full :std_logic;
  signal fifo_size :std_logic_vector(5 downto 0);
  signal nxt_col :std_logic_vector(31 downto 0);
  
  signal reset_n :std_logic;
  signal V_SYNC,H_SYNC :std_logic;
  
  --fsm states for starting and ending a read requeest to shared mem
  type read_state is(wait_for_first_sync,wait_for_fifo_rdy,req_data,get_data,pending);
  signal nxt_read, act_read :read_state;
  
 
  constant BASE_ADDRESS :std_logic_vector(22 downto 0) := std_logic_vector(to_unsigned(sh_mem_start_address,23));   
  constant TOP_ADDRESS :std_logic_vector(22 downto 0) := std_logic_vector(to_unsigned(sh_mem_end_address,23)); 
  signal nxt_addr, act_addr :std_logic_vector(22 downto 0);
  
  signal usync_fifo_empty,sync_fifo_empty :std_logic;
  signal v_sync_falling_edge,sync_v_sync,usync_v_sync :std_logic;
  
  signal fifo_reset,nxt_fifo_rdy,act_fifo_rdy :std_logic;
  
  
begin

sys_clk <= clk;
reset_n <= reset;

my_sync_fifo : altera_fifo
	PORT MAP
	(
		aclr => fifo_reset,
		data		=> nxt_col,
		rdclk		=> pixel_clk,
		rdreq		=> pop,
		wrclk		=> sys_clk,
		wrreq		=> push,
		q		=> fifo2crt,
		rdempty		=> fifo_empty,
		wrfull		=> fifo_full,
		wrusedw		=> fifo_size
	);

--recommended settings for 640*480@75Hz 
my_crt :crt_engine
  generic map( 
    DAC_WIDTH => 10,
    WORD_WIDTH => 32,
    ADDR_WIDTH => 32,
    HSC_WIDTH => 10,
    VSC_WIDTH => 10,
    HS_PULSE_TOP => 96, --96 -94 --93 --94 --96
    HS_FPORCH_TOP => 16, --16 24 --23 --24 --16
    HS_PIXEL_TOP => 640,
    HS_BPORCH_TOP => 48, --48 48 --47 --48 --48
    VS_PULSE_TOP => 2,
    VS_FPORCH_TOP => 11, --10 11 --11 --11 --11
    VS_LINE_TOP => 480,
    VS_BPORCH_TOP => 32 --33 --32 --32 --33 --32
    )
  port map( 
    reset => reset_n,
    async_fifo_reset => fifo_reset,
    pixel_clk => pixel_clk,
    ld_serializer => ld_serializer,
    fifo2crt => fifo_dout,
    serializer_empty => serializer_empty_n,
    dac_clk => VGA_CLOCK,
    v_blank => v_blank,
    h_blank => h_blank,
    H_SYNC => H_SYNC,
    V_SYNC => V_SYNC,
    R => VGA_R,
    G => VGA_G,
    B => VGA_B
    );

VGA_BLANK_N <= '1';
VGA_SYNC_N <= '0'; --not needed
VGA_VS <=  V_SYNC;
VGA_HS <=  H_SYNC;
  
  -----------
  --pixel_clk part
  -- this lines are synchronized to the pixel clk
  -----------
  
  
  --get new word from fifo 2 serializer
  comb_pixel :process(serializer_empty_n,fifo_empty,fifo2crt)
  begin
	pop <= '0';
	ld_serializer <= '0';
	fifo_dout <= (others => '0');

	if( serializer_empty_n = '0' and fifo_empty = '0') then
		pop <= '1';
		ld_serializer <= '1';
		fifo_dout <= fifo2crt;
	end if;
	
  end process;
  
  
  
  -------
  --sys_clk part
  --these lines are synchronized to the system clock
  --------
  
--synchronize v_blank, and detect its falling edge
sync_blank :process(sys_clk)
begin
	if(rising_edge(sys_clk)) then
		v_sync_falling_edge <= sync_v_sync;
		sync_v_sync <= usync_v_sync;
		usync_v_sync <= V_SYNC;
		sync_fifo_empty <= usync_fifo_empty;
		usync_fifo_empty <= fifo_empty;
	end if;
end process;
	
	
  sys_sync: process(sys_clk,reset_n)
  begin
    if(reset_n = '1') then 
      act_read <= wait_for_first_sync; 
      act_addr <= BASE_ADDRESS;
      act_fifo_rdy <= '0';
    elsif(rising_edge(sys_clk)) then
		act_read <= nxt_read;
      act_addr <= nxt_addr;
      act_fifo_rdy <= nxt_fifo_rdy;
    end if;
  end process;
  
  --default assignment for unused simpcon outputs
  wr_data <= (others => '0');
  wr <= '0';
  
  comb_sys: process(act_fifo_rdy,sync_fifo_empty,sync_v_sync,nxt_col,act_read,fifo_full,fifo_size,act_addr,v_sync_falling_edge,rd_data)
  begin
    nxt_read <= act_read;
    nxt_addr <= act_addr;
    nxt_fifo_rdy <= act_fifo_rdy;
    
    nxt_col <= (others => '0');
    rd <= '0';
    address <= (others => '0');
    push <= '0';
    fifo_reset <= '0';
    
    
    --calculate the next needed addresses from the shared mem 
    case act_read is
	when wait_for_first_sync =>
		--just pend here untill first v_sync falling edge is issued
		--the advance to state req_data is managed by the v_sync block behind the
		-- case statement
		nxt_read <= wait_for_first_sync;
		
	when wait_for_fifo_rdy =>
		--fifo takes two cylces after a reset to be operational again
		--this state can be reached thourgh the v_sync block behind the case statement
		if(act_fifo_rdy = '1') then
			nxt_fifo_rdy <= '0';
			nxt_read <= req_data;
		else
			nxt_fifo_rdy <= '1';
			nxt_read <= wait_for_fifo_rdy;
		end if;
		
	when req_data =>
		--change top HS_PIXEL_TOP*VS_LINE_TOP if you want to achieve native 640*480 again
		--if((act_addr = ((320*240)/4)-1)) then
		if((act_addr = (SCREEN_ADDR_TOP-1))) then
			nxt_addr <= (others => '0');
		else
			nxt_addr <= act_addr + '1';
		end if;
			
		address <= act_addr + BASE_ADDRESS;
		rd <= '1';
		nxt_read <= get_data;

	when  get_data =>
			if(rdy_cnt = B"00") then
				nxt_col <= rd_data;
				push <= '1';
				
				--determine if read from extmem shall continue
				if(fifo_size = (FIFO_MAX-1) ) then
					nxt_read <= pending;
				else
					nxt_read <= req_data;
				end if;
			else
				nxt_read <= get_data;
			end if;
			
	when  pending =>
		if(fifo_size < (B"10" & X"5")) then 
			nxt_read <= req_data;
		end if;
	when others => nxt_read <= req_data; 
	end case;
	
	
	--synchronize  to v_sync
	--the vertical sync condition has the last word, the actual read is cancelled,
	--the fifo is emptied and reading from shared mem is put back into synchronity between 
	--shared memory and crt engine
	if(v_sync_falling_edge = '1' and sync_v_sync = '0') then
		fifo_reset <= '1';
		nxt_addr <= (others => '0');
		nxt_read <= wait_for_fifo_rdy;
		rd <= '0';
		address <= BASE_ADDRESS;
	end if;
      
  end process;
  
end architecture rtl;
