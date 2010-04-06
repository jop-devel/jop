--
--
--  This file is a part of an evaluation of CSP with a JOP CMP
--
--  Copyright (C) 2010, Flavius Gruian )Flavius.Gruian@cs.lth.se)
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


library IEEE;
use IEEE.STD_LOGIC_1164.all;

package NoCTypes is

  constant NOCNODES : integer := 3;
  constant NOCADDRBITS			: integer := 3;
  
type NoCPacketType is (PTNil, PTData, PTEoD, PTAck);
subtype NoCAddr is std_logic_vector(NOCADDRBITS-1 downto 0);

type NoCPacket is record
	 -- this is also the Slot in TDMA!
    Src : NoCAddr;
	 -- destination
    Dst : NoCAddr;
    pType : NoCPacketType;
    Load : std_logic_vector(31 downto 0);
end record;

-- for SimpCon
type sc_addr_type is array(0 to NOCNODES-1) of STD_LOGIC_VECTOR (1 downto 0);
type sc_rdy_cnt_type is array(0 to NOCNODES-1) of STD_LOGIC_VECTOR (1 downto 0);
type sc_bit_type is array(0 to NOCNODES-1) of STD_LOGIC;
type sc_word_type is array(0 to NOCNODES-1) of STD_LOGIC_VECTOR (31 downto 0);
type sc_io_type is array(0 to NOCNODES-1) of NoCPacket;

--  function <function_name>  (signal <signal_name> : in <type_declaration>) return <type_declaration>;
function tob (arg : boolean) return std_ulogic;
--  procedure <procedure_name>	(<type_declaration> <constant_name>	: in <type_declaration>);

end NoCTypes;


package body NoCTypes is

---- Example 1
--  function <function_name>  (signal <signal_name> : in <type_declaration>  ) return <type_declaration> is
--    variable <variable_name>     : <type_declaration>;
--  begin
--    <variable_name> := <signal_name> xor <signal_name>;
--    return <variable_name>; 
--  end <function_name>;
--

function tob (arg : boolean)
return std_ulogic is begin
if arg then
return '1';
else
return '0';
end if;
end function tob;


--
---- Example 2
--  function <function_name>  (signal <signal_name> : in <type_declaration>;
--                         signal <signal_name>   : in <type_declaration>  ) return <type_declaration> is
--  begin
--    if (<signal_name> = '1') then
--      return <signal_name>;
--    else
--      return 'Z';
--    end if;
--  end <function_name>;
--
---- Procedure Example
--  procedure <procedure_name>  (<type_declaration> <constant_name>  : in <type_declaration>) is
--    
--  begin
--    
--  end <procedure_name>;
 
end NoCTypes;
