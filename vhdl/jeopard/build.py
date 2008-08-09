#
#   This file is part of JOP, the Java Optimized Processor
#     see <http://www.jopdesign.com/>
# 
#   Copyright (C) 2008, Jack Whitham
# 
#   This program is free software: you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
# 
#   This program is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
# 
#   You should have received a copy of the GNU General Public License
#   along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 

import os
ADDRESS_SIZE = 24

# NOTE: error types that are not handled properly
# - starting a method while it's already running

#
# This module generates:
#  1. A hardware interface wrapper.
#     This goes between the co-processor control channel and memory,
#     and the co-processor. Usually it will be part of the co-processor
#     VHDL. See mac_coprocessor.vhd for an example.
#     
#  2. A software interface wrapper.
#     This goes between Java code and the co-processor control channel.
#
# The input for this module is an interface definition language
# that looks like this:
# 
# COPROCESSOR $name
# METHOD $name
# PARAMETER $name $type 
# PARAMETER $name $type 
# PARAMETER $name $type 
# RETURN $type
# 
# METHOD $name
# PARAMETER $name $type 
# PARAMETER $name $type 
# PARAMETER $name $type 
# RETURN $type
# 
# etc 


class InterfaceDefinitionException(Exception):
    pass

class JavaType:
    name = "?"
    def getName(self):
        return self.name

    def getVectorSize(self):
        assert False, "extend this method"

    def convertForHardware(self, java_name, hardware_name):
        assert False, "extend this method"

    def convertForJava(self, hardware_name, java_name):
        assert False, "extend this method"

TYPE_LOOKUP = dict()
def RegisterType(java_type):
    assert isinstance(java_type, JavaType)
    TYPE_LOOKUP[ java_type.name ] = java_type

class VoidType(JavaType):
    name = "void"
    def getVectorSize(self):
        return 1

    def getName(self):
        return "int"

    def convertForHardware(self, java_name, hardware_name):
        return "%s = 1;\n" % hardware_name

    def convertForJava(self, hardware_name, java_name):
        return ""

RegisterType(VoidType())

class IntType(JavaType):
    name = "int"
    def getVectorSize(self):
        return 32

    def convertForHardware(self, java_name, hardware_name):
        return "%s = %s;\n" % (hardware_name, java_name)

    def convertForJava(self, hardware_name, java_name):
        return "%s = %s;\n" % (java_name, hardware_name)

RegisterType(IntType())

class IntArrayType(JavaType):
    name = "int[]"
    def getVectorSize(self):
        return ADDRESS_SIZE

    def convertForHardware(self, java_name, hardware_name):
        return "%s = _dereference ( %s ) ;\n" % (hardware_name, java_name)

    def convertForJava(self, hardware_name, java_name):
        assert False, "IMPOSSIBLE"

RegisterType(IntArrayType())

class AnyType(JavaType):
    def __init__(self, name):
        self.name = name
        self.functions = TYPE_LOOKUP.get(name, None)
        if ( self.functions == None ):
            raise InterfaceDefinitionException("Unknown type '%s'." % name)

    def getVectorSize(self):
        return self.functions.getVectorSize()

    def convertForHardware(self, java_name, hardware_name):
        return self.functions.convertForHardware(java_name, hardware_name)

    def convertForJava(self, hardware_name, java_name):
        return self.functions.convertForJava(hardware_name, java_name)

