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

classdef DiscreteMessage < ParameterizedMessage
    properties
        Energy;
        Weight;
    end
    methods
        function obj = DiscreteMessage(values,weightOrEnergy)
            narginchk(1,2);
            if (nargin == 1)
                if isjava(values) && isa(values, 'com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage')
                    obj.IParameters = values;
                    return;
                end
                
                weightOrEnergy = 'weight';
            else
                weightOrEnergy = validatestring(weightOrEnergy,{'weight','energy'});
            end
            validateattributes(values, {'numeric'}, {'vector', 'nonnan'});
            if (isscalar(values) && mod(values,1) == 0)
                % Is scalar integral value - specifies size of message
                size = values;
                values = [];
            else
                % 
                size = numel(values);
            end
            if (size <= 1)
                error('Size must be greater than one.');
            end
            modeler = getModeler();
            obj.IParameters = modeler.createDiscreteMessage(weightOrEnergy, size, values);
        end
        
        function energy = get.Energy(obj)
            energy = obj.IParameters.getEnergies();
        end
        
        function set.Energy(obj,energy)
            obj.IParameters.setEnergies(obj,energy);
        end
        
        function weights = get.Weight(obj)
            weights = obj.IParameters.getWeights();
        end
        
        function set.Weight(obj,weights)
            obj.IParameters.setWeights(weights);
        end
        
        function ind = end(obj,~,~)
            ind = obj.IParameters.size();
        end
        
        function varargout = subsref(obj,S)
            if (strcmp(S(1).type,'()'))
                varargout{:} = subsref(obj.Weight,S);
                return;
            end
            try
                varargout{:} = builtin('subsref',obj,S);
            catch e
                if isequal(e.identifier,'MATLAB:unassignedOutputs')
                else
                    rethrow(e);
                end
            end
        end
        
        function result = subsasgn(obj,S,value)
            if (strcmp(S(1).type,'()'))
                weights = builtin('subsasgn',obj.Weight,S,value);
                obj.IParameters.setWeights(weights);
                result = obj;
                return;
            end
            result = builtin('subsasgn',obj,S,value);
        end
        
        function disp(obj)
            fprintf('DiscreteMessage(%s)\n', mat2str(obj.Weight'));
        end
    end
end