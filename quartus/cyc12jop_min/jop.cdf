/* Quartus II Version 4.0 Build 190 1/28/2004 SJ Web Edition */
JedecChain;
	FileRevision(JESD32A);
	DefaultMfr(6E);

	P ActionCode(Ign)
		Device PartName(EPM7064AET44) MfrSpec(OpMask(0) FullPath("C:/usr/cpu/jop/quartus/cycconf/cyc_conf_init.pof"));
	P ActionCode(Cfg)
		Device PartName(EP1C12Q240) Path("C:/usr/cpu/jop/quartus/cyc12jop_min/") File("jop.sof") MfrSpec(OpMask(1));

ChainEnd;

AlteraBegin;
	ChainType(JTAG);
AlteraEnd;
