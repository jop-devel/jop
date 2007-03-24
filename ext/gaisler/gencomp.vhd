------------------------------------------------------------------------------
--  This file is a part of the GRLIB VHDL IP LIBRARY
--  Copyright (C) 2003, Gaisler Research
--
--  This program is free software; you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation; either version 2 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program; if not, write to the Free Software
--  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
-----------------------------------------------------------------------------
-- Package: 	gencomp
-- File:	gencomp.vhd
-- Author:	Jiri Gaisler - Gaisler Research
-- Description:	Delcation of portable memory modules
------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;

package gencomp is

---------------------------------------------------------------------------
-- BASIC DECLARATIONS
---------------------------------------------------------------------------

-- technologies and libraries

constant NTECH : integer := 19;
type tech_ability_type is array (0 to NTECH) of integer;

constant inferred    : integer := 0;
constant virtex      : integer := 1;
constant virtex2     : integer := 2;
constant memvirage   : integer := 3;
constant axcel       : integer := 4;
constant proasic     : integer := 5;
constant atc18       : integer := 6;
constant altera      : integer := 7;
constant stratix     : integer := 7;
constant umc         : integer := 8;
constant rhumc       : integer := 9;
constant proasic3    : integer := 10;
constant spartan3    : integer := 11;
constant ihp25       : integer := 12; 
constant rhlib18t    : integer := 13;
constant virtex4     : integer := 14; 
constant lattice     : integer := 15;
constant ut25        : integer := 16;
constant spartan3e   : integer := 17;
constant peregrine   : integer := 18;
constant memartisan  : integer := 19;

constant DEFMEMTECH  : integer := inferred; 
constant DEFPADTECH  : integer := inferred; 
constant DEFFABTECH  : integer := inferred; 

constant is_fpga : tech_ability_type :=
	(inferred => 1, virtex => 1, virtex2 => 1, axcel => 1, 
	 proasic => 1, altera => 1, proasic3 => 1, spartan3 => 1,
         virtex4 => 1, lattice => 1, spartan3e => 1, others => 0);

constant infer_mul : tech_ability_type := is_fpga;

constant syncram_2p_write_through : tech_ability_type :=
	(inferred => 0, virtex => 0, virtex2 => 1, memvirage => 1, 
	 axcel => 0, proasic => 0, atc18 => 0, altera => 0, 
	 umc => 0, rhumc => 1, proasic3 => 0, spartan3 => 1,
         ihp25 => 0, rhlib18t => 0, virtex4 => 1, lattice => 0,
	 ut25 => 0, spartan3e => 1, others => 0);

constant regfile_3p_write_through : tech_ability_type :=
	(inferred => 0, virtex => 0, virtex2 => 1, memvirage => 1, 
	 axcel => 0, proasic => 0, atc18 => 0, altera => 0, 
	 umc => 0, rhumc => 1, proasic3 => 0, spartan3 => 1,
         ihp25 => 1, rhlib18t => 0, virtex4 => 1, lattice => 0,
	 ut25 => 0, spartan3e => 1, others => 0);

constant regfile_3p_infer : tech_ability_type :=
	(inferred => 1, rhumc => 1, ihp25 => 1, rhlib18t => 0,
	 peregrine => 1, others => 0);

constant has_sram : tech_ability_type :=
	(inferred => 1, virtex => 1, virtex2 => 1, memvirage => 1, 
	 axcel => 1, proasic => 1, atc18 => 0, altera => 1, 
	 umc => 0, rhumc => 1, proasic3 => 1, spartan3 => 1,
         ihp25 => 1, rhlib18t => 1, virtex4 => 1, lattice => 1,
	 ut25 => 1, spartan3e => 1, others => 1);

constant has_2pram : tech_ability_type :=
	(inferred => 1, virtex => 1, virtex2 => 1, memvirage => 1, 
	 axcel => 1, proasic => 1, atc18 => 0, altera => 1, 
	 umc => 0, rhumc => 0, proasic3 => 1, spartan3 => 1,
         ihp25 => 0, rhlib18t => 1, virtex4 => 1, lattice => 1,
	 ut25 => 1, spartan3e => 1, memartisan => 1, others => 0);

