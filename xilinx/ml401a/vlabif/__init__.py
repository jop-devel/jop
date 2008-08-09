# 
# Debug Chain 
# Copyright (C) Jack Whitham 2008
# $Id: __init__.py,v 1.1 2008/08/09 12:29:30 jwhitham Exp $
# 
"""
Virtual Lab Interface

Provides software drivers to implement access to the Virtual Lab
Interface Hardware.

In order to use this module, you will need to understand 
Deferreds, a concept which is explained in the Twisted 
Python documentation. 

Version $Id: __init__.py,v 1.1 2008/08/09 12:29:30 jwhitham Exp $
"""


from debug_driver import DebugConfig, DebugDriver, SyncError
from debug_entity import DebugChainError
from debug_entity import ReadRegister, ReadWriteRegister, WriteRegister

from vlabif import VlabInterfaceError, VlabChannelTransport
from vlabif import VlabGenericProtocol, VlabInterfaceProtocol

from utils import SimpleDriver, makeNoise
from utils import SimpleDriver as EchoDriver


