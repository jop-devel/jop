# 
# Debug Chain Generator
# Copyright (C) Jack Whitham 2008
# $Id: debug_entity.py,v 1.3 2008/11/03 11:41:28 jwhitham Exp $
# 
#
# Copyright (C) 2008, Jack Whitham
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
import collections, md5


def convertIntToBin(item, size):
    mask = 1 << size
    if ( item >= mask ):
        raise DebugChainError("Value %u does not fit in %u bits." %
                    (item, size))

    out = []
    while ( mask != 1 ):
        mask = mask >> 1
        if ( item & mask ):
            out.append('1')
        else:
            out.append('0')
    return ''.join(out)

def convertBinToInt(item):
    mask = 1 << len(item)
    out = 0
    for ch in item:
        mask = mask >> 1
        if ( ch == '1' ):
            out |= mask
    return out

def checkBinaryString(item, direction, reg_name, size):
    if ( type(item) != str ):
        raise DebugChainError("%s for register %s should be a "
                "binary string." % (direction, reg_name))

    if ( len(item) != size ):
        raise DebugChainError("%s for register %s should be "
                "%u bits, got %u." % (direction, reg_name,
                        size, len(item)))
    for ch in item:
        if ( not ch in "01" ):
            raise DebugChainError("%s for register %s should be binary, "
                "but contains '%s'." % (direction, reg_name, ch))



class DebugChainError(Exception):
    """This exception is raised if the user attempts to load an
    illegal value into a debug chain register (e.g. loading 2 into
    a single bit register) or if there is a mismatch between the
    debug chain in hardware and software."""
    pass


class ReadWriteRegister:
    """Represents a debug register that can be read or written."""
    def __init__(self, input_name, output_name, size, value_should_be=None):
        """Create the register. 

        size is given in bits. A size of zero means the register is
        a single bit "std_logic" type. A size greater than zero means the
        register is a "std_logic_vector" type with one or more bits."""

        self.input_name = input_name
        self.output_name = output_name
        self.input_value_should_be = value_should_be

        self.size = size
        if ( self.size != 0 ):
            assert self.size > 0
            self.is_vector = True
        else:
            self.is_vector = False
            self.size = 1

        self.value = "0" * self.size
        if ( value_should_be != None ):
            self.value = value_should_be

        self.number = self.start_bit = self.end_bit = None

    def __str__(self):
        """Gets the value as a binary string, i.e. a string in
        which each character is '0' or '1'."""
        return self.value

    def get(self):
        """Gets the value as a binary string, i.e. a string in
        which each character is '0' or '1'."""
        return self.value

    def getInt(self):
        """Gets the value as an unsigned integer."""
        return convertBinToInt(self.value)

    def setInput(self, item):
        """This method is called by the driver."""
        if ( type(item) in (int, long) ):
            item = convertIntToBin(item, self.size)
            
        checkBinaryString(item, "Input", self.input_name, self.size) 

        if ( len(item) != self.size ):
            raise DebugChainError("Input for register %s should be "
                    "%u bits, got %u." % (self.input_name,
                            self.size, len(item)))
            
        self.value = item

    def setOutput(self, item):
        """Sets the value. The input may be a binary string or an 
        integer. If it is a binary string, its length must match the
        size of this register. If it is an integer, it must be between
        0 and the maximum value of the register."""
        if ( type(item) in (int, long) ):
            item = convertIntToBin(item, self.size)
        
        checkBinaryString(item, "Output", self.output_name, self.size) 

        self.value = item 

    def getText(self):
        """Gets a string containing the register name and value 
        in human-readable form."""
        name = self.output_name
        if (( name == None ) or ( self.input_name != None )):
            name = self.input_name
        if ( name == None ):
            name = '<unnamed>'

        v = self.getInt()
        if ( self.is_vector ):
            return '%-24s %08x %u' % (name, v, v)
        else:
            return '%-24s %u' % (name, v)
        

class ReadRegister(ReadWriteRegister):
    """Represents a debug register that can be read but not written."""
    def __init__(self, input_name, size, value_should_be=None):
        ReadWriteRegister.__init__(self,
                input_name=input_name,
                size=size,
                value_should_be=value_should_be,
                output_name=None)

    def setOutput(self, item):
        raise DebugChainError("This register can't be written.")
        
