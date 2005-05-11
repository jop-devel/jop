--
--	extension.vhd
--
--	contains interface to memory, multiplier and IO
--	MUX for din from stack
--	
--	resources on Cyclone
--	
--		 xxx LCs, xx MHz	
--
--		ext_addr and wr are one cycle earlier than data
--		dout is read one cycle after rd
--
--	address mapping:
--
--		0	io-address
--		1	io	read/write
--		2	st	mem_rd_addr		start read
--		2	ld	mem_rd_data		read data
--		3	st	wraddr		store write address
--		4	st	mem_wr_data		start write
--		5	ld	mul result
--		5	st	mul operand a
--		6	st	mul operand b and start mul
--		7	st	start bytecode load (or cache)
--		7	ld	read new pc base (for cache version)
--
--	todo:
--
--
--	2004-09-11	first version
--	2005-04-05	Reserve negative addresses for wishbone interface
--	2005-04-07	generate bsy from delayed wr or'ed with mem_bsy
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.numeric_std.all ;

entity extension is

generic (exta_width : integer);

port (
	clk, reset	: in std_logic;

-- core interface

	ain			: in std_logic_vector(31 downto 0);		-- from stack
	bin			: in std_logic_vector(31 downto 0);		-- from stack
	ext_addr	: in std_logic_vector(exta_width-1 downto 0);
	rd, wr		: in std_logic;
	bsy			: out std_logic;
	dout		: out std_logic_vector(31 downto 0);	-- to stack

-- mem interface

	mem_rd		: out std_logic;
	mem_wr		: out std_logic;
	mem_addr_wr	: out std_logic;
	mem_bc_rd	: out std_logic;
	mem_data	: in std_logic_vector(31 downto 0); 	-- output of memory module
	mem_bcstart	: in std_logic_vector(31 downto 0); 	-- start of method in bc cache
	mem_bsy		: in std_logic;
	
-- io interface

	io_rd		: out std_logic;
	io_wr		: out std_logic;
	io_addr_wr	: out std_logic;
	io_data		: in std_logic_vector(31 downto 0)		-- output of io module
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
	signal mem_wb_rd			: std_logic;
	signal mem_wb_wr			: std_logic;
	signal wraddr_wr			: std_logic;
	signal wraddr_msb			: std_logic;

	signal wraddr				: std_logic_vector(7 downto 0);		-- wishbone write address

	signal wr_dly				: std_logic;	-- generate a bsy with delayed wr

begin

	cmp_mul : mul
			port map (clk,
				ain, bin, mul_wr,
				mul_dout
		);

--
--	read
--
process(clk, reset, rd, ext_addr)
begin
	if (reset='1') then
		dout <= std_logic_vector(to_unsigned(0, 32));
		io_rd <= '0';
	elsif rising_edge(clk) then
		dout <= std_logic_vector(to_unsigned(0, 32));

		if (ext_addr="010") then
			dout <= mem_data;
		elsif (ext_addr="101") then
			dout <= mul_dout;
		elsif (ext_addr="111") then
			dout <= mem_bcstart;
		else
			dout <= io_data;
		end if;

		io_rd <= '0';
		if (ext_addr="001" and rd='1') then
			io_rd <= '1';
		end if;
	end if;
end process;



--
--	write
--
process(clk, reset, rd, wr)
begin
	if (reset='1') then
		io_addr_wr <= '0';
		mem_wb_rd <= '0';
		mem_wb_wr <= '0';
		wraddr_wr <= '0';
		mem_bc_rd <= '0';
		mul_wr <= '0';
		io_wr <= '0';
		wr_dly <= '0';


	elsif rising_edge(clk) then
		io_addr_wr <= '0';
		mem_wb_rd <= '0';
		mem_wb_wr <= '0';
		wraddr_wr <= '0';
		mem_bc_rd <= '0';
		mul_wr <= '0';
		io_wr <= '0';

		wr_dly <= wr;

--
--	wr is generated in decode and one cycle earlier than
--	the data to be written (e.g. read address for the memory interface)
--
		if (wr='1') then
			if (ext_addr="000") then
				io_addr_wr <= '1';		-- store real io address
			elsif (ext_addr="010") then
				mem_wb_rd <= '1';		-- start memory or wishbone read
			elsif (ext_addr="011") then
				wraddr_wr <= '1';		-- store write address
			elsif (ext_addr="100") then
				mem_wb_wr <= '1';		-- start memory or wishbone write
			elsif (ext_addr="101") then
				mul_wr <= '1';			-- start multiplier
			elsif (ext_addr="111") then
				mem_bc_rd <= '1';		-- start bc read
			else
				io_wr <= '1';
			end if;
		end if;
	end if;
end process;

--
--	memory read/write only from positive addresses
--
	mem_rd <= mem_wb_rd and not ain(31);
	mem_wr <= mem_wb_wr and not wraddr_msb;
	mem_addr_wr <= wraddr_wr;

--	just copy bsy for now

	bsy <= mem_bsy or wr_dly;


--
--	store write address (msb)
--		one cycle after write instruction (address is avaliable one cycle before ex stage)
--
process(clk, reset)

begin
	if (reset='1') then

		wraddr <= (others => '0');
		wraddr_msb <= '0';

	elsif rising_edge(clk) then

		if (wraddr_wr='1') then
			wraddr <= ain(7 downto 0);	-- store wishbone write address
			wraddr_msb <= ain(31);
		end if;

	end if;
end process;

end rtl;
