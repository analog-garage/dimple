%EventLogger Controls logging of Dimple events to console or output file.
% Here are some simple examples:
%   % Log all solver events for given factorGraph using default logger:
%   logger = getEventLogger();
%   logger.log('SolverEvent', factorGraph);
%   
%   logger.Verbosity = 2;
%   logger.open('logfile.txt');
%   logger.log('DimpleEvent', factorGraph);
%   logger.println('
%
%   See also getEventLogger

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
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
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

classdef EventLogger < handle
    properties
         % Path of output logfile or 'stdout' or 'stderr' for console.
        Filename;        
        
        % An integer indicating level of detail to output.
        %
        % Zero indicates minimal output, one indicates normal output, 
        % and a value of two or more indicates more verbose output.
        % A negative value indicates that no output is desired.
        %
        % The verbosity level applies to all events logged by this object.
        Verbosity;
        
        % The underlying Java logger object that implements the behavior.
        IEventLogger;
    end
    methods
        function obj = EventLogger(varargin)
            %EventLogger creates a new Dimple event logger instance.
            %
            % By default this returns an instance that logs to the standard
            % console error output. You can change this using open().
            %
            % Most users will want to simply use the global logger instance
            % returned by getEventLogger().
            %
            % See also getEventLogger
        	modeler = getModeler();
            obj.IEventLogger = modeler.createEventLogger();
        end
        
        function delete(obj)
            %delete Destructor method.
            % Enures that underlying file is closed when object is cleaned up.
            % This method is called automatically by MATLAB when the object
            % is destroyed. There is no reason for users to call this
            % directly.
            obj.close();
            obj.clear();
            delete@handle(obj);
        end
        
        function events = listEvents(obj)
            %listEvents returns an array of supported event names.
            events = obj.IEventLogger.listStandardEvents();
        end
        
        function name = get.Filename(obj)
            name = obj.IEventLogger.filename();
        end
        
        function v = get.Verbosity(obj)
            v = obj.IEventLogger.verbosity();
        end
        function set.Verbosity(obj,v)
            obj.IEventLogger.verbosity(v);
        end
        
        function open(obj,filename)
            %open Directs log output to specified file.
            %
            % This will close the previous output file and
            % redirect the logger to the new file. The special
            % values 'stdout' and 'stderr' will direct output to
            % the standard output or error ports of the console.
            obj.IEventLogger.open(pwd(), filename);
        end
        
        function clear(obj)
            %clear Unregisters all event logging for this object.
            %
            % This disables all event notification related to this object.
            % You may remove a single log registration using the unlog()
            % method.
            %
            % See also unlog
            obj.IEventLogger.clear();
        end
        
        function close(obj)
            %close Closes the log output file.
            %
            % Any events that occur while the output file is closed will
            % not be logged by this object. Note that closing the file does
            % not disable the underlying generation of events. Use the clear()
            % method to indicate that events should not be generated for this
            % logger.
            %
            % See also clear
            obj.IEventLogger.close();
        end
        
        function log(obj,event,source)
            %log Register to log events of given type on given target.
            %
            % event: string specifying a supported event name. Valid values
            %        are given by the listEvents() method.
            % source: the Dimple object for which events of the given type
            %         are to be logged. Typically this will be a
            %         FactorGraph or the DimpleEnvironment object, but it could be a subgraph,
            %         variable, or factor as well.
            %
            % Enables logging of the specified event type over the set of
            % Dimple objects rooted at the source object. This method may
            % be invoked multiple times with different event types and
            % sources. You may unregister logging using the unlog or 
            % clear methods.
            %
            % See also listEvents, unlog, clear
            if (isa(source,'MatrixObject'))
                source = source.VectorObject;
            elseif isa(source,'DimpleEnvironment')
                source = source.PEnvironment;
            end
            obj.IEventLogger.log(event,source);
        end
        
        function unlog(obj, event, source)
            %unlog Removes previous log registration for given event type and target
            %
            % The values of event and source arguments must match a
            % previous call to the log() method.
            % 
            % See also log, clear.
            if (isa(source,'MatrixObject'))
                source = source.VectorObject;
            elseif isa(source,'DimpleEnvironment')
                source = source.PEnvironment;
            end
            obj.IEventLogger.unlog(event,source);
        end
        
        function println(obj, message)
            % println Writes newline terminated message to log output.
            %
            % You can use this to test where log output is going, or
            % to annotate the log with other useful information to
            % help interpret the output.
            if (obj.IEventLogger.isOpen())
                obj.IEventLogger.out().println(message);
            end
        end
    end
end
