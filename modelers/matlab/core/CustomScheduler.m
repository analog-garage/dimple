%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2015 Analog Devices, Inc.
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

classdef CustomScheduler < Scheduler
    
    methods
        function obj = CustomScheduler(graph, schedulerType, varargin)
            % Create a new CustomScheduler for given FactorGraph.
            % The 'schedulerType' must identify the class of solver the
            % scheduler will be used for (e.g. 'BPOptions.scheduler' or
            % 'GibbsOptions.scheduler'). The remaining arguments define the
            % initial schedule in same format as the addEntries method.
            validateattributes(graph, {'FactorGraph'}, {'scalar'});
            validateattributes(schedulerType, ...
                {'char', 'com.analog.lyric.dimple.schedulers.SchedulerOptionKey'}, {});
            
            modeler = getModeler();
            scheduler = modeler.createCustomScheduler(graph.VectorObject, schedulerType, ...
                CustomScheduler.formatEntries(varargin{:}));
            obj@Scheduler(scheduler);
        end
        
        function addEntries(obj, varargin)
            % Add entries to the end of the schedule. 
            %
            % The entries may either be specified as arguments to this method or provided in a single
            % cell array. Each entry must be one of the following:
            %  - A Dimple Node array. A node update will be added for each
            %    variable or factor.
            %  - A two-element cell array containing a single Dimple
            %    variable, and single adjacent Dimple factor. An edge
            %    update will be added going from the first node to the
            %    second.
            %  - A single FactorGraph object for a subgraph.
            %  - A multi-element cell array beginning with a IBlockUpdater
            %   instance followed by either a single VariableBlock or by
            %   the Dimple variables that will comprise the block.
 
           obj.IScheduler.addCustomEntries(formatEntries(varargin{:}));
        end
    end
    
    methods(Access=private,Static)
        function entries = formatEntries(varargin)
            
            if numel(varargin) == 1
                args = varargin{1};
            else
                args = varargin;
            end

            n = numel(args);
            entries = cell(size(args));
            
            for i = 1:n
                arg = args{i};
                if iscell(arg)
                    % Could be either an edge or block entry
                    first = arg{1};
                    if isa(first, 'com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater')
                        % Must be a block entry
                        rest = arg(2:end);
                        validateattributes(rest, {'cell'}, {'nonempty'});
                        if isa(arg{2}, 'VariableBlock')
                            validateattributes(arg, {'cell'}, {'numel',2});
                        else
                            cellfun(@(n) validateattributes(n, {'Node'}, {'scalar'}), rest);
                        end
                        rest = cellfun(@(n) {n.VectorObject}, rest);
                        entries{i} = {first, rest{:}};
                    else
                        % An edge entry - replace with proxy objs
                        validateattributes(arg, {'cell'}, {'numel', 2});
                        cellfun(@(n) validateattributes(n, {'Node'}, {'scalar'}), arg);
                        entries{i} = cellfun(@(n) {n.VectorObject}, arg);
                    end
                else
                    % A node entry - replace with proxy obj
                    validateattributes(arg, {'Node'}, {'nonempty'});
                    entries{i} = arg.VectorObject;
                end
            end
        end
    end
end