--
--	memiobb.vhd
--
--	external memory and IO for JOP3 (BB version)
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
--		0	in, out port (12 Bit)	not used in bb
--		1	status
--		2	uart data (rd/wr)
--		3	reserved for ecp
--		4	ADC rd, exp write
--		5	I ADC
--		6	free
--
--		7	watch dog bit
--		8	leds, taster
--		9	relais, sensor
--		10	system clock counter
--		11	ms clock counter
--		12	display out
--		13	keybord (i/o)
--		14	triac out, sense u/i in
--		15	rs485 data (rd/wr)
--
--	status word:
--		0	uart transmit data register empty
--		1	uart read data register full
--		4	rs485 transmit data register empty
--		5	rs485 read data register full
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
--	2002-06-08	use exp port
--	2002-06-21	iadc
--
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity memio is
generic (clk_freq : integer; width : integer := 32; ioa_width : integer := 3;
	pc_width	: integer := 10;	-- address bits of internal instruction rom
	ram_cnt : integer := 1; rom_cnt : integer := 3);

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

	s1_txd		: out std_logic;
	s1_rxd		: in std_logic;
	s1_cts		: in std_logic;
	s1_rts		: out std_logic;

-- display

	disp		: out std_logic_vector(5 downto 0);

-- keyboard

	key_in		: in std_logic_vector(3 downto 0);
	key_out		: out std_logic_vector(3 downto 0);

-- triacs

	tr_on		: out std_logic;
	tr_dir		: out std_logic;
	sense_u		: in std_logic_vector(2 downto 0);
	sense_i		: in std_logic_vector(3 downto 0);

-- io ports
	wd			: out std_logic;
	led			: out std_logic_vector(4 downto 1);
	tast		: in std_logic_vector(3 downto 0);
	relais		: out std_logic_vector(3 downto 0);
	sensor		: in std_logic_vector(3 downto 0);

-- analog ports
	sdi			: in std_logic;
	sdo			: out std_logic;

-- analog ports
	iadc_sdi	: in std_logic_vector(3 downto 1);
	iadc_sdo	: out std_logic_vector(3 downto 1);

-- expansion port
	exp			: out std_logic_vector(8 downto 6)

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

component hdart is
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
end component hdart;

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

component iadc is
generic (clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;

	dout	: out std_logic_vector(31 downto 0);

	sdi		: in std_logic_vector(3 downto 1);
	sdo		: out std_logic_vector(3 downto 1)
);
end component iadc;

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

	signal disp_wr			: std_logic;
	signal key_wr			: std_logic;
	signal tr_wr			: std_logic;
	signal wd_wr			: std_logic;
	signal led_wr			: std_logic;
	signal rel_wr			: std_logic;
	signal exp_wr			: std_logic;

--
--	signals for uart connection
--
	signal ua_dout			: std_logic_vector(7 downto 0);
	signal ua_wr, ua_tdre	: std_logic;
	signal ua_rd, ua_rdrf	: std_logic;
	signal ua2_dout			: std_logic_vector(7 downto 0);
	signal ua2_wr, ua2_tdre	: std_logic;
	signal ua2_rd, ua2_rdrf	: std_logic;

--
--	signals for ADC
--
	signal sd_dout			: std_logic_vector(15 downto 0);
	signal iadc_dout		: std_logic_vector(31 downto 0);

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

	cmp_rs485 : hdart generic map (clk_freq, 38400)
			port map (clk, reset, s1_txd, s1_rxd,
				din(7 downto 0), ua2_dout,
				ua2_wr, ua2_tdre,
				ua2_rd, ua2_rdrf,
				s1_cts, s1_rts
		);

	cmp_sigdel : sigdel generic map (clk_freq)
			port map (clk, reset,
				sd_dout,
				sdi, sdo
		);

	cmp_iadc : iadc generic map (clk_freq)
			port map (clk, reset,
				iadc_dout,
				iadc_sdi, iadc_sdo
		);

	cmp_mul : mul generic map (width)
			port map (clk,
				din, mul_wra, mul_wrb,
				mul_dout
		);

