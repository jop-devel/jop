--
--	memeth.vhd		// new mem, io interface!!!
--
--	from mem to memeth in the old way!!!
--
--	TODO TODO split mem and io for different versions!!!!!!!!!
--
--	external memory and IO for JOP3 (jopcore version)
--
--		addr, wr are one cycle earlier than data
--		dout one cycle after read (ior)
--
--	resources on ACEX1K30-3
--
--	first mapping (for ldp, stp):
--
--		0	io-address
--		1	data read/write
--		2	st	mem_rd_addr		start read
--		2	ld	mem_rd_data		read data
--		3	st	mem_wr_addr		store write address
--		3	ld	mem_status		busy flag
--		4	st	mem_wr_data		start write
--		5	ld	mul result
--		5	st	mul operand a
--		6	st	mul operand b and start mul
--
--	io address mapping:
--
--		0	in, out port (8/4 Bit)	not used in bb
--		1	status
--		2	uart data (rd/wr)
--		3	reserved for ecp
--		4	ADC rd, exp write		BB
--		5	isa control and addr, I ADC on BB
--		6	isa data
--
--		7	watch dog bit
--		8	leds, taster			BB
--		9	relais, sensor			BB
--		10	system clock counter
--		11	ms clock counter		obsolete
--		12	display out				BB
--		13	keybord (i/o)			BB
--		14	triac out, sense u/i in	BB
--		15	rs485 data (rd/wr)		BB
--
--	status word:
--		0	uart transmit data register empty
--		1	uart read data register full
--		2	second uart transmit data register empty !not used
--		3	second uart read data register full !not used
--		4	rs485 transmit data register empty	BB
--		5	rs485 read data register full		BB
--
--
--
--
--	memory mapping (compatible with JOP1)
--	
--		x00000-x7ffff	external ram (w mirror)	max. 512 kW (4*4 MBit)
--		x80000-xfffff	external rom (w mirror)	max. 512 kB (4 MBit)
--
--	ram: 32 bit word
--		mapping ain(16 downto 0) to a(18 downto 2), ignoring ain(18,17)
--	rom: 8 bit word (for flash programming)
--		mapping ain(18 downto 0) to a(18 downto 0)
--
--	todo:
--		ff against meta stability on all inputs
--
--
--	2002-01-03	copy from memio.vhd for bb project
--	2002-01-06	added rs485 uart
--	2001-02-01	some changes in io port definitions
--	2002-05-07	added 'hw' mul
--	2002-07-27	io definitions for baseio
--	2002-08-02	second uart (use first for download and debug)
--	2002-11-01	removed second uart
--	2002-12-01	ram_cnt to 2 (for 20 MHz)
--
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity memio is
generic (clk_freq : integer; width : integer := 32; ioa_width : integer := 3;
	pc_width	: integer := 10;	-- address bits of internal instruction rom
	ram_cnt : integer := 2; rom_cnt : integer := 3);

port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);
	addr		: in std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: in std_logic;

	dout		: out std_logic_vector(width-1 downto 0);

-- external mem interface

	a			: out std_logic_vector(18 downto 0);
	d			: inout std_logic_vector(7 downto 0);
	nram_cs		: out std_logic;
	nrom_cs		: out std_logic;
	nrd			: out std_logic;
	nwr			: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	cts			: in std_logic;
	rts			: out std_logic;

-- io ports
	wd			: out std_logic;

-- ethernet card
	isa_d		: inout std_logic_vector(7 downto 0);
	isa_a		: out std_logic_vector(4 downto 0);
	isa_reset	: out std_logic;
	isa_nior	: out std_logic;
	isa_niow	: out std_logic;

-- io ports
	i			: in std_logic_vector(8 downto 1);
	o			: out std_logic_vector(4 downto 1);

-- analog ports
	sdi			: in std_logic_vector(2 downto 1);
	sdo			: out std_logic_vector(2 downto 1)
);
end memio ;

architecture rtl of memio is

component uart is
generic (clk_freq : integer; baud_rate : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;

	txd		: out std_logic;
	rxd		: in std_logic;

	din		: in std_logic_vector(7 downto 0);		-- send data
	dout	: out std_logic_vector(7 downto 0);		-- rvc data

	wr		: in std_logic;		-- send data
	tdre	: out std_logic;	-- transmit data register empty

	rd		: in std_logic;		-- read data
	rdrf	: out std_logic;	-- receive data register full

	cts		: in std_logic;
	rts		: out std_logic
);
end component uart;

