%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013 Analog Devices, Inc.
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

classdef DataSink < handle
    properties
        IDataSink;
        Dimensions;
        Indices;
    end
    
    methods
        function obj = DataSink(dimensions,dataSink)
            
            if nargin < 1
                dimensions = size(1);
            end
            obj.IDataSink = dataSink;
            obj.Dimensions = dimensions;
            indices = reshape(1:prod(dimensions),dimensions)-1;
            obj.Indices = indices;
        end
        
        function retval = hasNext(obj)
            retval = obj.IDataSink.hasNext();
        end
        
        function retval = getNext(obj)
            retval = obj.IDataSink.getNext();
            if ~isa(retval,'double')
                retval = cell(retval);
            end
            retval = MatrixObject.unpack(retval,obj.Indices,true);
        end
        
    end
    
    
end