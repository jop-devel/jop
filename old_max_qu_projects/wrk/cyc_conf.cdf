/* Quartus II Version 2.2 Build 176 02/04/2003 Service Pack 1 SJ Web Edition */
JedecChain;
	FileRevision(JESD32A);
	DefaultMfr(6E);

	P ActionCode(Cfg)
		Device PartName(EPM7064AET44) Path("C:\usr\cpu\jop\wrk\") File("cyc_conf.pof") MfrSpec(OpMask(1));
	P ActionCode(Ign)
		Device PartName(EP1C6) MfrSpec(OpMask(0));

ChainEnd;

AlteraBegin;
	ChainType(JTAG);
AlteraEnd;
