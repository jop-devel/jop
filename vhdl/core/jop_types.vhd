--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
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


--
--	jop_types.vhd
--
--	package type definitions definitions
--

library ieee;
use ieee.std_logic_1164.all;

package jop_types is

	constant EXTA_WIDTH : integer := 4;
  
--
--	extension address constants (used in extension.vhd and Instruction.java)
--
--		  7	st	wraddr		        store write address        

--	        8+0	st	mem_rd_addr		start read
--	          0	ld	mem_rd_data		read data
--	        8+1	st	mem_wr_data		start write
--              8+2     st      start array load
--              8+3     st      start array store
--	        8+4	st      start getfield
--	        8+5	st      start putfield
--	        8+6	st	mul operand a, b and start mul
--	          6	ld	mul result        
--	        8+7	st	start bytecode load (or cache)
--	          7	ld	read new pc base (for cache version)
--
	constant STMUL	: std_logic_vector(EXTA_WIDTH-1 downto 0) := "0110";
	constant STMWA	: std_logic_vector(EXTA_WIDTH-1 downto 0) := "0111"; 

	constant STMRA	: std_logic_vector(EXTA_WIDTH-1 downto 0) := "1000"; 
	constant STMWD	: std_logic_vector(EXTA_WIDTH-1 downto 0) := "1001"; 
	constant STALD	: std_logic_vector(EXTA_WIDTH-1 downto 0) := "1010"; 
	constant STAST	: std_logic_vector(EXTA_WIDTH-1 downto 0) := "1011"; 
	constant STGF	: std_logic_vector(EXTA_WIDTH-1 downto 0) := "1100"; 
	constant STPF	: std_logic_vector(EXTA_WIDTH-1 downto 0) := "1101"; 
	constant STCP	: std_logic_vector(EXTA_WIDTH-1 downto 0) := "1110"; 
	constant STBCR	: std_logic_vector(EXTA_WIDTH-1 downto 0) := "1111"; 

	constant LDMRD	        : std_logic_vector(EXTA_WIDTH-1 downto 0) := "0000"; 
	constant LDMUL	        : std_logic_vector(EXTA_WIDTH-1 downto 0) := "0110"; 
	constant LDBCSTART      : std_logic_vector(EXTA_WIDTH-1 downto 0) := "0111"; 

	type mem_in_type is record
		rd		: std_logic;
		wr		: std_logic;
		addr_wr	        : std_logic;
		bc_rd           : std_logic;
		iaload          : std_logic;
		iastore	        : std_logic;
		getfield        : std_logic;
		putfield        : std_logic;
		copy            : std_logic;
	end record;

	type mem_out_type is record
		dout		: std_logic_vector(31 downto 0);
		bcstart		: std_logic_vector(31 downto 0); 	-- start of method in bc cache
		bsy		: std_logic;
	end record;

	type exception_type is record
		spov	: std_logic;	-- stack overflow
		np		: std_logic;	-- null pointer
		ab		: std_logic;	-- array out of bounds
	end record;

	constant EXC_SPOV	: std_logic_vector(2 downto 0) := "001";
	constant EXC_NP		: std_logic_vector(2 downto 0) := "010";
	constant EXC_AB		: std_logic_vector(2 downto 0) := "011";

	-- interrupt and exception request to bcfetch
	type irq_bcf_type is record
		irq			: std_logic;	-- interrupt request, single cycle
		exc			: std_logic;	-- exception request, singel cycle
		ena			: std_logic;	-- globale enable
	end record;

	-- interrupt and exception ack when jfetch the interrupt bytecode
	type irq_ack_type is record
		ack_irq		: std_logic;	-- interrupt ack from bcfetch, single cycle
		ack_exc		: std_logic;	-- exception ack from bcfetch
	end record;


	type ser_in_type is record
		rxd			: std_logic;
		ncts		: std_logic;
	end record;
	type ser_out_type is record
		txd			: std_logic;
		nrts		: std_logic;
	end record;

	-- not usefull as it's inout
	type io_port_type is record
		l	: std_logic_vector(20 downto 1);
		r	: std_logic_vector(20 downto 1);
		t	: std_logic_vector(6 downto 1);
		b	: std_logic_vector(10 downto 1);
	end record;

	type irq_in_array_type is array (integer range <>) of irq_bcf_type;
	type irq_out_array_type is array (integer range <>) of irq_ack_type;
	type exception_array_type is array (integer range <>) of exception_type;
	
	-- CMP synchronization
	type sync_in_type is record
		s_in		: std_logic;
		lock_req	: std_logic;
	end record;
	
	type sync_out_type is record
		s_out	: std_logic;
		halted  : std_logic;
	end record;
	
	constant NO_SYNC : sync_out_type := (s_out => '0', halted => '0');
	type sync_in_array_type is array (integer range <>) of sync_in_type;
	type sync_out_array_type is array (integer range <>) of sync_out_type;

	-- constant for method length bits
	-- changes here require also the object layout to be changed
	constant METHOD_SIZE_BITS : integer := 10;
	
end jop_types;
