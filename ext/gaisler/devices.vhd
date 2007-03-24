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
-- Entity: 	devices
-- File:	devices.vhd
-- Author:	Jiri Gaisler, Gaisler Research
-- Description:	Vendor and devices id's for amba plug&play
------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
library grlib;
use grlib.amba.all;
-- pragma translate_off
use std.textio.all;
-- pragma translate_on

package devices is

-- Vendor codes

  constant VENDOR_GAISLER    : amba_vendor_type := 16#01#;
  constant VENDOR_PENDER     : amba_vendor_type := 16#02#;
  constant VENDOR_ESA        : amba_vendor_type := 16#04#;
  constant VENDOR_ASTRIUM    : amba_vendor_type := 16#06#;
  constant VENDOR_OPENCHIP   : amba_vendor_type := 16#07#;
  constant VENDOR_OPENCORES  : amba_vendor_type := 16#08#;
  constant VENDOR_CONTRIB    : amba_vendor_type := 16#09#;
  constant VENDOR_EONIC      : amba_vendor_type := 16#0B#;
  constant VENDOR_RADIONOR   : amba_vendor_type := 16#0F#;
  constant VENDOR_GLEICHMANN : amba_vendor_type := 16#10#;
  constant VENDOR_MENTA      : amba_vendor_type := 16#11#;
  constant VENDOR_SUN        : amba_vendor_type := 16#13#;
  constant VENDOR_EMBEDDIT   : amba_vendor_type := 16#EA#;
  constant VENDOR_CAL        : amba_vendor_type := 16#CA#;



