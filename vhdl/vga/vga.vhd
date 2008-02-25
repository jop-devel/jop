--
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2007, Christof Pitter
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


-------------------------------------------------------------
-- VGA-Ansteuerung mit dspio Board
-- 
-- 80 MHz, Auflösung 1024*768
-------------------------------------------------------------

-- TODO: 	* Use dual port ram
--				* Get rid of Altera dpram
--				* WAIT dazwischen herausgegeben jedoch leider geht dann JOP in die Knie
--				* neuer Arbiter hat dieses Problem gelöst
-- 190307: added access_scheme whether VGA accesses the Memory in blocked 
--         (access_scheme = '0') or spread mode (access_scheme = '1')	
--       : This version was used for the FPL paper			 


LIBRARY ieee;
USE ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity vga is
port
(
	clk   : in std_logic;
	reset : in std_logic;
	rgb : out std_logic_vector(3 downto 0);
	h_out	: out std_logic;
	v_out	: out std_logic;
	
	-- SimpCon interface

	address		: out std_logic_vector(22 downto 0);
	wr_data		: out std_logic_vector(31 downto 0);
	rd, wr		: out std_logic;
	rd_data		: in std_logic_vector(31 downto 0);
	rdy_cnt		: in unsigned(1 downto 0)

);
end vga;

architecture test of vga is

--
--  78.75 MHz clock, 1024*768, f_h = 60.02(P)kHz, f_v = 75.03(P)Hz 
--	positive Sync pulses!!!
constant time_a	    : std_logic_vector(10 downto 0) := "10100111111"; -- 1312
constant time_b	    : std_logic_vector(10 downto 0) := "00001011111"; -- 95
constant time_bc	  : std_logic_vector(10 downto 0) := "00100001111"; -- 271
constant time_bcd	  : std_logic_vector(10 downto 0) := "10100001111"; -- 1295

constant time_o		  : std_logic_vector(10 downto 0) := "01100100101"; -- 800
constant time_p		  : std_logic_vector(10 downto 0) := "00000000011"; -- 2
constant time_pq	  : std_logic_vector(10 downto 0) := "00000011110"; -- 30
constant time_pqr	  : std_logic_vector(10 downto 0) := "01100011110"; -- 798

signal hsync_counter 	: std_logic_vector(10 downto 0);
signal vsync_counter 	: std_logic_vector(10 downto 0);
signal h_sync		 			: std_logic;
signal v_sync		 			: std_logic;
signal h_enable	 			: std_logic;
signal h_reset		 		: std_logic;
signal rgb_reset   	  : std_logic;
signal v_reset     	  : std_logic;
signal rgb_en	 	 			: std_logic;

TYPE h_rgb_state_type is (start, wait_high, wait_low);
signal h_state		: h_rgb_state_type;
signal rgb_state	: h_rgb_state_type;

TYPE v_state_type is (start, wait_high, wait_q, wait_h, wait_low);
signal v_state		: v_state_Type;

signal line_count	: std_logic_vector(10 downto 0);
signal col_count	: std_logic_vector(10 downto 0);
signal h_flag		: std_logic;
signal rgb_flag		: std_logic;

-- read data in to the vga RAM
type state_type is (IDLE, REQUEST, WAIT1, RECEIVE);
signal current_state, next_state : state_type;
signal counter 	: unsigned(3 downto 0) := (others => '0');
signal c_en : std_logic:= '1';

-- write data from vga RAM to VGA
type state_type_VGA is (IDLE_RAM, READ_A, RDY_A, CHANGE_A, RES_A, 
												SPREAD_RES_A, READ_B, RDY_B, CHANGE_B, RES_B, SPREAD_RES_B, STOP);
signal this_state, follow_state : state_type_VGA;


