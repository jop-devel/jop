--
--	cache.vhd
--
--	Bytecode caching
--
--
--	2005-01-11	first version
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity cache is
generic (jpc_width : integer := 10; tag_width : integer := 14);
-- generic (jpc_width : integer; tag_width : integer := 14); -- use only 64KB cachable area!

port (

	clk, reset	: in std_logic;

	bc_len		: in std_logic_vector(9 downto 0);		-- length of method in words
	bc_addr		: in std_logic_vector(17 downto 0);		-- memory address of bytecode

	find		: in std_logic;							-- start lookup

	-- start of method in bc cache
	-- in 32 bit words - we load only at word boundries
	bcstart		: out std_logic_vector(jpc_width-3 downto 0);

	rdy			: out std_logic;						-- lookup finished
	in_cache	: out std_logic							-- method is in cache

);
end cache;

architecture rtl of cache is

--
--	signals for mem interface
--
	type state_type		is (
							idle, s1, s2
						);
	signal state 		: state_type;

	signal block_addr	: std_logic_vector(1 downto 0);
	-- tag_width can be used to reduce cachable area - saves a lot in the comperators
	signal use_addr		: std_logic_vector(tag_width-1 downto 0);
	signal bla, blb		: std_logic_vector(tag_width-1 downto 0);
	signal blc, bld		: std_logic_vector(tag_width-1 downto 0);
	signal nxt			: unsigned(1 downto 0);

	signal clr_idx		: std_logic_vector(3 downto 0);
	signal clr_val		: std_logic_vector(3 downto 0);

begin

	bcstart <= block_addr & "000000";
	use_addr <= bc_addr(tag_width-1 downto 0);

process(clk, reset, find)

begin
	if (reset='1') then
		state <= idle;
		rdy <= '1';
		in_cache <= '0';
		block_addr <= (others => '0');
		nxt <= "00";
		bla <= (others => '0');
		blb <= (others => '0');
		blc <= (others => '0');
		bld <= (others => '0');

	elsif rising_edge(clk) then

		case state is

			when idle =>
				state <= idle;
				rdy <= '1';
				if find = '1' then
					rdy <= '0';
					state <= s1;
				end if;

			-- check for a hit
			when s1 =>

				in_cache <= '0';
				state <= s2;
				block_addr <= std_logic_vector(nxt);

				if bla = use_addr then
					block_addr <= "00";
					in_cache <= '1';
					state <= idle;
				elsif blb = use_addr then
					block_addr <= "01";
					in_cache <= '1';
					state <= idle;
				elsif blc = use_addr then
					block_addr <= "10";
					in_cache <= '1';
					state <= idle;
				elsif bld = use_addr then
					block_addr <= "11";
					in_cache <= '1';
					state <= idle;
				end if;

			-- correct tag memory on a miss
			when s2 =>

				if clr_val(0) = '1' then
					bla <= (others => '0');
				end if;
				if clr_val(1) = '1' then
					blb <= (others => '0');
				end if;
				if clr_val(2) = '1' then
					blc <= (others => '0');
				end if;
				if clr_val(3) = '1' then
					bld <= (others => '0');
				end if;

				if nxt = "00" then
					bla <= use_addr;
				end if;
				if nxt = "01" then
					blb <= use_addr;
				end if;
				if nxt = "10" then
					blc <= use_addr;
				end if;
				if nxt = "11" then
					bld <= use_addr;
				end if;

				state <= idle;
				nxt <= nxt + unsigned(bc_len(7 downto 6)) + 1;


		end case;
					
	end if;
end process;

	clr_idx <= std_logic_vector(nxt) & bc_len(7 downto 6);

process(clr_idx) begin

	case clr_idx is

		when "0000" => clr_val <= "0000";
		when "0001" => clr_val <= "0010";
		when "0010" => clr_val <= "0110";
		when "0011" => clr_val <= "1110";
		when "0100" => clr_val <= "0000";
		when "0101" => clr_val <= "0100";
		when "0110" => clr_val <= "1100";
		when "0111" => clr_val <= "1101";
		when "1000" => clr_val <= "0000";
		when "1001" => clr_val <= "1000";
		when "1010" => clr_val <= "1001";
		when "1011" => clr_val <= "1011";
		when "1100" => clr_val <= "0000";
		when "1101" => clr_val <= "0001";
		when "1110" => clr_val <= "0011";
		when "1111" => clr_val <= "0111";
		when others => clr_val <= "0000";

	end case;

end process;

end rtl;
