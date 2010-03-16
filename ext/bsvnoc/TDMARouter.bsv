package TDMARouter;

import FIFOF::*;
import StmtFSM::*;
import SimpConIF::*;
import Vector::*;
import ConfigReg::*;

interface SndRcvIF#(numeric type n);
	  // send
	  method Action sndDestination(Bit#(n) dst);
	  // this actually starts the sending process
	  method Action sndCount(Byte counter);
	  method Action sndData(Word w);

	  // receive
	  method Bit#(n) rcvSource();
	  method Byte	 rcvCount();
	  method ActionValue#(Word) rcvData();
	  method Action  rcvReset(); // resets EoD

	  // some status info
	  method Bool isSending();
	  method Bool isReceiving();
	  method Bool isBusy();
	  method Bool isEoD();
	  method Bool rcvBufferFull();
	  method Bool rcvBufferEmpty();
	  method Bool sndBufferFull();
	  method Bool sndBufferEmpty();
	  method Bit#(n) thisAddress();  	  
endinterface

// (* synthesize *)
module mkTDMANode#(Bit#(n) id,
       	           Integer bufferSize,
       	           Reg#(WordPacket#(n)) inR,
       	           Reg#(WordPacket#(n)) outR)
		   (SndRcvIF#(n) sndrcv);

FIFOF#(Word) sndFIFO <- mkSizedFIFOF(bufferSize);
FIFOF#(Word) rcvFIFO <- mkSizedFIFOF(bufferSize);

Reg#(Bit#(n)) src <- mkRegA(id);
Reg#(Bit#(n)) dst <- mkRegA(0);
Reg#(Byte) sndCnt <- mkRegA(0);
Reg#(Byte) rcvCnt <- mkRegA(0);
 

Reg#(Bool) firstWord <- mkReg(False);
Reg#(Bool) sending <- mkReg(False);
Reg#(Bool) rcvMore <- mkReg(False);
Reg#(Bool) flagEoD <- mkReg(False);


(* preempts = "(detectSndStart,waitLastAck, defaultNil,detectRcvStart,continueRcv), justForward" *)

// ---------- sending rules ------------------------------
rule detectSndStart(
     inR.slot == id	    // this is my slot
     && sending
     && (firstWord || (!firstWord && inR.ctrl == Ack)) 
     && sndCnt > 0);
     $display("Sending word %h",sndCnt);

     let cntrl = (sndCnt == 1)? EoD:Data;

     // put a word from send fifo to output
     outR <= WordPacket {dst:dst, slot:inR.slot, load:sndFIFO.first,ctrl:cntrl};
     sndFIFO.deq;
     
     // send the first word
     firstWord <= False;

     if(!firstWord && inR.ctrl == Ack) 
     		   $display("Ack for %h.",sndCnt+1);

     // decrease the sent count
     sndCnt <= sndCnt - 1;       
endrule

rule waitLastAck(
		inR.slot == id
		&& sndCnt == 0
		&& sending
		&& !firstWord
		&& inR.ctrl == Ack);
        $display("Last Ack received.");
	sending <= False;
	// fills the slot with Nil, once the last ack is found
	outR <= WordPacket {dst:0, slot:inR.slot, load:0, ctrl:Nil};	
endrule

// default slot rule when not sending
rule defaultNil(inR.slot == id && !sending);
       outR <= WordPacket {dst:0, slot:inR.slot, load:0, ctrl:Nil};
endrule

(* fire_when_enabled *)
// -------- receiving rules -------------------------
rule detectRcvStart(
	inR.slot != id	    // do not receive from my own slot
     	&& inR.dst == id    // the packet destination is this node
	&& rcvCnt == 0      // buffer is empty
	&& !flagEoD  	    // also is allowed to receive the next message
	&& (inR.ctrl == Data || inR.ctrl == EoD) // is a data package
	&& !rcvMore);	    // is not already receiving
     $display("Receive Starts. Source is %h",inR.slot);
     $display("receiving %h (word %h) from %h", inR.load, 1, inR.slot);
     // follow these slots now
     src <= inR.slot;
     rcvFIFO.enq(inR.load);
     rcvCnt <= 1;
     if(inR.ctrl == Data)
     		 rcvMore <= True;
     else	 flagEoD <= True;
     // send Ack as well
     outR <= WordPacket {dst:inR.dst, slot:inR.slot, load:inR.load, ctrl:Ack};
endrule

(* fire_when_enabled *)
rule continueRcv(
        inR.slot != id	  // do not receive from my own slot
	&& inR.slot == src    // we are following this source
	&& (inR.ctrl == Data || inR.ctrl == EoD) // must be a data packet 
	&& rcvMore);      // we need to receive more 
     $display("receiving %h (word %h) from %h", inR.load, rcvCnt + 1, inR.slot);
     if(inR.ctrl == EoD) action
     		 rcvMore <= False;
		 flagEoD <= True;
		 $display("EoD received.");
		 endaction
     rcvFIFO.enq(inR.load);
     rcvCnt <= rcvCnt + 1;
     // send Ack as well
     outR <= WordPacket {dst:inR.dst, slot:inR.slot, load:inR.load, ctrl:Ack};
endrule
// --------------------------------------------------

// ---------- default slot rule, if anything else fails

rule justForward;
     outR <= inR;
endrule


rule advanceSlot;
//  The slot is now spinning around with the messages!
//   slot <= slot - 1;
   $display("Current slot is %h", inR.slot);
endrule

// ----------- access points for Send ----------------------
method Action sndDestination(Bit#(n) sendTo); // if(!sending);
       dst <= sendTo;
       $display("destination set to %h",sendTo);
endmethod

method Action sndCount(Byte count); // if(!sending);
       firstWord <= True;
       sending <= True;
       sndCnt <= count;
       sndFIFO.clear;
       $display("request sending %h words",count);
endmethod

method Action sndData(Word w); // if(sending);
       sndFIFO.enq(w);
       $display("data %h placed in the send FIFO",w);
endmethod

// ---------- access points for Receive --------------------
method Bit#(n) rcvSource();
       return src;
endmethod

method Byte rcvCount();
       return rcvCnt;
endmethod

method ActionValue#(Word) rcvData();
// Not anymore! EoD is software cleared, to allow receiving new
// messages at will instead of automatically 
//       if(rcvCnt == 1) flagEoD <= False;
       rcvCnt <= rcvCnt - 1;
       rcvFIFO.deq;
       return rcvFIFO.first;
endmethod

// use to signal that the message was received
// and allow new messages to come in
method Action rcvReset();
       flagEoD <= False;
// let's clear all
       rcvCnt <= 0;
       rcvFIFO.clear;
       rcvMore <= False;
endmethod

// --------- status info -------------------------------
method Bool isSending(); return sending; endmethod
method Bool isReceiving(); return rcvMore; endmethod
method Bool isBusy(); return sending || rcvMore ; endmethod
method Bool isEoD(); return flagEoD; endmethod

method Bool rcvBufferFull(); return !rcvFIFO.notFull; endmethod
method Bool rcvBufferEmpty(); return !rcvFIFO.notEmpty; endmethod
method Bool sndBufferFull(); return !sndFIFO.notFull; endmethod
method Bool sndBufferEmpty(); return !sndFIFO.notEmpty; endmethod 
method Bit#(n) thisAddress(); return id; endmethod 	  
// ------------------------------------------------------

endmodule


// ---------------- TDMA Router with SimpCon interface
typedef enum {StatusReg, RcvCntReg, RcvSourceReg, RcvDataReg} 
	TDMA_RdReg deriving (Bits,Eq);
// Read:
//   StatusReg: 
//              Low  8 bits  
//              |rcvFull|rcvEmpty|sndFull|sndEmpty|_|isRcv|isSnd|Busy|
// Write:
typedef enum {ResetReg, SndCntReg, SndDestReg, SndDataReg} 
	TDMA_WrReg deriving (Bits,Eq);
///////////////////////////////////////////////////////////////

module mkTDMANodeSCIF#(Bit#(n) id,
       	           Integer bufferSize,
       	           Reg#(WordPacket#(n)) inR,
       	           Reg#(WordPacket#(n)) outR)
		   (SimpConIF#(2))
 	provisos(Add#(a,n,8), Add#(b,n,32));

SndRcvIF#(n) node <- mkTDMANode(id,bufferSize,inR,outR);
// Reg#(Bit#(2)) cnt <- mkRegA(0);
Reg#(Word) res <- mkRegU();
Reg#(Word) ind <- mkRegU();

Reg#(Bool) rnw <- mkRegA(True);
Reg#(Bool) req <- mkRegA(False);
Reg#(TDMA_RdReg) rdreq <- mkRegU();
Reg#(TDMA_WrReg) wrreq <- mkRegU();

rule waitRcvData(req && rnw && rdreq == RcvDataReg);
     let aux <- node.rcvData();
     res <= aux;
//     cnt <= 0;
     req <= False;
endrule

rule do_readStatus(req && rnw && rdreq == StatusReg);
     Bit#(8) statusByte = {
              pack(node.rcvBufferFull),
       	      pack(node.rcvBufferEmpty),
       	      pack(node.sndBufferFull),
       	      pack(node.sndBufferEmpty),
       	      pack(node.isEoD),
       	      pack(node.isReceiving),
       	      pack(node.isSending),
       	      pack(node.isBusy)};
      
      Bit#(8) myAddress = zeroExtend(node.thisAddress);

      Bit#(32) statusWord = zeroExtend({statusByte, myAddress});

      res <= statusWord;
//      cnt <= 0;
      req <= False;
endrule

rule do_readRcvSource(req && rnw && rdreq == RcvSourceReg);
     res <= zeroExtend(node.rcvSource());
 //    cnt <= 0;
     req <= False;
endrule

rule do_readRcvCount(req && rnw && rdreq == RcvCntReg);
     res <= zeroExtend(node.rcvCount());
//     cnt <= 0;
     req <= False;
endrule

rule do_writeReset(req && !rnw && wrreq == ResetReg);
     node.rcvReset();
 //    cnt <= 0;
     req <= False;
     $display("EoD reset.");
endrule

rule do_writeSndCount(req && !rnw && wrreq == SndCntReg);
     node.sndCount(truncate(ind));    
//     cnt <= 0;
     req <= False;
endrule

rule do_writeSndDest(req && !rnw && wrreq == SndDestReg);
     node.sndDestination(truncate(ind));    
//     cnt <= 0;
     req <= False;
endrule

rule do_writeSndData(req && !rnw && wrreq == SndDataReg);
     node.sndData(ind);    
//     cnt <= 0;
     req <= False;
endrule

method Action rdwr_req(Bit#(2) address, Bool rd, 
       Bit#(32) wr_data, Bool wr);

       // a read request
       if(rd) begin
        TDMA_RdReg adr = unpack(address);
	rdreq <= adr;
	rnw <= True;
	req <= True;
       end 
       else
       // now the write part
       if(wr) begin
	TDMA_WrReg adr = unpack(address);
	wrreq <= adr;
	ind <= wr_data;
	rnw <= False;
	req <= True;
       end
    
endmethod


method Bit#(32) rd_data();
       return res;
endmethod

method Bit#(2) rdy_cnt();
       return req?3:0;
endmethod



endmodule

/*
module mkTestNodeSCIF(Empty);

Bit#(2) nid1 = 1;
Bit#(2) nid2 = 2;

Reg#(Bit#(2)) slotgen <- mkRegA(0);
Reg#(WordPacket#(2)) iFIFO <- mkConfigRegA(WordPacket {dst:_, slot:0, load:_, ctrl:Ack});
Reg#(WordPacket#(2)) iFIFO2 <- mkConfigRegU;
Reg#(WordPacket#(2)) oFIFO <- mkConfigRegU;

SimpConIF#(2) node <- mkTDMANodeSCIF(nid1, 2, iFIFO, iFIFO2);
SimpConIF#(2) node2 <- mkTDMANodeSCIF(nid2, 2, iFIFO2, oFIFO);

Stmt s = (seq
       $display("Read reg encoding StatusReg=%d, RcvCntReg=%d, RcvSourceReg=%d, RcvDataReg=%d",
	   StatusReg, RcvCntReg, RcvSourceReg, RcvDataReg);
       $display("Write reg encoding ResetReg=%d, SndCntReg=%d, SndDestReg=%d, SndDataReg=%d",
	   ResetReg, SndCntReg, SndDestReg, SndDataReg);
       node.rdwr_req(2'b00, True, _, False);
       await(node.rdy_cnt == 0);
       $display("Status bits %b",node.rd_data[15:0]);
       node.rdwr_req(2'b10, False, 2, True); // dest 2
       await(node.rdy_cnt == 0);
       node.rdwr_req(2'b01, False, 3, True); // count 3
       await(node.rdy_cnt == 0);
       node.rdwr_req(2'b11, False, 12, True); // send 12
       await(node.rdy_cnt == 0);
       node.rdwr_req(2'b11, False, 13, True); // send 13
       await(node.rdy_cnt == 0);
       node.rdwr_req(2'b11, False, 14, True); // send 14
       await(node.rdy_cnt == 0);
       noAction;
       noAction;
       noAction;
       noAction;
       noAction;
       noAction;
//       node.sndDestination(2);
//       node.sndCount(1);
//       node.sndData(5);
       noAction;
       noAction;
//       await(!node.isBusy);
//       node.sndDestination(3);
//       node.sndCount(3);
//       node.sndData(4);
//       node.sndData(3);
//       node.sndData(2);
       noAction;
       noAction;
       noAction;
       noAction;
       noAction;
//       await(!node.isBusy);
       iFIFO <=  WordPacket {dst:1, slot:0, load: 13, ctrl:EoD};
	$display("sent EoD");       
       noAction;
       noAction;
       iFIFO <= WordPacket {dst:_, slot:0, load: _, ctrl:Nil};
       node.rdwr_req(2'b00, False, _, True); // reset EoD
       await(node.rdy_cnt == 0);
       noAction;
       action
	iFIFO <=  WordPacket {dst:1, slot:0, load: 15, ctrl:EoD};
	$display("sent EoD");
       endaction
       noAction;
       noAction;
       noAction;
       iFIFO <= WordPacket {dst:_, slot:0, load: _, ctrl:Nil};
       node.rdwr_req(2'b00, True, _, False);
       await(node.rdy_cnt == 0);
       $display("Status bits %b",node.rd_data[15:0]);
       node.rdwr_req(2'b11, True, _, False);
       await(node.rdy_cnt == 0);
       node.rdwr_req(2'b00, True, _, False);
       await(node.rdy_cnt == 0);
       $display("Status bits %b",node.rd_data[15:0]);
       node.rdwr_req(2'b11, True, _, False);
       await(node.rdy_cnt == 0);
       node.rdwr_req(2'b00, True, _, False);
       await(node.rdy_cnt == 0);
       $display("Status bits %b",node.rd_data[15:0]);
//      await(!node.isBusy);
//      $display("rcv count %h",node.rcvCount());
       while(True) noAction;     
       endseq);

mkAutoFSM(s);


rule rcvInstantly;
     iFIFO <= WordPacket {dst:_, slot:iFIFO.slot+1, load:_, ctrl:Nil};
     $display("rdy_cnt = %d, rd_data = %h",node.rdy_cnt,node.rd_data);
     $display("iFIFO has %h, iFIFO2 has %h, oFIFO has %h",iFIFO, iFIFO2, oFIFO);
endrule


endmodule
*/

interface NoC#(numeric type n);
	  interface Vector#(n,SimpConIF#(2)) routers;
endinterface


// Making the NoC with SimpCon interfaces.
module mkRingNoC_SCIF#(Integer n, Integer buffsize)
       (Vector#(n,SimpConIF#(2)) routers)
       provisos(Log#(n,ln),Add#(a,ln,8),Add#(b,ln,32));

//let ln = 2; fromInteger(log2(n));

Vector#(n,Reg#(WordPacket#(ln))) coms; 
Vector#(n,SimpConIF#(2)) routers;

// create shift registers
for(Integer i=0;i<n;i=i+1)
begin
 // very important that the slots are different!
 coms[i]<- mkConfigRegA(WordPacket {dst:_, 
 	   			    slot:fromInteger(i),  
				    load:_, ctrl:Nil});
end

// now create therouters
// and associated nodes
for(Integer i=0;i<n;i=i+1)
begin
  routers[i] <- mkTDMANodeSCIF(fromInteger(i),
  	     	buffsize, coms[i],coms[mod(i+1,n)]);
end

return routers;

endmodule


// 3 nodes, size 2 i/o buffers
module mkNoC3bs2(NoC#(3));
      Vector#(3,SimpConIF#(2))  noc <- mkRingNoC_SCIF(3,2);
      interface routers = noc;
endmodule

endpackage
