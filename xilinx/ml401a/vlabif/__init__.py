# 
# Debug Chain 
# Copyright (C) Jack Whitham 2008
# $Id: __init__.py,v 1.3 2008/11/03 11:41:27 jwhitham Exp $
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
"""
Virtual Lab Interface

Provides software drivers to implement access to the Virtual Lab
Interface Hardware.

In order to use this module, you will need to understand 
Deferreds, a concept which is explained in the Twisted 
Python documentation. 

Version $Id: __init__.py,v 1.3 2008/11/03 11:41:27 jwhitham Exp $
"""


from debug_driver import DebugConfig, DebugDriver, SyncError
from debug_entity import DebugChainError
from debug_entity import ReadRegister, ReadWriteRegister, WriteRegister

from vlabif import VlabInterfaceError, VlabChannelTransport
from vlabif import VlabGenericProtocol, VlabInterfaceProtocol

from utils import SimpleDriver, makeNoise
from utils import SimpleDriver as EchoDriver


