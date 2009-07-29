library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_std.all;

use work.sc_pack.all;

entity directmapped is
generic (
	index_bits : integer := 11;
	line_cnt : integer := 2048);
port (
	clk, reset:	    in std_logic;

	inval:			in std_logic;

	cpu_out:		in sc_out_type;
	cpu_in:			out sc_in_type;

	mem_out:		out sc_out_type;
	mem_in:			in sc_in_type);
end directmapped;

architecture rtl of directmapped is

	type cache_line_type is record
		data	: std_logic_vector(31 downto 0);
		tag		: std_logic_vector(SC_ADDR_SIZE-index_bits-1 downto 0);
		valid   : std_logic;
	end record;

	signal int_reset : std_logic;
	
	signal ram_din : cache_line_type;
	signal ram_wraddress : std_logic_vector(index_bits-1 downto 0);
	signal ram_wren : std_logic;

	signal ram_dout : cache_line_type;
	signal ram_rdaddress : std_logic_vector(index_bits-1 downto 0);

	signal cpu_out_reg, next_cpu_out : sc_out_type;
	
	signal rddata_reg, next_rddata : std_logic_vector(31 downto 0);
	signal fetchtag_reg, next_fetchtag : std_logic_vector(SC_ADDR_SIZE-1 downto 0);
	signal fetch_reg, next_fetch : std_logic;
	
	type STATE_TYPE is (idle,
						rd0, rd1, rd2,
						wr0, wr1);
	signal state, next_state : STATE_TYPE;

	signal hit : std_logic;
	
begin

	int_reset <= reset or inval;
	
	cache_ram: entity work.sdpram
		generic map (
			width	   => 32+SC_ADDR_SIZE-index_bits+1,
			addr_width => index_bits)
		port map (
			wrclk	   => clk,
			data(32+SC_ADDR_SIZE-index_bits downto SC_ADDR_SIZE-index_bits+1) => ram_din.data,
			data(SC_ADDR_SIZE-index_bits downto 1) => ram_din.tag,
			data(0)    => ram_din.valid,
			wraddress  => ram_wraddress,
			wren	   => ram_wren,
			
			rdclk	   => clk,
			dout(32+SC_ADDR_SIZE-index_bits downto SC_ADDR_SIZE-index_bits+1) => ram_dout.data,
			dout(SC_ADDR_SIZE-index_bits downto 1) => ram_dout.tag,
			dout(0)    => ram_dout.valid,
			rdaddress  => ram_rdaddress,
			rden	   => '1');

	sync: process (clk, int_reset)
	begin  -- process sync
		if int_reset = '1' then  -- asynchronous reset (active low)
			
			cpu_out_reg <= ((others => '0'), (others => '0'), '0', '0', '0', bypass);
			rddata_reg <= (others => '0');
			fetchtag_reg <= (others => '0');
			fetch_reg <= '0';
			state <= idle;

		elsif clk'event and clk = '1' then  -- rising clock edge

			cpu_out_reg <= next_cpu_out;
			rddata_reg <= next_rddata;
			fetchtag_reg <= next_fetchtag;
			fetch_reg <= next_fetch;
			state <= next_state;

		end if;
	end process sync;
	
	async: process (cpu_out, mem_in, ram_dout,
					cpu_out_reg, rddata_reg, fetchtag_reg, fetch_reg, state)
	begin  -- process async

		next_cpu_out <= cpu_out_reg;
		next_rddata <= rddata_reg;
		next_fetchtag <= fetchtag_reg;
		next_fetch <= '0';
		next_state <= state;

		cpu_in.rd_data <= rddata_reg;
		cpu_in.rdy_cnt <= "00";

		mem_out.address <= cpu_out_reg.address;
		mem_out.wr_data <= cpu_out_reg.wr_data;
		mem_out.rd <= '0';
		mem_out.wr <= '0';
		mem_out.cache <= cpu_out_reg.cache;
		mem_out.atomic <= cpu_out_reg.atomic;

		ram_rdaddress <= cpu_out.address(index_bits-1 downto 0);

		ram_din.valid <= '1';
		ram_din.data <= mem_in.rd_data;
		ram_din.tag <= cpu_out_reg.address(SC_ADDR_SIZE-1 downto index_bits);
		ram_wraddress <= cpu_out_reg.address(index_bits-1 downto 0);
		ram_wren <= '0';		
		
		hit <= '0';

		if fetch_reg = '1' then
			cpu_in.rd_data <= mem_in.rd_data;
			next_rddata <= mem_in.rd_data;
			ram_din.tag <= fetchtag_reg(SC_ADDR_SIZE-1 downto index_bits);
			ram_wraddress <= fetchtag_reg(index_bits-1 downto 0);
			ram_wren <= '1';
		end if;

		case state is

			when rd0 =>
				cpu_in.rdy_cnt <= "11";

				if ram_dout.tag = cpu_out_reg.address(SC_ADDR_SIZE-1 downto index_bits)
					and ram_dout.valid = '1' then
					
					next_rddata <= ram_dout.data;
					next_state <= idle;
					
					hit <= '1';
				else
					next_state <= rd1;
				end if;

			when rd1 =>
				mem_out.rd <= '1';
				cpu_in.rdy_cnt <= "11";
				next_state <= rd2;

			when rd2 =>
				cpu_in.rdy_cnt <= mem_in.rdy_cnt;				
				if mem_in.rdy_cnt <= 1 then
					next_fetchtag <= cpu_out_reg.address;
					next_fetch <= '1';
					next_state <= idle;
				end if;
				
			when wr0 =>
				ram_din.data <= cpu_out_reg.wr_data;
				ram_wren <= '1';
				
				mem_out.wr <= '1';				
				cpu_in.rdy_cnt <= "11";
				next_state <= wr1;
				
			when wr1 =>
				cpu_in.rdy_cnt <= mem_in.rdy_cnt;				
				if mem_in.rdy_cnt <= 1 then
					next_state <= idle;
				end if;
				
			when others => null;
		end case;

		if state = idle
			or state = wr1
			or state = rd2 then

			if cpu_out.rd = '1' or cpu_out.wr = '1' then
				next_cpu_out <= cpu_out;
			end if;

			if cpu_out.rd = '1' and cpu_out.cache = direct_mapped then
				next_state <= rd0;
			end if;
			if cpu_out.wr = '1' and cpu_out.cache = direct_mapped then
				next_state <= wr0;
			end if;

		end if;
	
		
	end process async;
	
end rtl;
