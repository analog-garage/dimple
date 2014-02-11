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

classdef ImageDenoisingGraph < handle
    
    properties
        rows;
        cols;
        variables;
    end
    
    methods
        function obj = ImageDenoisingGraph(fg, factorFileName, ...
                xImageSize,  yImageSize,  xBlockSize, yBlockSize)
            
            thisDir = fileparts(mfilename('fullpath'));
            factorFileName = fullfile(thisDir, 'imageStats', factorFileName);
            
            blockSize = xBlockSize * yBlockSize;
            
            d = load(factorFileName);    % Loads factor table
            factorTableValues = reshape(d.factorTableValues, 2*ones(1,blockSize));
            clear('d');
            
            obj.rows = yImageSize;
            obj.cols = xImageSize;
            blockRows = obj.rows - yBlockSize + 1;
            blockCols = obj.cols - xBlockSize + 1;
            
            obj.variables = Bit(obj.rows, obj.cols);
            for row = 1:obj.rows
                obj.variables(row,:).setNames(['V_row' num2str(row)]);
            end
            
            domains = cell(1,blockSize);
            for i=1:blockSize; domains{i} = DiscreteDomain([0 1]); end;
            factorTable = FactorTable(factorTableValues,domains{:});
            
            yList = 1:blockRows;
            xList = 1:blockCols;
            tempVar = Bit();    % Do this to avoid creating a whole array of temp variables
            varPatches = repmat(tempVar,[blockCols,blockRows,xBlockSize*yBlockSize]);
            blockOffset = 1;
            for yb = 0:yBlockSize-1
                for xb = 0:xBlockSize-1
                    varPatches(:,:,blockOffset) = obj.variables(yb+yList,xb+xList);
                    blockOffset = blockOffset + 1;
                end
            end
            fg.addFactorVectorized(factorTable,{varPatches,[1,2]});
            
        end
    end
end
