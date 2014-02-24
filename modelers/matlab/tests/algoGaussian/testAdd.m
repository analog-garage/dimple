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

function testAdd()

    %TODO: sigma of 0
    %TODO: signa of Inf

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %First create a Factor Graph with a = b+c
    a = Real();
    b = Real();
    c = Real();

    mus = [8 10 -1];
    sigmas = [1 2 3];

    a.Input = [mus(1) sigmas(1)];
    b.Input = [mus(2) sigmas(2)];
    c.Input = [mus(3) sigmas(3)];

    fg = FactorGraph();
    fg.Solver = 'Gaussian';

    f = fg.addFactor(@add,a,b,c);

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %Solver the graph
    fg.solve();

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %Now compare against expected

    %First, a Factor computes it's mus as follows:
    %uout = ua + ub + uc
    %ub = uout-ua-uc
    %and it's sigmas as follows:
    %sigma^2 = othersigma^2 + theothersigma^2 ...

    %We'll compare the mus first.
    mu1 = mus(2)+mus(3);
    mu2 = mus(1)-mus(3);
    mu3 = mus(1)-mus(2);

    assertEqual(mu1,f.Ports{1}.OutputMsg.getMean);
    assertEqual(mu2,f.Ports{2}.OutputMsg.getMean);
    assertEqual(mu3,f.Ports{3}.OutputMsg.getMean);

    %Now the sigmas
    sigma1 = sqrt(sigmas(2)^2+sigmas(3)^2);
    sigma2 = sqrt(sigmas(1)^2+sigmas(3)^2);
    sigma3 = sqrt(sigmas(1)^2+sigmas(2)^2);
    assertElementsAlmostEqual(sigma1,f.Ports{1}.OutputMsg.getStandardDeviation);
    assertElementsAlmostEqual(sigma2,f.Ports{2}.OutputMsg.getStandardDeviation);
    assertElementsAlmostEqual(sigma3,f.Ports{3}.OutputMsg.getStandardDeviation);

    %Second, we'll calculate the beliefs to compare against the output belief.
    %Variable mus are calculated as follows:
    %mu is weighted average mu1*R1 + mu2*r2 / TotalR
    %And Rs (1/sigma^2) are calculated as
    %R = sum of rs

    %Convert sigmas to Rs
    inputRs = 1 ./ sigmas.^2;
    funcRs = 1 ./ [sigma1 sigma2 sigma3].^2;

    %Get the mus
    funcMus = [mu1 mu2 mu3];

    %Calculate expected sigmas
    Rs = inputRs + funcRs;
    expectedSigmas = sqrt(1./Rs);

    assertElementsAlmostEqual(expectedSigmas(1),a.Belief(2));
    assertElementsAlmostEqual(expectedSigmas(2),b.Belief(2));
    assertElementsAlmostEqual(expectedSigmas(3),c.Belief(2));

    %Calculate expected mus
    expectedMus = (mus .* inputRs + funcMus .* funcRs) ./ (inputRs + funcRs);
    assertElementsAlmostEqual(expectedMus(1),a.Belief(1));
    assertElementsAlmostEqual(expectedMus(2),b.Belief(1));
    assertElementsAlmostEqual(expectedMus(3),c.Belief(1));
    
    
    %%%%%%%%%%%
    %Test attempt to add discrete variable
    a = Discrete({1,2,3});
    b = Real();
    exFound = false;
    message = '';
    try
        fg.addFactor(@add,a,b);
    catch E
        exFound = true;
        message = E.message;
    end
    assertEqual(exFound,true);
    assertTrue(findstr(message,'Cannot connect discrete variable to this factor')> 0);
end

