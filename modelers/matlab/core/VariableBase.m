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
        
        % FIXME: Mod should use a java function too, but need version that
        % works like MATLAB for negative numbers (or vice versa)
        function z= mod(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@mod,@(z,x,y) z == mod(x,y));
        end
        
        function z = minus(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@minus,com.analog.lyric.dimple.FactorFunctions.Minus);
        end
        
        function z = mpower(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@mpower,com.analog.lyric.dimple.FactorFunctions.Power);
        end
        
        function z = and(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@and,com.analog.lyric.dimple.FactorFunctions.And);
        end
        
        function z = or(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@or,com.analog.lyric.dimple.FactorFunctions.Or);
        end
        
        function z = mtimes(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@mtimes,com.analog.lyric.dimple.FactorFunctions.Product);
        end
        
        function z = xor(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@xor,com.analog.lyric.dimple.FactorFunctions.Xor);
        end
        
        function z = plus(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@plus,com.analog.lyric.dimple.FactorFunctions.Sum);
        end
        
        function z = uminus(a)
            z = addUnaryOperatorOverloadedFactor(a,@uminus,com.analog.lyric.dimple.FactorFunctions.Negate);
        end
        
        function z = not(a)
            z = addUnaryOperatorOverloadedFactor(a,@not,com.analog.lyric.dimple.FactorFunctions.Not);
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
        
        
        function z = addBinaryOperatorOverloadedFactor(a,b,operation,factor)
            if (~isa(a.Domain, 'RealDomain') && ~isa(b.Domain, 'RealDomain'))
                
                domaina = a.Domain.Elements;
                domainb = b.Domain.Elements;
                zdomain = zeros(length(domaina)*length(domainb),1);
                
                curIndex = 1;
                
                if (isa(factor, 'com.analog.lyric.dimple.FactorFunctions.core.FactorFunction'))
                    for i = 1:length(domaina)
                        for j = 1:length(domainb)
                            zdomain(curIndex) = factor.eval({domaina{i} domainb{j}});
                            curIndex = curIndex+1;
                        end
                    end
                else
                    for i = 1:length(domaina)
                        for j = 1:length(domainb)
                            zdomain(curIndex) = operation(domaina{i},domainb{j});
                            curIndex = curIndex+1;
                        end
                    end
                end
                
                zdomain = unique(zdomain);
                zdomain = sort(zdomain);
                
                %znested = Variable(zdomain);
                z = Variable(zdomain);
                
                %Eventually can build combo table, right now, this will do
                fg = getFactorGraph();
                fg.addFactor(factor,z,a,b);
            
            else
                
                % At least one of the input variables is real, so the
                % output variable must be real
                
                if (~isa(factor, 'com.analog.lyric.dimple.FactorFunctions.core.FactorFunction'))
                    error('Can only override faction functions for real-valued operators');
                end
                
                z = Real();
                fg = getFactorGraph();
                fg.addFactor(factor,z,a,b);
                
            end
        end
        
        
        
        function z = addUnaryOperatorOverloadedFactor(a,operation,factor)
            if (~isa(a.Domain, 'RealDomain'))
                
                domaina = a.Domain.Elements;
                zdomain = zeros(length(domaina),1);
                
                curIndex = 1;
                
                if (isa(factor, 'com.analog.lyric.dimple.FactorFunctions.core.FactorFunction'))
                    for i = 1:length(domaina)
                        zdomain(curIndex) = factor.eval(domaina{i});
                        curIndex = curIndex+1;
                    end
                else
                    for i = 1:length(domaina)
                        zdomain(curIndex) = operation(domaina{i});
                        curIndex = curIndex+1;
                    end
                end
                
                zdomain = unique(zdomain);
                zdomain = sort(zdomain);
                
                %znested = Variable(zdomain);
                z = Variable(zdomain);
                
                %Eventually can build combo table, right now, this will do
                fg = getFactorGraph();
                fg.addFactor(factor,z,a);
            
            else
                
                % The input variables is real, so the
                % output variable must be real
                
                if (~isa(factor, 'com.analog.lyric.dimple.FactorFunctions.core.FactorFunction'))
                    error('Can only override faction functions for real-valued operators');
                end
                
                z = Real();
                fg = getFactorGraph();
                fg.addFactor(factor,z,a);
                
            end
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