constant has_dpram : tech_ability_type :=
	(inferred => 0, virtex => 1, virtex2 => 1, memvirage => 1, 
	 axcel => 1, proasic => 0, atc18 => 0, altera => 1, 
	 umc => 0, rhumc => 0, proasic3 => 1, spartan3 => 1,
         ihp25 => 0, rhlib18t => 0, virtex4 => 1, lattice => 1,
	 ut25 => 0, spartan3e => 1, memartisan => 1, others => 0);

constant padoen_polarity : tech_ability_type :=
        (inferred => 0, virtex => 0, virtex2 => 0, memvirage => 0,
	 axcel => 1, proasic => 1, atc18 => 0, altera => 0,
	 umc => 0, rhumc => 1, spartan3 => 0, proasic3 => 1,
         ihp25 => 1, rhlib18t => 0, virtex4 => 0, lattice => 0,
	 ut25 => 1, spartan3e => 0, peregrine => 1, others => 0);

constant has_pads : tech_ability_type :=
	(inferred => 0, virtex => 1, virtex2 => 1, memvirage => 0, 
	 axcel => 1, proasic => 1, atc18 => 1, altera => 0, 
	 umc => 0, rhumc => 1, proasic3 => 1, spartan3 => 1,
         ihp25 => 1, rhlib18t => 1, virtex4 => 1, lattice => 0,
	 ut25 => 1, spartan3e => 1, peregrine => 1, others => 0);

constant has_ds_pads : tech_ability_type :=
	(inferred => 0, virtex => 1, virtex2 => 1, memvirage => 0, 
	 axcel => 1, proasic => 0, atc18 => 0, altera => 0, 
	 umc => 0, rhumc => 0, proasic3 => 0, spartan3 => 1,
         ihp25 => 0, rhlib18t => 1, virtex4 => 1, lattice => 0,
	 ut25 => 1, spartan3e => 1, others => 0);

constant has_ds_combo : tech_ability_type :=
	( rhumc => 1, ut25 => 1, others => 0);

-- pragma translate_off

  subtype tech_description is string(1 to 9);

  type tech_table_type is array (0 to NTECH) of tech_description;

  constant tech_table : tech_table_type := (
  inferred  => "inferred ", virtex    => "virtex   ", 
  virtex2   => "virtex2  ", memvirage => "virage   ", 
  axcel     => "axcel    ", proasic   => "proasic  ",
  atc18     => "atc18    ", altera    => "altera   ",
  umc       => "umc      ", rhumc     => "rhumc    ",
  proasic3  => "proasic3 ", spartan3  => "spartan3 ",
  ihp25     => "ihp25    ", rhlib18t  => "rhlib18t ",
  virtex4   => "virtex4  ", lattice   => "lattice  ",
  ut25      => "ut025crh ", spartan3e => "spartan3e",
  peregrine => "peregrine", memartisan => "artisan  ");

-- pragma translate_on

-- input/output voltage

constant x18v      : integer := 1;
constant x25v      : integer := 2;
constant x33v      : integer := 3;
constant x50v      : integer := 5;

-- input/output levels

constant ttl      : integer := 0;
constant cmos     : integer := 1;
constant pci33    : integer := 2;
constant pci66    : integer := 3;
constant lvds     : integer := 4;
constant sstl2_i  : integer := 5;
constant sstl2_ii : integer := 6;
constant sstl3_i  : integer := 7;
constant sstl3_ii : integer := 8;

-- pad types

constant normal   : integer := 0;
constant pullup   : integer := 1;
constant pulldown : integer := 2;
constant opendrain: integer := 3;
constant schmitt  : integer := 4;

---------------------------------------------------------------------------
-- MEMORY
---------------------------------------------------------------------------

-- synchronous single-port ram
  component syncram
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8);
  port (
    clk      : in std_ulogic;
    address  : in std_logic_vector((abits -1) downto 0);
    datain   : in std_logic_vector((dbits -1) downto 0);
    dataout  : out std_logic_vector((dbits -1) downto 0);
    enable   : in std_ulogic;
    write    : in std_ulogic); 
  end component;

