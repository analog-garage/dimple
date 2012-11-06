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

classdef FiniteFieldVariable < Discrete
    
    properties
    end
    
    methods
        function obj = FiniteFieldVariable(poly,varargin)
            
            if numel(poly) ~= 1
                error('FiniteFieldVariable expects a polynomial to be specified as a single decimal number');
            end
            
            poly = double(dec2bin(poly)-'0');
            domain = 0:2^(length(poly)-1)-1;

            obj@Discrete(domain,varargin{:});
            
            obj.VectorObject.setProperty('primitivePolynomial',poly);
        end
    end
    
end

