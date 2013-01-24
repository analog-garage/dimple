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

classdef VariableStreamBase < IVariableStreamSlice
    properties
        IVariableStream;
        DataSource;
        DataSink;
        IVariableStreamSlice;
        Size;
        Variables;
    end
    
    
    
    methods
        
        function obj = VariableStreamBase(IVariableStream)
            obj.IVariableStream = IVariableStream;
        end

        function ret = get.IVariableStreamSlice(obj)
            ret = obj.IVariableStream;
        end
        
        function set.DataSink(obj,dataSink)
            obj.setDataSink(dataSink);
        end
        
        function ret = get.Size(obj)
            obj.IVariableStream.size();
        end
        
        function vars = get.Variables(obj)
           vars = wrapProxyObject(obj.IVariableStream.getVariables());
        end
        
        function set.DataSource(obj,dataSource)
           obj.setDataSource(dataSource); 
        end
        
        function sink = get.DataSink(obj)
           sink = obj.getDataSink(); 
        end
        function source = get.DataSource(obj)
            source = obj.getDataSource();
        end
        
        function slice = getSlice(obj,startIndex)
            if nargin < 3
                endIndex = Inf;
            end
            
            ISlice = obj.IVariableStream.getSlice(startIndex-1);
            
            slice = VariableStreamSlice(ISlice);
        end
        
        function var = get(obj,ind)
            ivar = obj.IVariableStream.get(ind-1);
            var = wrapProxyObject(ivar);
        end
        
    end
    
    methods(Access=protected)
        
        function setDataSink(obj,dataSink)
            if isa(dataSink,'DataSink')
                obj.IVariableStream.setDataSink(dataSink.IDataSink);
            else
                obj.IVariableStream.setDataSink(dataSink);
            end
        end
        
        function setDataSource(obj,dataSource)
           obj.IVariableStream.setDataSource(dataSource); 
        end
        
        function sink = getDataSink(obj)
           sink = obj.IVariableStream.getDataSink(); 
        end
        function source = getDataSource(obj)
            source = boj.IVariableStream.getDataSource();
        end
    end
end
