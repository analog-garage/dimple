% FactorGraphChild is the base class for Dimple model elements that are
% children of a FactorGraph but aren't necessarily nodes.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
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
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

classdef FactorGraphChild < MatrixObject
    properties
        GlobalId;
        UUID;
    end
    
    methods(Access=protected)
        function obj = FactorGraphChild(vectorObject,vectorIndices)
           obj@MatrixObject(vectorObject, vectorIndices); 
        end
        
        function names = wrapNames(obj, namesArray)
            names = cell(namesArray);
            if numel(names) == 1
                names = names{1};
            else
                names = reshape(names,size(obj.VectorIndices));
            end
        end

    end
    
    methods
        function ids = get.GlobalId(obj)
            ids = int64(obj.VectorObject.getIds());
        end
        
        function uuids = get.UUID(obj)
            uuids = obj.wrapNames(obj.VectorObject.getUUIDs());
        end
        
        function clearLocalOptions(obj)
            %clearLocalOptions Unsets all options on these objects.
            obj.VectorObject.clearOptions();
        end
        
        function value = getOption(obj,option)
            %getOption Returns current value of specified option.
            %
            % getOption(name)
            %
            %    name - a string qualified option name of the form
            %           'Class.field' (e.g. 'BPOptions.damping').
            %           An instance of the Java IOptionKey class may also
            %           be used.
            %
            % If invoked on a Node matrix with more than one element, this
            % will return a cell array of the same dimensions containing
            % the corresponding option setting for each element.
            %
            % See also setOption, dimpleOptions
        	value = obj.VectorObject.getOption(option);
            if obj.length == 1
                value = wrapOptionValue(value(1), obj);
            else
                value = reshape(cellfun(@(v) {wrapOptionValue(v, obj)}, cell(value)), size(obj.VectorIndices));
            end
        end
        	
        function unsetOption(obj,option)
            %unsetOption Unsets option on this object.
            %
            % unsetOption(name)
            %
            %    name - a string qualified option name of the form
            %           'Class.field' (e.g. 'BPOptions.damping').
            %           An instance of the Java IOptionKey class may also
            %           be used.
            %
            % See also clearLocalOptions, setOption, dimpleOptions
        	obj.VectorObject.unsetOption(option);
        end
        
        function setOption(obj,option,value)
            %setOption Sets value of specified option.
            %
            % setOption(name,value)
            %
            %    name  - a string qualified option name of the form
            %            'Class.field' (e.g. 'BPOptions.damping').
            %            An instance of the Java IOptionKey class may also
            %            be used.
            %
            %    value - value to give the option. May either be a single
            %            value or a cell array with dimensions matching
            %            the that of the Node matrix it is being invoked
            %            on.
            %
            % See also getOption, getLocalOptions, unsetOption, dimpleOptions
            if numel(value) == 1 || obj.length == 1
                obj.VectorObject.setOptionOnAll(option,unwrapOptionValue(value));
            else
                assert(iscell(value));
                obj.VectorObject.setOptionAcrossAll(option,value(:)); % FIXME
            end
        end
        
        function options = getLocalOptions(obj)
            %getLocalOptions Returns values of options set directly on
            %this object.
            %
            %  getLocalOptions()
            %
            % Returns a cell array containing values of options that are
            % set directly on the node(s). If there is only one node in
            % the expression, this will return a simple nx2 cell array with
            % each row containing a key and value. If there is more
            % than one nodes this will return a cell array with same
            % dimensions as the lhs where each cell contains a cell array
            % with the option settings for that node.
            %
            % See also clearLocalOptions, setOption, setOptions, unsetOption
            array = obj.VectorObject.getLocallySetOptions();
            length = array.length;
            if (length == 1)
                nodeOptions = array(1);
                options = reshape(cell(nodeOptions), 2, nodeOptions.length/2)';
            else
                options = cell(length,1);
                for i = 1:length
                    nodeOptions = array(i);
                    options{i} = reshape(cell(nodeOptions), 2, nodeOptions.length/2)';
                end
                options = cellfun(@(v) {wrapOptionValue(v, obj)}, options);
                options = reshape(options, size(obj.VectorIndices));
            end
        end
        
        function setOptions(obj,varargin)
            %setOptions Sets options from a cell array or comma-spearated list
            %
            %  setOptions(optionList)
            %
            %    optionVector is a comma-separated list with an even number of
            %       entries containing alternating keys and values. The
            %       specified options will be set on all nodes in the left
            %       hand side of the expression.
            %
            %  setOptions(optionVector)
            %
            %    optionVector is a cell array vector with an even number of
            %       entries containing alternating keys and values. The
            %       specified options will be set on all nodes in the left
            %       hand side of the expression.
            %
            %  setOptions(optionMatrix)
            %      
            %    optionMatrix is a nx2 cell array where each row contains a
            %       string option key name followed by the corresponding
            %       value. The specified options will be set on all nodes
            %       in the left hand side of the expression.
            %
            %  setOptions(optionArray)
            %
            %     optionArray is a cell array with dimensions matching the
            %        dimensions of the left hand side of the expression.
            %        Each cell will contain options to be set on the
            %        corresponding node in either optionVector or
            %        optionMatrix form as described above.
            %
            % Examples:
            %    
            %    % These variants are equivalent. All set the
            %    % specified options on all nodes.
            %    nodes.setOptions('BPOptions.iterations', 10,...
            %                      'BPOptions.damping' , .9);
            %    nodes.setOptions({'BPOptions.iterations', 10,...
            %                      'BPOptions.damping' , .9});
            %    nodes.setOptions({'BPOptions.iterations', 10;...
            %                      'BPOptions.damping', .9);
            %
            %    % Sets options on a 2x2 node.    
            %    options = cell(2,2);
            %    options{1,1} = {'BPOptions.iterations', 10;...
            %                    'BPOptions.damping', .85)
            %    options{2,2} = {'BPOptions.iterations', 12};
            %    nodes.setOptions(options);
            %
            % See also getLocalOptions, setOption
            if iscell(varargin{1})
                options = varargin{1};
            else
                options = varargin;
            end
            if isvector(options)
                assert(rem(numel(options),2) == 0, 'Options vector must have even length');
                options = reshape(options, 2, numel(options)/2)';
            end
            if ndims(options) == 2 && iscellstr(options(:,1))
                % Single set of options to be applied to all nodes.
                assert(size(options,2) == 2);
                obj.VectorObject.setOptionsOnAll(options(:,1),options(:,2));
            else
                obj.VectorObject.setOptionsAcrossAll(options(:));
            end
        end
        
    end % public methods
   
    methods(Static, Access=protected)
    end
end

function wrappedVal = wrapOptionValue(val, optionHolder)
    wrappedVal = wrapProxyObject(val);
    if ismethod(wrappedVal, 'setOptionHolder')
        wrappedVal.setOptionHolder(optionHolder);
    end
end

function unwrappedVal = unwrapOptionValue(val)
if isa(val, 'Scheduler')
    unwrappedVal = val.IScheduler;
else
    unwrappedVal = val;
end
end
