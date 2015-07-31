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
        FixedValue;
        
        Condition;
        Prior;
        Belief;
        Value;
    end
    
    methods
        
        function obj = VariableBase(vectorObject,indices,domain)
            obj@Node(vectorObject,indices);
            if (nargin < 3 || isempty(domain)) && ismethod(vectorObject,'getDomain')
                domain = wrapProxyObject(vectorObject.getDomain());
            end
            obj.Domain = domain;
        end
        
        % FIXME: Mod should use a java function too, but need version that
        % works like MATLAB for negative numbers (or vice versa)
        function z= mod(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@mod,@(z,x,y) z == mod(x,y));
        end
        
        function z = plus(a,b)
            if (iscomplex(a) || iscomplex(b))
                z = addComplexBinaryOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.factorfunctions.ComplexSum);
            elseif (isrealjoint(a) || isrealjoint(b))
                z = addRealJointBinaryOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.factorfunctions.RealJointSum);
            else
                z = addBinaryOperatorOverloadedFactor(a,b,@plus,com.analog.lyric.dimple.factorfunctions.Sum);
            end
        end
        
        function z = minus(a,b)
            if (iscomplex(a) || iscomplex(b))
                z = addComplexBinaryOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.factorfunctions.ComplexSubtract);
            elseif (isrealjoint(a) || isrealjoint(b))
                z = addRealJointBinaryOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.factorfunctions.RealJointSubtract);
            else
                z = addBinaryOperatorOverloadedFactor(a,b,@minus,com.analog.lyric.dimple.factorfunctions.Subtract);
            end
        end
        
        function z = uminus(a)
            if (isa(a,'Complex')) % Must be a variable
                z = addComplexUnaryOperatorOverloadedFactor(a,com.analog.lyric.dimple.factorfunctions.ComplexNegate);
            elseif (isa(a,'RealJoint')) % Must be a variable
                z = addRealJointUnaryOperatorOverloadedFactor(a,com.analog.lyric.dimple.factorfunctions.RealJointNegate);
            else
                z = addUnaryOperatorOverloadedFactor(a,@uminus,com.analog.lyric.dimple.factorfunctions.Negate);
            end
        end
        
        function z = times(a,b)
            if (iscomplex(a) || iscomplex(b))
                z = addComplexBinaryOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.factorfunctions.ComplexProduct);
            else
                z = addBinaryOperatorOverloadedFactor(a,b,@times,com.analog.lyric.dimple.factorfunctions.Product);
            end
        end
        
        function z = mtimes(a,b)
            if (hasScalarElements(a) && hasScalarElements(b))
                % Either Discrete or Real variables or real constant arrays
                if (isscalar(a) || isscalar(b))                    
                    z = addBinaryOperatorOverloadedFactor(a,b,@mtimes,com.analog.lyric.dimple.factorfunctions.Product);
                elseif ((nnz(size(a)>1)==1) && (nnz(size(b)>1)==1))
                    z = VectorInnerProduct(a, b);
                elseif ((nnz(size(a)>1)==1) || (nnz(size(b)>1)==1))
                    z = MatrixVectorProduct(a, b);
                else
                    z = MatrixProduct(a, b);
                end
            elseif (isscalar(a) || isscalar(b))
                % At least one input is Complex or RealJoint
                if (iscomplex(a) || iscomplex(b))
                    z = addComplexBinaryOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.factorfunctions.ComplexProduct);
                elseif (isa(a,'RealJoint') || isa(b,'RealJoint'))
                    % At least one input is RealJoint and neither are Complex
                    if (nnz(size(a)>1)==2 || nnz(size(b)>1)==2)
                        z = MatrixVectorProduct(a, b);
                    else
                        z = VectorInnerProduct(a, b);
                    end
                else
                    error('Multiplication of this type not supported');
                end
            else
                error('Multiplication of this type not supported');
            end
        end
        
        function z = rdivide(a,b)
            if (iscomplex(a) || iscomplex(b))
                z = addComplexBinaryOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.factorfunctions.ComplexDivide);
            else
                z = addBinaryOperatorOverloadedFactor(a,b,@rdivide,com.analog.lyric.dimple.factorfunctions.Divide);
            end
        end
        
        function z = mrdivide(a,b)
            if (~isscalar(b)); error('Overloaded matrix division not currently supported. Use "./" for pointwise division'); end;
            if (iscomplex(a) || iscomplex(b))
                z = addComplexBinaryOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.factorfunctions.ComplexDivide);
            else
                z = addBinaryOperatorOverloadedFactor(a,b,@mrdivide,com.analog.lyric.dimple.factorfunctions.Divide);
            end
        end
        
        function z = ctranspose(a)
            if (isa(a,'Complex')) % Must be a variable
                z = addComplexUnaryOperatorOverloadedFactor(a,com.analog.lyric.dimple.factorfunctions.ComplexConjugate).';
            else
                z = a.'; % No need for a factor in this case, just transpose the matrix
            end
        end
        
        function z = power(a,b)
            if (isnumeric(b) && isscalar(b) && (b == 2))
                z = addUnaryOperatorOverloadedFactor(a,@(x)(x*x),com.analog.lyric.dimple.factorfunctions.Square);
            else
                z = addBinaryOperatorOverloadedFactor(a,b,@power,com.analog.lyric.dimple.factorfunctions.Power);
            end
        end
        
        function z = mpower(a,b)
            if (~isscalar(b)); error('Overloaded matrix power not currently supported. Use ".^" for pointwise power'); end;
            if (isnumeric(b) && (b == 2))
                z = addUnaryOperatorOverloadedFactor(a,@(x)(x.*x),com.analog.lyric.dimple.factorfunctions.Square);
            else
                z = addBinaryOperatorOverloadedFactor(a,b,@mpower,com.analog.lyric.dimple.factorfunctions.Power);
            end
        end
        
        function z = and(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@and,com.analog.lyric.dimple.factorfunctions.And);
        end
        
        function z = or(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@or,com.analog.lyric.dimple.factorfunctions.Or);
        end
        
        function z = xor(a,b)
            z = addBinaryOperatorOverloadedFactor(a,b,@xor,com.analog.lyric.dimple.factorfunctions.Xor);
        end
        
        function z = not(a)
            z = addUnaryToBitOperatorOverloadedFactor(a,com.analog.lyric.dimple.factorfunctions.Not);
        end
        
        function z = lt(a,b)
            z = addBinaryToBitOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.factorfunctions.LessThan);
        end
        
        function z = gt(a,b)
            z = addBinaryToBitOperatorOverloadedFactor(a,b,com.analog.lyric.dimple.factorfunctions.GreaterThan);
        end
        
        function z = le(a,b)
            z = addBinaryToBitOperatorOverloadedFactor(b,a,com.analog.lyric.dimple.factorfunctions.GreaterThan);
        end
        
        function z = ge(a,b)
            z = addBinaryToBitOperatorOverloadedFactor(b,a,com.analog.lyric.dimple.factorfunctions.LessThan);
        end
        
        function z = abs(a)
            if (isa(a,'Complex')) % Must be a variable
                z = addComplexToRealUnaryOperatorOverloadedFactor(a,com.analog.lyric.dimple.factorfunctions.ComplexAbs);
            else
                z = addUnaryOperatorOverloadedFactor(a,@abs,com.analog.lyric.dimple.factorfunctions.Abs);
            end
        end

        function z = log(a)
            z = addUnaryOperatorOverloadedFactor(a,@log,com.analog.lyric.dimple.factorfunctions.Log);
        end

        function z = exp(a)
            if (isa(a,'Complex')) % Must be a variable
                z = addComplexUnaryOperatorOverloadedFactor(a,com.analog.lyric.dimple.factorfunctions.ComplexExp);
            else
                z = addUnaryOperatorOverloadedFactor(a,@exp,com.analog.lyric.dimple.factorfunctions.Exp);
            end
        end

        function z = sqrt(a)
            z = addUnaryOperatorOverloadedFactor(a,@sqrt,com.analog.lyric.dimple.factorfunctions.Sqrt);
        end

        function z = sin(a)
            z = addUnaryOperatorOverloadedFactor(a,@sin,com.analog.lyric.dimple.factorfunctions.Sin);
        end

        function z = cos(a)
            z = addUnaryOperatorOverloadedFactor(a,@cos,com.analog.lyric.dimple.factorfunctions.Cos);
        end

        function z = tan(a)
            z = addUnaryOperatorOverloadedFactor(a,@tan,com.analog.lyric.dimple.factorfunctions.Tan);
        end

        function z = asin(a)
            z = addUnaryOperatorOverloadedFactor(a,@asin,com.analog.lyric.dimple.factorfunctions.ASin);
        end

        function z = acos(a)
            z = addUnaryOperatorOverloadedFactor(a,@acos,com.analog.lyric.dimple.factorfunctions.ACos);
        end

        function z = atan(a)
            z = addUnaryOperatorOverloadedFactor(a,@atan,com.analog.lyric.dimple.factorfunctions.ATan);
        end

        function z = sinh(a)
            z = addUnaryOperatorOverloadedFactor(a,@sinh,com.analog.lyric.dimple.factorfunctions.Sinh);
        end

        function z = cosh(a)
            z = addUnaryOperatorOverloadedFactor(a,@cosh,com.analog.lyric.dimple.factorfunctions.Cosh);
        end

        function z = tanh(a)
            z = addUnaryOperatorOverloadedFactor(a,@tanh,com.analog.lyric.dimple.factorfunctions.Tanh);
        end

        function x = get.Domain(obj)
            x = obj.Domain;
        end
        
        
        function guess = get.Guess(obj)
            guess = obj.getGuess();
        end
        
        function set.Guess(obj,guess)
            obj.setGuess(guess);
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

        function condition = get.Condition(obj)
            condition = obj.getCondition();
        end
        
        function set.Condition(obj,value)
            obj.setCondition(value);
        end
        
        function prior = get.Prior(obj)
            prior = obj.getPrior();
        end
        
        function set.Prior(obj,value)
            obj.setPrior(value);
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
        
        function set.Belief(obj,value)
            error('Cannot set the Belief property');
        end
        
        function x = get.Value(obj)
            x = obj.getValue();
        end
        
        function set.Value(obj,value)
            error('Cannot set the Value property (to set a fixed value for a variable, use .FixedValue)');
        end
        
        function set.FixedValue(obj,value)
            obj.setFixedValue(value);
        end

        function x = get.FixedValue(obj)
            x = obj.getFixedValue();
        end
        
        function x = hasFixedValue(obj)
           varids = reshape(obj.VectorIndices,numel(obj.VectorIndices),1);
           hasFixedValue = obj.VectorObject.hasFixedValue(varids);
           x = MatrixObject.unpack(hasFixedValue,obj.VectorIndices);
        end

    end
    
    methods (Access=protected,Abstract=true)
        setInput(obj,value);
        x = getInput(obj);
        
        setFixedValue(obj,value);
        x = getFixedValue(obj);

        x = getBelief(obj);
        x = getValue(obj);
    end
    
    methods (Access = protected)
        
        function condition = getCondition(obj)
            condition = wrapProxyObject(cell(obj.VectorObject.getCondition()));
            condition = obj.unpack(condition, obj.VectorIndices);
        end
        
        function setCondition(obj,conditions)
            input = obj.pack(conditions, obj.VectorIndices);
            if ~iscell(input) && ~isfloat(input)
                input = num2cell(input);
            else
                input = unwrapProxyObject(input);
            end
            obj.VectorObject.setCondition(input);
        end
        
        function prior = getPrior(obj)
            prior = wrapProxyObject(cell(obj.VectorObject.getPrior()));
            prior = obj.unpack(prior, obj.VectorIndices);
        end
        
        function setPrior(obj,priors)
            input = obj.pack(priors, obj.VectorIndices);
            if ~iscell(input) && ~isfloat(input)
                input = num2cell(input);
            else
                input = unwrapProxyObject(input);
            end
            obj.VectorObject.setPrior(input);
        end
        
        function guess = getGuess(obj)
            tmp = cell(obj.VectorObject.getGuess());
            guess = obj.unpack(tmp,obj.VectorIndices);
        end
        
        function setGuess(obj,guess)
            input = obj.pack(guess,obj.VectorIndices);
            if ~iscell(input) && ~isfloat(input)
                input = num2cell(input);
            end
            obj.VectorObject.setGuess(input);
        end

        
        function z = addBinaryOperatorOverloadedFactor(a,b,operation,factor)
            % Sizes should be the same, or one of them should be length 1
            if (~((length(a) == length(b)) || (length(a) == 1) || (length(b) == 1)))
                error('Mismatch in dimension of input arguments');
            end
            
            % Use the largest size (one of them could be length 1)
            if (length(a) > length(b))
                vectorSize = size(a);
            else
                vectorSize = size(b);
            end

            % Check for constant inputs
            aConstant = false;
            bConstant = false;
            if (~isa(a, 'VariableBase')); aConstant = true; end;
            if (~isa(b, 'VariableBase')); bConstant = true; end;

            
            if ((aConstant || ~isa(a.Domain, 'RealDomain')) && (bConstant || ~isa(b.Domain, 'RealDomain')))
                
                % Determine the output domain from the inputs; account for constant inputs
                if (~aConstant)
                    domaina = a.Domain.Elements;
                else
                    domaina = num2cell(sort(unique(a)));
                end
                if (~bConstant)
                    domainb = b.Domain.Elements;
                else
                    domainb = num2cell(sort(unique(b)));
                end
                zdomain = zeros(length(domaina)*length(domainb),1);
                
                curIndex = 1;
                
                if (isa(factor, 'com.analog.lyric.dimple.factorfunctions.core.FactorFunction'))
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
                z = Discrete(zdomain, vs{:});
                
                %Eventually can build combo table, right now, this will do
                fg = getFactorGraph();
                fg.addFactorVectorized(factor,z,a,b);
            
            else
                
                % At least one of the input variables is real, so the
                % output variable must be real
                
                if (~isa(factor, 'com.analog.lyric.dimple.factorfunctions.core.FactorFunction'))
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
                
                if (isa(factor, 'com.analog.lyric.dimple.factorfunctions.core.FactorFunction'))
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
                z = Discrete(zdomain, vs{:});
                
                %Eventually can build combo table, right now, this will do
                fg = getFactorGraph();
                fg.addFactorVectorized(factor,z,a);
            
            else
                
                % The input variables is real, so the
                % output variable must be real
                
                if (~isa(factor, 'com.analog.lyric.dimple.factorfunctions.core.FactorFunction'))
                    error('Can only override faction functions for real-valued operators');
                end
                
                vs = num2cell(vectorSize);
                z = Real(vs{:});
                fg = getFactorGraph();
                fg.addFactorVectorized(factor,z,a);
                
            end
        end
        
        function z = addBinaryToBitOperatorOverloadedFactor(a,b,factor)
            % Sizes should be the same, or one of them should be length 1
            if (~((length(a) == length(b)) || (length(a) == 1) || (length(b) == 1)))
                error('Mismatch in dimension of input arguments');
            end
            
            % Use the largest size (one of them could be length 1)
            if (length(a) > length(b))
                vectorSize = size(a);
            else
                vectorSize = size(b);
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
        

        function z = addComplexBinaryOperatorOverloadedFactor(a,b,factor)
            % Sizes should be the same, or one of them should be length 1
            if (~((length(a) == length(b)) || (length(a) == 1) || (length(b) == 1)))
                error('Mismatch in dimension of input arguments');
            end
            
            % Use the largest size (one of them could be length 1)
            if (length(a) > length(b))
                vectorSize = size(a);
            else
                vectorSize = size(b);
            end

            % Check for constant inputs
            if (~isa(a, 'VariableBase'));
                a = [real(a) imag(a)];
            end;
            if (~isa(b, 'VariableBase'));
                b = [real(b) imag(b)];
            end;

            if (~isa(factor, 'com.analog.lyric.dimple.factorfunctions.core.FactorFunction'))
                error('Can only override faction functions for complex-valued operators');
            end
            
            % output variable must be Complex
            vs = num2cell(vectorSize);
            z = Complex(vs{:});
            fg = getFactorGraph();
            fg.addFactorVectorized(factor,z,a,b);
                
        end
        
        
        
        function z = addComplexUnaryOperatorOverloadedFactor(a,factor)
            vectorSize = size(a);

            if (~isa(factor, 'com.analog.lyric.dimple.factorfunctions.core.FactorFunction'))
                error('Can only override faction functions for complex-valued operators');
            end
            
            % output variable must be Complex
            vs = num2cell(vectorSize);
            z = Complex(vs{:});
            fg = getFactorGraph();
            fg.addFactorVectorized(factor,z,a);
                
        end
        
        
        function z = addComplexToRealUnaryOperatorOverloadedFactor(a,factor)
            vectorSize = size(a);

            if (~isa(factor, 'com.analog.lyric.dimple.factorfunctions.core.FactorFunction'))
                error('Can only override faction functions for complex-valued operators');
            end
            
            % output variable must be Real
            vs = num2cell(vectorSize);
            z = Real(vs{:});
            fg = getFactorGraph();
            fg.addFactorVectorized(factor,z,a);
                
        end
        
        
        function z = addRealJointBinaryOperatorOverloadedFactor(a,b,factor)
            % Sizes should be the same
            if (isa(a, 'VariableBase'))
                alength = a.Domain.NumElements;
            else
                alength = length(a);
            end;
            if (isa(b, 'VariableBase'))
                blength = b.Domain.NumElements;
            else
                blength = length(b);
            end;
            if (~((alength == blength) || (alength == 1) || (blength == 1)))
                error('Mismatch in dimension of input arguments');
            end
            
            % Use the largest size (one of them could be length 1)
            jointLength = max(alength, blength);

            % Allow constant scalars
            if (alength == 1 && isnumeric(a))
                a = repmat(a, 1, jointLength);
            end
            if (blength == 1 && isnumeric(b))
                b = repmat(b, 1, jointLength);
            end

            if (~isa(factor, 'com.analog.lyric.dimple.factorfunctions.core.FactorFunction'))
                error('Can only override faction functions for real-valued operators');
            end
            
            % Output variable must be RealJoint
            z = RealJoint(jointLength);
            fg = getFactorGraph();
            fg.addFactor(factor,z,a,b);
                
        end
        
        
        
        function z = addRealJointUnaryOperatorOverloadedFactor(a,factor)
            if (isa(a, 'VariableBase'))
                alength = a.Domain.NumElements;
            else
                alength = length(a);
            end;

            
            if (~isa(factor, 'com.analog.lyric.dimple.factorfunctions.core.FactorFunction'))
                error('Can only override faction functions for real-valued operators');
            end
            
            % output variable must be RealJoint
            z = RealJoint(alength);
            fg = getFactorGraph();
            fg.addFactor(factor,z,a);
                
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
    
    methods (Static)
        
        % Create a new variable of the same type as another variable
        function var = createFrom(other)
            otherSize = num2cell(size(other));
            if isa(other, 'Bit')
                var = Bit(otherSize{:});
            elseif isa(other, 'FiniteFieldVariable')
                var = FiniteFieldVariable(other.Domain.PrimitivePolynomial, otherSize{:});
            elseif isa(other, 'Discrete')
                var = Discrete(other.Domain.Elements, otherSize{:});
            elseif isa(other, 'Real')
                var = Real(other.Domain, otherSize{:});
            elseif isa(other, 'Complex')
                var = Complex(other.Domain, otherSize{:});
            elseif isa(other, 'RealJoint')
                var = RealJoint(other.Domain.RealDomains, otherSize{:});
            else
                error('Invalid variable type');
            end
        end
        
    end
    
end
