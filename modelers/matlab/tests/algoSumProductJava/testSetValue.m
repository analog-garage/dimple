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

function testSetValue()

    %setValue of single dimensional thing
    domain = {1,2,3};
    d = Discrete(domain,3,1);
    fg = FactorGraph();
    fg.addFactor(@(a) 1, d);
    value = randi(3,3,1);
    d.Value = value;
    assertEqual(d.Value,value);
    expectedBelief = double(repmat(value,1,length(domain)) == repmat(cell2mat(domain),length(value),1));
    assertElementsAlmostEqual(d.Belief, expectedBelief);
    
    %setValue of doubles
    domain = {1,2,3};
    d = Discrete(domain,3,2);
    fg = FactorGraph();
    fg.addFactor(@(a) 1, d);
    value = randi(3,3,2);
    d.Value = value;
    assertEqual(d.Value,value);
    expectedBelief = double(repmat(value,[1,1,length(domain)]) == repmat(reshape(cell2mat(domain),1,1,length(domain)),[size(value,1),size(value,2),1]));
    assertElementsAlmostEqual(d.Belief, expectedBelief);

    domain = {'a','bb','ccc'};
    d = Discrete(domain,2,1);
    fg = FactorGraph();
    fg.addFactor(@(a,b) 1, d(1),d(2));
    value = {'a'; 'bb'};
    msg = '';
    try
        d.Value = value;
    catch E
        msg = E.message;
    end

    assertEqual(msg,'Only scalar domains currently supported');

    %TODO: getValue when sovler not set

    %TODO: setValue when solver not set

    %TODO: setValue of Bits

    %TODO: setValue of strings

    %TODO: setValue when domainElements are arrays

    %TODO: setValue when domain is lost?
    
    %TODO: test that inputs can be restored after setting value
end
