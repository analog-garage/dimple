%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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
