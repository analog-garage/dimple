%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013 Analog Devices, Inc.
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

function testMultivariateDataSource()
    rand('seed',1);
    %rand('state',1);
    dsource = MultivariateDataSource();
    dsink = MultivariateDataSink();

    %create data
    F = rand(2);
    A = rand(2,1);
    N = 10;
    data = zeros(2,N);
    data(:,1) = A;
    for i = 2:N
        data(:,i) = F*data(:,i-1);
    end

    fg = FactorGraph();
    fg.Solver = 'gaussian';
    x = RealJoint(2,N,1);
    fg.addFactorVectorized(@constmult,x(2:end),F,x(1:end-1));

    x(1).Input = MultivariateNormalParameters(A,eye(2));

    fg.solve();

    for i = 1:N
       assertTrue(max(abs(x(i).Belief.Mean-data(:,i))) < .01);
    end

    setSolver('gaussian');
    x = RealJoint(2,2,1);
    ng = FactorGraph(x);
    ng.addFactor(@constmult,x(2),F,x(1));

    fg = FactorGraph();
    x = RealJointStream(2);
    fg.addFactor(ng,x,x.getSlice(2));
    x.get(1).Input = MultivariateNormalParameters(A,eye(2));
    x.DataSink = dsink;
    fg.NumSteps = 10;
    fg.solve();
    i = 1;
    while (dsink.hasNext())
        data2(:,i) = dsink.getNext().Mean;
        i = i+1;
    end
    diff = norm(data-data2);
    assertTrue(diff < 1e-6);
    setSolver('sumproduct');
end
