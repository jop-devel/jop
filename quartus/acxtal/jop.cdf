/* Quartus II Version 4.0 Build 190 1/28/2004 SJ Web Edition */
JedecChain;
	FileRevision(JESD32A);
	DefaultMfr(6E);

	P ActionCode(Ign)
		Device PartName(EPM7032AET44) MfrSpec(OpMask(0) FullPath("C:/usr/cpu/jop/quartus/acxconf/pld_init2.pof"));
	P ActionCode(Cfg)
		Device PartName(EP1K50T144) Path("C:/usr/cpu/jop/quartus/acxtal/") File("jop.sof") MfrSpec(OpMask(1));

ChainEnd;

AlteraBegin;
	ChainType(JTAG);
AlteraEnd;
