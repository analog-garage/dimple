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

function H = blockVandermonde()
    % Generate an LDPC code with a block Vandermonde-like structure.

    blocksize=31;
    blockrows=4;
    blockcols=11;
    shifted=[eye(blocksize) ; eye(blocksize)];

    H=zeros(blockrows*blocksize,blockcols*blocksize);
    for i=0:blockcols-1
        for j=0:blockrows-1
            k=i*j;
            H(blocksize*j+1:blocksize*(j+1), blocksize*i+1:blocksize*(i+1))...
                =shifted(k+1:k+blocksize,:);
        end
    end
end


