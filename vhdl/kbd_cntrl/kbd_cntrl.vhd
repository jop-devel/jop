--
--
--  This file is a part of PS2 Keyboard Controller Module 
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


-- **************************************
-- kbd_cntrl architecture
-- **************************************

--sending data has been shut off so far 

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use ieee.numeric_std.all;

library WORK;
use WORK.all;

architecture rtl of kbd_cntrl is
  
  
  component ps2_interface is
  generic ( 
    SYS_CLK_FREQ :integer := 50000000;
    TIMEOUT_BIT_WIDTH :integer := 13
    );
  port ( 
    reset :in std_logic;
    clk :in std_logic;
    data_in :in std_logic_vector(7 downto 0);
    data_out :out std_logic_vector(7 downto 0);
    wr_word :in std_logic;
    rd_buf :in std_logic;
    rcv_rdy :out std_logic;
    snd_rdy :out std_logic;
    parity_error :out std_logic;
    ps_datain :in std_logic;
    ps_dataout :out std_logic;
    data_oe :out std_logic;
    ps_clkin :in std_logic;
    ps_clkout :out std_logic;
    clk_oe :out std_logic
    );
end component;

component rom_scancode2_ascii is
 generic (width :integer := 16; addr_width :integer := 24);
 port(
	clk	:in std_logic;
	address	:in std_logic_vector(addr_width-1 downto 0);
	q	:out std_logic_vector(width-1 downto 0)
);
end component;

component rom_scancode2_ascii_shift is
 generic (width :integer := 16; addr_width :integer := 24);
 port(
	clk	:in std_logic;
	address	:in std_logic_vector(addr_width-1 downto 0);
	q	:out std_logic_vector(width-1 downto 0)
);
end component;

 
  signal data_in,data_out :std_logic_vector(7 downto 0);
  signal rd_buf,wr_word,rcv_rdy,snd_rdy,parity_error,ps_datain,ps_dataout,data_oe,ps_clkin,ps_clkout,clk_oe :std_logic;
  signal char_rdy :std_logic;
  
  signal act_ctrl_reg :std_logic_vector(7 downto 0);
  signal nxt_rcv_word,act_rcv_word :std_logic_vector(7 downto 0);
  
  signal nxt_rom_address,act_rom_address,ascii_addr,ascii_addr_shift:std_logic_vector(7 downto 0);

   
  
  type rcv_state is(listen,extended_key,key_released,long_dump,pend_dump_end);
  signal nxt_rcv_state,act_rcv_state :rcv_state;
  
  
  
  
  signal nxt_code,nxt_code_shift,nxt_code_ext,nxt_code_std ,act_code :std_logic_vector(15 downto 0);

  signal nxt_caps,nxt_shift,act_shift,act_caps :std_logic;
  signal nxt_char_rdy,act_char_rdy,nxt_scc_rdy,act_scc_rdy :std_logic;
  
  signal nxt_scan_code, act_scan_code :std_logic_vector(23 downto 0);
  signal nxt_key_rel,act_key_rel :std_logic;
  