class WriteRegister(ReadWriteRegister):
    """Represents a debug register that can be written but not read."""
    def __init__(self, output_name, size, value_should_be=None):
        ReadWriteRegister.__init__(self,
                output_name=output_name,
                size=size,
                value_should_be=value_should_be,
                input_name=None)

    def setInput(self, item):
        raise DebugChainError("This register can't be read.")

class DebugChain:
    """This class is a software representation of a debug chain."""
    def __init__(self, dc_name="dc"):
        self.debug_chain = []
        self.vhdl_body = []
        self.vhdl_header = []
        self.finished = False
        self.dc_name = dc_name
        self.check_bits = md5.md5("debug chain")


    def loadDebugChainData(self, dc_value):
        # dc_value should be a binary string, containing only 0 and 1.

        if ( not self.finished ):
            raise DebugChainError("Chain is unfinished.")

        for dr in self.debug_chain:
            if ( len(dc_value) < dr.size ):
                raise DebugChainError(
                        "Chain is too short - should be %d bits" %
                        self.getDebugChainLength())

            decoded_value = ''.join(reversed(dc_value[ :dr.size ]))

            if ( dr.input_name != None ):
                dr.setInput(decoded_value)

            if (( dr.input_value_should_be != None )
            and ( dr.input_value_should_be != decoded_value )):
                raise DebugChainError(
                    "Check value mismatch. Software: %x  Hardware: %x" % (
                        convertBinToInt(dr.input_value_should_be), 
                        convertBinToInt(decoded_value)))


            dc_value = dc_value[ dr.size: ]
                
        if ( len(dc_value) != 0 ):
            raise DebugChainError("Chain is too long - should be %d bits" %
                    self.getDebugChainLength())


    def getAsDict(self):
        reg_data = dict()
        for dr in self.debug_chain:
            if ( dr.input_name != None ):
                reg_data[ dr.input_name ] = dr
            if ( dr.output_name != None ):
                reg_data[ dr.output_name ] = dr

        return reg_data

    def getAsList(self):
        return self.debug_chain

    def saveDebugChainData(self):
        if ( not self.finished ):
            raise DebugChainError("Chain is unfinished.")

        dc_value = []
        for dr in self.debug_chain:
            dc_value.append(''.join(reversed(dr.value)))
            assert len(dr.value) == dr.size

        return ''.join(dc_value)

    def getDebugChainLength(self):
        sz = 0
        for dr in self.debug_chain:
            sz += dr.size
        return sz

    def makeDebugRegister(self, dr):
        if ( self.finished ):
            raise DebugChainError("Chain is finished.")

        if ( not isinstance(dr, ReadWriteRegister) ):
            raise DebugChainError("makeDebugRegister() must be called "
                "with a ReadWriteRegister instance." )

        dr.number = len(self.debug_chain)

        # Fill in start/end fields
        if ( len(self.debug_chain) != 0 ):
            dr.start_bit = self.debug_chain[ -1 ].end_bit + 1
        else:
            dr.start_bit = 0
        dr.end_bit = dr.start_bit + dr.size - 1

        # Add to list
        self.debug_chain.append(dr)

        # Add to check bits
        self.check_bits.update(hex(dr.start_bit) + hex(dr.size))

        # Fill in VHDL
        params = { 'size' : dr.size - 1,
                'number' : dr.number,
                'number_1' : dr.number + 1,
                'source' : dr.input_name,
                'target' : dr.output_name,
                'value' : dr.input_value_should_be,
                'shiftreg' : '',
                'dc_clock_name' : 'dc_control.dc_clock',
                'dc_shift_name' : 'dc_control.dc_shift',
                'dc_capture_name' : 'dc_control.dc_capture',
                'dc_ready_name' : 'dc_control.dc_ready',
                'dc_name' : self.dc_name,
                'notvector' : ' '}

        if ( dr.size > 1 ):
            params[ 'shiftreg' ] = """
            %(dc_name)s_section_%(number)u ( %(size)u - 1 downto 0 ) <=
                        %(dc_name)s_section_%(number)u ( %(size)u downto 1 ) ;
                """ % params

        if ( not dr.is_vector ):
            params[ 'notvector' ] = '( 0 ) '

        self.vhdl_body.append("""
%(dc_name)s_%(number)u : process ( %(dc_clock_name)s ) is
begin
    if (( %(dc_clock_name)s = '1' ) and %(dc_clock_name)s'event )
    then""" % params)

        if ( dr.input_name == None ):
            if ( dr.input_value_should_be != None ):
                self.vhdl_body.append("""
        if ( %(dc_capture_name)s = '1' )
        then
            %(dc_name)s_section_%(number)u <= "%(value)s" ;
        end if;""" % params)

        else:
            self.vhdl_body.append("""
        if ( %(dc_capture_name)s = '1' )
        then
            %(dc_name)s_section_%(number)u %(notvector)s<= %(source)s ;
        end if;""" % params)

        if ( dr.output_name != None ):
            self.vhdl_body.append("""
        if ( %(dc_ready_name)s = '1' )
        then
            %(target)s <= %(dc_name)s_section_%(number)u %(notvector)s;
        end if;""" % params)

        self.vhdl_body.append("""
        if ( %(dc_shift_name)s = '1' )
        then
            %(shiftreg)s
            %(dc_name)s_section_%(number)u ( %(size)u ) <=
                        %(dc_name)s_section_%(number_1)u ( 0 ) ;
        end if ;
    end if ;
end process %(dc_name)s_%(number)u ;\n""" % params)

        self.vhdl_header.append("""
signal %(dc_name)s_section_%(number)u 
                : std_logic_vector ( %(size)u downto 0 ) ;\n""" % params)

 
    def getSignals(self):
        if ( not self.finished ):
            raise DebugChainError("Chain is unfinished.")

        return ''.join(self.vhdl_header)
        
    def getBody(self):
        if ( not self.finished ):
            raise DebugChainError("Chain is unfinished.")

        return ''.join(self.vhdl_body)

    def finishChain(self):
        if ( self.finished ):
            raise DebugChainError("Chain is finished.")

        # Check bits are added to the end of the debug chain.
        # We add between 8 and 15 bits, choosing the number
        # that is added so that there will be a whole number of
        # bytes in the chain.
        num_bits = self.getDebugChainLength()
        round_up = ( num_bits + 7 ) & ~0x7
        need_to_add = round_up - num_bits
        need_to_add += 8

        nibbles = ( need_to_add / 4 ) + 1
        c = int(self.check_bits.hexdigest()[ : nibbles ], 16)
        cb = ''
        while ( len(cb) < need_to_add ):
            cb += str(( c >> len(cb) ) & 1)

        self.makeDebugRegister(ReadRegister(
                input_name=None,
                size=len(cb),
                value_should_be=cb))

        # Internal links to debug chain
        params = { 'last' : len(self.debug_chain),
                'dc_name' : self.dc_name,
                'dc_in' : 'dc_in',
                'dc_out' : 'dc_out' }

        self.vhdl_header.append("""
signal %(dc_name)s_section_%(last)u : 
            std_logic_vector ( 0 downto 0 ) ;\n""" % params)
        
        self.vhdl_body.append("""
%(dc_name)s_section_%(last)u ( 0 ) <= %(dc_in)s ;
%(dc_out)s <= %(dc_name)s_section_0 ( 0 ) ;\n""" % params)

        # Done
        self.finished = True

