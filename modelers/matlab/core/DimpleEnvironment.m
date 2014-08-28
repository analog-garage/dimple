%DimpleEnvironment holds shared state for Dimple
%
% There is only one active Dimple environment, which can be obtained by
% the static active() method:
%
%     env = DimpleEnvironment.active();
%
% The environment can be used to globally configure the behavior of Dimple. Specifically,
% it can be used in the following ways:
%
%   - To set default values of options for all Dimple models.
%   - As the source argument to log and unlog methods of EventLogger in
%     order to control event logging globally.
%
% See also DimpleEnvironment.active, EventLogger.log

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

classdef DimpleEnvironment < handle
    
    properties
        %The Java proxy object that provides the underlying implementation.
        PEnvironment;
    end
    
    methods(Static)
        function env = active()
            %active Returns singleton instance representing active Dimple environment.
            global g_activeEnvironment;
            if isempty(g_activeEnvironment)
                g_activeEnvironment = DimpleEnvironment();
            end
            env = g_activeEnvironment;
        end
    end

    methods(Access=private)
        function obj = DimpleEnvironment()
            obj.PEnvironment = com.analog.lyric.dimple.matlabproxy.PEnvironment();
        end
    end
    
    methods
        function clearOptions(obj)
            %clearOptions Unsets all options on the environment.
            %  This only affects options that are set directly on the
            %  environment itself.
            obj.PEnvironment.clearOptions();
        end

        function value = getOption(obj,option)
            %getOption Returns current value of specified option.
            %
            % getOption(name)
            %
            %    name - a string qualified option name of the form
            %           'Class.field' (e.g. 'SumProductOptions.damping').
            %           An instance of the Java IOptionKey class may also
            %           be used.
            %
            % See also setOption, dimpleOptions
        	value = obj.PEnvironment.getOption(option);
            value = value(1);
        end
        
        function unsetOption(obj,option)
            %unsetOption Unsets option on the environment.
            %
            % unsetOption(name)
            %
            %    name - a string qualified option name of the form
            %           'Class.field' (e.g. 'SumProductOptions.damping').
            %           An instance of the Java IOptionKey class may also
            %           be used.
            %
            % See also setOption, dimpleOptions
        	obj.PEnvironment.unsetOption(option);
        end
        
        function setOption(obj,option,value)
            %setOption Sets value of specified option.
            %
            % setOption(name,value)
            %
            %    name  - a string qualified option name of the form
            %            'Class.field' (e.g. 'SumProductOptions.damping').
            %            An instance of the Java IOptionKey class may also
            %            be used.
            %
            %    value - value to give the option. May either be a single
            %            value or a cell array with dimensions matching
            %            the that of the Node matrix it is being invoked
            %            on.
            %
            % See also getOption, getLocalOptions, unsetOption, dimpleOptions
            obj.PEnvironment.setOptionOnAll(option,value);
        end
        
        function options = getLocalOptions(obj)
            %getLocalOptions Returns values of options set directly on the environment.
            %
            %  getLocalOptions()
            %
            % Returns a cell array containing values of options that are
            % set directly on the node(s). This will be nx2 cell array with
            % each row containing a key and value.
            %
            % See also setOption, setOptions, unsetOption
            array = obj.PEnvironment.getLocallySetOptions();
            nodeOptions = array(1);
            options = reshape(cell(nodeOptions), 2, nodeOptions.length/2)';
        end
        
        function setOptions(obj,options)
            %setOptions Sets options from a cell array
            %
            %  setOptions(optionVector)
            %
            %    optionVector is a cell array vector with an even number of
            %       entries containing alternating keys and values.
            %
            %  setOptions(optionMatrix)
            %      
            %    optionMatrix is a nx2 cell array where each row contains a
            %       string option key name followed by the corresponding
            %       value.
            %
            % See also getLocalOptions, setOption
            assert(iscell(options), 'Options must be specified in a cell array');
            if isvector(options)
                assert(rem(numel(options),2) == 0, 'Options vector must have even length');
                options = reshape(options, 2, numel(options)/2)';
            end
            % Single set of options to be applied to all nodes.
            assert(size(options,2) == 2);
            obj.PEnvironment.setOptionsOnAll(options(:,1),options(:,2));
        end
    end
    
end

