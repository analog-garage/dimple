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

classdef HMMGraph < handle
    
    properties
        states;
        observations;
    end
    
    methods
        function obj = HMMGraph(fg, stages, stateDomainOrder, ...
                observationDomainOrder)
            
            stateDomain = DiscreteDomain(1 : stateDomainOrder);
            observationDomain = DiscreteDomain(1 : observationDomainOrder);
            
            obj.states = Discrete(stateDomain, stages, 1);
            obj.observations = Discrete(observationDomain, stages, 1);
            
            stateWeights = rand(stateDomainOrder, stateDomainOrder);
            observationWeights = rand(stateDomainOrder, observationDomainOrder);
            
            stateTransitionFactor = FactorTable(stateWeights, stateDomain, stateDomain);
            observationFactor = FactorTable(observationWeights, stateDomain, observationDomain);
            
            if stages > 1
                fg.addFactorVectorized(stateTransitionFactor, obj.states(1:stages-1), obj.states(2:stages));
            end
            fg.addFactorVectorized(observationFactor, obj.states, obj.observations);
            
        end
    end
end
