%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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

classdef MultivariateMsg < Msg
    properties
        Means;
        Covariance;
    end
    methods
        function obj = MultivariateMsg(means,covariance)
            modeler = getModeler();
            obj.IMsg = modeler.createMultivariateMsg(means,covariance);
        end
        
        function means = get.Means(obj)
           means = obj.IMsg.getMeans(); 
        end
        
        function covar = get.Covariance(obj)
           covar = obj.IMsg.getCovariance(); 
        end
        
    end
end
