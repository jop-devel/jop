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

	-- '1' while requesting and holding commit token.
	-- TODO register
	commit_token_request		: in std_logic_vector(0 to cpu_cnt-1);
	
	-- '1' when commit token is granted until token is given up 
	-- by resetting commit_token_request.
	-- TODO delay long enough for conflict detection
	commit_token_grant			: out std_logic_vector(0 to cpu_cnt-1)
);

end tm_coordinator;

architecture rtl of tm_coordinator is
	signal token_grant: std_logic_vector(0 to cpu_cnt-1);
	signal next_token_grant: std_logic_vector(0 to cpu_cnt-1);
begin
	commit_token_grant <= token_grant;

	sync: process (clk, reset) is
	begin
	    if reset = '1' then
	    	token_grant <= (others => '0');
	    elsif rising_edge(clk) then
	    	token_grant <= next_token_grant;
	    end if;
	end process sync;
	
	async: process(commit_token_request, token_grant) is
		variable commit_continued: std_logic;
		variable grant: std_logic_vector(0 to cpu_cnt-1);
		variable granted: std_logic;
	begin
		commit_continued := '0';		
	
		for i in 0 to cpu_cnt-1 loop
			commit_continued := commit_continued or 
				(token_grant(i) and commit_token_request(i));
		end loop;
		
		grant := (others => '0');		
		granted := '0';
		
		for i in 0 to cpu_cnt-1 loop
			if commit_token_request(i) = '1' and granted = '0' then
				grant(i) := '1';
				granted := '1';					
			end if;
		end loop;
		
		if commit_continued = '1' then
			next_token_grant <= token_grant;
		else
			next_token_grant <= grant;
		end if;
	end process async;

end architecture rtl;
