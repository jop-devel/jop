#
#   This file is part of JOP, the Java Optimized Processor
#     see <http://www.jopdesign.com/>
# 
#   Copyright (C) 2008-2009, Jack Whitham
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

import os, math, collections, glob
ADDRESS_SIZE = 24
LOOP_COST = 49      # $I$ - for fixedpr arbiter on ML401, 1 CPU

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
#
# The protocol for the control channel is designed so:
# - does not require any locking of the channel
#   hence, no deadlocks are possible
# - either: single access point is shared by all processors
#   or: one access point per processor
# - low bus overhead, chain can have any route through the system
# 


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
        return "%s = Native.rdMem ( Native.toInt ( %s ) );\n" % (hardware_name, java_name)

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
        self.ret_reg_num = None
        self.busy_reg_num = None
        self.coproc = None
        self.iterations = 1
        self.cost = 1
        self.loop_overhead = 25
        self.message_values = []
        self.message_variables = dict()

    def setOverhead(self, l):
        self.loop_overhead = l

    def setIterations(self, i):
        self.iterations = i

    def setCost(self, c):
        self.cost = c

    def addParameter(self, name, java_type):
        assert isinstance(java_type, JavaType)
        self.param_list.append((name, java_type, self.coproc.makeRegNumber()))

    def addReturn(self, java_type):
        assert isinstance(java_type, JavaType)
        self.ret_reg_num = self.coproc.makeRegNumber()
        self.ret_type = java_type

    def assignCP(self, coproc):
        assert isinstance(coproc, Coprocessor)
        assert self.coproc == None
        assert self.busy_reg_num == None
        self.coproc = coproc
        self.busy_reg_num = self.coproc.makeRegNumber()

    def generateJava(self):
        
        def HN(name):
            return '__hw_%s' % name

        base = self.coproc.getId() << 24
        out = collections.deque()
        ret_name = "__ret"

        # Communications with co-processor:
        out.append("// load parameters\n")
        for (name, java_type, reg_number) in self.param_list:
            for high in (0, 1):
                if ( high ):
                    if ( 16 >= java_type.getVectorSize() ):
                        continue

                out.append(self.makeWrite(self.makeMsgVar(
                      "0x%x | (((%s) >> %u) & 0xffff)" %
                        (( base | ( high << 23 ) | ( reg_number << 16 )),
                        HN(name), high * 16))))

        out.append("// start\n")
        out.append(self.makeWrite(self.makeMsgVar(hex( 
                base | ( self.busy_reg_num << 16 ) | 1 ))))
        out.append("// run (wait while busy)\n")
        out.append("int rc = 1;\n")
        out.append(self.makeCCTransaction("rc", 
                base | ( self.busy_reg_num << 16 ), True))

        # Handling of returned data
        if ( isinstance(self.ret_type, VoidType) ):
            # Nothing is returned.
            pass
        else:
            out.append("// get result\n")
            out.append("int %s, %s = 0;\n" % (ret_name, HN(ret_name)))
            out.append(self.makeCCTransaction(HN(ret_name), 
                      base | ( self.ret_reg_num << 16 ) | ( 1 << 23 ))) # high bits
            out.append(self.makeCCTransaction("rc", 
                      base | ( self.ret_reg_num << 16 ))) # low bits
            out.append("%s = rc | ( %s << 16 );\n" % (HN(ret_name), HN(ret_name)))
            out.append("// convert result\n")
            out.append(self.ret_type.convertForJava(HN(ret_name), ret_name))
            out.append("return %s;\n" % ret_name)
        out.append("}\n\n")

        rotate_by = len(out)

        # Create header of function:
        out.append("public %s %s (\n" % (self.ret_type.name, self.name))
        first = True
        for (name, java_type, reg_number) in self.param_list:
            if ( not first ):
                out.append(',\n')
            out.append("\t\t%s %s " % (java_type.name, name))
            first = False
        out.append(")\n")
        out.append("{\n")

        for (name, java_type, reg_number) in self.param_list:
            out.append("int %s; // 0x%x\n" % (HN(name), reg_number))

        out.append("// convert parameters\n")
        for (name, java_type, reg_number) in self.param_list:
            out.append(java_type.convertForHardware(name, HN(name)))

        # Constants - don't use "final" for these, because that moves
        # them out of the local variable area
        out.append("// I/O address\n")
        out.append("int __cci_addr = Const.IO_BASE + 0x30;\n")

        # All messages created before communication begins so that
        # no accesses to memory are necessary during communication.
        out.append("// create messages\n")
        for value in self.message_values:
            out.append("int %s = %s;\n" % (
                self.message_variables[ value ], value))

        out.rotate(-rotate_by)

        return ''.join(out)

    def generateVHDL(self):
        # Entity definition
        header_code = []
        for (name, java_type, reg_number) in self.param_list:
            header_code.append(
        "method_%s_param_%s : out std_logic_vector ( %u downto 0 );\n" % (
                self.name, name, java_type.getVectorSize() - 1))

        if ( not isinstance(self.ret_type, VoidType) ):
            header_code.append(
            "method_%s_return : in std_logic_vector ( %u downto 0 );\n" % (
                    self.name, self.ret_type.getVectorSize() - 1))

        header_code.append(
        "method_%s_start : out std_logic;\n" % (
                self.name))
        header_code.append(
        "method_%s_running : in std_logic;\n" % (
                self.name))

        # For computing "left downto right"
        def LR(java_type, high):
            right = high * 16
            left = right + 15
            if ( right >= java_type.getVectorSize() ):
                return (None, None)
            left = min(left, java_type.getVectorSize() - 1)
            return (left, right)

        # The start output is a momentary trigger, high for one clock cycle.
        default_code = []
        default_code.append("method_%s_start <= '0';\n" % (self.name))

        # The other things are parameter registers (WO), return register (RO)
        # and busy/start register (RW)
        register_code = []
        for (name, java_type, reg_number) in self.param_list:
            for high in (0, 1):
                (left, right) = LR(java_type, high)

                if ( right == None ):
                    continue

                register_code.append('when x"%02x" => ' % (
                        reg_number + (high * 128)))
                register_code.append(
                    'method_%s_param_%s ( %u downto %u ) <= ' % (
                        self.name, name, left, right))
                register_code.append(
                    'cc_in_data ( %u downto 0 ) ;\n' % ( left - right ))

        if ( not isinstance(self.ret_type, VoidType) ):
            reg_number = self.ret_reg_num
            for high in (0, 1):
                (left, right) = LR(self.ret_type, high)

                if ( right == None ):
                    continue

                register_code.append('when x"%02x" => ' % (
                        reg_number + (high * 128)))
                register_code.append(
                    'cc_out_data ( %u downto 0 ) <= ' % ( left - right ))
                register_code.append(
                    'method_%s_return ( %u downto %u ) ;\n' % (
                        self.name, left, right))

        register_code.append('when x"%02x" => ' % (self.busy_reg_num))
        register_code.append('method_%s_start <= cc_in_data ( 0 ); ' % (self.name))
        register_code.append('cc_out_data ( 0 ) <= method_%s_running ;\n' % (self.name))

        # Done
        return {
            "header_code" : ''.join(header_code),
            "register_code" : ''.join(register_code),
            "default_code" : ''.join(default_code),
            }
        

    def makeMsgVar(self, msg):
        name = self.message_variables.get(msg, None)
        if ( name == None ):
            name = "__msg%u" % (len(self.message_values))
            self.message_values.append(msg)
            self.message_variables[ msg ] = name
            assert len(self.message_values) == len(self.message_variables)
        return name

    def makeCCTransaction(self, out_var, msg, await_zero=False):
        fields = { 
            "cond" : "" ,
            "out_var" : out_var, 
            "msg" : msg, 
            "bound" : " // @WCA loop=1",
            "msg_var" : self.makeMsgVar(hex(msg)),
            "antimask" : self.makeMsgVar("0xffff"),
            "mask" : self.makeMsgVar("0x7fff0000"),
        }


        if ( await_zero ):
            bound_value = int(math.ceil((( 
                self.iterations * self.cost ) + self.loop_overhead ) / 
                    float(LOOP_COST)))
            fields[ "cond" ] = " || (( %(out_var)s & 1 ) != 0 )" % fields
            fields[ "bound" ] = " // @WCA loop<=%u" % bound_value

        return """
    {   // _ccTransaction(0x%(msg)x)
        int reply_masked = 0;
        int msg_masked = %(msg_var)s & %(mask)s;
        while (( reply_masked != msg_masked )%(cond)s) {%(bound)s
            Native.wrMem(%(msg_var)s, __cci_addr);
            %(out_var)s = Native.rdMem(__cci_addr);
            reply_masked = %(out_var)s & %(mask)s;
        }
        %(out_var)s &= %(antimask)s;
    }\n""" % fields

    def makeWrite(self, value):
        return "Native.wrMem(%s, __cci_addr);\n" % value
                    
        
