/* Quartus II Version 2.2 Build 176 02/04/2003 Service Pack 1 SJ Web Edition */
JedecChain;
	FileRevision(JESD32A);
	DefaultMfr(6E);

	P ActionCode(Ign)
		Device PartName(EPM7064AET44) MfrSpec(OpMask(0) FullPath("C:\usr\cpu\jop\wrk\cyc_conf_init.pof"));
	P ActionCode(Cfg)
		Device PartName(EP1C6Q240) Path("C:\usr\cpu\jop\wrk\") File("jopcyc.sof") MfrSpec(OpMask(1));

ChainEnd;

AlteraBegin;
	ChainType(JTAG);
AlteraEnd;