component sigdel is
generic (clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;

	dout	: out std_logic_vector(15 downto 0);

	sdi		: in std_logic;
	sdo		: out std_logic
);
end component sigdel;

component mul is

generic (width : integer);
port (
	clk			: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);
	wr_a		: in std_logic;
	wr_b		: in std_logic;		-- write to b starts multiplier
	dout		: out std_logic_vector(width-1 downto 0)
);
end component mul;

--
--	signals for indirect addressing
--
	signal io_addr			: std_logic_vector(3 downto 0);
	signal io_addr_wr		: std_logic;

	signal wd_wr			: std_logic;
	signal o_wr				: std_logic;

--
--	signals for uart connection
--
	signal ua_dout			: std_logic_vector(7 downto 0);
	signal ua_wr, ua_tdre	: std_logic;
	signal ua_rd, ua_rdrf	: std_logic;

--
--	signal for isa data bus
--
	signal isa_dout			: std_logic_vector(7 downto 0);
	signal isa_d_wr			: std_logic;
	signal isa_ctrl_wr		: std_logic;
	signal isa_dir			: std_logic;		-- direction of isa_d ('1' means driving out)

--
--	signals for ADC
--
	signal sd_dout			: std_logic_vector(31 downto 0);

--
--	signals for mulitiplier
--
	signal mul_dout				: std_logic_vector(width-1 downto 0);
	signal mul_wra, mul_wrb		: std_logic;

--
--	signals for mem interface
--
	type state_type		is (
							idl, rd1, rd2, rd3, rd4, wr1, wr2, wr3, wr4
						);
	signal state 		: state_type;

	signal mem_wr_addr		: std_logic_vector(19 downto 0);
	signal mem_dout			: std_logic_vector(width-1 downto 0);
	signal mem_din			: std_logic_vector(width-1 downto 0);
	signal mem_rd			: std_logic;
	signal mem_wr			: std_logic;
	signal mem_bsy			: std_logic;

	signal addr_wr			: std_logic;
	signal nwr_int			: std_logic;
	signal ram_access		: std_logic;

--
--	signal for timers
--
	signal clock_cnt		: unsigned (31 downto 0);


begin

	cmp_uart : uart generic map (clk_freq, 115200)
			port map (clk, reset, txd, rxd,
				din(7 downto 0), ua_dout,
				ua_wr, ua_tdre,
				ua_rd, ua_rdrf,
				cts, rts
		);

	cmp_sigdel1 : sigdel generic map (clk_freq)
			port map (clk, reset,
				sd_dout(15 downto 0),
				sdi(1), sdo(1)
		);

	cmp_sigdel2 : sigdel generic map (clk_freq)
			port map (clk, reset,
				sd_dout(31 downto 16),
				sdi(2), sdo(2)
		);

	cmp_mul : mul generic map (width)
			port map (clk,
				din, mul_wra, mul_wrb,
				mul_dout
		);

--
--	isa data bus
--
	isa_d <= isa_dout when isa_dir='1' else "ZZZZZZZZ";


--
--	read
--
process(clk, reset, rd, addr, io_addr, 
	ua_rdrf, ua_tdre, ua_dout,
	isa_d, i, sd_dout)
