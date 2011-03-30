library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_std.all;

use work.sc_pack.all;

entity datacache is
port (
	clk, reset:	    in std_logic;

	cpu_out:		in sc_out_type;
	cpu_in:			out sc_in_type;

	mem_out:		out sc_out_type;
	mem_in:			in sc_in_type);
end datacache;

architecture rtl of datacache is

	type mux_type is (bp, dmc, dm, fa);
	signal out_mux_reg, next_out_mux : mux_type;	
	signal in_mux_reg, next_in_mux : mux_type;	
	
	signal dmc_cpu_in, dm_cpu_in, fa_cpu_in : sc_in_type;
	signal dmc_mem_out, dm_mem_out, fa_mem_out : sc_out_type;

	signal bp_fetch, next_bp_fetch : std_logic;
	signal bp_rd_data, next_bp_rd_data : std_logic_vector(31 downto 0);
	
begin  -- rtl

	cmp_dmc: entity work.directmapped_const
		port map (
			clk		=> clk,
			reset	=> reset,
			cpu_in	=> dmc_cpu_in,
			cpu_out => cpu_out,
			mem_in	=> mem_in,
			mem_out => dmc_mem_out);

	cmp_dm: entity work.directmapped
		port map (
			clk		=> clk,
			reset	=> reset,
			cpu_in	=> dm_cpu_in,
			cpu_out => cpu_out,
			mem_in	=> mem_in,
			mem_out => dm_mem_out);

	cmp_fa: entity work.lru
		port map (
			clk		=> clk,
			reset	=> reset,
			cpu_in	=> fa_cpu_in,
			cpu_out => cpu_out,
			mem_in	=> mem_in,
			mem_out => fa_mem_out);

	sync: process (clk, reset)
	begin  -- process sync
		if reset = '1' then  			-- asynchronous reset (active low)
			out_mux_reg <= bp;
			in_mux_reg <= bp;
			bp_fetch <= '0';
			bp_rd_data <= (others => '0');			
		elsif clk'event and clk = '1' then  -- rising clock edge
			out_mux_reg <= next_out_mux;
			in_mux_reg <= next_in_mux;
			bp_fetch <= next_bp_fetch;
			bp_rd_data <= next_bp_rd_data;
		end if;
	end process sync;

	async: process (cpu_out, mem_in,
					out_mux_reg, in_mux_reg,
					dmc_mem_out, dm_mem_out, fa_mem_out,
					dmc_cpu_in, dm_cpu_in, fa_cpu_in,
					bp_rd_data, bp_fetch)

		variable bp_rd, bp_wr : std_logic;
		
	begin  -- process async
		
		next_out_mux <= out_mux_reg;
		next_in_mux <= in_mux_reg;
		next_bp_fetch <= '0';
		next_bp_rd_data <= bp_rd_data;

		case out_mux_reg is
			when dmc =>
				mem_out <= dmc_mem_out;
				cpu_in.rdy_cnt <= dmc_cpu_in.rdy_cnt;
			when dm =>
				mem_out <= dm_mem_out;
				cpu_in.rdy_cnt <= dm_cpu_in.rdy_cnt;
			when fa =>
				mem_out <= fa_mem_out;					   
				cpu_in.rdy_cnt <= fa_cpu_in.rdy_cnt;
			when others =>
				mem_out <= cpu_out;
				cpu_in.rdy_cnt <= mem_in.rdy_cnt;
		end case;

		bp_rd := '0';
		bp_wr := '0';

		if cpu_out.rd = '1' or cpu_out.wr = '1' then
			case cpu_out.cache is
				when direct_mapped_const =>
					next_out_mux <= dmc;									  
				when direct_mapped =>
					next_out_mux <= dm;									  
				when full_assoc =>
					next_out_mux <= fa;
				when others =>
					next_out_mux <= bp;
					-- immediate bypassing
					mem_out <= cpu_out;
					bp_rd := cpu_out.rd;
					bp_wr := cpu_out.wr;
			end case;
		end if;

		-- simplify rd/wr path; precondition: caches assert rd/wr only when necessary
		mem_out.rd <= dmc_mem_out.rd or dm_mem_out.rd or fa_mem_out.rd or bp_rd;
		mem_out.wr <= dmc_mem_out.wr or dm_mem_out.wr or fa_mem_out.wr or bp_wr;

		case in_mux_reg is
			when dmc =>
				cpu_in.rd_data <= dmc_cpu_in.rd_data;
			when dm =>
				cpu_in.rd_data <= dm_cpu_in.rd_data;
			when fa =>
				cpu_in.rd_data <= fa_cpu_in.rd_data;					   
			when others =>
				cpu_in.rd_data <= bp_rd_data;
		end case;

		if out_mux_reg = bp and mem_in.rdy_cnt(1) = '0' then
			next_in_mux <= bp;
			next_bp_fetch <= '1';
		end if;
		if out_mux_reg = dmc and dmc_cpu_in.rdy_cnt(1) = '0' then
			next_in_mux <= dmc;
		end if;
		if out_mux_reg = dm and dm_cpu_in.rdy_cnt(1) = '0' then
			next_in_mux <= dm;
		end if;
		if out_mux_reg = fa and fa_cpu_in.rdy_cnt(1) = '0' then
			next_in_mux <= fa;
		end if;

		if (out_mux_reg = bp and mem_in.rdy_cnt = "00") or bp_fetch = '1' then
			cpu_in.rd_data <= mem_in.rd_data;
			next_bp_rd_data <= mem_in.rd_data;
		end if;
		if out_mux_reg = dmc and dmc_cpu_in.rdy_cnt = "00" then
			cpu_in.rd_data <= dmc_cpu_in.rd_data;
		end if;
		if out_mux_reg = dm and dm_cpu_in.rdy_cnt = "00" then
			cpu_in.rd_data <= dm_cpu_in.rd_data;
		end if;
		if out_mux_reg = fa and fa_cpu_in.rdy_cnt = "00" then
			cpu_in.rd_data <= fa_cpu_in.rd_data;
		end if;

	end process async;
	
end rtl;