class Method:
    def __init__(self, name):
        self.name = name
        self.param_list = []
        self.ret_type = VoidType()
        self.method_id = None
        self.coproc_id = None

    def addParameter(self, name, java_type):
        assert isinstance(java_type, JavaType)
        self.param_list.append((name, java_type))

    def addReturn(self, java_type):
        assert isinstance(java_type, JavaType)
        self.ret_type = java_type

    def assignId(self, coproc_id, method_id):
        self.method_id = method_id
        self.coproc_id = coproc_id

    def generateJava(self):
        
        def HN(name):
            return '__hw_%s' % name

        out = []
        out.append("/* method id %u */\n" % self.method_id)
        out.append("public %s %s (\n" % (self.ret_type.name, self.name))
        first = True
        for (name, java_type) in self.param_list:
            if ( not first ):
                out.append(',\n')
            out.append("\t\t%s %s " % (java_type.name, name))
            first = False
        out.append(")\n")
        out.append("{\n")
        ret_name = "__ret"
        for (name, java_type) in self.param_list:
            out.append("int %s;\n" % HN(name))
        out.append("int %s;\n" % ret_name)

        for (name, java_type) in self.param_list:
            out.append(java_type.convertForHardware(name, HN(name)))

        payload_size = len(self.param_list)
        header_word = ( self.method_id << 8 ) | payload_size | (
                    self.coproc_id << 16 )

        out.append("_acquireLock();\n")
        out.append("_sendCCMessage(0x%x);\n" % header_word)
        for (name, java_type) in self.param_list:
            out.append("_sendCCMessage(%s);\n" % HN(name))
        out.append("_releaseLock();\n")
        out.append("// run\n")

        header_word = ( self.coproc_id << 24 ) | ( self.method_id << 8 ) | 1
        out.append("_awaitHeaderThenLock(0x%x);\n" % header_word)
        out.append("int %s = _receiveCCMessage();\n" % HN(ret_name))
        
        if ( isinstance(self.ret_type, VoidType) ):
            # Nothing is returned.
            out.append("_releaseLock();\n")
        else:
            out.append(self.ret_type.convertForJava(HN(ret_name), ret_name))
            out.append("_releaseLock();\n")
            out.append("return %s;\n" % ret_name)
        out.append("}\n\n")
        return ''.join(out)

    def generateVHDL(self):
        # Entity definition
        header_code = []
        for (name, java_type) in self.param_list:
            header_code.append(
        "method_%s_param_%s : out std_logic_vector ( %u downto 0 );\n" % (
                self.name, name, java_type.getVectorSize() - 1))

        header_code.append(
        "method_%s_return : in std_logic_vector ( %u downto 0 );\n" % (
                self.name, self.ret_type.getVectorSize() - 1))
        header_code.append(
        "method_%s_start : out std_logic;\n" % (
                self.name))
        header_code.append(
        "method_%s_running : in std_logic;\n" % (
                self.name))

        # Internal signals
        signals_code = []
        signals_code.append(
        "signal method_%s_waiting : std_logic;\n" % (self.name))

        # The start output is a momentary trigger; it is high
        # until the running signal is acknowledged
        default_code = []
        default_code.append("method_%s_start <= '0';\n" % (self.name))

        # Dispatcher and states
        def MS(number):
            return "MS_%s_%u" % (self.name, number)

        def WMS(number):
            return "when %s =>\n" % MS(state_number)

        dispatch_code = []
        dispatch_code.append(
            'when x"%02x" => opcode <= %s;\n' % (
                    self.method_id, MS(0)))

        method_code = []
        state_number = 0
        first = True
        method_code.append(WMS(state_number))
        # Parameter-reading code
        if ( len(self.param_list) != 0 ):
            for (name, java_type) in self.param_list:
                if ( first ):
                    first = False

                method_code.append(
                    "opcode <= WAIT_INPUT;\n")
                method_code.append(
                    "when_ready <= %s;\n" % MS(state_number + 1))

                state_number += 1
                method_code.append(WMS(state_number))
                method_code.append(
                    "method_%s_param_%s <= cc_register ( %u downto 0 ) ;\n" % (
                                self.name, name, java_type.getVectorSize() - 1))

        # Start code
        method_code.append("method_%s_start <= '1';\n" % (self.name))
        method_code.append("method_%s_waiting <= '1';\n" % (self.name))
        method_code.append("if ( method_%s_running = '1' )\n" % (self.name))
        method_code.append("then opcode <= CHANNEL_IS_OPEN;\n")
        method_code.append("end if;\n")
        state_number += 1

        # Re-entry point (on completion)
        # Send return packet
        reentry_number = state_number
        payload_size = 1
        header_word = ( self.method_id << 8 ) | payload_size | (
                    self.coproc_id << 24 )
        method_code.append(WMS(state_number))
        method_code.append("method_%s_waiting <= '0';\n" % (self.name))
        method_code.append('cc_register <= x"%08x";\n' % header_word)
        method_code.append("opcode <= WAIT_OUTPUT;\n")
        method_code.append("when_ready <= %s;\n" % MS(state_number + 1))

        state_number += 1
        method_code.append(WMS(state_number))
        method_code.append('cc_register <= method_%s_return;\n' % self.name)
        method_code.append("opcode <= WAIT_OUTPUT;\n")
        method_code.append("when_ready <= CHANNEL_IS_OPEN;\n")

        # Generate all states
        state_list = []
        state_number += 1
        for number in xrange(state_number):
            state_list.append(MS(number))
        state_list.append("")

        # Generate re-entry code
        reentry_code = []
        reentry_code.append("elsif ( method_%s_running = '0' )\n" % self.name)
        reentry_code.append("and ( method_%s_waiting = '1' )\n" % self.name)
        reentry_code.append("then opcode <= %s;\n" % MS(reentry_number))

        # Done
        return {
            "header_code" : ''.join(header_code),
            "method_code" : ''.join(method_code),
            "default_code" : ''.join(default_code),
            "signals_code" : ''.join(signals_code),
            "reentry_code" : ''.join(reentry_code),
            "dispatch_code" : ''.join(dispatch_code),
            "state_code" : ',\n'.join(state_list),
            }
        

        
