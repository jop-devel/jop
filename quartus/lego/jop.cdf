/* Quartus II Version 7.1 Build 156 04/30/2007 SJ Web Edition */
JedecChain;
	FileRevision(JESD32A);
	DefaultMfr(6E);

	P ActionCode(Ign)
		Device PartName(EPM7064AET44) MfrSpec(OpMask(0) FullPath("../cycconf/cyc_conf_init.pof"));
	P ActionCode(Cfg)
		Device PartName(EP1C12Q240) Path("./") File("jop.sof") MfrSpec(OpMask(1));

ChainEnd;

AlteraBegin;
	ChainType(JTAG);
AlteraEnd;
