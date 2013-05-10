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

function result = wrapProxyObject(proxyObject)
    result = [];
    
    if isempty(proxyObject)
        return;
    end
        
    if isjava(proxyObject)
        if proxyObject.isGraph()
            result = FactorGraph('VectorObject',proxyObject);
        elseif proxyObject.isFactor()
            if proxyObject.isDiscrete()
                result = DiscreteFactor(proxyObject,0:(proxyObject.size()-1));
            else
                result = Factor(proxyObject,0:(proxyObject.size()-1));
            end
            
        elseif proxyObject.isVariable()
            if proxyObject.isDiscrete()
                domain = cell(proxyObject.getDomain().getElements());
                indices = 0:(proxyObject.size()-1);
                result = Discrete(domain,'existing',proxyObject,indices:(proxyObject.size()-1));
            elseif proxyObject.isJoint()
                domain = RealJointDomain(proxyObject.getDomain().getNumVars());
                indices = 0:(proxyObject.size()-1);
                result = RealJoint(domain,'existing',proxyObject,indices);
            else
                domain = RealDomain(proxyObject.getDomain().getLowerBound(),proxyObject.getDomain().getUpperBound);
                indices = 0:(proxyObject.size()-1);
                result = Real(domain,'existing',proxyObject,indices);
            end
            
        elseif proxyObject.isDomain()
            if proxyObject.isDiscrete()
                result = DiscreteDomain(proxyObject);
            elseif proxyObject.isReal()
                result = RealDomain(proxyObject);
            elseif proxyObject.isJoint()
                result = RealJointDomain(proxyObject);
            end
        end
    end
    
    if isempty(result) && ~isempty(proxyObject)
        error('not supported');
    end
end
