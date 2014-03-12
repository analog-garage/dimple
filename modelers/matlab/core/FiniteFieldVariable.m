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

classdef FiniteFieldVariable < DiscreteVariableBase
    
    properties
    end
    
    methods
        function obj = FiniteFieldVariable(domain,varargin)
            
            if (~isa(domain, 'FiniteFieldDomain'))
                primitivePolynomial = domain;
                
                if numel(primitivePolynomial) ~= 1
                    error('FiniteFieldVariable expects a polynomial to be specified as a single decimal number');
                end
                
                domain = FiniteFieldDomain(primitivePolynomial);
            end
            
            obj@DiscreteVariableBase(domain,varargin{:});
        end
        
    end
    
    methods (Access = protected)
        function retval = createObject(obj,vectorObject,indices)
            retval = FiniteFieldVariable(obj.Domain,'existing',vectorObject,indices);
        end
        
        function setGuess(obj, guess)
            if (isa(guess,'gf'))
                guess = num2cell(guess.x);
            elseif (iscell(guess))
                guess = cellfun(@(a)gf2num(a), guess);
            end
            setGuess@DiscreteVariableBase(obj, guess);
        end

        function guess = getGuess(obj)
            guess = getGuess@DiscreteVariableBase(obj);
            guess = cell2mat(guess);
            guess = gf(guess, obj.Domain.N, obj.Domain.PrimitivePolynomial);
        end
        
        function setFixedValue(obj, value)
            if (isa(value,'gf'))
                value = value.x;
            elseif (iscell(value))
                value = cellfun(@(a)gf2num(a), value);
            end
            setFixedValue@DiscreteVariableBase(obj, value);
        end

        
        function value = getFixedValue(obj)
            value = getFixedValue@DiscreteVariableBase(obj);
            value = gf(value, obj.Domain.N, obj.Domain.PrimitivePolynomial);
        end        

    end
    
end


function out = gf2num(in)
if (isa(in,'gf'))
    out = in.x;
else
    out = in;
end

end

