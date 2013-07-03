%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013 Analog Devices, Inc.
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

%%%
% This function builds a Markov Random field with the specified
% height (H) and width (W)
function [fg,b,vertFT,horzFT] = buildMRF(H,W,vertWeights,horzWeights)

    fg = FactorGraph();
    b = Bit(H,W);

    vertFT = FactorTable(vertWeights,b.Domain,b.Domain);
    horzFT = FactorTable(horzWeights,b.Domain,b.Domain);

    if H > 1
        fg.addFactorVectorized(vertFT,b(1:end-1,:),b(2:end,:));
    end

    if W > 1
        fg.addFactorVectorized(horzFT,b(:,1:end-1),b(:,2:end));
    end

end