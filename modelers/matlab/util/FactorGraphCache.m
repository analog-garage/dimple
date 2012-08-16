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

classdef FactorGraphCache < handle
    %FACTORGRAPHCACHE An abstract class that provies graph caching.
    %   This class is meant to provide support for caching Factor Graphs
    %   based on some set of keys.  Users should provide an implementation
    %   for createGraph and, in most cases, should probably override
    %   createKey
    
    properties (Access=private)
        Map;        
    end
    properties
        NumGraphs;
    end
    
    methods
        
        function obj = FactorGraphCache()
           obj.Map = containers.Map(); 
        end
        
        function count = get.NumGraphs(obj)
            count = double(obj.Map.Count);
        end
        
        function [graph,vars] = get(obj,varargin)
            key = obj.createKey(varargin{:});
            
            if ~obj.Map.isKey(key)
                [graph,vars] = obj.createGraph(varargin{:});
                obj.Map(key) = {graph,vars};
            end
            
            result = obj.Map(key);
            graph = result{1};
            vars = result{2};
        end
    end
    
    methods (Access=protected)
        function key = createKey(obj,varargin)
            for i = 1:numel(varargin)
                if isnumeric(varargin{i})
                    varargin{i} = sprintf('%s_',num2str(varargin{i}));
                else
                    varargin{i} = sprintf('%s_',varargin{i});
                end
            end
            key = strcat(varargin{:});            
        end
    end
    
    methods (Abstract)
        [graph,vars] = createGraph(obj,varargin);
    end
    
end

