/* Quartus II Version 2.2 Build 176 02/04/2003 Service Pack 1 SJ Web Edition */
JedecChain;
	FileRevision(JESD32A);
	DefaultMfr(6E);

	P ActionCode(Ign)
		Device PartName(EPM7032AET44) MfrSpec(OpMask(0));
	P ActionCode(Cfg)
		Device PartName(EP1K50T144) Path("C:\usr\cpu\jop\wrk\") File("jopes.sof") MfrSpec(OpMask(1));

ChainEnd;

AlteraBegin;
	ChainType(JTAG);
AlteraEnd;
