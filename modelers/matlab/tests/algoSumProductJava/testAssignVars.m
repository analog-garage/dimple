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

function testAssignVars()

    a = Bit(4,4);
    for i = 1:4
        for j = 1:4
            a(i,j).Name = sprintf('a_%d_%d',i,j);
        end
    end

    b = Bit(2,2);
    for i = 1:2
        for j = 1:2
            b(i,j).Name = sprintf('b_%d_%d',i,j);
        end
    end

    a([1 3],[1 3]) = b;
    assertTrue(a(1,1) == b(1,1));
    assertTrue(a(1,3) == b(1,2));
    assertTrue(a(3,1) == b(2,1));
    assertTrue(a(3,3) == b(2,2));
    
    b = Discrete({1,2,3},2,2);
    message = '';
    try
        a(1:2,1:2) = b;
    catch e
        message = e.message;
    end
    assertEqual(message,'domains must match');
    
end