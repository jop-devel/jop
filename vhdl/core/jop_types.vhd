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

use work.jop_config_global.all;

package jop_types is

	constant MMU_WIDTH : integer := 4;
  
--
--	MMU instruction constants (used in jopcpu.vhd and Instruction.java)
--
	-- POP type
	constant STMUL	: std_logic_vector(MMU_WIDTH-1 downto 0) := "0000";
	constant STMWA	: std_logic_vector(MMU_WIDTH-1 downto 0) := "0001"; 
	constant STMRA	: std_logic_vector(MMU_WIDTH-1 downto 0) := "0010"; 
	constant STMWD	: std_logic_vector(MMU_WIDTH-1 downto 0) := "0011"; 
	constant STALD	: std_logic_vector(MMU_WIDTH-1 downto 0) := "0100"; 
	constant STAST	: std_logic_vector(MMU_WIDTH-1 downto 0) := "0101"; 
	constant STGF	: std_logic_vector(MMU_WIDTH-1 downto 0) := "0110"; 
	constant STPF	: std_logic_vector(MMU_WIDTH-1 downto 0) := "0111";
--
    constant STPFR	: std_logic_vector(MMU_WIDTH-1 downto 0) := "1111";	 
    constant STPSR	: std_logic_vector(MMU_WIDTH-1 downto 0) := "1111";	 
    constant STASTR	: std_logic_vector(MMU_WIDTH-1 downto 0) := "1111";
	
	constant STCP	: std_logic_vector(MMU_WIDTH-1 downto 0) := "1000"; 
	constant STBCR	: std_logic_vector(MMU_WIDTH-1 downto 0) := "1001"; 
	constant STIDX	: std_logic_vector(MMU_WIDTH-1 downto 0) := "1010"; 
	constant STPS	: std_logic_vector(MMU_WIDTH-1 downto 0) := "1011"; 
	constant STMRAC	: std_logic_vector(MMU_WIDTH-1 downto 0) := "1100"; 
	constant STMRAF	: std_logic_vector(MMU_WIDTH-1 downto 0) := "1101"; 
	constant STMWDF	: std_logic_vector(MMU_WIDTH-1 downto 0) := "1110"; 

	-- PUSH type
	constant LDMRD	   : std_logic_vector(MMU_WIDTH-1 downto 0) := "0000"; 
	constant LDMUL	   : std_logic_vector(MMU_WIDTH-1 downto 0) := "0001"; 
	constant LDBCSTART : std_logic_vector(MMU_WIDTH-1 downto 0) := "0010"; 

	-- no stack change
	constant STGS	   	: std_logic_vector(MMU_WIDTH-1 downto 0) := "0000"; 
	constant CINVAL	   	: std_logic_vector(MMU_WIDTH-1 downto 0) := "0001"; 
	constant ATMSTART	: std_logic_vector(MMU_WIDTH-1 downto 0) := "0010"; 
	constant ATMEND		: std_logic_vector(MMU_WIDTH-1 downto 0) := "0011"; 

	type mem_in_type is record
		bcopd		: std_logic_vector(15 downto 0);
		rd			: std_logic;	-- uncached read
		wr			: std_logic;	-- uncached write
		addr_wr	    : std_logic;
		bc_rd       : std_logic;
		iaload      : std_logic;
		iastore     : std_logic;
		stidx       : std_logic;
		getfield    : std_logic;
		putfield    : std_logic;
		getstatic   : std_logic;
		putstatic   : std_logic;
		putref		: std_logic;	-- indicates a putfield with references to objects
		rdc         : std_logic;	-- read with possible constant cache
		rdf         : std_logic;	-- read with coherent cache (fully assoc)
		wrf         : std_logic;	-- write into coherente cache
		copy        : std_logic;
		cinval		: std_logic;	-- invalidate the data cache
		atmstart	: std_logic;	-- start atomic section
		atmend		: std_logic;	-- end atomic section
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
		rollback: std_logic;	-- rollback RTTM transaction
	end record;

	constant EXC_SPOV	: std_logic_vector(2 downto 0) := "001";
	constant EXC_NP		: std_logic_vector(2 downto 0) := "010";
	constant EXC_AB		: std_logic_vector(2 downto 0) := "011";
	constant EXC_ROLLBACK: std_logic_vector(2 downto 0) := "100";

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

	-- object cache types

	type ocache_in_type is record
		handle	: std_logic_vector(OCACHE_ADDR_BITS-1 downto 0);
		index	: std_logic_vector(OCACHE_MAX_INDEX_BITS-1 downto 0);
		gf_val	: std_logic_vector(31 downto 0);
		pf_val	: std_logic_vector(31 downto 0);
		chk_gf	: std_logic;
		chk_pf	: std_logic;
		wr_gf	: std_logic;
		wr_pf	: std_logic;
		inval	: std_logic;
	end record;

	type ocache_out_type is record
		hit		: std_logic;
		-- just handle hit
		dout	: std_logic_vector(31 downto 0);
	end record;

	-- array cache types
	-- TODO: names should be changed when I have a Sigasi license again
	-- what about handle and len?

	type acache_in_type is record
		handle	: std_logic_vector(ACACHE_ADDR_BITS-1 downto 0);
		index	: std_logic_vector(ACACHE_MAX_INDEX_BITS-1 downto 0);
		ial_val	: std_logic_vector(31 downto 0); -- from memory
		ias_val	: std_logic_vector(31 downto 0); -- to memory (write through)
		chk_ial	: std_logic;
		chk_ias	: std_logic;
		wr_ial	: std_logic;
		wr_ial_idx : std_logic_vector(ACACHE_FIELD_BITS-1 downto 0);
		wr_ias	: std_logic;
		inval	: std_logic;
	end record;

	type acache_out_type is record
		hit		: std_logic;
		-- just handle hit
		dout	: std_logic_vector(31 downto 0);
	end record;
end jop_types;
