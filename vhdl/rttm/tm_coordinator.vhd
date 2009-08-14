library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
--use ieee.std_logic_unsigned.all;
use ieee.numeric_std.all;

use work.tm_pack.all;


entity tm_coordinator is
generic (
	cpu_cnt			: integer := 8;
	cpu_cnt_width	: integer := 3
);

port (
	clk							: in std_logic;
	reset						: in std_logic;

	commit_try					: in std_logic_vector(0 to cpu_cnt-1);
	commit_allow				: buffer std_logic_vector(0 to cpu_cnt-1)
	
	-- TODO
-- 	commit_in_address_valid		: in std_logic_vector(0 to cpu_cnt-1);
-- 	commit_in_address			: in data_array(0 to cpu_cnt-1);
-- 	
-- 	commit_out_address_valid	: out std_logic;
-- 	commit_out_address			: out data
);
end tm_coordinator;

architecture rtl of tm_coordinator is
	signal next_committer		: std_logic_vector(cpu_cnt_width-1 downto 0);
	signal committer			: std_logic_vector(cpu_cnt_width-1 downto 0);
	signal commit_race_winner	: std_logic_vector(cpu_cnt_width-1 downto 0);

	signal next_commit_allow	: std_logic_vector(0 to cpu_cnt-1);
	signal next_committing		: std_logic;
	signal committing			: std_logic;
	
	signal commit_race_result	: std_logic_vector(0 to cpu_cnt-1);
		
begin
	sync: process(clk, reset) is
	begin
		if reset = '1' then
			committing <= '0';
			committer <= (others => '0');
			commit_allow <= (others => '0');
		elsif rising_edge(clk) then
			committing <= next_committing;
			committer <= next_committer;
			commit_allow <= next_commit_allow;
		end if;
	end process sync;
	
	main: process(commit_allow, commit_race_result, commit_race_winner, 
		commit_try, committer, committing) is
	begin
		next_committer <= committer;
		next_commit_allow <= commit_allow;
	
		-- TODO explicit cast
		if committing = '0' or commit_try(to_integer(ieee.numeric_std.unsigned(committer))) = '0' then
			next_committer <= commit_race_winner;
			next_commit_allow <= commit_race_result;
		end if;
		
		next_committing <= '0';
		for i in 0 to cpu_cnt-1 loop
			if commit_try(to_integer(ieee.numeric_std.unsigned(committer))) = '1' then
				next_committing <= '1';
			end if;
		end loop; 		
	end process main;

	selector: process(commit_try) is
		-- TODO fairness
		variable var_commit_race_result: std_logic_vector(0 to cpu_cnt-1);
	begin
		commit_race_winner <= (others => '0');
		
		for i in 0 to cpu_cnt-1 loop
			var_commit_race_result(i) := commit_try(i);
			if i /= 0 then
				for j in 0 to i-1 loop
					var_commit_race_result(i) := var_commit_race_result(i) and 
						(not commit_try(j));
				end loop;
			end if;
			
			if var_commit_race_result(i) = '1' then
				commit_race_winner <= std_logic_vector(to_unsigned(i, cpu_cnt_width));
			end if;
			
			commit_race_result(i) <= var_commit_race_result(i);
		end loop;
	end process selector;
	
end rtl; 