class Coprocessor:
    def __init__(self, name):
        self.name = name
        self.method_list = []
        self.coproc_id = None

    def addMethod(self, method):
        assert isinstance(method, Method)
        self.method_list.append(method)

    def assignId(self, coproc_id):
        self.coproc_id = coproc_id
        method_id = 1
        for method in self.method_list:
            method.assignId(coproc_id, method_id)
            method_id += 1

    def generateJava(self):
        out = []
        out.append(JAVA_HEADER)
        out.append("/* coprocessor id %u */\n" % self.coproc_id)
        out.append("public class %s {\n" % self.name)
        for method in self.method_list:
            out.append(method.generateJava())
        out.append(JAVA_BODY)
        out.append("public %s () {\n" % self.name)
        out.append("JeopardIOFactory factory = "
                "JeopardIOFactory.getJeopardIOFactory();\n")
        out.append("control_channel = factory.getControlPort();\n")
        out.append("\n}\n}\n")
        return ''.join(out)
            
    def generateVHDL(self):
        codes = { "name" : self.name,
                "coproc_id" : self.coproc_id,
                }
        

        for method in self.method_list:
            for (key, value) in method.generateVHDL().iteritems():
                codes[ key ] = a = codes.get(key, [])
                a.append(value)
        
        for (key, value) in codes.iteritems():
            if ( type(value) == list ):
                codes[ key ] = ''.join(value)

        out = []
        out.append("""

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity %(name)s_if is
    port (
            clk             : in std_logic;
            reset           : in std_logic;

            -- Begin header code
            %(header_code)s
            -- End header code

            cc_out_data     : out std_logic_vector(31 downto 0);
            cc_out_wr       : out std_logic;
            cc_out_rdy      : in std_logic;

            cc_in_data      : in std_logic_vector(31 downto 0);
            cc_in_wr        : in std_logic;
            cc_in_rdy       : out std_logic
        );
end entity %(name)s_if;

architecture cpi of %(name)s_if is

    constant zero : std_logic_vector ( 7 downto 0 ) := x"00" ;

    signal cc_reg_full          : std_logic;
    signal out_message_counter  : std_logic_vector ( 7 downto 0 );
    signal in_message_left      : std_logic_vector ( 7 downto 0 );
    signal in_message_is_from   : std_logic_vector ( 7 downto 0 );
    signal method_id            : std_logic_vector ( 7 downto 0 );
    signal cc_register          : std_logic_vector ( 31 downto 0 );
    -- Begin state list 
    %(signals_code)s
    -- End state list

    type OpcodeType is ( 
            CHANNEL_IS_OPEN,
            PASS_MESSAGE,
            PASS_MESSAGE_1,
            CALL_METHOD,
            -- Begin state list 
            %(state_code)s
            -- End state list
            WAIT_INPUT,
            WAIT_OUTPUT );

    signal opcode, when_ready   : OpcodeType;
begin
    process(clk, reset) is
    begin
        if reset = '1' 
        then
            when_ready <= CHANNEL_IS_OPEN;
            opcode <= CHANNEL_IS_OPEN;
            cc_out_wr <= '0';
            cc_in_rdy <= '0';
            cc_register <= ( others => '0' ) ;
            cc_out_data <= ( others => '0' ) ;
            in_message_left <= zero ;
            in_message_is_from <= zero ;

        elsif rising_edge(clk) 
        then
            cc_in_rdy <= '0';
            cc_out_wr <= '0';
            -- Begin default code
            %(default_code)s
            -- End default code


            case opcode is
            when CHANNEL_IS_OPEN =>
                -- Receive a message header word
                if ( cc_in_wr = '1' )
                then
                    cc_register <= cc_in_data;
                    in_message_left <= cc_in_data ( 7 downto 0 ) ;

                    if ( cc_in_data ( 23 downto 16 ) = x"%(coproc_id)02x" ) 
                    then
                        -- message is for this co-processor
                        in_message_is_from <= cc_in_data ( 31 downto 24 ) ;
                        method_id <= cc_in_data ( 15 downto 8 ) ;
                        opcode <= CALL_METHOD;
                    else
                        -- message header to be relayed
                        when_ready <= PASS_MESSAGE;
                        opcode <= WAIT_OUTPUT;
                    end if;
                -- Begin reentry code
                %(reentry_code)s
                -- End reentry code
                else
                    cc_in_rdy <= '1';
                end if;

            when PASS_MESSAGE =>
                -- Get a new message payload word, if any remain.
                if ( in_message_left = zero )
                then
                    opcode <= CHANNEL_IS_OPEN;
                else
                    opcode <= WAIT_INPUT;
                    when_ready <= PASS_MESSAGE_1;
                    in_message_left <= in_message_left - 1;
                end if;

            when PASS_MESSAGE_1 =>
                -- Relay this word
                opcode <= WAIT_OUTPUT;
                when_ready <= PASS_MESSAGE;

            when CALL_METHOD =>
                case method_id is
                -- Begin dispatch code
                %(dispatch_code)s
                -- End dispatch code
                when others => opcode <= PASS_MESSAGE;
                end case ;

            -- Begin method code
            %(method_code)s
            -- End method code

            when WAIT_INPUT =>
                if ( cc_in_wr = '1' )
                then
                    cc_register <= cc_in_data ;
                    opcode <= when_ready;
                else
                    cc_in_rdy <= '1';
                end if;

            when WAIT_OUTPUT =>
                if ( cc_out_rdy = '1' )
                then
                    cc_out_data <= cc_register;
                    cc_out_wr <= '1';
                    opcode <= when_ready;
                end if ;
            end case;
        end if;
    end process;
end architecture cpi ;
""" % codes)
        return ''.join(out)



