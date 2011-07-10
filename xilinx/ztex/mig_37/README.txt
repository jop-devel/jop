*** Steps to create the Spartan-6 DDR memory interface for the ZTEX USB-FPGA module.
These instructions are based on using Xilinx ISE 13.1 (or 12.4)

Use Coregen/MIG 3.7 (or 3.61 for ISE 12.4) to create the controller. 
- Component Name: mig_37
- Bank 3 Memory Type DDR SDRAM
- Frequency: 200MHz
- Memory Part: MT46V32M16XX-5B-IT
- Configuration Selection: One 32-bit bi-directional port
- Memory Address Mapping Selection: Row, Bank, Column


Once the controller is generated copy all the vhdl files from the user_design/rtl directory to this directory
and apply patch:

patch < memc3_infrastructure.patch