-- Funktionen, die das Leben etwas erleichtern

    function Extend (X: Std_Logic_Vector; LENGTH : positive) 
      return Std_Logic_Vector is
    begin
      return IEEE.std_logic_arith.ext(X,LENGTH);
    end;


    function Max(LEFT, RIGHT : integer) return integer is
    begin
        if left > right then
            return left;
        else
            return right;
        end if;
    end;


 	function "+" (LEFT, RIGHT: Std_Logic_Vector) return Std_Logic_Vector is
        variable RESULT,l1,r1 : Std_Logic_Vector(
                    max(left'length,right'length)-1 downto 0);
    begin
        l1 := extend(left,result'length);
        r1 := extend(right,result'length);
		return(std_logic_vector(unsigned(l1)+unsigned(r1)));
    end;

component dpram is
PORT
	(
		address_a		: IN STD_LOGIC_VECTOR (6 DOWNTO 0);
		address_b		: IN STD_LOGIC_VECTOR (6 DOWNTO 0);
		clock		: IN STD_LOGIC ;
		data_a		: IN STD_LOGIC_VECTOR (31 DOWNTO 0);
		data_b		: IN STD_LOGIC_VECTOR (31 DOWNTO 0);
		wren_a		: IN STD_LOGIC  := '1';
		wren_b		: IN STD_LOGIC  := '1';
		q_a		: OUT STD_LOGIC_VECTOR (31 DOWNTO 0);
		q_b		: OUT STD_LOGIC_VECTOR (31 DOWNTO 0)
	);
end component;

signal address_a : std_logic_vector(6 downto 0);
signal address_b : std_logic_vector(6 downto 0);
signal data_a	 : std_logic_vector(31 downto 0);
signal data_b   : std_logic_vector(31 downto 0);
signal wren_a	: std_logic;
signal wren_b	: std_logic;
signal q_a	 : std_logic_vector(31 downto 0);
signal q_b   : std_logic_vector(31 downto 0);

signal counter_write : unsigned (7 downto 0);
signal counter_read  : unsigned (7 downto 0);
signal mem_vga_counter : unsigned(22 downto 0) := "00000101000000000000000";
signal counter8 : unsigned (3 downto 0);
signal wait_counter : unsigned (15 downto 0);
signal reset_counter : unsigned (29 downto 0);

signal access_scheme : std_logic;
signal spread_cnt : unsigned (3 downto 0);
-- jetzt geht's los

begin

access_scheme <= '0';

--rd <= '0';
--wr <= '0';
--wr_data <= (others => '0');
--address <= (others => '0');

---------- zuerst der HSYNC-Teil

	process(clk) -- zählt hsync_counter in die Höhe
	begin
		if rising_edge(clk) then
			if h_reset='1' then hsync_counter<=(others => '0');
			else hsync_counter<=hsync_counter+"00000000001";
			end if;
		end if;
	end process;

	process(clk)
	begin
		if rising_edge(clk) then
			if reset='1' then 
				h_state <= start;
				h_sync <= '0';
				h_reset <= '1'; 
			else 
				case h_state is
					when start =>
						h_reset <= '0';
						h_sync <= '1';
						h_state <= wait_high;
					when wait_high =>
						if hsync_counter=time_b then
							h_sync <= '0';
							h_state <= wait_low;
						end if;
					when wait_low =>
						if hsync_counter=time_a then
							h_reset <= '1';
							h_state <= start;
						end if;
				end case;
			end if;
		end if;
	end process;

-- dann der RGB-Teil

	process(clk)
	begin
		if rising_edge(clk) then
			if reset='1' then 
				rgb_state <= start;
				rgb_en <= '0';
			else 
				case rgb_state is
				when start =>
						rgb_state <= wait_low;
					when wait_low =>
						if hsync_counter=time_bc then
							rgb_en <= '1';
							rgb_state <= wait_high;
						end if;
					when wait_high =>
						if hsync_counter=time_bcd then
							rgb_en <='0';
							rgb_state <= start;
						end if;
				end case;
			end if;
		end if;
	end process;


-- der VSYNC-Teil

	process(clk)
	begin
		if rising_edge(clk) then
			if v_reset='1' then vsync_counter <= (others=>'0');
			elsif h_state = start then vsync_counter <= vsync_counter+"00000000001";
			end if;
		end if;
	end process;

	process(clk)
	begin
		if rising_edge(clk) then
			if reset='1' then
				v_state <= start;
				v_sync <= '0';
				v_reset <= '1';
				h_enable <= '0';
			elsif h_state=start then
				case v_state is
					when start =>
						v_sync <= '1';
						v_reset <= '0';
						v_state <= wait_high;
					when wait_high =>
						if vsync_counter=time_p then
							v_sync <= '0';
							v_state <= wait_q;
						end if;
					when wait_q =>
						if vsync_counter=time_pq then
							h_enable <= '1';
							v_state <= wait_h;
						end if;
					when wait_h =>
						if vsync_counter=time_pqr then
							h_enable <= '0';
							v_state <= wait_low;
						end if;
					when wait_low =>
						if vsync_counter=time_o then
							v_reset <= '1';
							v_state <= start;
						end if;
				end case;
			end if;
		end if;
	end process;


-- und die eigentliche Ausgabe

h_out <= h_sync; 
v_out <= v_sync;

-----------------------------------------------------------------
-- write data to the VGA 

vga_ram : dpram port map
	(
		address_a	=> address_a,	
		address_b	=> address_b,
		clock	=> clk,
		data_a => data_a,
		data_b => data_b,
		wren_a => wren_a,
		wren_b => wren_b,
		q_a => q_a,
		q_b	=> q_b
	);
	
	

-- Einlesen von 128*32-bit aus dem externen Speicher in den VGA dpram.
-- Ausgabe von diesen Vektoren auf VGA funktioniert!!!!
process(clk, reset)
begin
	if reset = '1' then
		this_state <= IDLE_RAM;
		counter_read <= (others => '0');
		counter_write <= (others => '0');
		counter8 <= (others => '0');
		mem_vga_counter <= "00000101000000000000000"; -- first address of the ext mem for VGA
		wr <= '0';
		rd <= '0';
		spread_cnt <= "0000";
		
	elsif rising_edge(clk) then
		this_state <= follow_state;

		case follow_state is
			when IDLE_RAM =>
			
			when READ_A =>
			-- READ FROM EXT MEM
				if (counter_read < 128) then
					wren_a <= '1'; -- write enable for dpram A
					address_a <= std_logic_vector(counter_read(6 downto 0)); -- addr for dpram
					rd <= '1'; -- rd enable for external memory
					address <= std_logic_vector(mem_vga_counter); -- address for ext memory
				end if;
				
			-- WRITE TO VGA
				wren_b <= '0';
				if rgb_en = '1' and h_enable = '1' then
					if counter_write < 128 then
						address_b <= std_logic_vector(counter_write(6 downto 0));
					
						case counter8 is
							when to_unsigned(0,4) => rgb <= q_b(3 downto 0);
							when to_unsigned(1,4) => rgb <= q_b(7 downto 4);
							when to_unsigned(2,4) => rgb <= q_b(11 downto 8);
							when to_unsigned(3,4) => rgb <= q_b(15 downto 12);
							when to_unsigned(4,4) => rgb <= q_b(19 downto 16);
							when to_unsigned(5,4) => rgb <= q_b(23 downto 20);
							when to_unsigned(6,4) => rgb <= q_b(27 downto 24);
							when to_unsigned(7,4) => rgb <= q_b(31 downto 28); 
							when others =>
						end case;
						counter8 <= counter8 + 1; 
						if counter8 = 7 then
							counter8 <= "0000";
							counter_write <= counter_write + 1;
						end if;
					end if;					                                                                                    
				else
					rgb <= "0000";
				end if;
								
			when RDY_A =>
				spread_cnt <= spread_cnt + 1;
			
			-- READ FROM EXT MEM
				rd <= '0';
				if counter_read < 128 then
					if rdy_cnt = "00" then -- data is available
						data_a <= rd_data; -- stores data in dpram
						if access_scheme = '0' then
							mem_vga_counter <= mem_vga_counter + 1; -- increases addr of ext memory 
							counter_read <= counter_read + 1; -- increases addr of dpram 
						end if;
					end if;
				end if;
					
			-- WRITE TO VGA
				wren_b <= '0';
				if rgb_en = '1' and h_enable = '1' then
					if counter_write < 128 then
						address_b <= std_logic_vector(counter_write(6 downto 0));
					
						case counter8 is
							when to_unsigned(0,4) => rgb <= q_b(3 downto 0);
							when to_unsigned(1,4) => rgb <= q_b(7 downto 4);
							when to_unsigned(2,4) => rgb <= q_b(11 downto 8);
							when to_unsigned(3,4) => rgb <= q_b(15 downto 12);
							when to_unsigned(4,4) => rgb <= q_b(19 downto 16);
							when to_unsigned(5,4) => rgb <= q_b(23 downto 20);
							when to_unsigned(6,4) => rgb <= q_b(27 downto 24);
							when to_unsigned(7,4) => rgb <= q_b(31 downto 28); 
							when others =>
						end case;
						counter8 <= counter8 + 1; 
						if counter8 = 7 then
							counter8 <= "0000";
							counter_write <= counter_write + 1;
						end if;
					end if;					                                                                                    
				else
					rgb <= "0000";
				end if;
			
		-- only entered if access_scheme = '1'	
			when SPREAD_RES_A =>
				spread_cnt <= "0000";
				mem_vga_counter <= mem_vga_counter + 1; -- increases addr of ext memory 
				counter_read <= counter_read + 1;
				
			when CHANGE_A =>
				wren_a <= '0';
				counter_read <= (others => '0');
				wren_b <= '1';
				counter_write <= (others => '0');
				counter8 <= (others => '0');
				rd <= '0';
				wr <= '0';
				spread_cnt <= "0000";
			
			when RES_A =>
				mem_vga_counter <= "00000101000000000000000";
				
			when READ_B =>
			-- READ FROM EXT MEM
				if (counter_read < 128) then
					wren_b <= '1'; -- write enable for dpram
					address_b <= std_logic_vector(counter_read(6 downto 0)); -- addr for dpram
					rd <= '1'; -- rd enable for external memory
					address <= std_logic_vector(mem_vga_counter); -- address for ext memory
				end if;
							
			-- WRITE TO VGA
				wren_a <= '0';
				if rgb_en = '1' and h_enable = '1' then
					if counter_write < 128 then
						address_a <= std_logic_vector(counter_write(6 downto 0));
						
						case counter8 is
							when to_unsigned(0,4) => rgb <= q_a(3 downto 0);
							when to_unsigned(1,4) => rgb <= q_a(7 downto 4);
							when to_unsigned(2,4) => rgb <= q_a(11 downto 8);
							when to_unsigned(3,4) => rgb <= q_a(15 downto 12);
							when to_unsigned(4,4) => rgb <= q_a(19 downto 16);
							when to_unsigned(5,4) => rgb <= q_a(23 downto 20);
							when to_unsigned(6,4) => rgb <= q_a(27 downto 24);
							when to_unsigned(7,4) => rgb <= q_a(31 downto 28); 
							when others =>
						end case;
						counter8 <= counter8 + 1; 
						if counter8 = 7 then
							counter8 <= "0000";
							counter_write <= counter_write + 1;
						end if;		
					end if;					                                                                                    
				else
					rgb <= "0000";
				end if;
				
			when RDY_B =>
				spread_cnt <= spread_cnt + 1;
				
			-- READ FROM EXT MEM
				rd <= '0';
				if counter_read < 128 then
					if rdy_cnt = "00" then -- data is available
						data_b <= rd_data; -- stores data in dpram
						if access_scheme = '0' then 
							mem_vga_counter <= mem_vga_counter + 1; -- increases addr of ext memory 
							counter_read <= counter_read + 1; -- increases addr of dpram 
						end if;
					end if;
				end if;
				
			-- WRITE TO VGA
				wren_a <= '0';
				if rgb_en = '1' and h_enable = '1' then
					if counter_write < 128 then
						address_a <= std_logic_vector(counter_write(6 downto 0));
						
						case counter8 is
							when to_unsigned(0,4) => rgb <= q_a(3 downto 0);
							when to_unsigned(1,4) => rgb <= q_a(7 downto 4);
							when to_unsigned(2,4) => rgb <= q_a(11 downto 8);
							when to_unsigned(3,4) => rgb <= q_a(15 downto 12);
							when to_unsigned(4,4) => rgb <= q_a(19 downto 16);
							when to_unsigned(5,4) => rgb <= q_a(23 downto 20);
							when to_unsigned(6,4) => rgb <= q_a(27 downto 24);
							when to_unsigned(7,4) => rgb <= q_a(31 downto 28); 
							when others =>
						end case;
						counter8 <= counter8 + 1; 
						if counter8 = 7 then
							counter8 <= "0000";
							counter_write <= counter_write + 1;
						end if;		
					end if;					                                                                                    
				else
					rgb <= "0000";
				end if;
			
			-- only entered if access_scheme = '1'
			when SPREAD_RES_B =>
				spread_cnt <= "0000";
				mem_vga_counter <= mem_vga_counter + 1; -- increases addr of ext memory 
				counter_read <= counter_read + 1;
			
			when CHANGE_B =>
				wren_b <= '0';
				counter_read <= (others => '0');
				wren_a <= '1';
				counter_write <= (others => '0');
				counter8 <= (others => '0');
				rd <= '0';
				wr <= '0';
				spread_cnt <= "0000";
				
			when RES_B =>
				mem_vga_counter <= "00000101000000000000000";
				
			when STOP =>  
				if rgb_en = '1' and h_enable = '1' then
					rgb <= "1110";
				else
					rgb <= "0000";
				end if;
			end case;
	end if;
end process;

process(this_state, counter_read, counter_write, rdy_cnt, mem_vga_counter, 
wait_counter, v_state, h_state, hsync_counter, access_scheme, spread_cnt)
	begin
		case this_state is
			when IDLE_RAM =>
				if ( (v_state = start) and (h_state = start) ) then
					follow_state <= READ_A;
				else
					follow_state <= IDLE_RAM;
				end if;

			when READ_A =>
				if ( hsync_counter = time_a ) then
					follow_state <= CHANGE_A;
				else
					follow_state <= RDY_A;
				end if;
				
			when RDY_A =>
				if ( hsync_counter = time_a ) then
				--if ((counter_read = 128) and (counter_write = 128)) then
					follow_state <= CHANGE_A;
				elsif (rdy_cnt = "00" and counter_read < 128) then
					if (access_scheme = '0') then
						follow_state <= READ_A;
					else
						if spread_cnt = 8 then
							follow_state <= SPREAD_RES_A;
						else
							follow_state <= RDY_A;
						end if;
					end if;
				else
					follow_state <= RDY_A;
				end if;
				
			when SPREAD_RES_A =>
					follow_state <= READ_A;
				
			when CHANGE_A =>
				if mem_vga_counter = "00001000000000000000000" then	
					follow_state <= RES_A;
				else
					follow_state <= READ_B;
				end if;
			
			when RES_A =>
				follow_state <= READ_B;
						
			when READ_B =>
				if (hsync_counter = time_a) then
					follow_state <= CHANGE_B;
				else
					follow_state <= RDY_B;
				end if;					
				
			when RDY_B =>
				if (hsync_counter = time_a) then
				--if ((counter_read = 128) and (counter_write = 128)) then
					follow_state <= CHANGE_B;
				elsif (rdy_cnt = "00" and counter_read < 128) then
					if (access_scheme = '0') then
						follow_state <= READ_B;
					else
						if spread_cnt = 8 then
							follow_state <= SPREAD_RES_B;
						else
							follow_state <= RDY_B;
						end if;
					end if;
				else
					follow_state <= RDY_B;
				end if;
					
			when SPREAD_RES_B =>
					follow_state <= READ_B;
					
			when CHANGE_B =>
				if mem_vga_counter = "00001000000000000000000" then
					follow_state <= RES_B;
				else
					follow_state <= READ_A;
				end if;
			
			when RES_B =>
					follow_state <= READ_A;
			
			when STOP =>
				follow_state <= STOP;
		end case;
end process;

end test;
