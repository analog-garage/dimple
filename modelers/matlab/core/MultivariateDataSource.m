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

classdef MultivariateDataSource < DataSource
    methods
        function obj = MultivariateDataSource(dimensions)
            if nargin < 1
                dimensions = size(1);
            end
            
            obj@DataSource(dimensions);
        end
        
        function dataSource = getIDataSourceFromModeler(obj,sz)
            modeler = getModeler();
            dataSource = modeler.getMultivariateDataSource(sz);            
        end
        
        function add(obj,means,covariance)
            packedMeans = MatrixObject.pack(means,obj.Indices);
            packedCovariance = MatrixObject.pack(covariance,obj.Indices);            
            obj.IDataSource.add(packedMeans,packedCovariance);
        end
    end
    
end
