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

classdef GenericDataSource < DataSource
    properties
    end
    methods
        function obj = GenericDataSource(dimensions,initialData)
            if nargin < 1
                dimensions = [1 1];
            end
            
            hasData = nargin > 1;
            
            if nargin == 1 && ~isrow(dimensions)
                initialData = dimensions;
                dimensions = size(1);
                hasData = 1;
            end
            
            obj@DataSource(dimensions);
            
            
            if hasData
                obj.add(initialData);
            end
        end
        
        
        function add(obj,data)
            % stuff
            
            packedData = MatrixObject.pack(data,obj.Indices);
            if length(size(packedData)) == 3
                obj.IDataSource.addMultiple(packedData);
            else
                obj.IDataSource.add(packedData);
            end
        end
        
        %function dataSource = getIDataSourceFromModeler(obj,sz)
        %    modeler = getModeler();
        %    dataSource = modeler.getDoubleArrayDataSource(sz);
        %end
    end
    
end