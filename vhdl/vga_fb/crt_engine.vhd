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
-- crt_engine architecture
-- **************************************

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use ieee.numeric_std.all;

library WORK;
use WORK.all;

architecture rtl of crt_engine is
  
  
  component serializer_1to4 is
  generic ( 
    PARALLEL_WIDTH :integer := 32;
    SERIALIZED_WIDTH :integer := 8
    );
  port ( 
    clk :in std_logic;
    reset :in std_logic;
    ld :in std_logic;
    enable :in std_logic;
    d_in :in std_logic_vector(PARALLEL_WIDTH-1 downto 0);
    d_out :out std_logic_vector(SERIALIZED_WIDTH-1 downto 0);
    empty :out std_logic
    );
end component;
  
  ---serializer definitions  
  signal pixel_out :std_logic_vector(7 downto 0);
  signal enable_serializer :std_logic;
  
  ---crt definitions
  type h_state is(HS_RESET,HS_PULSE,HSFRONT_PORCH,HS_PIXEL,HSBACK_PORCH);
  signal act_h_state, nxt_h_state :h_state;
  
  type v_state is(VS_RESET,VS_PULSE,VSFRONT_PORCH,VS_LINE,VSBACK_PORCH);
  signal act_v_state, nxt_v_state :v_state;
  
  constant HPULSE_TOP :std_logic_vector(HSC_WIDTH-1 downto 0) := std_logic_vector(to_unsigned(HS_PULSE_TOP,HSC_WIDTH));
  constant HFPORCH_TOP :std_logic_vector(HSC_WIDTH-1 downto 0) := std_logic_vector(to_unsigned(HS_FPORCH_TOP,HSC_WIDTH));
  constant HPIXEL_TOP :std_logic_vector(HSC_WIDTH-1 downto 0) := std_logic_vector(to_unsigned(HS_PIXEL_TOP,HSC_WIDTH));
  constant HBPORCH_TOP :std_logic_vector(HSC_WIDTH-1 downto 0) := std_logic_vector(to_unsigned(HS_BPORCH_TOP,HSC_WIDTH));
  
  constant VPULSE_TOP :std_logic_vector(VSC_WIDTH-1 downto 0) := std_logic_vector(to_unsigned(VS_PULSE_TOP,VSC_WIDTH));
  constant VFPORCH_TOP :std_logic_vector(VSC_WIDTH-1 downto 0) := std_logic_vector(to_unsigned(VS_FPORCH_TOP,VSC_WIDTH));
  constant VLINE_TOP :std_logic_vector(VSC_WIDTH-1 downto 0) := std_logic_vector(to_unsigned(VS_LINE_TOP,VSC_WIDTH));
  constant VBPORCH_TOP :std_logic_vector(VSC_WIDTH-1 downto 0) := std_logic_vector(to_unsigned(VS_BPORCH_TOP,VSC_WIDTH));
  
  
  signal act_hs_cnt, nxt_hs_cnt :std_logic_vector(HSC_WIDTH-1 downto 0);
  signal act_vs_cnt, nxt_vs_cnt :std_logic_vector(VSC_WIDTH-1 downto 0);
  signal act_col_cnt,nxt_col_cnt :std_logic_vector(HSC_WIDTH-1 downto 0);
  
  signal reset_line_cnt,reset_hs_cnt,reset_vs_cnt,reset_col_cnt :std_logic;
  signal enable_line_cnt,enable_hs_cnt,enable_vs_cnt,enable_col_cnt :std_logic;
  signal act_line_cnt, nxt_line_cnt :std_logic_vector(VSC_WIDTH-1 downto 0);
  signal hsync, vsync, hblank, vblank :std_logic;
  