begin
	if (reset='1') then
		ua_rd <= '0';
		dout <= std_logic_vector(to_unsigned(0, width));
	elsif rising_edge(clk) then
		ua_rd <= '0';
		dout <= std_logic_vector(to_unsigned(0, width));

		if (rd='1') then
			if (addr="010") then
				dout <= mem_din;
			elsif (addr="011") then
				dout <= std_logic_vector(to_unsigned(0, width-1)) & mem_bsy;
			elsif (addr="101") then
				dout <= mul_dout;
			else
				case io_addr(3 downto 0) is			-- use only io_addr
					when "0000" =>
						dout <= std_logic_vector(to_unsigned(0, width-8)) & i;
					when "0001" =>
						dout <= std_logic_vector(to_unsigned(0, width-6)) &
										"0000" & ua_rdrf & ua_tdre;
					when "0010" =>
						dout <= std_logic_vector(to_unsigned(0, width-8)) & ua_dout;
						ua_rd <= '1';
					when "0100" =>
						dout <= sd_dout;
					when "0110" =>
						dout <= std_logic_vector(to_unsigned(0, width-8)) & isa_d;
					when "1010" =>
						dout <= std_logic_vector(clock_cnt);
					when others =>
						null;
				end case;
			end if;
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

		ua_wr <= '0';
		mem_rd <= '0';
		mem_wr <= '0';
		addr_wr <= '0';
		mul_wra <= '0';
		mul_wrb <= '0';
		wd_wr <= '0';
		isa_d_wr <= '0';
		isa_ctrl_wr <= '0';
		o_wr <= '0';


	elsif rising_edge(clk) then
		io_addr_wr <= '0';

		ua_wr <= '0';
		mem_rd <= '0';
		mem_wr <= '0';
		addr_wr <= '0';
		mul_wra <= '0';
		mul_wrb <= '0';
		wd_wr <= '0';
		isa_d_wr <= '0';
		isa_ctrl_wr <= '0';
		o_wr <= '0';

		if (wr='1') then
			if (addr="000") then
				io_addr_wr <= '1';		-- store real io address
			elsif (addr="010") then
				mem_rd <= '1';			-- start read
			elsif (addr="011") then
				addr_wr <= '1';			-- store write address
			elsif (addr="100") then
				mem_wr <= '1';			-- start write
			elsif (addr="101") then
				mul_wra <= '1';
			elsif (addr="110") then
				mul_wrb <= '1';
			else
				case io_addr(3 downto 0) is
					when "0000" =>
						o_wr <= '1';
					when "0010" =>
						ua_wr <= '1';
					when "0101" =>
						isa_ctrl_wr <= '1';
					when "0110" =>
						isa_d_wr <= '1';
					when "0111" =>
						wd_wr <= '1';
					when others =>
						null;
				end case;
			end if;
		end if;
	end if;
end process;

--
--	io_addr, port buffer, wr_addr write
--		one cycle after io write (address is avaliable one cycle before ex stage)
--
process(clk, reset, din, addr_wr, wd_wr, isa_d_wr, isa_ctrl_wr, o_wr)

begin
	if (reset='1') then

		io_addr <= (others => '0');
		mem_wr_addr <= std_logic_vector(to_unsigned(0, 20));
		wd <= '0';

		isa_dout <= (others => '0');
		isa_a <= (others => '0');
		isa_reset <= '0';
		isa_nior <= '1';
		isa_niow <= '1';
		isa_dir <= '0';
		o <= (others => '0');

	elsif rising_edge(clk) then

		if (io_addr_wr='1') then
			io_addr <= din(3 downto 0);
		end if;
		if (addr_wr='1') then
			mem_wr_addr <= din(19 downto 0);	-- store write address
		end if;
		if (wd_wr='1') then
			wd <= din(0);
		end if;
		if (isa_d_wr='1') then
			isa_dout <= din(7 downto 0);
		end if;
		if (isa_ctrl_wr='1') then
			isa_a <= din(4 downto 0);
			isa_reset <= din(5);
			isa_nior <= not din(6);
			isa_niow <= not din(7);
			isa_dir <= din(8);
		end if;
		if (o_wr='1') then
			o <= din(3 downto 0);
		end if;


	end if;
end process;


--
--	'delay' nwr 1/2 cycle -> change on falling edge
--
process(clk, reset, nwr_int)

begin
	if (reset='1') then
		nwr <= '1';
	elsif falling_edge(clk) then
		nwr <= nwr_int;
	end if;

end process;

--
--	state machine for external memory (single byte static ram, flash)
--
process(clk, reset, din, mem_wr_addr, mem_rd, mem_wr)

	variable i : integer range 0 to 7;

