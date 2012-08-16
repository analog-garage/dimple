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

function [reconMean,reconSig,reconPx] = softbyte2gauss(moment,var_moment,minVal,maxVal)
    numbits = length(moment);
    if nargin < 3
        minVal = 0;
        maxVal = 2^numbits-1;
    else
        error('not yet supported');
    end
    
    reconMean = 0;
    for i = 1:length(moment)
        reconMean = reconMean + 2^(i-1)*moment(i);
    end
    
    reconSig = 0;
    
    
    if nargin >= 2
        recon_moment = 0;
        for i = 1:length(var_moment)
            recon_moment = recon_moment + 2^(i-1)*var_moment(i);
        end
        reconSig = sqrt(recon_moment-reconMean^2);
    end
    
    reconPx = 0;
    
end
