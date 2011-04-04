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

--  constant NOCNODES : integer := 3;
  constant NOCADDRBITS			: integer := 3;
  constant NOCPACKETBITS : integer := 2*NOCADDRBITS+2+32;
  
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
type sc_addr_type is array(natural RANGE <>) of STD_LOGIC_VECTOR (1 downto 0);
type sc_rdy_cnt_type is array(natural RANGE <>) of STD_LOGIC_VECTOR (1 downto 0);
type sc_bit_type is array(natural RANGE <>) of STD_LOGIC;
type sc_word_type is array(natural RANGE <>) of STD_LOGIC_VECTOR (31 downto 0);
type sc_io_type is array(natural RANGE <>) of NoCPacket;

--  function <function_name>  (signal <signal_name> : in <type_declaration>) return <type_declaration>;
function tob (arg : boolean) return std_ulogic;
function to_std_logic_vector(pT:NoCPacketType) return std_logic_vector;
function to_NoCPacketType(v:std_logic_vector(1 downto 0)) return NoCPacketType;
function to_std_logic_vector(L: NoCPacket) return std_logic_vector;
function to_NoCPacket(v:std_logic_vector(NOCPACKETBITS-1 downto 0)) return NoCPacket;
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

function to_std_logic_vector(pT:NoCPacketType) return std_logic_vector is
variable RetVal: std_logic_vector(1 downto 0);
begin
case pT is
 when PTNil => RetVal := "00";
 when PTData => RetVal := "01";
 when PTEoD => RetVal := "10";
 when PTAck => RetVal := "11";
-- when others => <output> <= "00";
end case;
return(RetVal);
end function to_std_logic_vector;


function to_NoCPacketType(v:std_logic_vector(1 downto 0)) return NoCPacketType is
begin
	case v is
		when "00" => return PTNil;
		when "01" => return PTData;
		when "10" => return PTEoD;
		when "11" => return PTAck;
		when others => return PTNil;
	end case;
end function to_NoCPacketType;


function to_std_logic_vector(L: NoCPacket) return std_logic_vector is
variable RetVal: std_logic_vector(NOCPACKETBITS-1 downto 0);
begin
RetVal := L.Src & L.Dst & to_std_logic_vector(L.pType) & L.Load;
return(RetVal);
end function to_std_logic_vector;

function to_NoCPacket(v:std_logic_vector(NOCPACKETBITS-1 downto 0)) return NoCPacket is
variable ret: NoCPacket;
begin

ret.Src := v(NOCPACKETBITS-1 downto 34+NOCADDRBITS);
ret.Dst := v(34+NOCADDRBITS-1 downto 34);
ret.pType := to_NoCPacketType(v(33 downto 32));
ret.Load := v(31 downto 0);
return ret;
end function to_NoCPacket;

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
