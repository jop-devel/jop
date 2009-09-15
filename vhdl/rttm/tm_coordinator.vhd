library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;


entity tm_coordinator is
generic (
	cpu_cnt			: integer := 32
);

port (
	clk							: in std_logic;
	reset						: in std_logic;

	commit_try					: in std_logic_vector(0 to cpu_cnt-1);
	commit_allow				: out std_logic_vector(0 to cpu_cnt-1)
);

end tm_coordinator;

architecture rtl of tm_coordinator is
	signal commit_allow_internal: std_logic_vector(0 to cpu_cnt-1);
	signal next_commit_allow_internal: std_logic_vector(0 to cpu_cnt-1);
begin
	commit_allow <= next_commit_allow_internal;

	sync: process (clk, reset) is
	begin
	    if reset = '1' then
	    	commit_allow_internal <= (others => '0');
	    elsif rising_edge(clk) then
	    	commit_allow_internal <= next_commit_allow_internal;
	    end if;
	end process sync;
	
	async: process(commit_try, commit_allow_internal) is
		variable commit_continued: std_logic;
		variable allow: std_logic_vector(0 to cpu_cnt-1);
		variable allowed: std_logic;
	begin
		commit_continued := '0';		
	
		for i in 0 to cpu_cnt-1 loop
			commit_continued := commit_continued or 
				(commit_allow_internal(i) and commit_try(i));
		end loop;
		
		allow := (others => '0');		
		allowed := '0';
		
		for i in 0 to cpu_cnt-1 loop
			if commit_try(i) = '1' and allowed = '0' then
				allow(i) := '1';
				allowed := '1';					
			end if;
		end loop;
		
		if commit_continued = '1' then
			next_commit_allow_internal <= commit_allow_internal;
		else
			next_commit_allow_internal <= allow;
		end if;
	end process async;

end architecture rtl;
