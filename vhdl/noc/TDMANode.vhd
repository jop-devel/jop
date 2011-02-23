--
--
--  This file is a part of an evaluation of CSP with a JOP CMP
--
--  Copyright (C) 2010, Flavius Gruian (Flavius.Gruian@cs.lth.se)
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;


use work.NoCTypes.ALL;

entity TDMANode is
	 Generic (
			  myAddr : NoCAddr := (others => '0');
			  BufferSize: integer := 4;
			  BufferAddrBits: integer := 2
	 );
	Port (  Clk : in  STD_LOGIC;
           Rst : in  STD_LOGIC;
			  -- SimpCon Signals
           Addr : in  STD_LOGIC_VECTOR (1 downto 0);
           wr : in  STD_LOGIC;
           wr_data : in  STD_LOGIC_VECTOR (31 downto 0);
           rd : in  STD_LOGIC;
           rd_data : out  STD_LOGIC_VECTOR (31 downto 0);
           rdy_cnt : out  STD_LOGIC_VECTOR (1 downto 0);
			  
			  -- NoC IO
           nocIn : in  NoCPacket;
           nocOut : out  NoCPacket
	);
end TDMANode;

architecture Behavioral of TDMANode is

component SimpConSlaveIF is
	 Generic (
			  myAddr : NoCAddr := (others => '0')
	 );
    Port ( Clk : in  STD_LOGIC;
           Rst : in  STD_LOGIC;
			  -- SimpCon Signals
           Addr : in  STD_LOGIC_VECTOR (1 downto 0);
           wr : in  STD_LOGIC;
           wr_data : in  STD_LOGIC_VECTOR (31 downto 0);
           rd : in  STD_LOGIC;
           rd_data : out  STD_LOGIC_VECTOR (31 downto 0);
           rdy_cnt : out  STD_LOGIC_VECTOR (1 downto 0);
			  -- towards the rest of the system
			  
			  -- from the Receiver part, and rcvFIFO
			  isRcv : in STD_LOGIC;
			  isRcvBufferFull : in STD_LOGIC; 
			  isRcvBufferEmpty : in STD_LOGIC;
			  isEoD : in STD_LOGIC;
			  rcvSrc : in STD_LOGIC_VECTOR (NOCADDRBITS-1 downto 0);
           deq : out STD_LOGIC;
           rcvFirst : in STD_LOGIC_VECTOR (31 downto 0);
			  resetRcv : out STD_LOGIC;
			  
			  isSndBufferFull : in STD_LOGIC; 
			  isSndBufferEmpty : in STD_LOGIC;
			  isSnd : in STD_LOGIC;
			  setSndCount : out STD_LOGIC;
			  sndCount : out STD_LOGIC_VECTOR (31 downto 0);
			  setSndDst : out STD_LOGIC;
			  sndDst : out STD_LOGIC_VECTOR (NOCADDRBITS-1 downto 0);
			  enq : out STD_LOGIC;
			  sndData : out STD_LOGIC_VECTOR (31 downto 0)
			);
end component;


component SizedFIFOF is
	 Generic (
		SIZE: integer := 4;
		ADDRBITS: integer := 2
	 );
    Port ( Clk : in  STD_LOGIC;
			  Rst : in STD_LOGIC;
			  DataIn : in  STD_LOGIC_VECTOR (31 downto 0);
           Enq : in  STD_LOGIC;
           First : out  STD_LOGIC_VECTOR (31 downto 0);
           Deq : in  STD_LOGIC;
           Full : out  STD_LOGIC;
           Empty : out  STD_LOGIC);

end component;

component Sender is
    Port ( Clk : in  STD_LOGIC;
           Rst : in  STD_LOGIC;
			  
			  -- my NoC id
			  slotID : in NoCAddr;
           nocIn : in  NoCPacket;
           nocOut : out  NoCPacket;
			  
			  -- interface with rcv FIFO
           sndBufferEmpty : in  BOOLEAN;
           deq : out BOOLEAN;
           sndData : in STD_LOGIC_VECTOR (31 downto 0);

			  -- destination address of the message
			  sndDst : in NoCAddr;
			  setSndDst : in BOOLEAN;
			  -- how many words to send
			  sndCnt : in STD_LOGIC_VECTOR (31 downto 0);
			  -- also causes start sending!
			  setSndCnt : in BOOLEAN;
			  --
			  isSending : out BOOLEAN
			  );
end component;


component Receiver is
    Port ( Clk : in  STD_LOGIC;
           Rst : in  STD_LOGIC;
			  
			  -- my NoC id
			  slotID : in NoCAddr;
           nocIn : in  NoCPacket;
           nocOut : out  NoCPacket;
			  
			  -- interface with rcv FIFO
           rcvBufferFull : in  BOOLEAN;
           enq : out BOOLEAN;
           rcvData : out  STD_LOGIC_VECTOR (31 downto 0);

			  isRcv : out BOOLEAN;
			  
			  -- end of data seen
			  EoD : out BOOLEAN;
			  -- source address of the message
			  rcvSrc : out NoCAddr;
			  -- reset reception
			  ackEoD : in BOOLEAN
			  );
