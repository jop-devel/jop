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

-- **************************************
-- serializer architecture
-- **************************************

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use ieee.numeric_std.all;

library WORK;
use WORK.all;


architecture rtl of serializer_1to4 is

  signal NXT_REG, ACT_REG :std_logic_vector(PARALLEL_WIDTH-1 downto 0);
  
  signal nxt_enable, act_enable,nxt_empty,act_empty :std_logic;
  
  signal NXT_SERCNT, ACT_SERCNT :std_logic_vector(1 downto 0);
begin
  
  sync_serializer: process(CLK,reset)
  begin
    if(reset = '1') then
      ACT_SERCNT <= B"00";
      ACT_REG <= (others => '0');
      act_enable <= '0';
      act_empty <= '0';
    elsif(rising_edge(CLK)) then
      ACT_SERCNT <= NXT_SERCNT;
      ACT_REG <= NXT_REG;
      act_enable <= nxt_enable;
      act_empty <= nxt_empty;
    end if;
  end process;
  
  comb_serializer: process(ACT_SERCNT,ld,ACT_REG,d_in,act_enable,enable,act_empty)
  begin
    NXT_SERCNT <= ACT_SERCNT;
    NXT_REG <= ACT_REG;
    nxt_enable <= act_enable;
    nxt_empty <= act_empty;
    d_out <= (others => '0');
        
    if(ld = '1') then
      NXT_REG <= d_in;
      NXT_SERCNT <= (others => '0');
      nxt_enable <= '1';
      nxt_empty <= '1';
    end if;
    
    if(enable = '1') then

     NXT_SERCNT <= ACT_SERCNT + '1';
      
     case ACT_SERCNT is
      when B"00" =>
        d_out <= ACT_REG(SERIALIZED_WIDTH-1 downto 0);  
      when B"01" =>
        d_out <= ACT_REG((SERIALIZED_WIDTH*2)-1 downto SERIALIZED_WIDTH);
      when B"10" =>
        d_out <= ACT_REG((SERIALIZED_WIDTH*3)-1 downto (SERIALIZED_WIDTH*2));
        --tell one cycle earlier that you are empty - it takes one cycle to load next
        --parallel word
        nxt_empty <= '0';
      when B"11" =>
        d_out <= ACT_REG((SERIALIZED_WIDTH*4)-1 downto (SERIALIZED_WIDTH*3));
        nxt_enable <= '0';
      when others => 
			nxt_empty <= '0';
			NXT_SERCNT <= B"00";
     end case;
     
  end if;
   
     
  end process;
  
  empty <= act_empty;
end architecture rtl;
