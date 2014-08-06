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
    properties(Constant)
        modeler = getModeler();
    end
    methods(Static)
        function logToConsole(level)
            %logToConsole configures Dimple to log to the console at the specified level.
            %
            % MATLAB's default logging configuration for the java.util.logging service
            % that is used by Dimple does not enable logging output. This method
            % provides an easy way to enable console logging without having
            % to bother with configuring MATLAB's Java properties.
            logger = DimpleLogger.modeler.getLogger();
            logger.logToConsole(level);
        end
        
        function resetConfiguration()
            %resetConfiguration resets java.util.logging configuration
            logger = DimpleLogger.modeler.getLogger();
            logger.resetConfiguration();
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