def makeInterfaces(definition_fname, java_dir, vhdl_dir):

    all_cps = []
    coproc = None
    method = None

    for line in file(definition_fname):
        line = line.split('#')[ 0 ].strip()
        if ( len(line) == 0 ):
            continue

        fields = line.split()
        command = fields[ 0 ].upper()

        def NFields(n):
            if ( len(fields) != ( n + 1 )):
                raise InterfaceDefinitionException(
                    "%s statement takes %u parameters." % (command, n))

        def HasCP():
            if ( coproc == None ):
                raise InterfaceDefinitionException(
                    "%s statement must follow COPROCESSOR.")

        def HasMethod():
            HasCP()
            if ( method == None ):
                raise InterfaceDefinitionException(
                    "%s statement must follow METHOD.")

        if ( command == "COPROCESSOR" ):
            NFields(1)
            coproc = Coprocessor(fields[ 1 ])
            all_cps.append(coproc)

        elif ( command == "METHOD" ):
            NFields(1)
            HasCP()
            method = Method(fields[ 1 ])
            coproc.addMethod(method)
        elif ( command == "PARAMETER" ):
            NFields(2)
            HasMethod()
            method.addParameter(fields[ 1 ], AnyType(fields[ 2 ]))
        elif ( command == "RETURN" ):
            NFields(1)
            HasMethod()
            method.addReturn(AnyType(fields[ 1 ]))
        else:
            raise InterfaceDefinitionException(
                    "%s statement is unknown." % command)
            

    coproc_id = 1
    for coproc in all_cps:
        coproc.assignId(coproc_id)
        coproc_id += 1

    for coproc in all_cps:
        jname = os.path.join(java_dir, "%s.java" % coproc.name)
        file(jname, 'wt').write(coproc.generateJava())

        vname = os.path.join(vhdl_dir, "%s_if.vhd" % coproc.name)
        file(vname, 'wt').write(coproc.generateVHDL())

JAVA_HEADER = """
package test;


import com.jopdesign.io.ControlChannel;
import com.jopdesign.io.JeopardIOFactory;
import com.jopdesign.sys.Native;
"""

JAVA_BODY = """
    private ControlChannel control_channel ;

    private void _sendCCMessage ( int word )
    {
        while ( ! control_channel.txEmpty () ) { /* yield */ }
        control_channel.write ( word ) ;
    }

    private int _receiveCCMessage ()
    {
        while ( ! control_channel.rxFull () ) { /* yield */ }
        int msg = control_channel.read () ;
        control_channel.advance () ;
        return msg;
    }
                    
    private int _dereference ( int [] a )
    {
        return Native.rdMem ( Native.toInt ( a ) ) ;
    }

    private void _acquireLock()
    {
        while ( ! control_channel.tryToAcquireLock() ) { /* yield */ }
    }

    private void _awaitHeaderThenLock(int header_expect)
    {
        while ( true ) {
            _acquireLock () ;
            int header = control_channel.read();
            if ( header == header_expect ) 
            {
                control_channel.advance();
                return ;
            }
            _releaseLock () ;
            /* yield */
        }
    }

    private void _releaseLock()
    {
        control_channel.releaseLock();
    }
"""

if ( __name__ == "__main__" ):
    makeInterfaces(definition_fname="mac_coprocessor.def", 
                java_dir="../../java/target/src/test/test", 
                vhdl_dir=".")




