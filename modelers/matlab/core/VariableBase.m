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

classdef VariableBase < handle
    properties (Access=public)
        Indices;
        VarMat;
    end
    properties (Abstract = true)
        Input;
        Belief;
    end
    properties
        Domain;
        Value;
        Factors;
        FactorsTop;
        FactorsFlat;
        Solver;
        Name;
        Names;
        ExplicitName;
        QualifiedName;
        Label;
        QualifiedLabel;
        UUID;
        Ports;
        Energy;
        Guess;
    end
    
    methods
        
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
        
        function disp(obj)
            disp(obj.Label);
        end
        
        function update(obj)
            obj.VarMat.update();
        end
        
        function setNames(obj, baseName)
            obj.VarMat.setNames(baseName);
        end
        
        function updateEdge(obj,portOrFactor)
            var = obj.getSingleVariable();
            
            if isa(portOrFactor,'Factor')
                portNum = var.getPortNum(portOrFactor.IFactor);
                var.updateEdge(portNum);
            else
                obj.VarMat.updateEdge(portOrFactor-1);
            end
        end
        
        function ports = get.Ports(obj)
            var = obj.getSingleVariable();
            
            ports = cell(var.getPorts());
            
            for i = 1:numel(ports)
                ports{i} = Port(ports{i});
            end
        end
        
        
        function x = length(obj)
            x = length(obj.Indices);
        end
        
        function x = size(obj,varargin)
            x = size(obj.Indices,varargin{:});
        end
        
        function set.Name(obj,name)
            var = obj.getSingleVariable();
            var.setName(name);
        end
        function set.Label(obj,name)
            obj.VarMat.setLabel(name);
        end

        
        function names = get.Name(obj)
            names = obj.wrapNames(obj.VarMat.getNames());
        end
        function names = get.QualifiedName(obj)
            names = obj.wrapNames(obj.VarMat.getQualifiedNames());
        end
        function names = get.ExplicitName(obj)
            names = obj.wrapNames(obj.VarMat.getExplicitNames());
        end
        function names = get.Label(obj)
            names = obj.wrapNames(obj.VarMat.getNamesForPrint());
        end
        function names = get.QualifiedLabel(obj)
            names = obj.wrapNames(obj.VarMat.getQualifiedNamesForPrint());
        end
        function uuids = get.UUID(obj)
            uuids = obj.wrapNames(obj.VarMat.getUUIDs());
        end
        
        function portNum = getPortNum(obj,f)
            var = obj.getSingleVariable();
            portNum = var.getPortNum(f.IFactor)+1;
        end
        
        function var = getSingleVariable(obj)
            if obj.VarMat.size() ~= 1
                error('Only one variable supported');
            end
            var = obj.VarMat.getVariable(0);
        end
        
        function x = end(obj,k,n)
            if n == 1
                x = numel(obj.Indices);
            else
                x = size(obj.Indices,k);
            end
        end
        
        function x = get.Domain(obj)
            x = obj.Domain;
        end
        
        function s = get.Solver(obj)
            solvers = cell(size(obj.Indices));
            for i = 1:numel(obj.Indices)
                var = obj.VarMat.getVariable(obj.Indices(i));
                solvers{i} = var.getSolver();
            end
            s = solvers;
            if numel(s) == 1
                s = s{1};
            end
        end
        
        function x = get.Value(obj)
            x = obj.getValue();
        end
        
        function retval = eq(a,b)
            %TODO: Behave like matrix equal
            retval = a.isequal(b);
        end
        
        function retval = isequal(a,b)
            if ~isa(b,'VariableBase')
                retval = false;
            else
                retval = isequal(a.Indices,b.Indices) && isequal(a.VarMat.getIds(),b.VarMat.getIds());
            end
        end
        
        function x = ctranspose(a)
            x = transpose(a);
        end
        
        function x = horzcat(varargin)
            
            
            x = VariableBase.docat(@horzcat,varargin{:});
        end
        
        function x = vertcat(varargin)

            x = VariableBase.docat(@vertcat,varargin{:});
        end
        function x = reshape(obj,varargin)
            indices = reshape(obj.Indices,varargin{:});
            x = obj.createVariable(obj.Domain,obj.VarMat,indices);
        end
        
        function x = fliplr(obj)
            indices = fliplr(obj.Indices);
            x = obj.createVariableFromReorderedIndices(indices);
        end
        
        function x = flipud(obj)
            indices = flipud(obj.Indices);
            x = obj.createVariableFromReorderedIndices(indices);
        end
        
        function x = transpose(a)
            x = a.createVariable(a.Domain,a.VarMat,a.Indices');
        end
        
        function x = f(a,b)
            x = 1+2;
        end
        
        function energy = getEnergy(obj)
            energy = sum(obj.VarMat.getEnergy());
        end
        
        function guess = getGuess(obj)
            guess = wrapNames(obj.VarMat.getGuess());
        end
        
        %{
        function retval = split(obj,varargin)
            if (obj.VarMat.size() ~= 1)
                error('only support with one variable for now');
            end
            
            
            v = obj.VarMat.getVariable(0);
            ifactors = cell(size(varargin));
            
            for i = 1:numel(varargin)
                tmp = varargin{i};
                ifactors{i} = tmp.IFactor;
            end
            
            varmat = v.split(ifactors);
            
            retval = obj.createVariable(obj.Domain,varmat,0);
            
        end
        %}
        
        function b = subsref(a,s)
            b = [];
            dontdescend = 0;
            switch s(1).type
                case '()'
                    %TODO: put in subroutine?
                    ind = a.Indices(s(1).subs{:});
                    b = a.createVariableFromReorderedIndices(ind);
                case '.'
                    switch s(1).subs
                        case 'Indices'
                            b = a.Indices;
                        case 'Domain'
                            b = a.getDomain();
                        case 'Input'
                            b = a.getInput();
                        case 'Belief'
                            b = a.getBelief();
                        case 'Value'
                            b = a.getValue();
                        case 'VarMat'
                            b = a.VarMat;
                        %case 'split'
                        %    b = a.split(s(2).subs{:});
                        %   dontdescend = 1;
                        case 'createVariable'
                            b = a.createVariable(s(2).subs{:});
                            dontdescend = 1;
                        case 'getFactors'
                            b = a.getFactors(s(2).subs{:});
                            dontdescend = 1;
                        case 'Factors'
                            b = a.getFactors(intmax);
                        case 'FactorsFlat'
                            b = a.getFactors(intmax);
                        case 'FactorsTop'
                            b = a.getFactors(0);
                        case 'Solver'
                            b = a.getSolver();
                        case 'Name'
                            b = a.getName();
                        case 'QualifiedName'
                            b = a.getQualifiedName();
                        case 'ExplicitName'
                            b = a.getExplicitName();
                        case 'Label'
                            b = a.getLabel();
                        case 'QualifiedLabel'
                            b = a.getQualifiedLabel();
                        case 'UUID'
                            b = a.getUUID();
                            %b = a.VarMat.getUUIDs();
                        case 'Ports'
                            b = a.getPorts();
                        case 'update'
                            a.update();
                        case 'updateEdge'
                            a.updateEdge(s(2).subs{1});
                            dontdescend = 1;
                        case 'setNames'
                            a.setNames(s(2).subs{1});
                            dontdescend = 1;
                        case 'getSingleVariable'
                            b = a.getSingleVariable();
                        case 'getPortNum'
                            b = a.getPortNum(s(2).subs{1});
                            dontdescend = 1;
                        case 'Energy'
                            b = a.getEnergy();
                        case 'Guess'
                            b = a.getGuess();
                        otherwise
                            error(['Unrecognized method: ' s(1).subs]);
                    end
                case '{}'
                    error('brackets are not supported');
                otherwise
                    error('Specify value for x as obj(x)')
            end
            
            if (length(s) > 1 && dontdescend == 0)
                try
                    
                    b = subsref(b,s(2:end));
                catch E
                    %We have to catch any exceptions because, in the
                    %case where the java method does not return a value
                    %an exception will be thrown.  We want to ignore
                    %that exception.  The performance of this is
                    %probably not great since we are performing a
                    %string comparison.
                    %For better performance, retrieve the java object
                    %first and then call the method.
                    if ~ isequal(E.identifier,'MATLAB:unassignedOutputs')
                        throw(E);
                    end
                end
                
            end
        end
        
        function a = subsasgn(a,s,b)
            
            if numel(s) > 1
                tmp = subsref(a,s(1));
                subsasgn(tmp,s(2:end),b);
            else
                
                switch s(1).type
                    case '.'
                        switch s(1).subs
                            case 'Input'
                                a.setInput(b);
                            case 'Name'
                                a.setName(b);
                            case 'Label'
                                a.setLabel(b);
                            case 'Guess'
                                a.setGuess(b);
                            otherwise
                                error(['method: ' s.subs ' does not exist.']);
                        end
                    case '()'
                        inds = a.Indices(s(1).subs{:});
                        a.createVariable(a.Domain,a.VarMat,inds);
                        
                    otherwise
                        error('subassign not handled');
                end
            end
            
        end
        
    end
    
    methods (Access = protected)
        
        function x = createVariableFromReorderedIndices(obj,indices)
            varids = reshape(indices,numel(indices),1);
            varmat = obj.VarMat.getSlice(varids);
            indices = reshape(0:(numel(varids)-1),size(indices));
            x = obj.createVariable(obj.Domain,varmat,indices);
        end
        
        
        function z = addOperatorOverloadedFactor(a,b,operation,factor)
            %TODO: make sure a and b are not vectors
            
            %Create variables with domains of a and b
            %TODO: eventually require a copy
            %aprime = Variable(a.Domain);
            %bprime = Variable(b.Domain);
            
            %figure out domain
            %TODO: for now only support discrete
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
            
            %op = Variable({operation});
            %factor = @(z,x,y,op) z==op(x,y);
            %fg.addFactor(factor,z,a,b,op);
            
            fg.addFactor(factor,z,a,b);
            
            %TODO: can't do this because the function name ends up being
            %the same
            %factor = @(z,x,y) z == operation(x,y);
            %fg.addFactor(factor,z,a,b);
            
        end
        
        
        function x = checkMatchAndCreateVariable(obj,args,v_all)
            v = args{1};
            for i = 2:length(args)
                if v.Solver ~= args{i}.Solver
                    error('Solvers dont match');
                end
            end
            x = v.createVariable(v_all);
        end
        
        function varids = getVarIds(obj)
            v = obj.V;
            varids = zeros(numel(v),1);
            for i = 1:numel(v)
                varids(i) = v(i).VarId;
            end
        end
        
        function setGuess(obj,guess)
            if obj.VarMat.size() > 1
                error('only support one variable right now');
            end
            obj.VarMat.setGuess(guess);
        end
        
        
        function name = getName(obj)
            name = obj.Name;
        end
        function name = getQualifiedName(obj)
            name = obj.QualifiedName;
        end
        function name = getExplicitName(obj)
            name = obj.ExplicitName;
        end
        function name = getLabel(obj)
            name = obj.Label;
        end
        function name = getQualifiedLabel(obj)
            name = obj.QualifiedLabel;
        end
        function name = getUUID(obj)
            name = obj.UUID;
        end
        
        function ports = getPorts(obj)
            ports = obj.Ports;
        end
        
        function setName(obj,name)
            obj.Name = name;
        end
        function setLabel(obj,name)
            obj.Label = name;
        end
        
        function b = getDomain(obj)
            b = obj.Domain;
        end
        
        function s = getSolver(obj)
            s = obj.Solver;
        end
                
        function factors = getFactors(obj,relativeNestingDepth)
            tmp = cell(obj.VarMat.getFactors(relativeNestingDepth));
            factors = cell(size(tmp));
            for i = 1:length(factors)
                if tmp{i}.isGraph()
                    factors{i} = FactorGraph('igraph',tmp{i});
                elseif tmp{i}.isDiscrete()
                    factors{i} = DiscreteFactor(tmp{i});
                else
                    factors{i} = Factor(tmp{i});
                end
            end
        end
        
        
        function names = wrapNames(obj, namesArray)
            names = cell(namesArray);
            
            
            if numel(names) == 1
                names = names{1};
            else
                names = reshape(names,size(obj.Indices));
            end
        end
    end
    
    methods (Abstract, Access = public)
        createVariable(obj,domain,varMat,indices)
    end
    
    %Abstract methods that must be implemented in a derived class
    methods (Abstract, Access = protected)
        setInput(obj,b)
        getInput(obj)
        getBelief(obj)
        getValue(obj)
    end
    
    methods (Static)
        function x = docat(catmethod,varargin)
            
            for i = 1:length(varargin)
               if ~isa(varargin{i},'VariableBase')
                   error('horzcat only supported with Variables');
               end
            end

            
            indices_all = [];
            var_mat_indices_all = [];
            
            varMats = cell(size(varargin));
            
            firstDomain = varargin{1}.Domain;
            
            %TODO: check domains match
            for i = 1:length(varargin)
                
                if ~isequal(varargin{i}.Domain,firstDomain)
                    error('Cannot concatenate variables with domains that dont match');
                end
                
                indices = varargin{i}.Indices;
                varMats{i} = varargin{i}.VarMat;
                var_mat_indices = ones(size(indices))*i-1;
                indices_all = catmethod(indices_all,indices);
                var_mat_indices_all = catmethod(var_mat_indices_all,var_mat_indices);
            end
            
            one_d_indices_all = reshape(indices_all,numel(indices_all),1);
            one_d_var_mat_indices_all = reshape(var_mat_indices_all,numel(var_mat_indices_all),1);
            
            varMat = varargin{1}.VarMat.concat(varMats,one_d_var_mat_indices_all,one_d_indices_all);
            indices = 0:numel(indices_all)-1;
            indices = reshape(indices,size(indices_all));
            
            x = varargin{1}.createVariable(varargin{1}.Domain,varMat,indices);
        end
        
    end
    
end
