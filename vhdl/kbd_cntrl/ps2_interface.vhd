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

--the sender is implemented, but not tested, and not functional


-- **************************************
-- ps2_interface architecture
-- **************************************

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use ieee.numeric_std.all;

library WORK;
use WORK.all;

architecture rtl of ps2_interface is 


--sender/receiver fsm  
type rcv_state is(start_bit,data,parity,stop_bit);
signal nxt_rcv,act_rcv :rcv_state;

type snd_state is(handshake,start_bit,data,parity,stop_bit,ack,ack_finished);
signal nxt_snd,act_snd :snd_state;

--synchronisation regs
signal usync_clk,sync_clk,edge_clk :std_logic;
signal usync_data,sync_data,sample_bit :std_logic;

--controle registers
signal nxt_mute_rcv,act_mute_rcv :std_logic;
signal nxt_parity, act_parity :std_logic;
signal nxt_bit_cnt, act_bit_cnt :std_logic_vector(3 downto 0);
signal nxt_rcv_rdy,act_rcv_rdy,nxt_snd_rdy,act_snd_rdy :std_logic;

--handshake timeout counter
constant CALC_TIMEOUT :integer := (SYS_CLK_FREQ/10000)-1; --use 1 000 000 for simulation
constant TIMEOUT_TOP :std_logic_vector(TIMEOUT_BIT_WIDTH-1 downto 0) := std_logic_vector(to_unsigned(CALC_TIMEOUT,TIMEOUT_BIT_WIDTH));
signal nxt_timeout, act_timeout :std_logic_vector(TIMEOUT_BIT_WIDTH-1 downto 0);
signal nxt_enable_timeout,act_enable_timeout :std_logic;
--data registers
signal nxt_rcv_buf,act_rcv_buf :std_logic_vector(7 downto 0);
signal nxt_snd_buf,act_snd_buf :std_logic_vector(7 downto 0);
  