end component;


signal rcvFull,rcvEmpty, sndFull, sndEmpty: STD_LOGIC;
signal rcvDeq, rcvEnq, sndDeq, sndEnq: STD_LOGIC;
signal isSnd,setSndDst,setSndCnt,isRcv,isEoD,resetRcv : STD_LOGIC;

signal bsndEmpty,bsndDeq,bsetSndDst,bsetSndCnt,bisSnd,brcvFull,brcvEnq : BOOLEAN;
signal bisRcv,bisEoD,bresetRcv : BOOLEAN;

signal sndData,sndFirst,rcvData,rcvFirst: STD_LOGIC_VECTOR (31 downto 0);
signal sndCnt: STD_LOGIC_VECTOR (31 downto 0);
signal sndDst,rcvSrc: NoCAddr;
signal nocOutRCV, nocOutSND, inocOut : NoCPacket;

begin

bsndEmpty <= sndEmpty = '1';
sndDeq <= tob(bsndDeq);
bsetSndDst <= setSndDst = '1';
bsetSndCnt <= setSndCnt = '1';
isSnd <= tob(bisSnd);
isRcv <= tob(bisRcv);
isEoD <= tob(bisEoD);
brcvFull <= rcvFull = '1';
rcvEnq <= tob(brcvEnq);
bresetRcv <= resetRcv = '1';


process (Clk, Rst)
begin
	if rising_edge(Clk) then
	   if (Rst = '1') then
 			inocOut <= (Src => myAddr, Dst => myAddr, pType => PTNil, Load => (others =>'0'));		
		else
			if(nocIn.src = myAddr) then
				inocOut <= nocOutSND;	
			else  -- can only emit in my slot
				inocOut <= nocOutRCV; -- i can receive in all others
			end if;
		end if;
   end if;	
end process;

nocOut <= inocOut;

busIF: SimpConSlaveIF
generic map (
	myAddr => myAddr
)

port map (
	Clk => Clk,
	Rst => Rst,
	Addr => Addr,
	wr => wr,
	wr_data => wr_data,
	rd => rd,
	rd_data => rd_data,
	rdy_cnt => rdy_cnt,
	
	isRcv => isRcv,
	isRcvBufferFull => rcvFull,
	isRcvBufferEmpty => rcvEmpty,
	isEoD => isEoD,
	rcvSrc => rcvSrc,
   deq => rcvDeq,
   rcvFirst => rcvFirst,
	resetRcv => resetRcv,
			  
	isSndBufferFull => sndFull, 
	isSndBufferEmpty => sndEmpty,
	isSnd => isSnd,
   setSndCount => setSndCnt,
   sndCount => sndCnt,
	setSndDst => setSndDst,
	sndDst => sndDst,
	enq => sndEnq,
	sndData => sndData
);

sndFIFO: SizedFIFOF
generic map (
	SIZE => BufferSize,
	ADDRBITS => BufferAddrBits
)
port map (
	Clk => Clk,
	Rst => Rst,
	DataIn => sndData, 
   Enq => sndEnq,
   First => sndFirst,
   Deq => sndDeq,
	Full => sndFull,
	Empty => sndEmpty
);

rcvFIFO: SizedFIFOF
generic map (
	SIZE => BufferSize,
	ADDRBITS => BufferAddrBits
)
port map (
	Clk => Clk,
	Rst => Rst,
	DataIn => rcvData, 
   Enq => rcvEnq,
   First => rcvFirst,
   Deq => rcvDeq,	
	Full => rcvFull,
	Empty => rcvEmpty
);


sndSide : Sender 
port map ( Clk => Clk,
           Rst => Rst,
			  slotID => myAddr,
           nocIn => nocIn,
           nocOut => nocOutSND,
			  
			  -- interface with rcv FIFO
           sndBufferEmpty => bsndEmpty,
           deq => bsndDeq,
           sndData => sndFirst,

			  -- destination address of the message
			  sndDst => sndDst,
			  setSndDst => bsetSndDst,
			  -- how many words to send
			  sndCnt => sndCnt,
			  -- also causes start sending!
			  setSndCnt => bsetSndCnt,
			  --
			  isSending => bisSnd
);


rcvSide: Receiver
port map (
			  Clk => Clk,
           Rst => Rst,
			  slotID => myAddr,
           nocIn => nocIn,
           nocOut => nocOutRCV,
			  -- interface with rcv FIFO
           rcvBufferFull => brcvFull, 
           enq => brcvEnq, 
           rcvData => rcvData,
			  
			  isRcv => bisRcv,
			  -- end of data seen
			  EoD => bisEoD, 
			  -- source address of the message
			  rcvSrc => rcvSrc,
			  -- reset reception
			  ackEoD => bresetRcv
);

end Behavioral;