begin
 
  
  word_serializer : serializer_1to4
  generic map( 
    PARALLEL_WIDTH => 32,
    SERIALIZED_WIDTH => 8)
  port map( 
    clk => pixel_clk,
    reset => async_fifo_reset,
    enable => enable_serializer,
    ld => ld_serializer,
    d_in => fifo2crt,
    d_out => pixel_out,
    empty => serializer_empty
    );
  
  --route clk to dacs
  dac_clk <= pixel_clk;
  
  sync_counter : process(pixel_clk,reset) 
  begin
    if(reset = '1') then
      act_hs_cnt <= (others => '0');
      act_vs_cnt <= (others => '0');
      act_line_cnt <= (others => '0');
      act_col_cnt <= (others => '0');
    elsif (rising_edge(pixel_clk)) then
      act_hs_cnt <= nxt_hs_cnt;
      act_vs_cnt <= nxt_vs_cnt;
      act_line_cnt <= nxt_line_cnt;
      act_col_cnt <= nxt_col_cnt;
    end if;
  end process;
  
  comb_counter : process(act_col_cnt,enable_col_cnt,reset_col_cnt,act_line_cnt,reset_line_cnt,enable_line_cnt,act_hs_cnt,act_vs_cnt,reset_hs_cnt,reset_vs_cnt,enable_vs_cnt,enable_hs_cnt)
  begin
    nxt_hs_cnt <= act_hs_cnt;
    nxt_vs_cnt <= act_vs_cnt;
    nxt_line_cnt <= act_line_cnt;
    nxt_col_cnt <= act_col_cnt;
    
    --horizontal sycnhronization counter
    if(reset_hs_cnt = '1') then
      nxt_hs_cnt <= (others => '0');
    else
      if(enable_hs_cnt = '1') then
        nxt_hs_cnt <= act_hs_cnt + '1';
      end if;
    end if;
    
    --vertical sync counter
    if(reset_vs_cnt = '1') then
      nxt_vs_cnt <= (others => '0');
    else
      if(enable_vs_cnt = '1') then
        nxt_vs_cnt <= act_vs_cnt + '1';
      end if;
    end if;
     
    --line counter, =+1 if 640 pixels have been written 
    if(reset_line_cnt = '1') then
      nxt_line_cnt <= (others => '0');
    else
      if(enable_line_cnt = '1') then
        nxt_line_cnt <= act_line_cnt + '1';
      end if;
    end if;
    
    --column counter =+1 if a line has been written out
    if(reset_col_cnt = '1') then
      nxt_col_cnt <= (others => '0');
    else
      if(enable_col_cnt = '1') then
        nxt_col_cnt <= act_col_cnt + '1';
      end if;
    end if;
    
  end process;
  
  
  sync_hs_vs_fsm: process(pixel_clk,reset)
  begin
    if(reset = '1') then
      act_h_state <= HS_RESET;
      act_v_state <= VS_RESET;
      
    elsif(rising_edge(pixel_clk)) then
      act_h_state <= nxt_h_state;
      act_v_state <= nxt_v_state;
  
    end if;
  end process;
 
 
