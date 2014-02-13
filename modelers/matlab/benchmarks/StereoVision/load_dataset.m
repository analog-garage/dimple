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

function dataset = load_dataset(name)
    
    % load_dataset - Loads a Stereovsion dataset.
    %
    %   Returns a struct with the following fields:
    %
    %       Name        : Name of the dataset.
    %       Left.Image  : Left scene image.
    %       Right.Image : Right scene image.
    
    thisDir = fileparts(mfilename('fullpath'));
    datasets_path = fullfile(thisDir, 'datasets');
    dataset_path  = fullfile(datasets_path, name);
    left_image_name = 'imageL.tiff';
    right_image_name = 'imageR.tiff';
    
    % Create dataset struct.
    dataset = struct( ...
        'Name', name, ...
        'Left', struct( ...
        'Image', []), ...
        'Right', struct( ...
        'Image', []));
    
    % Load dataset images.
    dataset.Left.Image = imread(fullfile(dataset_path, left_image_name));
    dataset.Right.Image = imread(fullfile(dataset_path, right_image_name));
    
    % Check that dataset images have the same dimensions.
    if size(dataset.Left.Image) ~= size(dataset.Right.Image)
        error('Stereovision:FPT:LoadDataset', 'The dimensions of the image files do not match.');
    end
    
end
