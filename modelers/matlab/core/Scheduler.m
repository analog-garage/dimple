%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2014-2015 Analog Devices, Inc.
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
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

classdef Scheduler < handle
    
    properties
        % Underlying Java scheduler object.
        IScheduler;
        
        % Graph instance associated with this Scheduler, if any.
        Graph;
    end
    
    methods(Access=public)
        
        function obj = Scheduler(arg1, varargin)
            % Holds scheduler instance for a graph.
            %
            %   Scheduler(graph, scheduler)
            %   Scheduler(graph)
            %   Scheduler(scheduler)
            %
            % 'scheduler' argument can either be a string naming the
            % scheduler class or a Java PScheduler instance.
            %
            % If graph argument is provided, then the addBlockScheduleEntry
            % method will accept variables as arguments.
            narginchk(1,2);
            
            if (nargin == 2)
                validateattributes(arg1, {'FactorGraph'}, {'scalar'});
                obj.Graph = arg1;
                arg2 = varargin{2};
                validateattributes(arg2, {'char', 'com.analog.lyric.dimple.matlabproxy.PScheduler'}, {});
                if (ischar(arg2))
                    modeler = getModeler;
                    obj.IScheduler = modeler.createScheduler(arg2);
                else
                    obj.IScheduler = arg2;
                end
            else
                validateattributes(arg1, ...
                    {'char', 'com.analog.lyric.dimple.matlabproxy.PScheduler', 'FactorGraph'}, ...
                    {});
                if isa(arg1, 'com.analog.lyric.dimple.matlabproxy.PScheduler')
                    obj.IScheduler = arg1;
                elseif ischar(arg1)
                    modeler = getModeler;
                    obj.IScheduler = modeler.createScheduler(arg1);
                elseif isa(arg1,'FactorGraph')
                    % Deprecated syntax - grab current scheduler from graph
                    obj.Graph = arg1;
                    obj.IScheduler = arg1.VectorObject.getScheduler();
                end
            end
        end
        
        function disp(obj)
            disp(obj.IScheduler.getDelegate().getClass().getSimpleName());
        end
        
        function addBlockScheduleEntry(obj, varargin)
            % Deprecated as of release 0.08.
            %
            % Use addBlockWithReplacement instead.
            addBlockWithReplacement(obj, varargin{:});
        end
        
        function addBlockWithReplacement(obj, arg1, varargin)
            % Adds a block schedule entry replacing existing node and edge entries
            if (isempty(varargin))
                validateattributes(arg1, {'cell'}, {});
                updater = arg1{1};
                args = arg1(2:end);
            else
                updater = arg1;
                args = varargin;
            end
                
            validateattributes(updater, {'com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater'}, {});
            
            block = [];
            if (numel(args) == 1)
                block = args{1};
                validateattributes(block, {'VariableBlock'}, {});
            else
                cellfun(@(n) validateattributes(n, {'VariableBase'},{}), args);
                if isa(obj.Graph, 'FactorGraph')
                    block = obj.Graph.addVariableBlock(args{:});
                end
            end
            
            if (isa(block, 'VariableBase'))
                obj.IScheduler.addBlockScheduleEntry(updater, block.VectorObject);
            else
                vars = cellfun(@(v) {v.VectorObject}, args);
                obj.IScheduler.addBlockScheduleEntry(updater, vars);
            end
        end
        
        function addBlockScheduleEntries(obj, varargin)
            % Deprecated as of release 0.08.
            %
            % Use addBlocksWithReplacement instead.
            addBlocksWithReplacement(obj, varargin{:});
        end
        
        function addBlocksWithReplacement(obj, varargin)
            numEntries = numel(varargin);
            
            for i=1:numEntries
                args = varargin{i};
                obj.addBlockScheduleEntry(args{:});
            end
        end

    end

    
end
