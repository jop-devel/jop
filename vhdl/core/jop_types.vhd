--
--	jop_types.vhd
--
--	package type definitions definitions
--

library ieee;
use ieee.std_logic_1164.all;

package jop_types is

--
--	extension address constants (used in extension.vhd and Instrucion.java)
--
--		0	st	mem_rd_addr		start read
--		0	ld	mem_rd_data		read data
--		1	st	wraddr		store write address
--		2	st	mem_wr_data		start write
--		5	st	mul operand a, b and start mul
--		5	ld	mul result
--		6	free
--		7	st	start bytecode load (or cache)
--		7	ld	read new pc base (for cache version)
--
	constant STMRA	: std_logic_vector(2 downto 0) := "000"; 
	constant STMWA	: std_logic_vector(2 downto 0) := "001"; 
	constant STMWD	: std_logic_vector(2 downto 0) := "010"; 
	constant STALD	: std_logic_vector(2 downto 0) := "011"; 
	constant STAST	: std_logic_vector(2 downto 0) := "100"; 
	constant STMUL	: std_logic_vector(2 downto 0) := "101"; 
	constant STBCR	: std_logic_vector(2 downto 0) := "111"; 

	constant LDMRD	: std_logic_vector(2 downto 0) := "000"; 
	constant LDMUL	: std_logic_vector(2 downto 0) := "101"; 
	constant LDBCSTART	: std_logic_vector(2 downto 0) := "111"; 

	type mem_in_type is record
		rd		: std_logic;
		wr		: std_logic;
		addr_wr	: std_logic;
		bc_rd	: std_logic;
		iaload	: std_logic;
		iastore	: std_logic;
	end record;

	type mem_out_type is record
		dout		: std_logic_vector(31 downto 0);
		bcstart		: std_logic_vector(31 downto 0); 	-- start of method in bc cache
		bsy			: std_logic;
	end record;

	type exception_type is record
		spov	: std_logic;	-- stack overflow
		np		: std_logic;	-- null pointer
		ab		: std_logic;	-- array out of bounds
	end record;

	constant EXC_SPOV	: std_logic_vector(2 downto 0) := "001";
	constant EXC_NP		: std_logic_vector(2 downto 0) := "010";
	constant EXC_AB		: std_logic_vector(2 downto 0) := "011";

	type irq_bcf_type is record
		irq			: std_logic;	-- interrupt request, single cycle
		irq_ena		: std_logic;	-- interrupt enable (pendig int is fired on ena)

		exc_int		: std_logic;	-- exception request, singel cycle
	end record;

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
		lock		: std_logic;
	end record;
	
	type sync_out_type is record
		s_out	: std_logic;
		release : std_logic;
	end record;
	
	constant NO_SYNC : sync_out_type := (s_out => '0', release => '0');
	type sync_in_array_type is array (integer range <>) of sync_in_type;
	type sync_out_array_type is array (integer range <>) of sync_out_type;


end jop_types;