class Coprocessor:
    def __init__(self, name, package_name):
        self.name = name
        self.method_list = []
        self.coproc_id = None
        self.reg_count = 0
        self.package_name = package_name

    def addMethod(self, method):
        assert isinstance(method, Method)
        self.method_list.append(method)
        method.assignCP(self)

    def getId(self):
        return self.coproc_id

    def assignId(self, coproc_id):
        assert coproc_id < 128
        self.coproc_id = coproc_id

    def makeRegNumber(self):
        rc = self.reg_count
        self.reg_count += 1
        assert rc < 128
        return rc

    def generateJava(self):
        out = []
        out.append("package %s;\n" % self.package_name)
        out.append(JAVA_HEADER)
        out.append("/* coprocessor id %u */\n" % self.coproc_id)
        out.append("public class %s {\n" % self.name)
        for method in self.method_list:
            out.append(method.generateJava())
        out.append(JAVA_BODY)
        out.append("public final static %s INSTANCE = new %s () ;\n" % (
                      self.name, self.name))
        out.append("public static %s getInstance () {\n" % self.name)
        out.append("return %s.INSTANCE;\n" % self.name)
        out.append("}\n")
        out.append("protected %s () {\n" % self.name)
        #out.append("JeopardIOFactory factory = "
        #        "JeopardIOFactory.getJeopardIOFactory();\n")
        #out.append("control_channel = factory.getControlPort();\n")
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


