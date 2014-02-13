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

classdef StereoVisionGraph < handle
    
    properties
        height;
        width;
        depth;
        variables;
    end
    
    methods
        function obj = StereoVisionGraph(factorgraph, datasetName)
            
            dataset = load_dataset(datasetName);
            
            obj.depth = 75;       % Number of depth levels represented
            
            % Load images.
            imageL = dataset.Left.Image;
            imageR = dataset.Right.Image;
            
            % Set parameters.
            ed     = 0.01;
            ep     = 0.05;
            sigmaD = 8;
            sigmaP = 0.6;
            sigmaF = 0.3;
            
            % Convert the images to grayscale without using the image processing
            % toolbox.
            color2gray = @(image) ...
                0.2989 * double(image(:, :, 1)) + ...
                0.5870 * double(image(:, :, 2)) + ...
                0.1140 * double(image(:, :, 3));
            inputL = color2gray(imageL);
            inputR = color2gray(imageR);
            [obj.height, obj.width] = size(inputL);
            
            % Create temporary function handles.
            rho_d_ = @(y, x, d) rho_d(x, y, d, ed, sigmaD, sigmaF, inputL, inputR);
            rho_p_ = @(ds, dt) rho_p(ds, dt, ep, sigmaP);
            
            % Check image sizes.
            if (size(inputL) ~= size(inputR))
                error('Stereovision:FPT:GetFactorgraph','Mismatched image sizes.');
            end
            
            % Create variables and add inputs.
            obj.variables = Variable(0:(obj.depth - 1), obj.height, obj.width);
            inputs = zeros(obj.height, obj.width, obj.depth);
            depthRange = 0:obj.depth-1;
            for i = 1:obj.height
                for j = 1:obj.width
                    inputs(i,j,:) = rho_d_(i, j, depthRange);
                end
            end
            obj.variables.Input = inputs;
            
            % Add factors.
            vLeft = obj.variables(:,1:end-1);
            vRight = obj.variables(:,2:end);
            vLower = obj.variables(1:end-1,:);
            vUpper = obj.variables(2:end,:);
            factorgraph.addFactorVectorized(rho_p_, vLeft, vRight);
            factorgraph.addFactorVectorized(rho_p_, vLower, vUpper);
            
        end
    end
end
