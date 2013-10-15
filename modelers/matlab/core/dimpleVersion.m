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

function version=dimpleVersion()
    p = mfilename('fullpath');
    %strsplit doesn't work in MATLAB 2012
    s=textscan(p,'%s','delimiter',filesep());
    s = s{1};
    s = s(1:end-1);
    %strjoin doesn't work in MATLAB 2012
    c = cell(length(s) + length(s)-1);
    index = 1;
    for i = 1:length(s)
       c{index} = s{i};
       if i < length(s)
           c{index+1} = filesep();
       end
       index = index+2;
    end
    p = [c{:}];
    filename = [p filesep() fullfile('..','..','..','LONG_VERSION')];
    
    try
        
        f = fopen(filename);
        version = strtrim(fgets(f));

    catch e
       filename = [p filesep() fullfile('..','..','..','LONG_VERSION')];
        f = fopen(filename);
        part1 = strtrim(fgets(f));

        version = [part1 ' UNKNOWN'];
    end
end