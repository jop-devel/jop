/* Quartus II Version 3.0 Build 199 06/26/2003 SJ Web Edition */
JedecChain;
	FileRevision(JESD32A);
	DefaultMfr(6E);

	P ActionCode(Ign)
		Device PartName(EPM7032AET44) MfrSpec(OpMask(0) FullPath("C:\usr\cpu\jop\mwrk\pld_init2.pof"));
	P ActionCode(Cfg)
		Device PartName(EP1K50T144) Path("C:\usr\cpu\jop\ossi\") File("jop.sof") MfrSpec(OpMask(1));

ChainEnd;

AlteraBegin;
	ChainType(JTAG);
AlteraEnd;