class DebugEntity(DebugChain):
    def __init__(self, **p):
        DebugChain.__init__(self, **p)
        self.vhdl_entity = []

    def makeDebugRegister(self, dr):
        DebugChain.makeDebugRegister(self, dr)

        for (direction, name) in [ ("in", dr.input_name),
                                ("out", dr.output_name) ]:
            if ( name != None ):
                if ( dr.is_vector ):
                    self.vhdl_entity.append("\t\t%-20s"
                        ": %s std_logic_vector ( %u downto 0 ) ;\n" % (
                            name, direction, dr.size - 1))
                else:
                    self.vhdl_entity.append("\t\t%-20s"
                        ": %s std_logic ;\n" % (name, direction))

    def getVHDL(self):
        params = { "entity" : ''.join(self.vhdl_entity) ,
                "signals" : self.getSignals(),
                "body" : self.getBody(),
                "dc_name" : self.dc_name,
                }
        return """
library ieee ;
use ieee.std_logic_1164.all ;
use work.vlabifhw_pack.all;

entity %(dc_name)s is
port (
%(entity)s
\t\tdc_control          : in DC_Control_Wires;
\t\tdc_out              : out std_logic;
\t\tdc_in               : in std_logic ) ;
end entity %(dc_name)s ;

architecture dc1 of %(dc_name)s is
%(signals)s
begin
%(body)s
end architecture dc1 ;


""" % params