-- Gaisler Research device id's

  constant GAISLER_LEON2DSU  : amba_device_type := 16#002#;
  constant GAISLER_LEON3     : amba_device_type := 16#003#;
  constant GAISLER_LEON3DSU  : amba_device_type := 16#004#;
  constant GAISLER_ETHAHB    : amba_device_type := 16#005#;
  constant GAISLER_APBMST    : amba_device_type := 16#006#;
  constant GAISLER_AHBUART   : amba_device_type := 16#007#;
  constant GAISLER_SRCTRL    : amba_device_type := 16#008#;
  constant GAISLER_SDCTRL    : amba_device_type := 16#009#;
  constant GAISLER_SSRCTRL   : amba_device_type := 16#00A#;
  constant GAISLER_APBUART   : amba_device_type := 16#00C#;
  constant GAISLER_IRQMP     : amba_device_type := 16#00D#;
  constant GAISLER_AHBRAM    : amba_device_type := 16#00E#;
  constant GAISLER_GPTIMER   : amba_device_type := 16#011#;
  constant GAISLER_PCITRG    : amba_device_type := 16#012#;
  constant GAISLER_PCISBRG   : amba_device_type := 16#013#;
  constant GAISLER_PCIFBRG   : amba_device_type := 16#014#;
  constant GAISLER_PCITRACE  : amba_device_type := 16#015#;
  constant GAISLER_DMACTRL   : amba_device_type := 16#016#;
  constant GAISLER_AHBTRACE  : amba_device_type := 16#017#;
  constant GAISLER_DSUCTRL   : amba_device_type := 16#018#;
  constant GAISLER_CANAHB    : amba_device_type := 16#019#;
  constant GAISLER_GPIO      : amba_device_type := 16#01A#;
  constant GAISLER_AHBROM    : amba_device_type := 16#01B#;
  constant GAISLER_AHBJTAG   : amba_device_type := 16#01C#;
  constant GAISLER_ETHMAC    : amba_device_type := 16#01D#;
  constant GAISLER_SWNODE    : amba_device_type := 16#01E#;
  constant GAISLER_SPW       : amba_device_type := 16#01F#;
  constant GAISLER_AHB2AHB   : amba_device_type := 16#020#;
  constant GAISLER_USBCTRL   : amba_device_type := 16#021#;
  constant GAISLER_USBDCL    : amba_device_type := 16#022#;
  constant GAISLER_DDRMP     : amba_device_type := 16#023#;
  constant GAISLER_ATACTRL   : amba_device_type := 16#024#;
  constant GAISLER_DDRSP     : amba_device_type := 16#025#;
  constant GAISLER_EHCI      : amba_device_type := 16#026#;
  constant GAISLER_UHCI      : amba_device_type := 16#027#;

  constant GAISLER_AHBDMA    : amba_device_type := 16#02A#;
  constant GAISLER_NUHOSP3   : amba_device_type := 16#02B#;

  constant GAISLER_GRTM      : amba_device_type := 16#030#;
  constant GAISLER_GRTC      : amba_device_type := 16#031#;
  constant GAISLER_GRPW      : amba_device_type := 16#032#;
  constant GAISLER_GRCTM     : amba_device_type := 16#033#;

  constant GAISLER_GRHCAN    : amba_device_type := 16#034#;
  constant GAISLER_GRFIFO    : amba_device_type := 16#035#;
  constant GAISLER_GRADCDAC  : amba_device_type := 16#036#;
  constant GAISLER_GRPULSE   : amba_device_type := 16#037#;
  constant GAISLER_GRTIMER   : amba_device_type := 16#038#;
  constant GAISLER_AHB2PP    : amba_device_type := 16#039#;
  constant GAISLER_GRVERSION : amba_device_type := 16#03A#;
  constant GAISLER_APB2PW    : amba_device_type := 16#03B#;
  constant GAISLER_PW2APB    : amba_device_type := 16#03C#;

  constant GAISLER_AHBMST_EM : amba_device_type := 16#040#;
  constant GAISLER_AHBSLV_EM : amba_device_type := 16#041#;
  constant GAISLER_GRTESTMOD : amba_device_type := 16#042#;

  constant GAISLER_FTAHBRAM  : amba_device_type := 16#050#;
  constant GAISLER_FTSRCTRL  : amba_device_type := 16#051#;
  constant GAISLER_AHBSTAT   : amba_device_type := 16#052#;
  constant GAISLER_LEON3FT   : amba_device_type := 16#053#;
  constant GAISLER_FTMCTRL   : amba_device_type := 16#054#;
  constant GAISLER_FTSDCTRL  : amba_device_type := 16#055#;
  constant GAISLER_FTSRCTRL8 : amba_device_type := 16#056#;

  constant GAISLER_APBPS2    : amba_device_type := 16#060#;
  constant GAISLER_VGACTRL   : amba_device_type := 16#061#;
  constant GAISLER_LOGAN     : amba_device_type := 16#062#;
  constant GAISLER_SVGACTRL  : amba_device_type := 16#063#;

  constant GAISLER_B1553BC   : amba_device_type := 16#070#;
  constant GAISLER_B1553RT   : amba_device_type := 16#071#;
  constant GAISLER_B1553BRM  : amba_device_type := 16#072#;

  constant GAISLER_AES       : amba_device_type := 16#073#;
  constant GAISLER_ECC       : amba_device_type := 16#074#;

  constant GAISLER_PCIF      : amba_device_type := 16#075#;

-- Sun Microsystems

  constant SUN_T1	      : amba_device_type := 16#001#;
  constant SUN_S1	      : amba_device_type := 16#011#;

-- Caltech

  constant CAL_DDRCTRL	      : amba_device_type := 16#188#;

-- European Space Agency device id's

  constant ESA_LEON2        : amba_device_type := 16#002#;
  constant ESA_LEON2APB     : amba_device_type := 16#003#;
  constant ESA_IRQ          : amba_device_type := 16#005#;
  constant ESA_TIMER        : amba_device_type := 16#006#;
  constant ESA_UART         : amba_device_type := 16#007#;
  constant ESA_CFG          : amba_device_type := 16#008#;
  constant ESA_IO           : amba_device_type := 16#009#;
  constant ESA_MCTRL        : amba_device_type := 16#00F#;
  constant ESA_PCIARB       : amba_device_type := 16#010#;
  constant ESA_HURRICANE    : amba_device_type := 16#011#;
  constant ESA_SPW_RMAP     : amba_device_type := 16#012#;
  constant ESA_AHBUART      : amba_device_type := 16#013#;
  constant ESA_SPWA         : amba_device_type := 16#014#;
  constant ESA_BOSCHCAN     : amba_device_type := 16#015#;
  constant ESA_IRQ2         : amba_device_type := 16#016#;
  constant ESA_AHBSTAT      : amba_device_type := 16#017#;
  constant ESA_WPROT        : amba_device_type := 16#018#;

