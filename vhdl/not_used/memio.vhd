--
--	memio.vhd
--
--	external memory and IO for JOP3
--
--		addr, wr are one cycle earlier than data
--		dout one cycle after read (ior)
--
--	resources on ACEX1K30-3
--		 24 LCs, xx MHz		only io ports
--		395 LCs, 73 MHz		plus uart and memory
--		777 LCs, 53 MHz		plus ecp and jfetch
--		659 LCs, 67 MHz		without jfetch (for jop3)
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
--
--	io address mapping:
--
--		0	in, out port (12 Bit)
--		1	status
--		2	uart data (rd/wr)
--		3	reserved for ecp
--		4-6	ext. memory interface
--
--		10	system clock counter (24 MHz)
--		11	ms clock counter
--		12	display out
--		13	keybord (i/o)
--		14	triac out, sense u/i in
--
--	status word:
--		0	uart transmit data register empty
--		1	uart read data register full
--		2	ecp transmit data register empty
--		3	ecp read data register full
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
--		ecp, time, byte code fetch,...
--		'real' write and read buffers (read_addr, 2nd write_addr....)
--		mem_bsy could go low again earlier
--			=> new mem start in last state of rd or wr
--		mem_cancel
--		ff against meta stability on all inputs
--
--
--	2001-05-15	first version (only inp, outp)
--	2001-05-27	added uart (from JOP1)
--	2001-05-29	added memory interface
--	2001-06-02	byte code fetch logic
--	2001-06-04	system clock counter
--	2001-06-06	ms counter, jtbl output not registered (1 cycle less on jp)
--				jopd read 16 bit signed or 8 bit unsigned java bc operand
--	2001-06-07	compute java pc branch address from jopd and take it with st jbranch
--	2001-06-08	calculate cond. for ifgt (from accu) and take it with 'st jbrgt'
--	2001-06-10	do all cond branches
--	2001-06-12	added ecp
--	2001-07-14	remove java fetch for jop3
--	2001-07-18	change for Xilinx
--	2001-10-24	changed rom_cnt to 5 (problems with programing!)
--	2001-10-26	changed nrom_cs and nrd to Z/0 (for config PLD) not so good?
--	2001-10-29	indirect addressing
--	2001-11-22	plus display, keyboard, triacs
--	2001-11-30	mem access direct and indirect
--	2001-12-08	only direct mem access, reorder addresses, ioa_width 3
--	2001-12-22	nrom_cs and nrd back to 1/0 (map in jop.vhd)
--
--


Library IEEE ;
use IEEE.std_logic_1164.all ;
use ieee.numeric_std.all ;

entity memio is
generic (clk_freq : integer := 24000000; width : integer := 32; ioa_width : integer := 3;
	pc_width	: integer := 10;	-- address bits of internal instruction rom
	ram_cnt : integer := 3; rom_cnt : integer := 5);

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

	max_oe		: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	cts			: in std_logic;
	rts			: out std_logic;

-- lpt interface

	lpt_d		: inout std_logic_vector(7 downto 0);
	lpt_s		: out std_logic_vector(7 downto 3);
	lpt_c		: in std_logic_vector(3 downto 0);

-- display

	disp		: out std_logic_vector(5 downto 0);

-- keyboard

	key_in		: in std_logic_vector(3 downto 0);
	key_out		: out std_logic_vector(3 downto 0);

-- triacs

	tr_p		: out std_logic_vector(2 downto 0);
	tr_dir		: out std_logic;
	sense_u		: in std_logic_vector(2 downto 0);
	sense_i		: in std_logic_vector(3 downto 0);

-- io ports

	inp			: in std_logic_vector(11 downto 0);
	outp		: out std_logic_vector(11 downto 0)

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

component ecp is

port (
	clk		: in std_logic;
	reset	: in std_logic;

	lpt_d	: inout std_logic_vector(7 downto 0);
	lpt_s	: out std_logic_vector(7 downto 3);
	lpt_c	: in std_logic_vector(3 downto 0);

	din		: in std_logic_vector(7 downto 0);		-- send data
	dout	: out std_logic_vector(7 downto 0);		-- rvc data

	wr		: in std_logic;			-- send data
	tdre	: out std_logic;		-- transmit data register empty

	rd		: in std_logic;			-- read data
	rdrf	: out std_logic			-- receive data register full
);
end component ecp;


--
--	signals for indirect addressing
--
	signal io_addr			: std_logic_vector(3 downto 0);
	signal io_addr_wr		: std_logic;

	signal port_wr			: std_logic;		-- output port wr ena
	signal disp_wr			: std_logic;
	signal key_wr			: std_logic;
	signal tr_wr			: std_logic;

--
--	signals for uart connection
--
	signal ua_dout			: std_logic_vector(7 downto 0);
	signal ua_wr, ua_tdre	: std_logic;
	signal ua_rd, ua_rdrf	: std_logic;

--
--	signals for ecp connection
--
	signal ecp_dout			: std_logic_vector(7 downto 0);
	signal ecp_wr, ecp_tdre	: std_logic;
	signal ecp_rd, ecp_rdrf	: std_logic;

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
	signal clock_ms			: unsigned (31 downto 0);

begin

	cmp_uart : uart generic map (clk_freq, 115200)
			port map (clk, reset, txd, rxd,
				din(7 downto 0), ua_dout,
				ua_wr, ua_tdre,
				ua_rd, ua_rdrf,
				cts, rts
		);