-- synchronous two-port ram (1 read, 1 write port)
  component syncram_2p
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8; sepclk : integer := 0;
           wrfst : integer := 0);
  port (
    rclk     : in std_ulogic;
    renable  : in std_ulogic;
    raddress : in std_logic_vector((abits -1) downto 0);
    dataout  : out std_logic_vector((dbits -1) downto 0);
    wclk     : in std_ulogic;
    write    : in std_ulogic;
    waddress : in std_logic_vector((abits -1) downto 0);
    datain   : in std_logic_vector((dbits -1) downto 0));
  end component;

-- synchronous dual-port ram (2 read/write ports)
  component syncram_dp
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8);
  port (
    clk1     : in std_ulogic;
    address1 : in std_logic_vector((abits -1) downto 0);
    datain1  : in std_logic_vector((dbits -1) downto 0);
    dataout1 : out std_logic_vector((dbits -1) downto 0);
    enable1  : in std_ulogic;
    write1   : in std_ulogic;
    clk2     : in std_ulogic;
    address2 : in std_logic_vector((abits -1) downto 0);
    datain2  : in std_logic_vector((dbits -1) downto 0);
    dataout2 : out std_logic_vector((dbits -1) downto 0);
    enable2  : in std_ulogic;
    write2   : in std_ulogic); 
  end component;

-- synchronous 3-port regfile (2 read, 1 write port)
  component regfile_3p
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8;
           wrfst : integer := 0; numregs : integer := 64);
  port (
    wclk   : in  std_ulogic;
    waddr  : in  std_logic_vector((abits -1) downto 0);
    wdata  : in  std_logic_vector((dbits -1) downto 0);
    we     : in  std_ulogic;
    rclk   : in  std_ulogic;
    raddr1 : in  std_logic_vector((abits -1) downto 0);
    re1    : in  std_ulogic;
    rdata1 : out std_logic_vector((dbits -1) downto 0);
    raddr2 : in  std_logic_vector((abits -1) downto 0);
    re2    : in  std_ulogic;
    rdata2 : out std_logic_vector((dbits -1) downto 0));
  end component;

-- 64-bit synchronous single-port ram with 32-bit write strobe
  component syncram64
  generic (tech : integer := 0; abits : integer := 6);
  port (
    clk     : in  std_ulogic;
    address : in  std_logic_vector (abits -1 downto 0);
    datain  : in  std_logic_vector (63 downto 0);
    dataout : out std_logic_vector (63 downto 0);
    enable  : in  std_logic_vector (1 downto 0);
    write   : in  std_logic_vector (1 downto 0));
  end component;

  component syncramft
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8;
	ft : integer range 0 to 2 := 0 );
  port (
    clk      : in std_ulogic;
    address  : in std_logic_vector((abits -1) downto 0);
    datain   : in std_logic_vector((dbits -1) downto 0);
    dataout  : out std_logic_vector((dbits -1) downto 0);
    write    : in std_ulogic; 
    enable   : in std_ulogic;
    error    : out std_logic_vector((dbits + 7) / 8 downto 0));
  end component;

  component syncram_2pft
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8;
	sepclk : integer := 0; wrfst : integer := 0; ft : integer := 0);
  port (
    rclk     : in std_ulogic;
    renable  : in std_ulogic;
    raddress : in std_logic_vector((abits -1) downto 0);
    dataout  : out std_logic_vector((dbits -1) downto 0);
    wclk     : in std_ulogic;
    write    : in std_ulogic;
    waddress : in std_logic_vector((abits -1) downto 0);
    datain   : in std_logic_vector((dbits -1) downto 0);
    error    : out std_logic_vector(((dbits + 7) / 8)-1 downto 0));
  end component;


---------------------------------------------------------------------------
-- PADS
---------------------------------------------------------------------------

component inpad 
  generic (tech : integer := 0; level : integer := 0; 
	voltage : integer := x33v; filter : integer := 0;
	strength : integer := 0);
  port (pad : in std_ulogic; o : out std_ulogic);
