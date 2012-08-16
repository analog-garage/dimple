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

function testPrinting

    fg = FactorGraph();
    expectedName = 'MyFactorGraph';
    fg.Name = expectedName;
    x = extractName(evalc('fg'));
    assertTrue(isequal(x,expectedName));
    
    b = Bit(3,1);
    expectedName = 'MyBit';
    b(1).Name = expectedName;
    x = extractName(evalc('b(1)'));
    assertTrue(isequal(x,expectedName));
    
    f = fg.addFactor(@xorDelta,b);
    expectedName = 'MyFactor';
    f.Name = expectedName;
    x = extractName(evalc('f'));
    assertTrue(isequal(x,expectedName));
   
    expectedName = 'fg';
    fg.Label = expectedName;
    x = extractName(evalc('fg'));
    assertTrue(isequal(x,expectedName));
    
    
    expectedName = 'b';
    b.Label = expectedName;
    x = extractName(evalc('b(1)'));
    assertTrue(isequal(x,expectedName));
    x = extractName(evalc('b(2)'));
    assertTrue(isequal(x,expectedName));
    x = extractName(evalc('b(3)'));
    assertTrue(isequal(x,expectedName));
    
    
    expectedName = 'f';
    f.Label = expectedName;
    x = extractName(evalc('f'));
    assertTrue(isequal(x,expectedName));
    
    function name = extractName(result)
        ind = find(result=='=',1);
        name = strtrim(result(ind+1:end));
    end

end
