import os

from core.service import CoreService, addservice
from core.misc.ipaddr import IPv4Prefix, IPv6Prefix
from subprocess import call


class MyService(CoreService):

    # a unique name is required, without spaces
    _name = "Adhoc"
    # you can create your own group here
    _group = "Routing"
    # list of other services this service depends on
    _depends = ()
    # per-node directories
    _dirs = ()
    # generated files (without a full path this file goes in the node's dir,
    #  e.g. /tmp/pycore.12345/n1.conf/)
    _configs = ()
    # this controls the starting order vs other enabled services
    _startindex = 50
    # list of startup commands, also may be generated during startup
    _startup = ('sh /home/core/Desktop/aer/tp2/code/boot.sh', )
    # list of shutdown commands
    _shutdown = ()

addservice(MyService)    