end component; 

component inpadv 
  generic (tech : integer := 0; level : integer := 0; 
	   voltage : integer := x33v; width : integer := 1);
  port (
    pad : in  std_logic_vector(width-1 downto 0); 
    o   : out std_logic_vector(width-1 downto 0));
end component; 

component iopad 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	   voltage : integer := x33v; strength : integer := 12; 
	   oepol : integer := 0);
  port (pad : inout std_ulogic; i, en : in std_ulogic; o : out std_ulogic);
end component;

component iopadv 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1; 
	   oepol : integer := 0);
  port (
    pad : inout std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0);
    en  : in  std_ulogic;
    o   : out std_logic_vector(width-1 downto 0));
end component;

component iopadvv is
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1;
	oepol : integer := 0);
  port (
    pad : inout std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0);
    en  : in  std_logic_vector(width-1 downto 0);
    o   : out std_logic_vector(width-1 downto 0));
end component; 

component iodpad 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	   voltage : integer := x33v; strength : integer := 12; 
	   oepol : integer := 0);
  port (pad : inout std_ulogic; i : in std_ulogic; o : out std_ulogic);
end component;

component iodpadv 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1; 
	   oepol : integer := 0);
  port (
    pad : inout std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0);
    o   : out std_logic_vector(width-1 downto 0));
end component;

component outpad 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	   voltage : integer := x33v; strength : integer := 12);
  port (pad : out std_ulogic; i : in std_ulogic);
end component;

component outpadv 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0; 
	   voltage : integer := x33v; strength : integer := 12; width : integer := 1);
  port (
    pad : out std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0));
end component; 

component odpad 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	   voltage : integer := x33v; strength : integer := 12; 
	   oepol : integer := 0);
  port (pad : out std_ulogic; i : in std_ulogic);
end component;

component odpadv 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1; 
	   oepol : integer := 0);
  port (
    pad : out std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0));
end component; 

component toutpad 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	   voltage : integer := x33v; strength : integer := 12; 
	   oepol : integer := 0);
  port (pad : out std_ulogic; i, en : in std_ulogic);
end component;

component toutpadv 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1; 
	   oepol : integer := 0);
  port (
    pad : out std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0);
    en  : in  std_ulogic);
end component;

component toutpadvv is
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1;
	oepol : integer := 0);
  port (
    pad : out std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0);
    en  : in  std_logic_vector(width-1 downto 0));
end component;

component clkpad 
  generic (tech : integer := 0; level : integer := 0; 
	   voltage : integer := x33v; arch : integer := 0);
  port (pad : in std_ulogic; o : out std_ulogic);
end component; 

component inpad_ds
  generic (tech : integer := 0; level : integer := lvds; voltage : integer := x33v);
  port (padp, padn : in std_ulogic; o : out std_ulogic);
end component; 

component inpad_dsv
  generic (tech : integer := 0; level : integer := lvds; 
	   voltage : integer := x33v; width : integer := 1);
  port (
    padp : in  std_logic_vector(width-1 downto 0); 
    padn : in  std_logic_vector(width-1 downto 0); 
    o   : out std_logic_vector(width-1 downto 0));
end component; 

component outpad_ds 
  generic (tech : integer := 0; level : integer := lvds; 
	voltage : integer := x33v; oepol : integer := 0);
  port (padp, padn : out std_ulogic; i, en : in std_ulogic);
end component;

component outpad_dsv
  generic (tech : integer := 0; level : integer := lvds;
	voltage : integer := x33v; width : integer := 1);
  port (
    padp : out std_logic_vector(width-1 downto 0); 
    padn : out std_logic_vector(width-1 downto 0); 
    i, en: in  std_logic_vector(width-1 downto 0));
end component;

component lvds_combo  is
  generic (tech : integer := 0; voltage : integer := 0; width : integer := 1;
		oepol : integer := 0);
  port (odpadp, odpadn, ospadp, ospadn : out std_logic_vector(0 to width-1); 
        odval, osval, en : in std_logic_vector(0 to width-1); 
	idpadp, idpadn, ispadp, ispadn : in std_logic_vector(0 to width-1);
	idval, isval : out std_logic_vector(0 to width-1));
