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

function version=dimpleVersion(longVersion)
    if nargin < 1
       longVersion = 1; 
    end

    p = mfilename('fullpath');
    s = strsplit(p,'/');
    s = s(1:end-1);
    p = strjoin(s,'/');
    filename = [p '/../../../LONG_VERSION'];
    
    try
        
        f = fopen(filename);
        part1 = strtrim(fgets(f));
        part2 = strtrim(fgets(f));

        if longVersion ~= 0
            version = [part1 ' ' part2];
        else
            version = part1;
        end
    catch e
       filename = [p '/../../../VERSION'];
        f = fopen(filename);
        part1 = strtrim(fgets(f));

        if longVersion ~= 0
            version = [part1 ' UNKNOWN'];
        else
            version = part1;
        end
    end
end