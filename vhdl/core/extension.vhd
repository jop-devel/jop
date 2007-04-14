--
--	extension.vhd
--
--	contains interface to memory, multiplier and IO
--	MUX for din from stack
--	
--	resources on Cyclone
--	
--		 55 LCs (+xxx for mul)
--
--		ext_addr and wr are one cycle earlier than data
--		dout is read one cycle after rd
--
--	address mapping see jop_tpyes.vhd
--
--
--	2004-09-11	first version
--	2005-04-05	Reserve negative addresses for wishbone interface
--	2005-04-07	generate bsy from delayed wr or'ed with mem_out.bsy
--	2005-05-30	added wishbone interface
--	2005-11-28	Substitute WB interface by the SimpCon IO interface ;-)
--				All IO devices are now memory mapped
--	2007-04-13	Changed memory connection to records
--				New array instructions
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.numeric_std.all ;

use work.jop_types.all;
use work.sc_pack.all;

entity extension is

generic (exta_width : integer);

port (
	clk, reset	: in std_logic;

-- core interface

	ain			: in std_logic_vector(31 downto 0);		-- TOS
	bin			: in std_logic_vector(31 downto 0);		-- NOS
	ext_addr	: in std_logic_vector(exta_width-1 downto 0);
	rd, wr		: in std_logic;
	bsy			: out std_logic;
	dout		: out std_logic_vector(31 downto 0);	-- to stack

-- mem interface
	mem_in		: out mem_in_type;
	mem_out		: in mem_out_type;

-- SimpCon master io interface

	sc_io_out		: out sc_io_out_type;
	sc_io_in		: in sc_in_type
);
end extension;

architecture rtl of extension is

--
--	components:
--

component mul is

port (
	clk			: in std_logic;

	ain			: in std_logic_vector(31 downto 0);
	bin			: in std_logic_vector(31 downto 0);
	wr			: in std_logic;		-- write starts multiplier
	dout		: out std_logic_vector(31 downto 0)
);
end component mul;

--
--	signals for mulitiplier
--
	signal mul_dout				: std_logic_vector(31 downto 0);
	signal mul_wr				: std_logic;

--
--	Signals
--
	signal mem_scio_rd			: std_logic;	-- memory or SimpCon IO read
	signal mem_scio_wr			: std_logic;	-- memory or SimpCon IO write
	signal wraddr_wr			: std_logic;

	-- msb selects mem/io
	signal wraddr_msb			: std_logic;
	signal was_a_mem_rd			: std_logic;

	signal was_a_iaload			: std_logic;	-- select memory for array load

	signal wr_dly				: std_logic;	-- generate a bsy with delayed wr

	signal exr, exr_next		: std_logic_vector(31 downto 0); 	-- extension data register

--
--	SimpCon specific signals
--
	-- SimpCon IO write address
	signal sc_wr_addr			: std_logic_vector(IO_ADDR_SIZE-1 downto 0);
	signal sc_bsy				: std_logic;
	signal sc_rd				: std_logic;


begin

	cmp_mul : mul
			port map (clk,
				ain, bin, mul_wr,
				mul_dout
		);

	dout <= exr;

--
--	read
--
--	TODO: the read MUX could be set by using the
--	according wr/ext_addr from JOP and not the
--	following rd/ext_addr
--	Than no intermixing of mul/mem and io operations
--	is allowed. But we are not using interleaved mul/mem/io
--	operations in jvm.asm anyway.
--
--	TAKE CARE when mem_out.bcstart is read!
--
--   ** bcstart is also read without a mem_bc_rd JOP wr !!! ***
--		=> a combinatorial mux select on rd and ext_adr==7!
--
--		The rest could be set with JOP wr start transaction 
--		Is this also true for io_data?
--
--	29.11.2005 evening: I think this solution driving the exr
--	mux from ext_addr is quite ok. The pipelining from rd/ext_adr
--	to A is fixed.
--
process(clk, reset)
begin
	if (reset='1') then
		exr <= (others => '0');
	elsif rising_edge(clk) then

		if (ext_addr=LDMRD) then
			if was_a_mem_rd='1' or was_a_iaload='1' then
				exr <= mem_out.dout;
			else
				exr <= sc_io_in.rd_data;
			end if;
		elsif (ext_addr=LDMUL) then
			exr <= mul_dout;
		-- elsif (ext_addr=LDBCSTART) then
		else
			exr <= mem_out.bcstart;
		end if;

	end if;
