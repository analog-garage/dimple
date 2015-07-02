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

function testSetValueGibbs()

    %setValue of single dimensional thing
    domain = {1,2,3};
    d = Discrete(domain,3,1);
    fg = FactorGraph();
    fg.Solver = 'Gibbs';
    fg.addFactor(@(a) sum(a), d);
    value = randi(3,3,1);
    d.FixedValue = value;
    fg.initialize();
    assert(all(reshape(d.hasFixedValue,numel(value),1) == true(numel(value),1)));
    assertEqual(d.FixedValue,value);
    assertEqual(d.Value,value);
    expectedBelief = double(repmat(value,1,length(domain)) == repmat(cell2mat(domain),length(value),1));
    assertElementsAlmostEqual(d.Belief, expectedBelief);
    assertElementsAlmostEqual(fg.Solver.getTotalPotential(), -log(sum(value)), 'absolute');
    
    %setValue of doubles
    domain = {1,2,3};
    d = Discrete(domain,3,2);
    fg = FactorGraph();
    fg.Solver = 'Gibbs';
    fg.addFactor(@(a) sum(a(:)), d);
    value = randi(3,3,2);
    d.FixedValue = value;
    fg.initialize();
    assert(all(reshape(d.hasFixedValue,numel(value),1) == true(numel(value),1)));
    assertEqual(d.FixedValue,value);
    assertEqual(d.Value,value);
    expectedBelief = double(repmat(value,[1,1,length(domain)]) == repmat(reshape(cell2mat(domain),1,1,length(domain)),[size(value,1),size(value,2),1]));
    assertElementsAlmostEqual(d.Belief, expectedBelief);
    assertElementsAlmostEqual(fg.Solver.getTotalPotential(), -log(sum(value(:))), 'absolute');

    domain = {'a','bb','ccc'};
    d = Discrete(domain,2,1);
    fg = FactorGraph();
    fg.Solver = 'Gibbs';
    fg.addFactor(@(a,b) 1, d(1),d(2));
    value = {'a'; 'bb'};
    msg = '';
    try
        d.FixedValue = value;
    catch E
        msg = E.message;
    end
    assertEqual(msg,'Only scalar domains currently supported');
    
    % Test real variables
    domain = [-10, 10];
    a = Real(domain);
    b = Real(domain);
    fg = FactorGraph();
    fg.Solver = 'Gibbs';
    fg.addFactor('Equality', a, b);
    a.Input = {'Normal',1,1};
    assert(isa(a.Input, 'NormalParameters'));
    value = rand();
    assert(~a.hasFixedValue);
    a.FixedValue = value;
    assert(isempty(a.Input));
    assert(a.hasFixedValue);
    assertEqual(a.FixedValue,value);
    assert(~b.hasFixedValue);
    b.FixedValue = value;
    fg.initialize();
    assert(b.hasFixedValue);
    assertEqual(b.FixedValue,value);
    assertEqual(fg.Solver.getTotalPotential(), 0);
    b.FixedValue = value + 1;
    assert(b.hasFixedValue);
    assertEqual(b.FixedValue,value+1);
    assertEqual(fg.Solver.getTotalPotential(), Inf);
    a.Input = {'Normal',1,1};
    assert(~a.hasFixedValue);
    try
        a.FixedValue = 20;   % Out of domain bounds
    catch E
        msg1 = E.message;
    end
    assert(~isempty(strfind(msg1,'Attempt to set fixed value outside of variable domain.')));
    try
        a.FixedValue = -20;   % Out of domain bounds
    catch E
        msg2 = E.message;
    end
    assert(~isempty(strfind(msg2,'Attempt to set fixed value outside of variable domain.')));

    
    % No beliefs or values for reals at this point


    %TODO: getValue when sovler not set

    %TODO: setValue when solver not set

    %TODO: setValue of Bits

    %TODO: setValue of strings

    %TODO: setValue when domainElements are arrays

    %TODO: setValue when domain is lost?
    
    %TODO: test that inputs can be restored after setting value
end