begin
	if (reset='1') then
		state <= idl;
		a <= "ZZZZZZZZZZZZZZZZZZZ";
		d <= "ZZZZZZZZ";
		nram_cs <= '1';
		nrom_cs <= '1';
		nrd <= '1';
		nwr_int <= '1';
		ram_access <= '1';
		mem_din <= std_logic_vector(to_unsigned(0, width));
		mem_dout <= std_logic_vector(to_unsigned(0, width));
		mem_bsy <= '0';

	elsif rising_edge(clk) then

		case state is

			when idl =>
				a <= "ZZZZZZZZZZZZZZZZZZZ";
				d <= "ZZZZZZZZ";
				nrd <= '1';
				nwr_int <= '1';
				nram_cs <= '1';
				nrom_cs <= '1';
				ram_access <= '1';
				mem_bsy <= '0';

				if (mem_rd='1') then
					if (din(19)='1') then
						a <= din(18 downto 0);
						nrom_cs <= '0';
						ram_access <= '0';
						i := rom_cnt;
					else
						a <= din(16 downto 0) & "00";
						nram_cs <= '0';
						ram_access <= '1';
						i := ram_cnt;
					end if;
					mem_bsy <= '1';
					nrd <= '0';
					state <= rd1;
				elsif (mem_wr='1') then
					mem_dout <= din;
					if (mem_wr_addr(19)='1') then
						a <= mem_wr_addr(18 downto 0);
						nrom_cs <= '0';
						ram_access <= '0';
						i := rom_cnt;
					else
						a <= mem_wr_addr(16 downto 0) & "00";
						nram_cs <= '0';
						ram_access <= '1';
						i := ram_cnt+1;			-- one more for single cycle read
					end if;
					mem_bsy <= '1';
					nwr_int <= '0';
					state <= wr1;
				end if;

--
--	memory read
--
			when rd1 =>
				i := i-1;
				if (i=0) then
					if (ram_access='1') then
						state <= rd2;
						mem_din(7 downto 0) <= d;
						a(1 downto 0) <= "01";
						i := ram_cnt;
					else
						state <= idl;
						mem_bsy <= '0';
						mem_din <= std_logic_vector(to_unsigned(0, width-8)) & d;
					end if;
				end if;

			when rd2 =>
				i := i-1;
				if (i=0) then
					state <= rd3;
					mem_din(15 downto 8) <= d;
					a(1 downto 0) <= "10";
					i := ram_cnt;
				end if;
					
			when rd3 =>
				i := i-1;
				if (i=0) then
					state <= rd4;
					mem_din(23 downto 16) <= d;
					a(1 downto 0) <= "11";
					i := ram_cnt;
				end if;
					
			when rd4 =>
				i := i-1;
				if (i=0) then
					state <= idl;
					mem_din(31 downto 24) <= d;
					mem_bsy <= '0';
				end if;
--
--	memory write
--
			when wr1 =>
				i := i-1;
				d <= mem_dout(7 downto 0);
				if (i=1) then
					nwr_int <= '1';
				end if;
				if (i=0) then
					if (ram_access='1') then
						nwr_int <= '0';
						state <= wr2;
						a(1 downto 0) <= "01";
						i := ram_cnt+1;
					else
						state <= idl;
						mem_bsy <= '0';
					end if;
				end if;

			when wr2 =>
				i := i-1;
				d <= mem_dout(15 downto 8);
				nwr_int <= '0';
				if (i=1) then
					nwr_int <= '1';
				end if;
				if (i=0) then
					state <= wr3;
					nwr_int <= '0';
					a(1 downto 0) <= "10";
					i := ram_cnt+1;
				end if;
					
			when wr3 =>
				i := i-1;
				d <= mem_dout(23 downto 16);
				if (i=1) then
					nwr_int <= '1';
				end if;
				if (i=0) then
					state <= wr4;
					nwr_int <= '0';
					a(1 downto 0) <= "11";
					i := ram_cnt+1;
				end if;
					
			when wr4 =>
				i := i-1;
				d <= mem_dout(31 downto 24);
				if (i=1) then
					nwr_int <= '1';
				end if;
				if (i=0) then
					state <= idl;
					mem_bsy <= '0';
					nwr_int <= '1';
				end if;
					
		end case;
					
	end if;
end process;

--
--	xx MHz clock
--

process(clk, reset)

begin
	if (reset='1') then
		clock_cnt <= to_unsigned(0, clock_cnt'length);

	elsif rising_edge(clk) then
		clock_cnt <= clock_cnt+1;
	end if;

end process;

end rtl;
