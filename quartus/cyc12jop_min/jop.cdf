/* Quartus II Version 4.1 Build 208 09/10/2004 Service Pack 2 SJ Web Edition */
JedecChain;
	FileRevision(JESD32A);
	DefaultMfr(6E);

	P ActionCode(Cfg)
		Device PartName(EPM7064AET44) Path("C:/usr/cpu/jop/quartus/cycconf/") File("cyc_conf_init.pof") MfrSpec(OpMask(1));
	P ActionCode(Ign)
		Device PartName(EP1C12Q240) MfrSpec(OpMask(0) FullPath("C:/usr/cpu/jop/quartus/cyc12jop_min/jop.sof"));

ChainEnd;

AlteraBegin;
	ChainType(JTAG);
AlteraEnd;
