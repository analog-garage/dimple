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

function testVariable()

    %TODO: test get Belief
    domain = {1,0};
    A=Variable(domain,4,4);
    
    assertEqual(A(1,1).Belief,[.5 .5]);
    
    
    %test fliplr
    b = Bit(1,3);
    b2 = fliplr(b);
    
    for i = 1:size(b,2)
       assertTrue(b(i)==b2(4-i));
    end
    
    %test flipud
    b = Bit(3,1);
    b2 = flipud(b);
    
    for i = 1:size(b,2)
       assertTrue(b(i)==b2(4-i));
    end
    
    %Test ability to call java methods
    fg = FactorGraph();
    x = Bit();
    y = Bit();
    z = x+y;
    
    b = x.Solver.setDamping(0,4);
    assertEqual(x.Solver.getDamping(0),4);
    
    %Test cannot concat domains that don't match
    a = Bit();
    b = Bit();
    
    %First make sure basic concatentation works.
    c = [a b];
    
    d = Variable({1,2,3});
    
    %now try horzcat
    failed = false;
    try
        [a d];
    catch E
        failed = true;
        assertTrue(isequal(E.message,'Cannot concatenate variables with domains that dont match'));
    end
    
    assertTrue(failed);
    
    %then vertcat
    failed = false;
    try
        [a; d];
    catch E
        failed = true;
        assertTrue(isequal(E.message,'Cannot concatenate variables with domains that dont match'));
    end
    
    assertTrue(failed);
end

