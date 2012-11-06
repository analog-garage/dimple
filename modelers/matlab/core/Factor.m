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

classdef Factor < handle
    
    properties
        IFactor;
        Name;
        ExplicitName;
        QualifiedName;
        Label;
        QualifiedLabel;
        UUID;
        Variables;
        Solver;
        Ports;
        Score;
        InternalEnergy;
        BetheEntropy;
        DirectedTo;
    end
    
    methods
        function obj = Factor(ifactor)
            obj.IFactor = ifactor;
        end
        
        function ports = get.Ports(obj)
            ports = cell(obj.IFactor.getPorts());
            
            for i = 1:length(ports)
                ports{i} = Port(ports{i});
            end
        end
        
        function disp(obj)
           disp(obj.Label); 
        end
        
        function solver = get.Solver(obj)
            solver = obj.IFactor.getSolver();
        end
        
        function update(obj)
            obj.IFactor.update();
        end
        
        function score = get.Score(obj)
            score = obj.IFactor.getScore();
        end
        
        function be = get.BetheEntropy(obj)
            be = obj.IFactor.getBetheEntropy();
        end
        
        function ie = get.InternalEnergy(obj)
            ie = obj.IFactor.getInternalEnergy();
        end
        
        function updateEdge(obj,portNumOrPort)
            if isa(portNumOrPort,'VariableBase')
                var = portNumOrPort.getSingleNode();
                portNum = obj.IFactor.getPortNum(var);
                obj.IFactor.updateEdge(portNum);
            else
                obj.IFactor.updateEdge(portNumOrPort-1);
            end
        end
        
        function retval = eq(a,b)
            retval = a.equals(b);
        end
        
        function retval = equals(a,b)
            retval = a.IFactor.getId() == b.IFactor.getId();
        end
        
        function name = get.Name(obj)
            name = char(obj.IFactor.getName());
        end
        
        function name = get.ExplicitName(obj)
            name = char(obj.IFactor.getExplicitName());
        end

        
        function set.DirectedTo(obj,variables)
           if ~iscell(variables)
               variables = {variables};
           end
           for i = 1:length(variables)
               variables{i} = variables{i}.VectorObject;
           end
           obj.IFactor.setDirectedTo(variables);
        end
        function variables = get.DirectedTo(obj)
            pvarvector = obj.IFactor.getDirectedToVariables();
            variables = wrapProxyObject(pvarvector);
        end
        
        function name = get.QualifiedName(obj)
            name = char(obj.IFactor.getQualifiedName());
        end
        
        function name = get.Label(obj)
            name = char(obj.IFactor.getLabel());
        end
        function name = get.QualifiedLabel(obj)
            name = char(obj.IFactor.getQualifiedLabel());
        end
        function uuid = get.UUID(obj)
            uuid = obj.IFactor.getUUID();
        end
        
        function set.Name(obj,name)
            obj.IFactor.setName(name);
        end
        function set.Label(obj,name)
            obj.IFactor.setLabel(name);
        end
        
        
        function portNum = getPortNum(obj,var)
            var = var.getSingleNode();
            portNum = obj.IFactor.getPortNum(var)+1;
        end
        
        
        %get the combination table associated with this
        function variables = get.Variables(obj)
            vars = obj.IFactor.getConnectedVariableVector();
            
            variables = cell(vars.size(),1);
            
            for i = 1:length(variables)
                var = vars.getSlice(i-1);
                domain = cell(var.getDomain());
                indices = 0;
                variables{i} = Variable(domain,'existing',var,indices);
            end
            
            
        end
    end
    
end

