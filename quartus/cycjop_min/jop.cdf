/* Quartus II Version 4.1 Build 181 06/29/2004 SJ Web Edition */
JedecChain;
	FileRevision(JESD32A);
	DefaultMfr(6E);

	P ActionCode(Ign)
		Device PartName(EPM7064AET44) MfrSpec(OpMask(0) FullPath("C:/usr/cpu/jop/quartus/cycconf/cyc_conf_init.pof"));
	P ActionCode(Cfg)
		Device PartName(EP1C6Q240) Path("C:/usr/cpu/jop/quartus/cycjop_min/") File("jop.sof") MfrSpec(OpMask(1));

ChainEnd;

AlteraBegin;
	ChainType(JTAG);
AlteraEnd;
