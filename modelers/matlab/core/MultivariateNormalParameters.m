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

classdef MultivariateNormalParameters < ParameterizedMessage
    properties
        Mean;
        Covariance;
        InformationVector;
        InformationMatrix;
        Means; % For backward compatibility
    end
    methods
        function obj = MultivariateNormalParameters(mean, covariance)
            if ~isnumeric(mean)
                obj.IParameters = mean;
            else
                modeler = getModeler();
                obj.IParameters = modeler.createMultivariateNormalParameters(mean,covariance);
            end
        end
        
        function mean = get.Mean(obj)
           mean = obj.IParameters.getMean(); 
        end
        
        function mean = get.Means(obj)  % For backward compatibility
           mean = obj.IParameters.getMean(); 
        end
        
        function covar = get.Covariance(obj)
           covar = obj.IParameters.getCovariance(); 
        end
        
        function informationVector = get.InformationVector(obj)
           informationVector = obj.IParameters.getInformationVector(); 
        end
        
        function informationMatrix = get.InformationMatrix(obj)
           informationMatrix = obj.IParameters.getInformationMatrix(); 
        end

    end
end
