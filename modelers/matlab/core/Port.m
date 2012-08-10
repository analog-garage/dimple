classdef Port < handle
    properties
        IPort;
        InputMsg;
        OutputMsg;
    end
    methods
        function obj = Port(iport)
            obj.IPort = iport;
        end
        
        function msg = get.InputMsg(obj)
            msg = obj.IPort.getInputMsg();
        end
        
        function set.InputMsg(obj,msg)
            obj.IPort.setInputMsg(msg);
        end
        
        function msg = get.OutputMsg(obj)
            msg = obj.IPort.getOutputMsg();
        end
        
        function set.OutputMsg(obj,msg)
            obj.IPort.setOutputMsg(msg);
        end
        
        function retval = eq(a,b)
            retval = a.equals(b);
        end
        
        function retval = equals(a,b)
            retval = a.IPort == b.IPort;
        end
    end
    
    
    
end
