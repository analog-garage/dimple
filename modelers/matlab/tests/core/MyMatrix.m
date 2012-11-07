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

classdef MyMatrix < MatrixObject
    %Used to test matrixobject.
    
    properties
        Value;
    end
    methods
        function obj = MyMatrix(vectorObject,indices)
            obj@MatrixObject(vectorObject,indices);
        end
        
        function v = get.Value(obj)
            v = MatrixObject.unpack(obj.VectorObject.getValues(),obj.VectorIndices);            
        end
        
        function set.Value(obj,value)
            obj.VectorObject.setValues(MatrixObject.pack(value,obj.VectorIndices));
        end
        
        function retval = getObject(obj,index)
            retval = obj(index);
        end
        
    end
    methods (Access=protected)
        function retval = createObject(obj,vectorObject,indices)
            retval = MyMatrix(vectorObject,indices);
        end
        
        function verifyCanConcatenate(obj,otherObjects)
            for i = 1:length(otherObjects)
                
                value = otherObjects{i}.Value;
                if max(value(:)) > 1000
                    error('for the sake of the test dont allow this');
                end
            end
        end
        
    end
end