end process;


--
--	write
--
process(clk, reset)
begin
	if (reset='1') then
		mem_scio_rd <= '0';
		mem_scio_wr <= '0';
		wraddr_wr <= '0';
		mem_in.bc_rd <= '0';
		mem_in.iaload <= '0';
		mem_in.iastore <= '0';
		mul_wr <= '0';
		wr_dly <= '0';
		was_a_iaload <= '0';


	elsif rising_edge(clk) then
		mem_scio_rd <= '0';
		mem_scio_wr <= '0';
		wraddr_wr <= '0';
		mem_in.bc_rd <= '0';
		mem_in.iaload <= '0';
		mem_in.iastore <= '0';
		mul_wr <= '0';

		wr_dly <= wr;

--
--	wr is generated in decode and one cycle earlier than
--	the data to be written (e.g. read address for the memory interface)
--
		if wr='1' then
			was_a_iaload <= '0';

			if ext_addr=STMRA then
				mem_scio_rd <= '1';		-- start memory or io read
			elsif ext_addr=STMWA then
				wraddr_wr <= '1';		-- store write address
			elsif ext_addr=STMWD then
				mem_scio_wr <= '1';		-- start memory or io write
			elsif ext_addr=STALD then
				mem_in.iaload <= '1';	-- start an array load
				was_a_iaload <= '0';
			elsif ext_addr=STAST then
				mem_in.iastore <= '1';	-- start an array store
			elsif ext_addr=STMUL then
				mul_wr <= '1';			-- start multiplier
			-- elsif ext_addr=STBCR then
			else
				mem_in.bc_rd <= '1';		-- start bc read
			end if;
		end if;

	end if;
end process;

--
--	memory read/write only from positive addresses
--
	mem_in.rd <= mem_scio_rd and not ain(31);
	mem_in.wr <= mem_scio_wr and not wraddr_msb;
	mem_in.addr_wr <= wraddr_wr;

	-- a JOP wr generates the first bsy cycle
	-- the following are generated by the memory
	-- system or the SimpCon device
	bsy <= wr_dly or mem_out.bsy or sc_bsy;

	sc_bsy <= '1' when sc_io_in.rdy_cnt=3 else '0';

--
--	store write address (msb)
--		one cycle after write instruction (address is avaliable one cycle before ex stage)
--	store read address msb for exr MUX
--
process(clk, reset)

begin
	if (reset='1') then

		sc_wr_addr <= (others => '0');
		wraddr_msb <= '0';
		was_a_mem_rd <= '0';

	elsif rising_edge(clk) then

		if wraddr_wr='1' then
			-- store SimpCon write address
			sc_wr_addr <= ain(IO_ADDR_SIZE-1 downto 0);
			wraddr_msb <= ain(31);
		end if;

		if mem_scio_rd='1' then
			was_a_mem_rd <= not ain(31);
		end if;

	end if;
end process;

--
--	SimpCon connections
--
	-- use negativ addresses for SimpCon IO devices
	sc_rd <= mem_scio_rd and ain(31);
	-- we need the additional signal for the addr MUX
	-- can be avoided when removing the wr addr store.
	sc_io_out.rd <= sc_rd;
	sc_io_out.wr <= mem_scio_wr and wraddr_msb;
	sc_io_out.wr_data <= ain;

--
--	SimpCon address MUX
--
--	TODO: change memory instruction so that the address
--	is in A and the data is in B. So we have a single
--	microinstruction for read AND write and we don't
--	need to store the write address and mux it here and
--	in mem_sc.
--
process(ain, sc_wr_addr, sc_rd)
begin
	if sc_rd='1' then
		sc_io_out.address <= ain(IO_ADDR_SIZE-1 downto 0);
	else
		sc_io_out.address <= sc_wr_addr;
	end if;
end process;

end rtl;
