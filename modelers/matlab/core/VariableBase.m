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

classdef VariableBase < Node
    properties
        Domain;
        Factors;
        FactorsTop;
        FactorsFlat;
        Guess;
        
        Input;
        Belief;
    end
    
    methods
        
        function obj = VariableBase(vectorObject,indices)
            obj@Node(vectorObject,indices);
        end
        
        function z= mod(a,b)
            z = addOperatorOverloadedFactor(a,b,@mod,@(z,x,y) z == mod(x,y));
        end
        
        function z = minus(a,b)
            z = addOperatorOverloadedFactor(a,b,@minus,@(z,x,y) z == (x-y));
        end
        
        function z = mpower(a,b)
            z = addOperatorOverloadedFactor(a,b,@mpower,@(z,x,y) z==(x^y));
        end
        
        function z = and(a,b)
            z = addOperatorOverloadedFactor(a,b,@and,@(z,x,y) z == (x&y));
        end
        
        function z = or(a,b)
            z = addOperatorOverloadedFactor(a,b,@or,@(z,x,y) z==(x|y));
        end
        
        function z = mtimes(a,b)
            z = addOperatorOverloadedFactor(a,b,@mtimes,@(z,x,y) z == (x*y));
        end
        
        function z = xor(a,b)
            z = addOperatorOverloadedFactor(a,b,@xor,@(z,x,y) z == xor(x,y));
        end
        
        function z = plus(a,b)
            z = addOperatorOverloadedFactor(a,b,@plus,@(z,x,y) z == (x+y));
        end
        
        function x = get.Domain(obj)
            x = obj.Domain;
        end
        
        
        function guess = get.Guess(obj)
            tmp = cell(obj.VectorObject.getGuess());
            guess = obj.unpack(tmp,obj.VectorIndices);
        end
        
        function set.Guess(obj,guess)
            input = obj.pack(guess,obj.VectorIndices);
            if ~iscell(input) && ~isfloat(input)
                input = num2cell(input);
            end
            obj.VectorObject.setGuess(input);
        end
        
        
        function factors = get.Factors(obj)
            factors = obj.getFactors(intmax);
        end
        
        function factors = get.FactorsTop(obj)
            factors = obj.getFactors(0);
        end
        
        function factors = get.FactorsFlat(obj)
            factors = obj.getFactors(intmax);
        end
        
        function factors = getFactors(obj,relativeNestingDepth)
            tmp = obj.VectorObject.getFactors(relativeNestingDepth);
            factors = cell(tmp.size(),1);
            for i = 1:tmp.size()
                factors{i} = wrapProxyObject(tmp.getSlice(i-1));
            end
        end
        
        
        function set.Input(obj,value)
            obj.setInput(value);
        end
        
        function x = get.Input(obj)
            x = obj.getInput();
        end
        
        function x = get.Belief(obj)
            x = obj.getBelief();
        end
                
    end
    
    methods (Access=protected,Abstract=true)
        setInput(obj,value);
        x = getInput(obj);
        x = getBelief(obj);
    end
    
    methods (Access = protected)
        
        
        function z = addOperatorOverloadedFactor(a,b,operation,factor)
            domaina = a.Domain.Elements;
            domainb = b.Domain.Elements;
            zdomain = zeros(length(domaina)*length(domainb),1);
            
            curIndex = 1;
            
            for i = 1:length(domaina)
                for j = 1:length(domainb)
                    zdomain(curIndex) = operation(domaina{i},domainb{j});
                    curIndex = curIndex+1;
                end
            end
            
            zdomain = unique(zdomain);
            zdomain = sort(zdomain);
            
            %znested = Variable(zdomain);
            z = Variable(zdomain);
            
            %Eventually can build combo table, right now, this will do
            fg = getFactorGraph();
            
            fg.addFactor(factor,z,a,b);
            
        end
        
        
        function b = getDomain(obj)
            b = obj.Domain;
        end
        
        
        function verifyCanConcatenate(obj,otherObjects)
            for i = 1:length(otherObjects)
                if ~isa(otherObjects{i},'VariableBase')
                    error('Only variables can be concatentated with other variables');
                end
                if ~isequal(obj.Domain,otherObjects{i}.Domain)
                    error('Domains must match when concatenating');
                end
            end
        end
        
    end
    
end
