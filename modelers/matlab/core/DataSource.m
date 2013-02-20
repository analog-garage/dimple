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

classdef DataSource < handle
    properties
        Dimensions;
        Indices;
        IDataSource;
    end
    methods
        function obj = DataSource(dimensions)
            
            obj.Dimensions = dimensions;
            obj.Indices = reshape(1:prod(dimensions),dimensions)-1;
            tmp = obj.getIDataSourceFromModeler(prod(dimensions));
            obj.IDataSource = tmp;
            
        end
        
        
        
        function retval = hasNext(obj)
            retval = obj.IDataSource.hasNext();
        end
        
    end
    
    methods(Abstract=true)
        dataSource = getIDataSourceFromModeler(obj,dimensions);
    end
end