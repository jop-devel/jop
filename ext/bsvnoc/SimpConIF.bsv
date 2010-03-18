package SimpConIF;

// define some types
typedef Bit#(32) Word;
typedef Bit#(8) Byte;


// Could do this: 
// 	 1. say that an Ack frame is when dst == slot == id
//	 2. send CNT as the first word
// BUT: explicit control is easier to debug.
// plus EoD can be send for one word

// OBS: some timeout or reset process would be nice to have
//      but this is something for future versions

typedef enum {Nil, Data, Ack, EoD} PacketType deriving(Bits, Eq);

typedef struct {
	Bit#(addrbits) dst;	// destination address
	Bit#(addrbits) slot;
	PacketType ctrl;
	lType load;
} Packet#(type lType, type addrbits) deriving(Bits);

typedef Packet#(Word, n) WordPacket#(numeric type n);


interface SimpConIF#(numeric type addrbits);
	  (* prefix = "", always_enabled, always_ready *)
	  method Action rdwr_req(
	  	 Bit#(addrbits) address, Bool rd, // read req
	  	 Bit#(32) wr_data, Bool wr); // write req

	  (* always_enabled, always_ready *)
	  method Bit#(32) rd_data();

	  // not sure how to deal with rdy_cnt
	  (* always_enabled, always_ready *)
	  method Bit#(2) rdy_cnt();

	  // debug outputs
//	  (* always_enabled, always_ready *)
//	  method WordPacket#(addrbits) in_frame();
endinterface


// just a test module
// two write registers, r1 r0. reads get r1+r0@0 and r1-r0@1
module mkSimpleSlave(SimpConIF#(1));


Reg#(Bit#(32)) r0 <- mkRegU();
Reg#(Bit#(32)) r1 <- mkRegU();
Reg#(Bit#(32)) res <- mkRegU();
Reg#(Bool) isadd <- mkRegU();
Reg#(Bit#(2)) wr_cnt <- mkRegA(0); 
Reg#(Bit#(2)) rd_cnt <- mkRegA(0); 

rule do_wr(wr_cnt > 0);
     wr_cnt <= wr_cnt - 1;
endrule

rule do_rd2(rd_cnt == 1);
     if(isadd) res <= r0 + r1;
     else res <= r1 - r0;
     rd_cnt <= 0;
endrule

method Action rdwr_req(Bit#(1) address, Bool rd, 
       Bit#(32) wr_data, Bool wr);
       if(wr) action
        if(address == 0) action r0 <= wr_data; endaction
        else action r1 <= wr_data; endaction
        wr_cnt <= 3;	// for rdy_cnt
       endaction
       if(rd)
       action
        isadd <= (address == 0);
        rd_cnt <= 1;
       endaction
endmethod

method Bit#(32) rd_data();
       return res;
endmethod

method Bit#(2) rdy_cnt();
       return rd_cnt | wr_cnt;
endmethod

endmodule

endpackage
