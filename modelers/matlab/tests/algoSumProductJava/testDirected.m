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

function testDirected
    fg = FactorGraph();
    b = Bit(4,1);
    b(1).Name = 'b1';
    b(2).Name = 'b2';
    b(3).Name = 'b3';
    b(4).Name = 'b4';
    myfac = @(a) rand();

    %test error if not normalized
    message = '';
    try
        f = fg.addFactorDirected(myfac,{b},{b(1),b(3)});
    catch e
       message = e.message;
    end

    assertTrue(~isempty(findstr(message,'weights must be normalized')));

    %test no error if normalized
    f = fg.addFactorDirected(@xorDelta,{b},{b(1),b(3)});
    tmp = f.DirectedTo;
    assertEqual(prod(size(tmp)),2);
    assertTrue(tmp(1)==b(1));
    assertTrue(tmp(2)==b(3));
end

