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
        Value;
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
            z = addBinaryOperatorOverloadedFactor(a,b,@minus,com.analog.lyric.dimple.FactorFunctions.Subtract);
        end
        
        function z = power(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@mpower,com.analog.lyric.dimple.FactorFunctions.Power);
        end
        
        function z = mpower(a,b)
            if (~isscalar(a) || ~isscalar(b)); error('Overloaded matrix power not currently supported. Use ".^" for pointwise power'); end;
            z = addBinaryOperatorOverloadedFactor(a,b,@mpower,com.analog.lyric.dimple.FactorFunctions.Power);
        end
        
        function z = and(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@and,com.analog.lyric.dimple.FactorFunctions.And);
        end
        
        function z = or(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@or,com.analog.lyric.dimple.FactorFunctions.Or);
        end
        
        function z = times(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@mtimes,com.analog.lyric.dimple.FactorFunctions.Product);
        end
        
        function z = mtimes(a,b)
            if (~isscalar(a) || ~isscalar(b)); error('Overloaded matrix product not currently supported. Use ".*" for pointwise product'); end;
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
            z = addUnaryToBitOperatorOverloadedFactor(a,com.analog.lyric.dimple.FactorFunctions.Not);
        end
        
        function z = lt(a,b)
            z = addBinaryToBitOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.FactorFunctions.LessThan);
        end
        
        function z = gt(a,b)
            z = addBinaryToBitOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.FactorFunctions.GreaterThan);
        end
        
        function z = le(a,b)
            z = addBinaryToBitOperatorOverloadedFactor(b,a,com.analog.lyric.dimple.FactorFunctions.GreaterThan);
        end
        
        function z = ge(a,b)
            z = addBinaryToBitOperatorOverloadedFactor(b,a,com.analog.lyric.dimple.FactorFunctions.LessThan);
        end
        
        function z = abs(a)
            z = addUnaryOperatorOverloadedFactor(a,@abs,com.analog.lyric.dimple.FactorFunctions.Abs);
        end

        function z = log(a)
            z = addUnaryOperatorOverloadedFactor(a,@log,com.analog.lyric.dimple.FactorFunctions.Log);
        end

        function z = exp(a)
            z = addUnaryOperatorOverloadedFactor(a,@exp,com.analog.lyric.dimple.FactorFunctions.Exp);
        end

        function z = sqrt(a)
            z = addUnaryOperatorOverloadedFactor(a,@sqrt,com.analog.lyric.dimple.FactorFunctions.Sqrt);
        end

        function z = sin(a)
            z = addUnaryOperatorOverloadedFactor(a,@sin,com.analog.lyric.dimple.FactorFunctions.Sin);
        end

        function z = cos(a)
            z = addUnaryOperatorOverloadedFactor(a,@cos,com.analog.lyric.dimple.FactorFunctions.Cos);
        end

        function z = tan(a)
            z = addUnaryOperatorOverloadedFactor(a,@tan,com.analog.lyric.dimple.FactorFunctions.Tan);
        end

        function z = asin(a)
            z = addUnaryOperatorOverloadedFactor(a,@asin,com.analog.lyric.dimple.FactorFunctions.ASin);
        end

        function z = acos(a)
            z = addUnaryOperatorOverloadedFactor(a,@acos,com.analog.lyric.dimple.FactorFunctions.ACos);
        end

        function z = atan(a)
            z = addUnaryOperatorOverloadedFactor(a,@atan,com.analog.lyric.dimple.FactorFunctions.ATan);
        end

        function z = sinh(a)
            z = addUnaryOperatorOverloadedFactor(a,@sinh,com.analog.lyric.dimple.FactorFunctions.Sinh);
        end

        function z = cosh(a)
            z = addUnaryOperatorOverloadedFactor(a,@cosh,com.analog.lyric.dimple.FactorFunctions.Cosh);
        end

        function z = tanh(a)
            z = addUnaryOperatorOverloadedFactor(a,@tanh,com.analog.lyric.dimple.FactorFunctions.Tanh);
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
        
        function x = get.Value(obj)
            x = obj.getValue();
        end
        
        function set.Value(obj,value)
            obj.setValue(value);
        end

    end
    
    methods (Access=protected,Abstract=true)
        setInput(obj,value);
        x = getInput(obj);
        x = getBelief(obj);
        setValue(obj,value);
        x = getValue(obj);
    end
    
    methods (Access = protected)
        
        
        function z = addBinaryOperatorOverloadedFactor(a,b,operation,factor)
            vectorSize = size(a);
            if (size(b) ~= vectorSize)
                    error('Vector arguments to overloaded operators must have the same dimensions');
            end
            
            if (~isa(a.Domain, 'RealDomain') && ~isa(b.Domain, 'RealDomain'))
                
                domaina = a.Domain.Elements;
                domainb = b.Domain.Elements;
                zdomain = zeros(length(domaina)*length(domainb),1);
                
                curIndex = 1;
                
                if (isa(factor, 'com.analog.lyric.dimple.FactorFunctions.core.FactorFunction'))
                    for i = 1:length(domaina)
                        for j = 1:length(domainb)
                            zdomain(curIndex) = factor.getDeterministicFunctionValue({domaina{i} domainb{j}});
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
                vs = num2cell(vectorSize);
                z = Variable(zdomain, vs{:});
                
                %Eventually can build combo table, right now, this will do
                fg = getFactorGraph();
                fg.addFactorVectorized(factor,z,a,b);
            
            else
                
                % At least one of the input variables is real, so the
                % output variable must be real
                
                if (~isa(factor, 'com.analog.lyric.dimple.FactorFunctions.core.FactorFunction'))
                    error('Can only override faction functions for real-valued operators');
                end
                
                vs = num2cell(vectorSize);
                z = Real(vs{:});
                fg = getFactorGraph();
                fg.addFactorVectorized(factor,z,a,b);
                
            end
        end
        
        
        
        function z = addUnaryOperatorOverloadedFactor(a,operation,factor)
            vectorSize = size(a);
            if (~isa(a.Domain, 'RealDomain'))
                
                domaina = a.Domain.Elements;
                zdomain = zeros(length(domaina),1);
                
                curIndex = 1;
                
                if (isa(factor, 'com.analog.lyric.dimple.FactorFunctions.core.FactorFunction'))
                    for i = 1:length(domaina)
                        zdomain(curIndex) = factor.getDeterministicFunctionValue(domaina{i});
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
                vs = num2cell(vectorSize);
                z = Variable(zdomain, vs{:});
                
                %Eventually can build combo table, right now, this will do
                fg = getFactorGraph();
                fg.addFactorVectorized(factor,z,a);
            
            else
                
                % The input variables is real, so the
                % output variable must be real
                
                if (~isa(factor, 'com.analog.lyric.dimple.FactorFunctions.core.FactorFunction'))
                    error('Can only override faction functions for real-valued operators');
                end
                
                vs = num2cell(vectorSize);
                z = Real(vs{:});
                fg = getFactorGraph();
                fg.addFactorVectorized(factor,z,a);
                
            end
        end
        
        function z = addBinaryToBitOperatorOverloadedFactor(a,b,factor)
            vectorSize = size(a);
            if (size(b) ~= vectorSize)
                    error('Vector arguments to overloaded operators must have the same dimensions');
            end

            %znested = Bit();
            vs = num2cell(vectorSize);
            z = Bit(vs{:});
                
            %Eventually can build combo table, right now, this will do
            fg = getFactorGraph();
            fg.addFactorVectorized(factor,z,a,b);
        end
        
        function z = addUnaryToBitOperatorOverloadedFactor(a,factor)
            vectorSize = size(a);

            %znested = Bit();
            vs = num2cell(vectorSize);
            z = Bit(vs{:});
                
            %Eventually can build combo table, right now, this will do
            fg = getFactorGraph();
            fg.addFactorVectorized(factor,z,a);
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