--	cmp_ecp: ecp port map ( clk, reset, lpt_d, lpt_s, lpt_c,
--		din(7 downto 0), ecp_dout, ecp_wr, ecp_tdre, ecp_rd, ecp_rdrf);
lpt_d <= (others => 'Z');
lpt_s(6 downto 3) <= lpt_c;
lpt_s(7) <= '0';



--
--	read
--
process(clk, reset, rd, addr, io_addr, inp, ua_rdrf, ua_tdre, ua_dout, key_in, sense_u, sense_i)
begin
	if (reset='1') then
		ua_rd <= '0';
		ecp_rd <= '0';
		dout <= std_logic_vector(to_unsigned(0, width));
	elsif rising_edge(clk) then
		ua_rd <= '0';
		ecp_rd <= '0';
		dout <= std_logic_vector(to_unsigned(0, width));

		if (rd='1') then
			if (addr="010") then
				dout <= mem_din;
			elsif (addr="011") then
				dout <= std_logic_vector(to_unsigned(0, width-1)) & mem_bsy;
			else
				case io_addr(3 downto 0) is			-- use only io_addr
					when "0000" =>
						dout <= std_logic_vector(to_unsigned(0, width-12)) & inp;
					when "0001" =>
						dout <= std_logic_vector(to_unsigned(0, width-4)) 
										& ecp_rdrf & ecp_tdre & ua_rdrf & ua_tdre;
					when "0010" =>
						dout <= std_logic_vector(to_unsigned(0, width-8)) & ua_dout;
						ua_rd <= '1';
					when "0011" =>
						dout <= std_logic_vector(to_unsigned(0, width-8)) & ecp_dout;
						ecp_rd <= '1';
					when "1010" =>
						dout <= std_logic_vector(clock_cnt);
					when "1011" =>
						dout <= std_logic_vector(clock_ms);
					when "1101" =>
						dout <= std_logic_vector(to_unsigned(0, width-4)) & key_in;
					when "1110" =>
						dout <= std_logic_vector(to_unsigned(0, width-7)) & sense_i & sense_u;
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
		ecp_wr <= '0';
		port_wr <= '0';
		mem_rd <= '0';
		mem_wr <= '0';
		addr_wr <= '0';
		disp_wr <= '0';
		key_wr <= '0';
		tr_wr <= '0';


	elsif rising_edge(clk) then
		io_addr_wr <= '0';

		ua_wr <= '0';
		ecp_wr <= '0';
		port_wr <= '0';
		mem_rd <= '0';
		mem_wr <= '0';
		addr_wr <= '0';
		disp_wr <= '0';
		key_wr <= '0';
		tr_wr <= '0';

		if (wr='1') then
			if (addr="000") then
				io_addr_wr <= '1';		-- store real io address
			elsif (addr="010") then
				mem_rd <= '1';			-- start read
			elsif (addr="011") then
				addr_wr <= '1';			-- store write address
			elsif (addr="100") then
				mem_wr <= '1';			-- start write
			else
				case io_addr(3 downto 0) is
					when "0000" =>
						port_wr <= '1';
					when "0010" =>
						ua_wr <= '1';
					when "0011" =>
						ecp_wr <= '1';
					when "1100" =>
						disp_wr <= '1';
					when "1101" =>
						key_wr <= '1';
					when "1110" =>
						tr_wr <= '1';
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
process(clk, reset, din, port_wr, disp_wr, key_wr, tr_wr, addr_wr)

begin
	if (reset='1') then

		io_addr <= (others => '0');
		outp <= (others => '0');
		mem_wr_addr <= std_logic_vector(to_unsigned(0, 20));
		disp <= (others => '0');
		key_out <= (others => '0');
		tr_p <= (others => '0');
		tr_dir <= '0';

	elsif rising_edge(clk) then

		if (io_addr_wr='1') then
			io_addr <= din(3 downto 0);
		end if;
		if (port_wr='1') then
			outp <= din(11 downto 0);
		end if;
		if (addr_wr='1') then
			mem_wr_addr <= din(19 downto 0);	-- store write address
		end if;
		if (disp_wr='1') then
			disp <= din(5 downto 0);
		end if;
		if (key_wr='1') then
			key_out <= din(3 downto 0);
		end if;
		if (tr_wr='1') then
			tr_p <= din(2 downto 0);
			tr_dir <= din(3);
		end if;

	end if;
end process;


	max_oe <= '0';

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
						i := ram_cnt;
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
						i := ram_cnt;
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
					i := ram_cnt;
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
					i := ram_cnt;
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
--	24 MHz clock
--

process(clk, reset)

begin
	if (reset='1') then
		clock_cnt <= to_unsigned(0, clock_cnt'length);

	elsif rising_edge(clk) then
		clock_cnt <= clock_cnt+1;
	end if;

end process;

process(clk, reset)

	variable div		: integer range 0 to 23999;

begin
	if (reset='1') then
		div := 0;
		clock_ms <= to_unsigned(0, clock_ms'length);

	elsif rising_edge(clk) then

		if (div=23999) then		-- 24 MHz
			div := 0;
			clock_ms <= clock_ms+1;
		else
			div := div+1;
		end if;

	end if;

end process;

end rtl;