begin
  
  
  --instanciate ps2 interface
  ps2_cntrl : ps2_interface 
  generic map(
    SYS_CLK_FREQ => CLK_FREQ,
    TIMEOUT_BIT_WIDTH => TIMEOUT_REG_WIDTH
    )
  port map( 
    reset => reset,
    clk => clk,
    data_in => data_in,
    data_out => data_out,
    wr_word => wr_word,
    rd_buf => rd_buf,
    rcv_rdy => rcv_rdy,
    snd_rdy => snd_rdy,
    parity_error => parity_error,
    ps_datain => ps_datain,
    ps_dataout => ps_dataout,
    data_oe => data_oe,
    ps_clkin => ps_clkin,
    ps_clkout => ps_clkout,
    clk_oe => clk_oe
    );
  
    
 ascii_lookup_shift : rom_scancode2_ascii_shift
 generic map(width => 16, addr_width => 8)
 port map(
	clk	=> clk,
	address => 	ascii_addr_shift,
	q	=> nxt_code_shift
);
  
  ascii_lookup : rom_scancode2_ascii
 generic map(width => 16, addr_width => 8)
 port map(
	clk	=> clk,
	address => 	ascii_addr,
	q	=> nxt_code_std
);

  
  rdy_cnt <= B"00";
  rd_data(31 downto 24) <= (others => '0');
  
  
  nxt_code <= nxt_code_std or nxt_code_shift;
  
  sync_kbd_cntrl: process(CLK,reset)
  begin
    if(reset = '1') then
      act_ctrl_reg <= (others => '0');
      act_rom_address <= (others => '0');
      act_caps <= '0';
      act_shift <= '0';
      act_code <= (others => '0');
      act_char_rdy <= '0';
      act_scc_rdy <= '0';
      act_scan_code <= (others => '0');
      act_key_rel <= '0';
      act_rcv_state <= listen;
    elsif(rising_edge(CLK)) then
      act_rom_address <= nxt_rom_address;
      act_rcv_state <= nxt_rcv_state;
      act_caps <= nxt_caps;
      act_shift <= nxt_shift;
      act_code <= nxt_code;
      act_scan_code <= nxt_scan_code;
      act_char_rdy <= nxt_char_rdy;
      act_scc_rdy <= nxt_scc_rdy;
      act_key_rel <= nxt_key_rel;
      
      
      --ctrl reg stuff
    if(parity_error = '1') then
      act_ctrl_reg(0) <= '1';
    end if;
    
    if(act_char_rdy = '1' and nxt_code /= X"00") then --a scan code has been converted  succesfully -- because it takes two cycles untill  received scan code has been looked up
		  act_ctrl_reg(1) <= '1';
	  end if;
    
    if(snd_rdy = '1') then
      act_ctrl_reg(2) <= '1';
    end if;
    
    act_ctrl_reg(3) <= nxt_caps;
    
    if(act_scc_rdy = '1') then -- scan code ready to read
		  act_ctrl_reg(4) <= '1';
	 end if;
	 
	 --if(act_key_rel = '1') then --a key has been released
		act_ctrl_reg(5) <= act_key_rel;
	--end if;
	
	wr_word <= '0';
      if(rd = '1') then
        if(address(1 downto 0) = B"00") then --access to cntrl register
          rd_data(23 downto 0) <= X"0000" & B"00"  & act_ctrl_reg(5 downto 0); 
          act_ctrl_reg(4 downto 0) <= (others => '0'); --clr status bits upon read
        
        elsif(address(1 downto 0) = B"01") then --access to received data reg
          rd_data(23 downto 0) <= X"00" & act_code(15 downto 0);
        
        elsif(address(1 downto 0) = B"10") then 
          rd_data(23 downto 0) <= act_scan_code(23 downto 0);
          act_scan_code(23 downto 0) <= (others => '0');
        end if;
     
     --the sender is not functional, so writing here will be ignored    
    elsif(wr = '1') then
        if((address(1 downto 0) = B"00")) then --access to cntrl register
          act_ctrl_reg(7 downto 0) <= wr_data(7 downto 0);
        elsif((address(1 downto 0) = B"01")) then --write to be send data
          wr_word <= '1';
          data_in(7 downto 0) <= wr_data(7 downto 0);
        end if;
    end if;
     
        
    end if;
  end process;
  
  comb_kbd_cntrl: process(act_key_rel,act_scan_code,nxt_rom_address,nxt_caps,nxt_shift,act_shift,act_char_rdy,wr_data,wr,rd,address,data_out,act_code,act_caps,act_rcv_state,act_ctrl_reg,act_rom_address,parity_error,rcv_rdy,snd_rdy)
  begin
    nxt_rom_address <= act_rom_address;
    nxt_rcv_state <= act_rcv_state;
    nxt_caps <= act_caps;
    nxt_char_rdy <= act_char_rdy;
    nxt_shift <= act_shift;
    nxt_scan_code <= act_scan_code;
    nxt_key_rel <= act_key_rel;
    
    rd_buf <= '0';
    nxt_char_rdy <= '0';
    nxt_scc_rdy <= '0';
    
    
    ascii_addr <= (others => '0');
    ascii_addr_shift <= (others => '0');
    
    case(act_rcv_state) is
    when listen => 
      if(rcv_rdy = '1') then
        rd_buf <= '1';
        
        
        if(data_out = X"f0") then --break code
          nxt_scan_code(15 downto 8) <= data_out;
          nxt_rcv_state <= key_released;
        
        elsif(data_out = X"e0") then --extended key
          nxt_scan_code(15 downto 8) <= data_out;
          nxt_rcv_state <= extended_key;
         
       elsif(data_out = X"12" or data_out = X"59") then --lshift or rshift
          nxt_shift <= '1';
          nxt_scan_code(15 downto 8) <= data_out(7 downto 0);
          nxt_scc_rdy <= '1';
          nxt_rcv_state <= listen;
          
        elsif(data_out = X"58") then --caps lock
          if(act_caps = '0') then --toggle caps lock
            nxt_caps <= '1';
            nxt_scan_code(15 downto 8) <= X"58";
          else
            nxt_scan_code(15 downto 8) <= X"00";
            nxt_caps <= '0';
          end if;
          nxt_scc_rdy <= '1';
          nxt_rcv_state <= listen;
          
        else
          
          nxt_rom_address(7 downto 0) <= data_out(7 downto 0);
          nxt_scan_code(7 downto 0) <= data_out(7 downto 0);
          nxt_key_rel <= '0';
          nxt_char_rdy <= '1';
          nxt_scc_rdy <= '1';
          nxt_rcv_state <= listen;
        end if;
      end if;
      
    when extended_key => 
      if(rcv_rdy = '1') then
        rd_buf <= '1';
        
        if(data_out = X"f0") then
          nxt_scan_code(23 downto 16) <= act_scan_code(15 downto 8);
          nxt_scan_code(15 downto 8) <= data_out(7 downto 0);
          nxt_rcv_state <= key_released;
          
        else
          nxt_rom_address(7 downto 0) <= (others => '0');
          nxt_scan_code(7 downto 0) <= data_out(7 downto 0);
          if(data_out = X"5a") then --enter on numpad
			nxt_scan_code(15 downto 8) <= (others => '0');
			nxt_rom_address(7 downto 0) <= data_out(7 downto 0);
			nxt_char_rdy <= '1';
		  elsif(data_out = X"4a") then -- / on mumpad
			nxt_rom_address(7 downto 0) <= data_out(7 downto 0);
			nxt_char_rdy <= '1';
		  end if;
		  
		  nxt_key_rel <= '0';
          nxt_scc_rdy <= '1';
          nxt_rcv_state <= listen;
        end if;
      end if;
                
    when key_released =>
        if(rcv_rdy = '1') then
        rd_buf <= '1';
          nxt_rom_address(7 downto 0) <= data_out(7 downto 0); --X"00";
          nxt_scan_code(7 downto 0) <= data_out(7 downto 0);
          if(data_out = X"12" or data_out = X"59") then
            nxt_shift <= '0';
          end if;
          nxt_char_rdy <= '1';
          nxt_scc_rdy <= '1';
          nxt_key_rel <= '1';
          nxt_rcv_state <= listen;
        end if;
    when others => null;
    end case;
    
    --mux rom addresses
    if((nxt_caps xor nxt_shift) = '1') then
       ascii_addr_shift <= nxt_rom_address;
    else
      ascii_addr <= nxt_rom_address;
    end if;
      
    
         
  end process;
  
  kbd_clk_oe <= clk_oe;
  kbd_data_oe <= data_oe;
  kbd_data_out <= ps_dataout;
  ps_datain <= kbd_data_in;
  kbd_clk_out <= ps_clkout;
  ps_clkin <= kbd_clk_in; 
  
  
end architecture rtl;