--

--
--	read
--
process(clk, reset, rd, addr, io_addr, 
	ua_rdrf, ua_tdre, ua_dout, ua2_rdrf, ua2_tdre, ua2_dout, key_in, sense_u, sense_i, tast, sensor)
begin
	if (reset='1') then
		ua_rd <= '0';
		ua2_rd <= '0';
		dout <= std_logic_vector(to_unsigned(0, width));
	elsif rising_edge(clk) then
		ua_rd <= '0';
		ua2_rd <= '0';
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
					when "0001" =>
						dout <= std_logic_vector(to_unsigned(0, width-6)) &
										ua2_rdrf & ua2_tdre & "00" & ua_rdrf & ua_tdre;
					when "0010" =>
						dout <= std_logic_vector(to_unsigned(0, width-8)) & ua_dout;
						ua_rd <= '1';
					when "0100" =>
						dout <= std_logic_vector(to_unsigned(0, width-16)) & sd_dout;
					when "0101" =>
						dout <= iadc_dout;
					when "1000" =>
						dout <= std_logic_vector(to_unsigned(0, width-4)) & tast;
					when "1001" =>
						dout <= std_logic_vector(to_unsigned(0, width-4)) & sensor;
					when "1010" =>
						dout <= std_logic_vector(clock_cnt);
					when "1101" =>
						dout <= std_logic_vector(to_unsigned(0, width-4)) & key_in;
					when "1110" =>
						dout <= std_logic_vector(to_unsigned(0, width-7)) & sense_i & sense_u;
					when "1111" =>
						dout <= std_logic_vector(to_unsigned(0, width-8)) & ua2_dout;
						ua2_rd <= '1';
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
		ua2_wr <= '0';
		mem_rd <= '0';
		mem_wr <= '0';
		addr_wr <= '0';
		disp_wr <= '0';
		key_wr <= '0';
		tr_wr <= '0';
		wd_wr <= '0';
		led_wr <= '0';
		rel_wr <= '0';
		mul_wra <= '0';
		mul_wrb <= '0';
		exp_wr <= '0';


	elsif rising_edge(clk) then
		io_addr_wr <= '0';

		ua_wr <= '0';
		ua2_wr <= '0';
		mem_rd <= '0';
		mem_wr <= '0';
		addr_wr <= '0';
		disp_wr <= '0';
		key_wr <= '0';
		tr_wr <= '0';
		wd_wr <= '0';
		led_wr <= '0';
		rel_wr <= '0';
		mul_wra <= '0';
		mul_wrb <= '0';
		exp_wr <= '0';

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
					when "0010" =>
						ua_wr <= '1';
					when "0100" =>
						exp_wr <= '1';
					when "0111" =>
						wd_wr <= '1';
					when "1000" =>
						led_wr <= '1';
					when "1001" =>
						rel_wr <= '1';
					when "1100" =>
						disp_wr <= '1';
					when "1101" =>
						key_wr <= '1';
					when "1110" =>
						tr_wr <= '1';
					when "1111" =>
						ua2_wr <= '1';
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
process(clk, reset, din, disp_wr, key_wr, tr_wr, wd_wr, led_wr, rel_wr, addr_wr)

begin
	if (reset='1') then

		io_addr <= (others => '0');
		mem_wr_addr <= std_logic_vector(to_unsigned(0, 20));
		disp <= (others => '0');
		key_out <= (others => '0');
		tr_on <= '0';
		tr_dir <= '0';
		wd <= '0';
		led <= (others => '0');
		relais <= (others => '0');
		exp <= (others => '0');

	elsif rising_edge(clk) then

		if (io_addr_wr='1') then
			io_addr <= din(3 downto 0);
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
			tr_on <= din(0);
			tr_dir <= din(1);
		end if;
		if (wd_wr='1') then
			wd <= din(0);
		end if;
		if (led_wr='1') then
			led <= din(3 downto 0);
		end if;
		if (rel_wr='1') then
			relais <= din(3 downto 0);
		end if;
		if (exp_wr='1') then
			exp <= din(2 downto 0);
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

end rtl;