-- OpenChip ID's

  constant OPENCHIP_APBGPIO     : amba_device_type := 16#001#;
  constant OPENCHIP_APBI2C      : amba_device_type := 16#002#;
  constant OPENCHIP_APBSPI      : amba_device_type := 16#003#;
  constant OPENCHIP_APBCHARLCD  : amba_device_type := 16#004#;
  constant OPENCHIP_APBPWM      : amba_device_type := 16#005#;
  constant OPENCHIP_APBPS2      : amba_device_type := 16#006#;
  constant OPENCHIP_APBMMCSD    : amba_device_type := 16#007#;
  constant OPENCHIP_APBNAND     : amba_device_type := 16#008#;
  constant OPENCHIP_APBLPC      : amba_device_type := 16#009#;
  constant OPENCHIP_APBCF       : amba_device_type := 16#00A#;
  constant OPENCHIP_APBSYSACE   : amba_device_type := 16#00B#;
  constant OPENCHIP_APB1WIRE    : amba_device_type := 16#00C#;
  constant OPENCHIP_APBJTAG     : amba_device_type := 16#00D#;
  constant OPENCHIP_APBSUI      : amba_device_type := 16#00E#;


-- Gleichmann's device id's

  constant GLEICHMANN_CUSTOM   : amba_device_type := 16#001#;
  constant GLEICHMANN_GEOLCD01 : amba_device_type := 16#002#;
  constant GLEICHMANN_DAC      : amba_device_type := 16#003#;
  constant GLEICHMANN_HPI      : amba_device_type := 16#004#;
  constant GLEICHMANN_SPI      : amba_device_type := 16#005#;
  constant GLEICHMANN_HIFC     : amba_device_type := 16#006#;


-- Contribution library ID's

  constant CONTRIB_CORE1        : amba_device_type := 16#001#;
  constant CONTRIB_CORE2        : amba_device_type := 16#002#;