end component;

---------------------------------------------------------------------------
-- BUFFERS
---------------------------------------------------------------------------

  component techbuf is
    generic(
      buftype  :  integer range 0 to 4 := 0;
      tech     :  integer range 0 to NTECH := inferred);
    port(
      i        :  in  std_ulogic;
      o        :  out std_ulogic
    );
  end component;

---------------------------------------------------------------------------
-- CLOCK GENERATION
---------------------------------------------------------------------------

type clkgen_in_type is record
  pllref  : std_logic;			-- optional reference for PLL
  pllrst  : std_logic;			-- optional reset for PLL
  pllctrl : std_logic_vector(1 downto 0);  -- optional control for PLL
end record;

type clkgen_out_type is record
  clklock : std_logic;
  pcilock : std_logic;
end record;

component clkgen 
  generic (
    tech     : integer := DEFFABTECH; 
    clk_mul  : integer := 1; 
    clk_div  : integer := 1;
    sdramen  : integer := 0;
    noclkfb  : integer := 1;
    pcien    : integer := 0;
    pcidll   : integer := 0;
    pcisysclk: integer := 0;
    freq     : integer := 25000;
    clk2xen  : integer := 0);      
port (
    clkin   : in  std_logic;
    pciclkin: in  std_logic;
    clk     : out std_logic;			-- main clock
    clkn    : out std_logic;			-- inverted main clock
    clk2x   : out std_logic;			-- 2x clock
    sdclk   : out std_logic;			-- SDRAM clock
    pciclk  : out std_logic;			-- PCI clock
    cgi     : in clkgen_in_type;
    cgo     : out clkgen_out_type;
    clk4x   : out std_logic;			-- 4x clock
    clk1xu  : out std_logic;			-- unscaled 1X clock
    clk2xu  : out std_logic);			-- unscaled 2X clock
end component;

component clkmux is
  generic( tech : integer := 0);
  port(
    i0, i1  :  in  std_ulogic;
    sel     :  in  std_ulogic;
    o       :  out std_ulogic
  );
end component;

---------------------------------------------------------------------------
-- TAP controller   
---------------------------------------------------------------------------

component tap
  generic (
    tech   : integer := 0;    
    irlen  : integer range 2 to 8 := 4;
    idcode : integer range 0 to 255 := 9;
    id_msb : integer range 0 to 65535 := 0;
    id_lsb : integer range 0 to 65535 := 0);
  port (
    rst         : in std_ulogic;
    tck         : in std_ulogic;
    tms         : in std_ulogic;
    tdi         : in std_ulogic;
    tdo         : out std_ulogic;
    tapo_tck    : out std_ulogic;
    tapo_tdi    : out std_ulogic;
    tapo_inst   : out std_logic_vector(7 downto 0);
    tapo_rst    : out std_ulogic;
    tapo_capt   : out std_ulogic;
    tapo_shft   : out std_ulogic;
    tapo_upd    : out std_ulogic;
    tapo_xsel1  : out std_ulogic;
    tapo_xsel2  : out std_ulogic;
    tapi_en1    : in std_ulogic;
    tapi_tdo1   : in std_ulogic;
    tapi_tdo2   : in std_ulogic
    );
end component;

---------------------------------------------------------------------------
-- DDR registers
---------------------------------------------------------------------------

component ddr_ireg is
generic ( tech : integer);
port ( Q1 : out std_ulogic;
         Q2 : out std_ulogic;
         C1 : in std_ulogic;
         C2 : in std_ulogic;
         CE : in std_ulogic;
         D : in std_ulogic;
         R : in std_ulogic;
         S : in std_ulogic);
end component;

component ddr_oreg is generic ( tech : integer);
  port
    ( Q : out std_ulogic;
      C1 : in std_ulogic;
      C2 : in std_ulogic;
      CE : in std_ulogic;
      D1 : in std_ulogic;
      D2 : in std_ulogic;
      R : in std_ulogic;
      S : in std_ulogic);
end component;

end;