begin
  
  sync_ps2_interface: process(CLK,reset)
  begin
    if(reset = '1') then
      act_snd <= handshake;
      act_rcv <= start_bit;
      act_parity <= '0';
      act_bit_cnt <= (others => '0');
      act_rcv_rdy <= '0';
      act_snd_rdy <= '0';
      act_mute_rcv <= '0';
      act_timeout <= (others => '0');
      act_rcv_buf <= (others => '0');
      act_snd_buf <= (others => '0');
      act_enable_timeout <= '0';
      --sync regs clk
      usync_clk <= '0';
      sync_clk <= '0';
      edge_clk <= '0';
      --sync regs data
      usync_data <= '0';
      sync_data <= '0';
      sample_bit <= '0';
    elsif(rising_edge(CLK)) then
      act_snd <= nxt_snd;
      act_rcv <= nxt_rcv;
      act_parity <= nxt_parity;
      act_bit_cnt <= nxt_bit_cnt;
      act_rcv_rdy <= nxt_rcv_rdy;
      act_snd_rdy <= nxt_snd_rdy;
      act_mute_rcv <= nxt_mute_rcv;
      act_timeout <= nxt_timeout;
      act_rcv_buf <= nxt_rcv_buf;
      act_snd_buf <=  nxt_snd_buf;
      act_enable_timeout <= nxt_enable_timeout;
      --synchronize clk
      edge_clk <= sync_clk;
      sync_clk <= usync_clk;
      usync_clk <= ps_clkin;
      --synchronize data
      sample_bit <= sync_data;
      sync_data <= usync_data;
      usync_data <= ps_datain;
    end if;
  end process;
  
  comb_ps2_interface: process(nxt_rcv_rdy,data_in,act_enable_timeout,act_timeout,act_mute_rcv,rd_buf,wr_word,act_rcv_buf,act_snd_buf,act_snd,act_rcv,act_parity,act_bit_cnt,act_rcv_rdy,act_snd_rdy,edge_clk,sync_clk,sample_bit,sync_data)
  begin
    --default assignments
    nxt_snd <= act_snd;
    nxt_rcv <= act_rcv;
    nxt_parity <= act_parity;
    nxt_bit_cnt <= act_bit_cnt;
    nxt_rcv_rdy <= act_rcv_rdy;
    nxt_snd_rdy <= act_snd_rdy;
    nxt_mute_rcv <= act_mute_rcv;
    nxt_timeout <= act_timeout;
    nxt_enable_timeout <= act_enable_timeout;
    nxt_rcv_buf <= act_rcv_buf;
    nxt_snd_buf <= act_snd_buf;
    
    data_out <= (others => '0');
    parity_error <= '0';
    rcv_rdy <= '0';
    snd_rdy <= '0';
    
    --switch of clk and data outputs
    ps_clkout <= '1';
    ps_dataout <= '1';
    clk_oe <= '0';
    data_oe <= '0';
    
    --read from receiver buffer
    if(rd_buf = '1' and nxt_rcv_rdy = '1') then
      data_out <= act_rcv_buf;
    end if;
    
    --write to sender buffer and mute receiver, if receiving is not complete ignore write access
    --if(wr_word = '1') then -- and act_rcv_rdy = '1') then
    --  nxt_mute_rcv <= '1';
    --  nxt_enable_timeout <= '1';
    --  nxt_snd_buf <= data_in;
    --end if;
    
  --if(act_mute_rcv = '0') then
    --receiver machine
    case(act_rcv) is
    when start_bit =>
      --detect falling clk edge, and make sure its a start bit
      if((edge_clk = '1' and sync_clk = '0') and sample_bit = '0') then
        nxt_bit_cnt <= (others => '0');
        nxt_rcv_rdy <= '0';
        nxt_parity <= '0';
        nxt_rcv <= data;
      end if;
      
    when data =>
      if(edge_clk = '1' and sync_clk = '0')  then
        nxt_bit_cnt <= act_bit_cnt + '1';
        nxt_rcv_buf(to_integer(unsigned(act_bit_cnt))) <= sample_bit;
        nxt_parity <= act_parity xor sample_bit;
        
        if(act_bit_cnt = X"7") then
			--nxt_parity <= not act_parity;
			nxt_bit_cnt <= (others =>'0');
			nxt_rcv <= parity;
		end if;
        
      end if;
      
    when parity =>
      if(edge_clk = '1' and sync_clk = '0')  then
        if(sample_bit /= act_parity) then
          parity_error <= '1';
        end if;
        nxt_rcv <= stop_bit;
      end if;
      
    when stop_bit =>
      if((edge_clk = '1' and sync_clk = '0') and sample_bit = '1')  then
        nxt_rcv_rdy <= '1';
        rcv_rdy <= '1';
        nxt_rcv <= start_bit;
      end if;
    end case;
 --else
      
    --sender machine
    --case(act_snd) is
    --when handshake =>    
    --  if(act_enable_timeout = '1') then
    --      nxt_snd_rdy <= '0';
    --      ps_clkout <= '0';
    --     clk_oe <= '1';
    --      nxt_timeout <= act_timeout + '1';
    --      
    --      if(act_timeout = (TIMEOUT_TOP-1)) then
    --        nxt_enable_timeout <= '0';
    --        --release clk line
    --        clk_oe <= '0';
    --        ps_clkout <= '1';
    --        --pull down data line
    --        data_oe <= '1';
    --        ps_dataout <= '0';
    --        nxt_snd <= start_bit;
    --      end if;
    --  end if;
    --    
    --when start_bit =>
    -- ps_dataout <= '0';
    --  data_oe <= '1';
    --  --nxt bit on rising clk_edge generated by partner device
    --  if(edge_clk = '1' and sync_clk = '0')  then 
    --    nxt_bit_cnt <= (others => '0'); --reset bit counter
    --    nxt_snd <= data;
    --  end if;
      
    --when data =>
    --  data_oe <= '1';
    --  ps_dataout <= act_snd_buf(to_integer(unsigned(act_bit_cnt)));
      
    --  if(edge_clk = '1' and sync_clk = '0')  then
    --    nxt_bit_cnt <= act_bit_cnt + '1';
    --    nxt_parity <= act_parity xor act_snd_buf(to_integer(unsigned(act_bit_cnt)));
    --    
    --    if(act_bit_cnt = X"7") then
    --      --nxt_parity <= not act_parity;
    --      nxt_snd <= parity;
    --   end if;
    --  end if;
    
      
    --when parity =>
    --  data_oe <= '1';
    --  ps_dataout <=  act_parity;
    --  if(edge_clk = '1' and sync_clk = '0')  then
    --    nxt_snd <= stop_bit;
    --  end if;
      
    --when stop_bit =>
    --  data_oe <= '1';
    --  ps_dataout <= '1';
    --  if(edge_clk = '1' and sync_clk = '0')  then
    --    nxt_snd <= ack; 
    --  end if;
    --  
    --when ack =>
      --detect ack bit
    --  if(edge_clk = '1' and sync_clk = '0' and sample_bit = '0')  then
    --    nxt_snd_rdy <= '1';
    --    snd_rdy <= '1';
    --    nxt_bit_cnt <= (others => '0');
    --    nxt_parity <= '0';
    --    nxt_mute_rcv <= '0';
    --    nxt_snd <= ack_finished; 
    --  end if;
      
     --when ack_finished => 
	--	if(edge_clk = '0' and sync_clk = '1' and sample_bit = '1')  then
	--		nxt_snd <= handshake;
	--	end if;
    --end case;
  --end if; 
    
  end process;
  
  
end architecture rtl;