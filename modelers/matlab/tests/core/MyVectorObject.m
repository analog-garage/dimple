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

classdef MyVectorObject < handle
    %Used to test MatrixObject
    
    properties
        ValueObjects;
    end
    methods
        function obj = MyVectorObject(valueObjects)
            obj.ValueObjects = valueObjects;
        end
        
        %This is a hack.  ids should really be unique identifiers.
        function ids = getIds(obj)
            ids = zeros(size(obj.ValueObjects));
            for i = 1:numel(ids)
                ids(i) = obj.ValueObjects{i}.Value;
            end
        end
        
        function retval = getSlice(obj,ids)
            tmp = cell(numel(ids),1);
            for i = 1:length(tmp)
                tmp{i} = obj.ValueObjects{ids(i)+1};
            end
            retval = MyVectorObject(tmp);
        end
        
        function replace(obj, vectorObject,indices)
            for i = 1:length(indices)
               obj.ValueObjects{indices(i)+1} = vectorObject.ValueObjects{i}; 
            end
        end
        
        function retval = concat(obj,vectorObjects,...
                one_d_vector_object_indices_all,one_d_indices_all)
            tmp = cell(numel(one_d_indices_all),1);
            for i = 1:length(tmp)
                tmp{i} = vectorObjects{one_d_vector_object_indices_all(i)+1}.ValueObjects{one_d_indices_all(i)+1};
            end
            
            retval = MyVectorObject(tmp);
        end
        
        function setValues(obj,values)
            for i = 1:length(values)
                obj.ValueObjects{i}.Value = values(i);
            end
            
        end
        function retval = getValues(obj)
            retval = zeros(length(obj.ValueObjects),1);
            for i = 1:length(retval)
                retval(i) = obj.ValueObjects{i}.Value;
            end
        end
    end
    
    
end