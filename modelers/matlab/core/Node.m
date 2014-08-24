%Node is the base class for Dimple model elements
%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012-2014 Analog Devices, Inc.
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

classdef Node < MatrixObject
    properties
        Name;
        Names;
        ExplicitName;
        QualifiedName;
        Label;
        QualifiedLabel;
        UUID;
        Ports;
        Solver;
        Score;
        InternalEnergy;
        BetheEntropy;
    end
    methods
        function obj = Node(vectorObject,VectorIndices)
            obj@MatrixObject(vectorObject,VectorIndices);
        end
        
        function ports = get.Ports(obj)
            var = obj.getSingleNode();
            
            ports = cell(var.getPorts(0));
            
            for i = 1:numel(ports)
                ports{i} = Port(ports{i});
            end
        end
        
        function set.Name(obj,name)
            var = obj.getSingleNode();
            var.setName(name);
        end
        function set.Label(obj,name)
            obj.VectorObject.setLabel(name);
        end
        
        function value = getOption(obj,option)
            %getOption Returns current value of specified option.
            %
            % getOption(name)
            %
            %    name - a string qualified option name of the form
            %           'Class.field' (e.g. 'SumProductOptions.damping').
            %           An instance of the Java IOptionKey class may also
            %           be used.
            %
            % If invoked on a Node matrix with more than one element, this
            % will return a cell array of the same dimensions containing
            % the corresponding option setting for each element.
            %
            % See also setOption, dimpleOptions
        	value = obj.VectorObject.getOptions(option,false);
            if numel(value) == 1
                value = value(1);
            else
                value = reshape(cell(value), size(obj.VectorIndices));
            end
        end
        	
        function unsetOption(obj,option)
            %unsetOption Unsets option on this object.
            %
            % unsetOption(name)
            %
            %    name - a string qualified option name of the form
            %           'Class.field' (e.g. 'SumProductOptions.damping').
            %           An instance of the Java IOptionKey class may also
            %           be used.
            %
            % See also setOption, dimpleOptions
        	obj.VectorObject.unsetOption(option);
        end
        
        function setOption(obj,option,value)
            %setOption Returns current value of specified option.
            %
            % setOption(name,value)
            %
            %    name  - a string qualified option name of the form
            %            'Class.field' (e.g. 'SumProductOptions.damping').
            %            An instance of the Java IOptionKey class may also
            %            be used.
            %
            %    value - value to give the option. May either be a single
            %            value or a cell array with dimensions matching
            %            the that of the Node matrix it is being invoked
            %            on.
            %
            % See also getOption, unsetOption, dimpleOptions
            if numel(value) == 1
                obj.VectorObject.setOption(option,value);
            else
                assert(iscell(value));
                obj.VectorObject.setOptions(option,value(:));
            end
        end
        
        function update(obj)
            obj.VectorObject.update();
        end
        
        function updateEdge(obj,portOrNode)
            var = obj.getSingleNode();
            
            if isa(portOrNode,'Node')
                var.updateEdge(portOrNode.VectorObject);
            else
                obj.VectorObject.updateEdge(portOrNode-1);
            end
        end
        function disp(obj)
            disp(obj.Label);
        end
        
        function invokeSolverMethod(obj,methodName,varargin)
            obj.VectorObject.invokeSolverMethod(methodName,varargin);
        end
        
        function retval = invokeSolverMethodWithReturnValue(obj,methodName,varargin)
            retval = cell(obj.VectorObject.invokeSolverMethodWithReturnValue(methodName,varargin));
            retval = MatrixObject.unpack(retval,obj.VectorIndices,true);
        end
        
        function names = get.Name(obj)
            names = obj.wrapNames(obj.VectorObject.getNames());
        end
        function names = get.QualifiedName(obj)
            names = obj.wrapNames(obj.VectorObject.getQualifiedNames());
        end
        function names = get.ExplicitName(obj)
            names = obj.wrapNames(obj.VectorObject.getExplicitNames());
        end
        function names = get.Label(obj)
            names = obj.wrapNames(obj.VectorObject.getNamesForPrint());
        end
        function names = get.QualifiedLabel(obj)
            names = obj.wrapNames(obj.VectorObject.getQualifiedNamesForPrint());
        end
        function uuids = get.UUID(obj)
            uuids = obj.wrapNames(obj.VectorObject.getUUIDs());
        end
        
        function portNum = getPortNum(obj,f)
            var = obj.getSingleNode();
            portNum = var.getPortNum(f.VectorObject)+1;
        end
        
        function s = get.Solver(obj)
            s = obj.getSolver();
        end
        
        function set.Solver(obj,solver)
            obj.setSolverInternal(solver);
        end
        
        
        function setNames(obj, baseName)
            obj.VectorObject.setNames(baseName);
        end
        
        function score = get.Score(obj)
            score = sum(obj.VectorObject.getScore());
        end
        
        function be = get.BetheEntropy(obj)
            be = obj.VectorObject.getBetheEntropy();
        end
        function ie = get.InternalEnergy(obj)
            ie = obj.VectorObject.getInternalEnergy();
        end
        
        function node = getSingleNode(obj)
            if obj.VectorObject.size() ~= 1
                error('Only one variable supported');
            end
            node = obj.VectorObject;
        end
        
    end
    
    methods(Access=protected)
        function setSolverInternal(obj,solver)
            error('not supported');
        end
        
        function s = getSolver(obj)
            indices = reshape(obj.VectorIndices,numel(obj.VectorIndices),1);
            tmp = cell(obj.VectorObject.getSolvers(indices));
            s = MatrixObject.unpack(tmp,obj.VectorIndices,true);
        end
        
        
        function names = wrapNames(obj, namesArray)
            names = cell(namesArray);
            
            
            if numel(names) == 1
                names = names{1};
            else
                names = reshape(names,size(obj.VectorIndices));
            end
        end
    end
end