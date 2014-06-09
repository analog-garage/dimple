%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2014 Analog Devices, Inc.
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

classdef Scheduler
    
    properties(Access=private)
        Graph;
    end
    
    methods(Access=public)
        
        function obj = Scheduler(graph)
            obj.Graph = graph;
        end
        
        function disp(obj)
            disp(obj.Graph.VectorObject.getScheduler().getClass().getName());
        end
        
        function addBlockScheduleEntry(obj, varargin)
            import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
            
            args = varargin;
            if (numel(args) == 1)
                args = args{1};
            end
            
            updater = args{1};
            if ~isa(updater, 'com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater')
                error('First element of block schedule entry must be a block updater');
            end
            
            scheduleElements = {};
            for j=2:length(args)
                nodes = num2cell(args{j}.VectorObject.getModelerNodes());
                scheduleElements = [scheduleElements; nodes];
            end
            
            javaScheduleEntry = BlockScheduleEntry(updater, scheduleElements);
            
            obj.Graph.VectorObject.getModelerNode(0).getSchedule;   % Make sure the schedule is created
            javaScheduler = obj.Graph.VectorObject.getScheduler();
            javaScheduler.addBlockScheduleEntry(javaScheduleEntry);
        end
        
        function addBlockScheduleEntries(obj, varargin)
            import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
            
            numEntries = numel(varargin);
            
            obj.Graph.VectorObject.getModelerNode(0).getSchedule;   % Make sure the schedule is created
            javaScheduler = obj.Graph.VectorObject.getScheduler();

            for i=1:numEntries
                args = varargin{i};
                
                updater = args{1};
                if ~isa(updater, 'com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater')
                    error('First element of block schedule entry must be a block updater');
                end
                
                scheduleElements = {};
                for j=2:length(args)
                    nodes = num2cell(args{j}.VectorObject.getModelerNodes());
                    scheduleElements = [scheduleElements; nodes];
                end
                
                javaScheduleEntry = BlockScheduleEntry(updater, scheduleElements);
                javaScheduler.addBlockScheduleEntry(javaScheduleEntry);

            end

        end

    end

    
end
