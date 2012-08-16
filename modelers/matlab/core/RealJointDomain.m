%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
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

classdef RealJointDomain < Domain
    
    methods
        function obj = RealJointDomain(varargin)
            
            %should allow either a number or a vector of RealDomains
            if length(varargin) == 1 && isnumeric(varargin{1})
                domains = cell(varargin{1},1);
                for i = 1:length(domains)
                    domains{i} = RealDomain();
                end
            else
                domains = varargin;
            end
            
            idomains = cell(size(domains));
            for i = 1:length(idomains)
                idomains{i} = domains{i}.IDomain;
            end
            
            modeler = getModeler();
            obj.IDomain = modeler.createRealJointDomain(idomains);
        end
        
        function retval = isDiscrete(obj)
            retval = false;
        end
    end
end
