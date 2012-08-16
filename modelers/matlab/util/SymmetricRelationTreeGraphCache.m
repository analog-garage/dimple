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

classdef SymmetricRelationTreeGraphCache < FactorGraphCache
    properties
    end
    
    methods (Access=protected)
        function key = createKey(obj,deltaFunc,varConstructor,domain,n)
            
            tmp = cell(size(domain));
            for i = 1:numel(domain)
                tmp{i} = num2str(domain(i));
            end
            domain = strcat(tmp{:});
            
            key = sprintf('%s_%s_%s_%s',func2str(deltaFunc),func2str(varConstructor),domain,num2str(n));
        end
        
    end
    
    methods
        
        function [graph_out,external_vars] = createGraph(obj,deltaFunc,varConstructor,domain,n)
            
            
            % This code builds a factor graph that enforces an n-variable
            % deltaFunc equation.  The
            % factor graph that minimizes the maximum path length between any pair of
            % nodes is a modified nearly-balanced binary tree.  The modification can be
            % seen as follows: if we want to force 4 variables to graph to zero, then the
            % factor graph should have 5 nodes and look like this:
            %   *         *
            %     >x-*-x<
            %   *         *
            % where "*" is a variable node and "x" is a deltaFunc function node.
            
            % We can construct the resulting graphs fairly directly by using a modified
            % "Ahnentafel list" (cf Wikipedia) in much the same way that heaps
            % traditionally do.
            
            % These graphs have 2n-3 nodes (internal and external) for n>2.
            
            if n <= 1
                error ('Must have input argument > 1');
            end
            
            if n == 2
                external_vars=varConstructor(domain,n,1);
                graph_out=FactorGraph(external_vars);
                graph_out.addFactor(@equality,external_vars(:));
            else
                
                all_vars=varConstructor(domain,2*n-3,1);
                external_vars=all_vars(n-2:2*n-3);
                
                graph_out=FactorGraph(external_vars);
                graph_out.addFactor(deltaFunc,all_vars(1:3));
                for i=1:n-3
                    graph_out.addFactor(deltaFunc,all_vars(i),all_vars(2*i+2),all_vars(2*i+3));
                end
            end
        end
    end
end