begin

    process(clk, reset) is
    begin
        if reset = '1' 
        then
            cc_out_wr <= '0';
            cc_in_rdy <= '0';
            cc_out_data <= ( others => '0' ) ;

        elsif rising_edge(clk) 
        then
-- Begin default code
%(default_code)s
-- End default code
            cc_in_rdy <= cc_out_rdy;
            cc_out_wr <= '0';

            if ( cc_in_wr = '1' )
            then
                cc_out_data <= cc_in_data;
                if (( cc_in_data ( 31 ) = '0' ) -- command (from CPU)
                and ( cc_in_data ( 30 downto 24 ) = x"%(coproc_id)02x" ))
                then
                    cc_out_data ( 31 ) <= '1' ; -- reply (from coproc)
                    cc_out_data ( 15 downto 0 ) <= ( others => '0' );
                    case cc_in_data ( 23 downto 16 ) is
-- Begin register code
%(register_code)s
-- End register code
                    when others => null;
                    end case;
                end if;
                cc_out_wr <= '1';
            end if;
        end if;
    end process;
end architecture cpi ;
""" % codes)
        return ''.join(out)



def makeInterfaces(definition_fname):

    all_cps = []
    coproc = None
    method = None
    package_name = "test"
    java_dir = vhdl_dir = '.'

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

        if ( command == "PACKAGE" ):
            NFields(1)
            package_name = fields[ 1 ]
        elif ( command == "JAVA" ):
            NFields(1)
            java_dir = fields[ 1 ]
        elif ( command == "VHDL" ):
            NFields(1)
            vhdl_dir = fields[ 1 ]
        elif ( command == "COPROCESSOR" ):
            NFields(1)
            coproc = Coprocessor(fields[ 1 ], package_name)
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
        elif ( command == "ITERATIONS" ):
            NFields(1)
            method.setIterations(int(fields[ 1 ]))
        elif ( command == "LOOP_OVERHEAD" ):
            NFields(1)
            method.setOverhead(int(fields[ 1 ]))
        elif ( command == "COST" ):
            NFields(1)
            method.setCost(int(fields[ 1 ]))
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


//import com.jopdesign.io.ControlChannel;
//import com.jopdesign.io.JeopardIOFactory;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;
"""

JAVA_BODY = """

"""

if ( __name__ == "__main__" ):
    for idl in glob.glob("*.def"):
        makeInterfaces(definition_fname=idl)




