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
        Filename;
        Verbosity;
        IEventLogger;
    end
    methods
        function obj = EventLogger(varargin)
        	modeler = getModeler();
            obj.IEventLogger = modeler.createEventLogger();
        end
        
        function delete(obj)
            % Make sure file gets closed if object is cleaned up.
            obj.close();
            obj.clear();
            delete@handle(obj);
        end
        
        function events = listEvents(obj)
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
            obj.IEventLogger.open(pwd(), filename);
        end
        
        function clear(obj)
            obj.IEventLogger.clear();
        end
        
        function close(obj)
            obj.IEventLogger.close();
        end
        
        function log(obj,event,source)
            if (isa(source,'MatrixObject'))
                source = source.VectorObject;
            end
            obj.IEventLogger.log(event,source);
        end
    end
end
