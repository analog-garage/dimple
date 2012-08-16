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

function [ output_args ] = matchDelta( x,y,z )
% "z" is an "output" bit, in that it equals 0 if x==y, and is unset if
% x!=y.  If x and y were bits, then z = x NAND y.

if x==y
    if z==1
        output_args=0;
    else
        output_args=1;
    end
else
    output_args=0.5;
    %output_args=1;
end

%{
if and(x==y, z==1)
    output_args=0;
    %output_args=0.001;
else
    output_args=1;
end
%}

end

