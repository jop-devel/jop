/* Quartus II Version 3.0 Build 199 06/26/2003 SJ Web Edition */
JedecChain;
	FileRevision(JESD32A);
	DefaultMfr(6E);

	P ActionCode(Cfg)
		Device PartName(EPM7064AET44) Path("C:\usr\cpu\jop\quartus\cycconf\") File("cyc_conf_init.pof") MfrSpec(OpMask(1));
	P ActionCode(Ign)
		Device PartName(EP1C6) MfrSpec(OpMask(0));

ChainEnd;

AlteraBegin;
	ChainType(JTAG);
AlteraEnd;
