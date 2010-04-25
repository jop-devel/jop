--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2009-2010, Peter Hilber (peter@hilber.name)
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--


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
	commit_token_grant			: out std_logic_vector(0 to cpu_cnt-1)
	
	--
	-- The time between the release of the commit token and the next 
	-- granting of the commit token is long enough to detect any conflicts 
	-- between two transactions, if the memory arbiter satisfies the 
	-- requirements in the thesis "Hardware Transactional Memory for a 
	-- Real-Time Chip Multiprocessor", paragraph "Memory arbiter behavior 
	-- requirements".   
	--
	
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
