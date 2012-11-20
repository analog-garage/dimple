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

    xorNormalized = @(x) xorDelta(x)/2;
    
    %test no error if normalized
    f = fg.addDirectedFactor(xorNormalized,{b},{b(1),b(3)});
    tmp = f.DirectedTo;
    assertEqual(prod(size(tmp)),2);
    assertTrue(tmp(1)==b(1));
    assertTrue(tmp(2)==b(3));

    %Also set with directedTo
    fg = FactorGraph();
    b = Bit(4,1);
    f = fg.addFactor(xorNormalized,b);
    f.DirectedTo = {b(1),b(3)};
    tmp = f.DirectedTo;
    assertEqual(prod(size(tmp)),2);
    assertTrue(tmp(1)==b(1));
    assertTrue(tmp(2)==b(3));

    %test error if not normalized
    message = '';
    try
        f = fg.addDirectedFactor(myfac,{b},{b(1),b(3)});
    catch e
        message = e.message;
    end

    assertTrue(~isempty(findstr(message,'weights must be normalized')));    
    
    %Test vectorized version
    xorNormalized = @(x) xorDelta(x)/4;
    b = Bit(10,5);
    fg = FactorGraph();
    f = fg.addFactorVectorized(xorNormalized,{b, 1});
    f.DirectedTo = {b(:,1:2), b(:,3)};
    for i = 1:size(b,1)
        assertTrue(isequal(f(i).DirectedTo,b(i,1:3)));
    end

end


