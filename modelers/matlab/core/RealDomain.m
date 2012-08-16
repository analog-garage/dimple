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

classdef RealDomain < Domain
    
    properties
       LB;
       UB;
    end
    
    methods
        function obj = RealDomain(lowerBound,upperBound)
            if nargin < 2
                upperBound = Inf;
            end
            if nargin < 1
                lowerBound = -Inf;
            end
            obj.UB = upperBound;
            obj.LB = lowerBound;
            modeler = getModeler();
            obj.IDomain = modeler.createRealDomain(lowerBound,upperBound);
            
        end
        
        function result = isDiscrete(obj)
            result = false;
        end        
        
        function val = isequal(obj,other)
            if ~isa(other,'RealDomain')
                val = false;
            else
                val = isequal(obj.LB,other.LB) && isequal(obj.UB,other.UB);
            end
        end

    end
end
