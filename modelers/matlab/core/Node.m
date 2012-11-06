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
        function obj = Node(vectorObject,indices)
            obj@MatrixObject(vectorObject,indices);
        end
        
        function ports = get.Ports(obj)
            var = obj.getSingleNode();
            
            ports = cell(var.getPorts());
            
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
            portNum = var.getPortNum(f.IFactor)+1;
        end
        
        
        
        function s = get.Solver(obj)
            solvers = cell(size(obj.Indices));
            for i = 1:numel(obj.Indices)
                node = obj.VectorObject.getNode(obj.Indices(i));
                solvers{i} = node.getSolver();
            end
            s = solvers;
            if numel(s) == 1
                s = s{1};
            end
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
            node = obj.VectorObject.getNode(0);
        end
        
    end
    
    methods(Access=protected)
        
        function names = wrapNames(obj, namesArray)
            names = cell(namesArray);
            
            
            if numel(names) == 1
                names = names{1};
            else
                names = reshape(names,size(obj.Indices));
            end
        end
    end
end