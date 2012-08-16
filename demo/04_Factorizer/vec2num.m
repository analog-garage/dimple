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

function num = vec2num(vec,twoscomplement)
    if nargin < 2
        twoscomplement = 0;
    end
    
    if ~twoscomplement
        num = 0;
        for i =1:length(vec)
           tmp = double(vec(i) > .5);
           num = num+bitshift(tmp,i-1);
        end
    else
        sn = vec(end);
        if (sn > 0)
            vec = ~vec;
        end
        
        num = 0;
        for i =1:(length(vec)-1);
           tmp = double(vec(i) > .5);
           num = num+bitshift(tmp,i-1);
        end        
        if sn > 0
           num = -num -1; 
        end
    end
end