hs_fsm: process(act_h_state,act_hs_cnt,act_line_cnt,act_col_cnt)
  begin
    nxt_h_state <= act_h_state;
  
    hsync <= '1';
    hblank <= '0';
    enable_hs_cnt <= '0';
    reset_hs_cnt <= '0';
    reset_col_cnt <= '0';
    enable_line_cnt <= '0';
    enable_col_cnt <= '0';
    enable_vs_cnt <= '0';
    
    case act_h_state is
      when HS_RESET => nxt_h_state <= HS_PULSE;
        
      when HS_PULSE => 
        enable_hs_cnt <= '1';
        hsync <= '0';
        hblank <= '1';
        
        if(act_hs_cnt = (HPULSE_TOP)) then
          enable_hs_cnt <= '0';
          hsync <= '1';
          nxt_h_state <= HSBACK_PORCH;
        end if;
        
        
      when HSFRONT_PORCH =>
        enable_hs_cnt <= '1';
        hblank <= '1';
        
        if(act_hs_cnt = (HPULSE_TOP+HBPORCH_TOP+HPIXEL_TOP+HFPORCH_TOP)) then
          enable_hs_cnt <= '0';
          enable_line_cnt <= '1';
          enable_vs_cnt <= '1';
          reset_hs_cnt <= '1';
          nxt_h_state <= HS_PULSE;
        end if;
        
        
      when HS_PIXEL =>
        enable_hs_cnt <= '1';
        enable_col_cnt <= '1';
        
        --if(act_hs_cnt = (HPULSE_TOP+HBPORCH_TOP+HPIXEL_TOP)-1) then
        if(act_col_cnt = (HPIXEL_TOP)) then 
          reset_col_cnt <= '1';
          enable_col_cnt <= '0';
          enable_hs_cnt <= '0';
          hblank <= '1';
          nxt_h_state <= HSFRONT_PORCH;
        end if;
        
      when HSBACK_PORCH =>
        enable_hs_cnt <= '1';
        hblank <= '1';
        
        if(act_hs_cnt = (HPULSE_TOP+HBPORCH_TOP)) then
          enable_hs_cnt <= '0';
          hblank <= '0';
          nxt_h_state <= HS_PIXEL;
        end if;
        
      when others => nxt_h_state <= HS_RESET;
    end case;

    
  end process;

  vs_fsm: process(act_v_state,act_vs_cnt,act_line_cnt)
  begin
    nxt_v_state <= act_v_state;
    vsync <= '1';
    vblank <= '0';
    reset_vs_cnt <= '0';
    reset_line_cnt <= '0';
    
    case act_v_state is
      when VS_RESET => nxt_v_state <= VS_PULSE;
        
      when VS_PULSE =>
        vsync <= '0';
        vblank <= '1';
        
        if(act_vs_cnt = (VPULSE_TOP)) then
          nxt_v_state <= VSBACK_PORCH;
        end if;
        
      when VSFRONT_PORCH =>
        vblank <= '1';
        if(act_vs_cnt = (VPULSE_TOP+VBPORCH_TOP+VLINE_TOP+VFPORCH_TOP)) then
          reset_vs_cnt <= '1';
          nxt_v_state <= VS_PULSE;
        end if;
        
      when VS_LINE =>
        if(act_vs_cnt = (VPULSE_TOP+VBPORCH_TOP+VLINE_TOP)) then
          vblank <= '1';
          nxt_v_state <= VSFRONT_PORCH;
        end if;
        
        
      when VSBACK_PORCH =>
        vblank <= '1';
        
        if(act_vs_cnt = (VPULSE_TOP+VBPORCH_TOP)) then
		reset_line_cnt <= '1';
		vblank <= '0';
          nxt_v_state <= VS_LINE;
        end if;
        
      when others => nxt_v_state <= VS_RESET;
    end case;
  end process;
  
  
  rgb_mux :process(hblank,vblank,pixel_out,act_col_cnt,act_line_cnt)
  begin
    
  if(vblank = '1' or hblank = '1') then
    r <= (others => '0');
    g <= (others => '0');
    b <= (others => '0');
    enable_serializer <= '0'; 
    
    --clean do achieve 640*480 again   
    --START_COMMENTME
  elsif((act_col_cnt > (B"00" & X"9f") and act_col_cnt < (B"01" & X"e0")) and
		(act_line_cnt > (B"00" & X"77") and act_line_cnt < (B"01" & X"68")) ) then
	
	--add serailized pixel here
	--r(2 downto 0) <= B"111";
	r(6 downto 0) <= (others => '0');
	r(9 downto 7) <= pixel_out(7 downto  5);
    
	--g(2 downto 0) <= (others => '0');
	g(6 downto 0) <= (others => '0');
	g(9 downto 7) <= pixel_out(4 downto 2);
    
	--b(1 downto 0) <= (others => '0');
	b(7 downto 0) <= (others => '0');
	b(9 downto 8) <= pixel_out(1 downto 0);
    enable_serializer <= '1';
		
    --clean do achieve 640*480 again 
  elsif( (act_line_cnt = (B"00" & X"00") or act_col_cnt = (B"00" & X"00")) or ( act_line_cnt = (B"01" & X"df") or act_col_cnt = (B"10" & X"7f") ) ) then
	r <= (others => '0');
	g(6 downto 0) <= (others => '0');
    g(9 downto 7) <= B"100";
    b <= (others => '0');
    enable_serializer <= '0'; 
    --END_COMMENTME 
  else
	r <= (others => '0');
    g <= (others => '0');
    b <= (others => '0');
    enable_serializer <= '0';  
  end if;
  
  end process;
  
  h_blank <= hblank;
  v_blank <= vblank;
  
  h_sync <= hsync;
  v_sync <= vsync;
  
end architecture rtl;
