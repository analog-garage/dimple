%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
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

classdef FactorGraphStream < handle
    properties
        IFactorGraphStream;
        hasNext;
        BufferSize;
    end
    methods
        function obj = FactorGraphStream(intf)
            obj.IFactorGraphStream = intf;
        end
        
        function next = get.hasNext(obj)
           next = obj.IFactorGraphStream.hasNext(); 
        end
        
        function advance(obj)
            obj.IFactorGraphStream.advance();
        end
        
        function set.BufferSize(obj,sz)
            obj.IFactorGraphStream.setBufferSize(sz);
        end
        
        function sz = get.BufferSize(obj)
            sz = obj.IFactorGraphStream.getBufferSize();
        end
    end
end
