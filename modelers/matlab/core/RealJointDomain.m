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
    
    properties
        NumElements;
        RealDomains;
    end
    
    methods
        function obj = RealJointDomain(varargin)
            
            % Either a number or a vector of RealDomains
            if isempty(varargin)
                error('Constructor is missing arguments');
            elseif length(varargin) == 1
                arg = varargin{1};
                if isa(arg, 'com.analog.lyric.dimple.matlabproxy.PRealJointDomain')
                    obj.IDomain = arg;
                    obj.RealDomains = wrapProxyObject(arg.getRealDomains());
                    return;
                elseif isnumeric(arg)
                    domains = cell(arg,1);
                    for i = 1:length(domains)
                        domains{i} = RealDomain();
                    end
                else
                    domains = arg;
                end
            else
                if isnumeric(varargin{1})
                    dimension = varargin{1};
                    domains = varargin(2:end);
                    if (numel(domains) == 1)
                        domains = cell(1,dimension);
                        for i=1:dimension
                            domains{i} = varargin{2};
                        end
                    elseif (numel(domains) ~= dimension);
                        error('Number of domain elements must match the number of joint variable elements');
                    end
                else
                    domains = varargin;
                end
            end
            
            domains = domains(:);
            idomains = cell(size(domains));
            for i = 1:length(idomains)
                idomains{i} = domains{i}.IDomain;
            end
            
            modeler = getModeler();
            obj.IDomain = modeler.createRealJointDomain(idomains);
            obj.RealDomains = domains;
        end
        
        function retval = isDiscrete(obj)
            retval = false;
        end
        
        function numElements = get.NumElements(obj)
            numElements = obj.IDomain.getNumVars();
        end
    end
end
