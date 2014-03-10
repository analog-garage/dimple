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

classdef FiniteFieldDomain < Domain
    properties (SetAccess=immutable)
        PrimitivePolynomial;
        Elements;
        N;  % Number of bits
    end
    
    methods
        
        function obj = FiniteFieldDomain(primitivePolynomial)
            modeler = getModeler();
            obj.IDomain = modeler.createFiniteFieldDomain(primitivePolynomial);
        end
        
        function val = isequal(obj,other)
            if ~isa(other,'FiniteFieldDomain')
                val = false;
            else
                val = isequal(obj.IDomain.getPrimitivePolynomial,other.IDomain.getPrimitivePolynomial);
            end
        end
        
        function val = get.PrimitivePolynomial(obj)
            val = obj.IDomain.getPrimitivePolynomial;
        end
        
        function val = get.Elements(obj)
            val = num2cell(double(obj.IDomain.getElements)');
        end
        
        function val = get.N(obj)
            val = obj.IDomain.getN();
        end

        function result = isDiscrete(obj)
            result = true;
        end

    end
    
end
