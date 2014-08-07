%DimpleLogger controls Dimple's logging configuration.
%
%   This class is for controlling simple text logging originating from
%   Dimple.  It is distinct from the EventLogger, which is specific to logging
%   Dimple events.

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

classdef DimpleLogger
    properties(Constant, Access=private)
        modeler = getModeler();
    end
    methods(Static)
        function level = getLevel()
            %getLevel indicates the current minimum logging level
            logger = DimpleLogger.modeler.getLogger();
            level = logger.getLevel();
        end
        
        function setLevel(level)
            %setLevel sets the new minimum logging level.
            %
            % Messages logged at a lower level will be discarded.
            logger = DimpleLogger.modeler.getLogger();
            logger.setLevel(level);
        end
        
        function logToConsole()
            %logToConsole configures Dimple to log to the console.
            logger = DimpleLogger.modeler.getLogger();
            logger.logToConsole();
        end
        
        function logToFile(filename)
            %lotToFile configures Dimple to append to specified filename
            logger = DimpleLogger.modeler.getLogger();
            logger.logToFile(filename);
        end
        
        function logError(message)
            %logError logs an error message using Dimple's logging configuration
            logger = DimpleLogger.modeler.getLogger();
            logger.logError(message);
        end
        
        function logWarning(message)
            %logWarning logs a warning message using Dimple's logging configuration
            logger = DimpleLogger.modeler.getLogger();
            logger.logWarning(message);
        end
    end
end
