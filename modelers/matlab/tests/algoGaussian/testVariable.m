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

function testVariable()

    %Test default inputs (sets mu to 0 and sigma to Inf
    %Test a sigma of infinity
    vars = Real(3,1);

    fg = FactorGraph();
    fg.Solver = 'Gaussian';

    f = fg.addFactor(@add,vars(1),vars(2),vars(3));

    fg.solve();

    for i = 1:length(vars)
        assertEqual(vars(i).Belief(1),0);
        assertEqual(vars(i).Belief(2),Inf);
    end

    %And, even if we set mus to non zero, they will be brought back to zero.
    vars(1).Input = [1 Inf];
    vars(2).Input = [2 Inf];
    vars(3).Input = [3 Inf];

    fg.solve();

    for i = 1:length(vars)
        assertEqual(vars(i).Belief(1),0);
        assertEqual(vars(i).Belief(2),Inf);
    end

    %What happens if sigma is 0
    vars(1).Input = [5 0];
    vars(2).Input = [2 0];
    vars(3).Input = [3 0];

    fg.solve();

    for i = 1:length(vars)
        assertEqual(vars(i).Belief(1),vars(i).Input(1));
    end


    %What happens if sigma is 0
    vars(1).Input = [5 0];
    vars(2).Input = [3 0];
    vars(3).Input = [3 0];

    fg.solve();


    for i = 1:length(vars)
        exFound = false;
        message = '';
        try
            vars(i).Belief
        catch E
            exFound = true;
            message = E.message;
        end
        assertEqual(exFound,true);
        assertTrue(findstr(message,'variable node failed in gaussian solver because two incoming messages were certain of conflicting things.')>0);
    end

    %Test a negative sigmas
    exFound = false;
    message = '';
    try
        vars(1).Input = [2 -1];
    catch E
        exFound=true;
        message = E.message;
    end


    assertEqual(exFound,true);
    assertTrue(findstr(message,'expect sigma to be >= 0')>0);

    %Test variable with multiple edges
    inner = Real();
    outter = Real(4,1);
    fg = FactorGraph();
    fg.Solver = 'Gaussian';

    for i = 1:4
       fg.addFactor(@add,inner,outter(i));    
    end

    mus = [10 11 12 13];
    sigmas = [1 2 3 4];

    for i = 1:4
       outter(i).Input = [mus(i) sigmas(i)]; 
    end


    fg.solve();

    %Check inner belief
    rs = 1./ sigmas.^2;
    mu = sum(mus .* rs) / sum(rs);

    expectedSigma = 1/sqrt(sum(rs));

    assertEqual(inner.Belief(1),mu);
    assertEqual(inner.Belief(2),expectedSigma);

    %Check one outter beliefs
    inmus = mus(1:3);
    insigmas = sigmas(1:3);
    rs = 1 ./ insigmas .^ 2;
    mu = sum(inmus .* rs) / sum(rs);
    sigma = 1/sqrt(sum(rs));

    assertEqual(inner.Ports{4}.OutputMsg(1),mu);
    assertEqual(inner.Ports{4}.OutputMsg(2),sigma);

    %Test Sigma of 0 non conflicting in inner node
    mus = ones(4,1)*5;
    sigmas = zeros(4,1);
    for i = 1:4
        outter(i).Input = [mus(1) sigmas(1)];
    end

    fg.solve();

    assertEqual(inner.Belief,[5; 0]);

    %Test Sigma of 0 conflicting in inner node
    mus = ones(4,1)*5;
    sigmas = zeros(4,1);
    for i = 1:4
        outter(i).Input = [mus(1) sigmas(1)];
    end
    outter(1).Input = [6 0];

    exFound = false;
    message = '';
    try
        fg.solve();
    catch E
        exFound = true;
        message = E.message;
    end

    assertTrue(exFound);
    result = findstr(message,'variable node failed in gaussian solver because two incoming messages were certain of conflicting things.');
    result = result(1);
    assertTrue(result>0);


end
