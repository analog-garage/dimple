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

function [x,y] = factorNum(num,x,y)

    z = num;
    if nargin < 2
        numxbits = getNumBits(z)-1;
        numybits = numxbits;    
    else
        %numbits = max(getNumBits(x),getNumBits(y));
        numxbits = getNumBits(x);
        numybits = getNumBits(y);
    end
    numzbits = numxbits+numybits;

    found = zeros(numzbits,1);
    sx = ones(numxbits,1)*.5;
    sy = ones(numybits,1)*.5;
    xo = 0;
    yo = 0;
    zo = 0;
    for i =1:numzbits
        [sx,sy,sz] = softMultiply(sx,sy,num2vec(z,numzbits),1);
        %sx
        %sy
        maxCertainty = 0;
        %Find most certain bit of x/y that hasn't already been found.
        mcb = 0;
        xy = [sx' sy'];
        for ufb = 1:numzbits
            if found(ufb) == 0
                certainty = abs(xy(ufb)-.5);
                if certainty >= maxCertainty
                    mcb = ufb;
                    maxCertainty = certainty;
                end
            end
        end
        if mcb == 0
            break; %Not found
        end
        found(mcb) = 1;
        xy(mcb) = xy(mcb) > .5;
        xy(find(xy == 1)) = .9;
        xy(find(xy == 0)) = .1;
        sx = xy(1:numxbits);
        sy = xy((numxbits+1):end);
        xo = vec2num(sx);
        yo = vec2num(sy);
        zo = vec2num(sz);
        %xo
        %yo
        %zo
        if z/xo == floor(z/xo) && xo ~= 1
            yo = z/xo;
            break;
        elseif z/yo == floor(z/yo) && yo ~= 1
            xo = z/yo;
            break;
        end
    end

    sx
    sy
    sz
    xo
    yo
    zo
    x = xo;
    y = yo;
end