-- pragma translate_off

  constant GAISLER_DESC : vendor_description :=  "Gaisler Research        ";

  constant gaisler_device_table : device_table_type := (
   GAISLER_LEON2DSU  => "Leon2 Debug Support Unit       ",
   GAISLER_LEON3     => "Leon3 SPARC V8 Processor       ",
   GAISLER_LEON3DSU  => "Leon3 Debug Support Unit       ",
   GAISLER_ETHAHB    => "OC ethernet AHB interface      ",
   GAISLER_AHBRAM    => "Generic AHB SRAM module        ",
   GAISLER_APBMST    => "AHB/APB Bridge                 ",
   GAISLER_AHBUART   => "AHB Debug UART                 ",
   GAISLER_SRCTRL    => "Simple SRAM Controller         ",
   GAISLER_SDCTRL    => "PC133 SDRAM Controller         ",
   GAISLER_SSRCTRL   => "Synchronous SRAM Controller    ",
   GAISLER_APBUART   => "Generic UART                   ",
   GAISLER_IRQMP     => "Multi-processor Interrupt Ctrl.",
   GAISLER_GPTIMER   => "Modular Timer Unit             ",
   GAISLER_PCITRG    => "Simple 32-bit PCI Target       ",
   GAISLER_PCISBRG   => "Simple 32-bit PCI Bridge       ",
   GAISLER_PCIFBRG   => "Fast 32-bit PCI Bridge         ",
   GAISLER_PCITRACE  => "32-bit PCI Trace Buffer        ",
   GAISLER_DMACTRL   => "AMBA DMA controller            ",
   GAISLER_AHBTRACE  => "AMBA Trace Buffer              ",
   GAISLER_DSUCTRL   => "DSU/ETH controller             ",
   GAISLER_GRTM      => "CCSDS Telemetry Encoder        ",
   GAISLER_GRTC      => "CCSDS Telecommand Decoder      ",
   GAISLER_GRPW      => "PacketWire to AMBA AHB I/F     ",
   GAISLER_GRCTM     => "CCSDS Time Manager             ",
   GAISLER_GRHCAN    => "ESA HurriCANe CAN with DMA     ",
   GAISLER_GRFIFO    => "FIFO Controller                ",
   GAISLER_GRADCDAC  => "ADC / DAC Interface            ",
   GAISLER_GRPULSE   => "General Purpose I/O with Pulses",
   GAISLER_GRTIMER   => "Timer Unit with Latches        ",
   GAISLER_AHB2PP    => "AMBA AHB to Packet Parallel I/F",
   GAISLER_GRVERSION => "Version and Revision Register  ",
   GAISLER_APB2PW    => "PacketWire Transmit Interface  ",
   GAISLER_PW2APB    => "PacketWire Receive Interface   ",
   GAISLER_AHBMST_EM => "AMBA Master Emulator           ",
   GAISLER_AHBSLV_EM => "AMBA Slave Emulator            ",
   GAISLER_CANAHB    => "OC CAN AHB interface           ",
   GAISLER_GPIO      => "General Purpose I/O port       ",
   GAISLER_AHBROM    => "Generic AHB ROM                ",
   GAISLER_AHB2AHB   => "AHB-to-AHB Bridge              ",
   GAISLER_NUHOSP3   => "Nuhorizons Spartan3 IO I/F     ",
   GAISLER_FTAHBRAM  => "Generic FT AHB SRAM module     ",
   GAISLER_FTSRCTRL  => "Simple FT SRAM Controller      ",
   GAISLER_LEON3FT   => "Leon3-FT SPARC V8 Processor    ",
   GAISLER_FTMCTRL   => "Memory controller with EDAC    ",
   GAISLER_AHBSTAT   => "AHB Status Register            ",
   GAISLER_AHBJTAG   => "JTAG Debug Link                ",
   GAISLER_ETHMAC    => "GR Ethernet MAC                ",
   GAISLER_SWNODE    => "SpaceWire Node Interface       ",
   GAISLER_SPW       => "SpaceWire Serial Link          ",
   GAISLER_VGACTRL   => "VGA controller                 ",
   GAISLER_APBPS2    => "PS2 interface                  ",
   GAISLER_LOGAN     => "On chip Logic Analyzer         ",
   GAISLER_SVGACTRL  => "SVGA frame buffer              ",
   GAISLER_B1553BC   => "AMBA Wrapper for Core1553BBC   ",
   GAISLER_B1553RT   => "AMBA Wrapper for Core1553BRT   ",
   GAISLER_B1553BRM  => "AMBA Wrapper for Core1553BRM   ",
   GAISLER_AES       => "Advanced Encryption Standard   ",
   GAISLER_ECC       => "Elliptic Curve Cryptography    ",
   GAISLER_PCIF      => "AMBA Wrapper for CorePCIF      ",
   GAISLER_USBCTRL   => "USB 2.0 Controller             ",
   GAISLER_USBDCL    => "USB Debug Communication Link   ",
   GAISLER_DDRMP     => "Multi-port DDR controller      ",
   GAISLER_ATACTRL   => "ATA controller                 ",
   GAISLER_DDRSP     => "Single-port DDR266 controller  ",
   GAISLER_GRTESTMOD => "Test report module             ",
   others            => "Unknown Device                 ");

   constant gaisler_lib : vendor_library_type := (
     vendorid 	     => VENDOR_GAISLER,
     vendordesc      => GAISLER_DESC,
     device_table    => gaisler_device_table
   );

  constant ESA_DESC : vendor_description := "European Space Agency   ";

  constant esa_device_table : device_table_type := (
   ESA_LEON2        => "Leon2 SPARC V8 Processor       ",
   ESA_LEON2APB     => "Leon2 Peripheral Bus           ",
   ESA_IRQ          => "Leon2 Interrupt Controller     ",
   ESA_TIMER        => "Leon2 Timer                    ",
   ESA_UART         => "Leon2 UART                     ",
   ESA_CFG          => "Leon2 Configuration Register   ",
   ESA_IO           => "Leon2 Input/Output             ",
   ESA_MCTRL        => "Leon2 Memory Controller        ",
   ESA_PCIARB       => "PCI Arbiter                    ",
   ESA_HURRICANE    => "HurriCANe/HurryAMBA CAN Ctrl   ",
   ESA_SPW_RMAP     => "UoD/Saab SpaceWire/RMAP link   ",
   ESA_AHBUART      => "Leon2 AHB Debug UART           ",
   ESA_SPWA         => "ESA/ASTRIUM SpaceWire link     ",
   ESA_BOSCHCAN     => "SSC/BOSCH CAN Ctrl             ",
   ESA_IRQ2         => "Leon2 Secondary Irq Controller ",
   ESA_AHBSTAT      => "Leon2 AHB Status Register      ",
   ESA_WPROT        => "Leon2 Write Protection Register",
   others           => "Unknown Device                 ");

   constant esa_lib : vendor_library_type := (
     vendorid       => VENDOR_ESA,
     vendordesc     => ESA_DESC,
     device_table   => esa_device_table
   );

  constant OPENCHIP_DESC : vendor_description := "OpenChip                ";

  constant openchip_device_table : device_table_type := (
    OPENCHIP_APBGPIO    => "APB General Purpose IO         ",
    OPENCHIP_APBI2C     => "APB I2C Interface              ",
    OPENCHIP_APBSPI     => "APB SPI Interface              ",
    OPENCHIP_APBCHARLCD => "APB Character LCD              ",
    OPENCHIP_APBPWM     => "APB PWM                        ",
    OPENCHIP_APBPS2     => "APB PS/2 Interface             ",
    OPENCHIP_APBMMCSD   => "APB MMC/SD Card Interface      ",
    OPENCHIP_APBNAND    => "APB NAND(SmartMedia) Interface ",
    OPENCHIP_APBLPC     => "APB LPC Interface              ",
    OPENCHIP_APBCF      => "APB CompactFlash (IDE)         ",
    OPENCHIP_APBSYSACE  => "APB SystemACE Interface        ",
    OPENCHIP_APB1WIRE   => "APB 1-Wire Interface           ",
    OPENCHIP_APBJTAG    => "APB JTAG TAP Master            ",
    OPENCHIP_APBSUI     => "APB Simple User Interface      ",

    others              => "Unknown Device                 ");

  constant openchip_lib : vendor_library_type := (
    vendorid 	        => VENDOR_OPENCHIP,
    vendordesc          => OPENCHIP_DESC,
    device_table        => openchip_device_table
  );

  constant GLEICHMANN_DESC : vendor_description := "Gleichmann Electronics  ";

  constant gleichmann_device_table : device_table_type := (
    GLEICHMANN_CUSTOM   => "Custom device                  ",
    GLEICHMANN_GEOLCD01 => "GEOLCD01 graphics system       ",
    GLEICHMANN_DAC      => "Sigma delta DAC                ",
    GLEICHMANN_HPI      => "AHB-to-HPI bridge              ",
    GLEICHMANN_SPI      => "SPI master                     ",
    GLEICHMANN_HIFC     => "Human interface controller     ",
    others              => "Unknown Device                 ");

  constant gleichmann_lib : vendor_library_type := (
    vendorid     => VENDOR_GLEICHMANN,
    vendordesc   => GLEICHMANN_DESC,
    device_table => gleichmann_device_table
    );

  constant CONTRIB_DESC : vendor_description := "Various contributions   ";

  constant contrib_device_table : device_table_type := (
   CONTRIB_CORE1    => "Contributed core 1             ",
   CONTRIB_CORE2    => "Contributed core 2             ",
   others           => "Unknown Device                 ");

   constant contrib_lib : vendor_library_type := (
     vendorid 	     => VENDOR_CONTRIB,
     vendordesc      => CONTRIB_DESC,
     device_table    => contrib_device_table
   );

  constant MENTA_DESC : vendor_description :=  "Menta                   ";

  constant menta_device_table : device_table_type := (
   others              => "Unknown Device                 ");

   constant menta_lib : vendor_library_type := (
     vendorid 	       => VENDOR_MENTA,
     vendordesc        => MENTA_DESC,
     device_table      => menta_device_table
   );

  constant SUN_DESC : vendor_description := "Sun Microsystems        ";

  constant sun_device_table : device_table_type := (
   SUN_T1           => "Niagara T1 SPARC V9 Processor  ",
   SUN_S1           => "Niagara S1 SPARC V9 Processor  ",
   others           => "Unknown Device                 ");

   constant sun_lib : vendor_library_type := (
     vendorid 	       => VENDOR_SUN,
     vendordesc        => SUN_DESC,
     device_table      => sun_device_table
   );

  constant OPENCORES_DESC : vendor_description :=  "OpenCores               ";

  constant opencores_device_table : device_table_type := (
   others              => "Unknown Device                 ");

   constant opencores_lib : vendor_library_type := (
     vendorid 	       => VENDOR_OPENCORES,
     vendordesc        => OPENCORES_DESC,
     device_table      => opencores_device_table
   );

  constant EMBEDDIT_DESC : vendor_description :=  "Embedd.it               ";

  constant embeddit_device_table : device_table_type := (
   others              => "Unknown Device                 ");

   constant embeddit_lib : vendor_library_type := (
     vendorid 	       => VENDOR_EMBEDDIT,
     vendordesc        => EMBEDDIT_DESC,
     device_table      => embeddit_device_table
   );

  constant eonic_device_table : device_table_type := (
   others              => "Unknown Device                 ");

  constant EONIC_DESC : vendor_description :=  "Eonic BV                ";

  constant eonic_lib : vendor_library_type := (
     vendorid 	       => VENDOR_EONIC,
     vendordesc        => EONIC_DESC,
     device_table      => eonic_device_table
   );

  constant radionor_device_table : device_table_type := (
   others              => "Unknown Device                 ");

  constant RADIONOR_DESC : vendor_description :=  "Radionor Communications ";

   constant radionor_lib : vendor_library_type := (
     vendorid 	       => VENDOR_RADIONOR,
     vendordesc        => RADIONOR_DESC,
     device_table      => radionor_device_table
   );

  constant UNKNOWN_DESC : vendor_description :=  "Unknown vendor          ";

  constant unknown_device_table : device_table_type := (
   others              => "Unknown Device                 ");

   constant unknown_lib : vendor_library_type := (
     vendorid 	       => 0,
     vendordesc        => UNKNOWN_DESC,
     device_table      => unknown_device_table
   );

  constant iptable : device_array := (
    VENDOR_GAISLER     => gaisler_lib,
    VENDOR_ESA         => esa_lib,
    VENDOR_OPENCHIP    => openchip_lib,
    VENDOR_OPENCORES   => opencores_lib,
    VENDOR_CONTRIB     => contrib_lib,
    VENDOR_EONIC       => eonic_lib,
    VENDOR_GLEICHMANN  => gleichmann_lib,
    VENDOR_MENTA       => menta_lib,
    VENDOR_EMBEDDIT    => embeddit_lib,
    VENDOR_SUN         => sun_lib,
    VENDOR_RADIONOR    => radionor_lib,
    others             => unknown_lib);

-- pragma translate_on

end;

