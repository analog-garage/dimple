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

setSolver('sumproduct');

%% Test basic vectorized discrete
for version = 1:3

    %100 time steps
    N = 100;

    %Now
    d1 = Discrete(0:1,3,3);
    %Future
    d2 = Discrete(0:1,3,3);

    %Create graph
    ng = FactorGraph(d1,d2);

    %Create checkerboard constraints.  This is a grid that enforces that all
    %neighbors must be different.
    ng.addFactorVectorized(@(a,b) a ~= b, d1(1:end-1,:),d1(2:end,:));
    ng.addFactorVectorized(@(a,b) a ~= b, d1(:,1:end-1),d2(:,2:end));

    %Each time step should have the same values.
    ng.addFactorVectorized(@(a,b) a == b, d1, d2);

    %Create the streaming graph.
    fg = FactorGraph();
    dd = DiscreteStream(0:1,3,3);
    fg.addFactor(ng,dd,dd.getSlice(2));

    %Create the inputs
    sinput0 = [0.4 0.6 0.4; ...
              0.6 0.4 0.6; ...
              0.4 0.6 0.4];
    sinput1 = 1-sinput0;

    sinput = zeros(3,3,2);
    sinput(:,:,1) = sinput0;
    sinput(:,:,2) = sinput1;

    input = zeros(3,3,2,N);
    for i = 1:N
        input(:,:,:,i) = sinput;
    end

    %Try three different ways of setting the input
    if version == 1
        dsrc = DoubleArrayDataSource([3 3],input);
    elseif version == 2

        dsrc = DoubleArrayDataSource([3 3]);
        for i = 1:N
            dsrc.add(sinput);
        end
    else
        dsrc = DoubleArrayDataSource([3 3]);
        dsrc.add(input);
    end

    dsink = DoubleArrayDataSink([3 3]);

    %Set the data source and data sink
    dd.DataSource = dsrc;
    dd.DataSink = dsink;

    %Solve the graph
    fg.solve();

    %Get all the results
    while dsink.hasNext()
       tmp = dsink.getNext();
    end

    %Look at the last result
    expected = sinput1 > 0.5;
    diff = expected-tmp(:,:,2);
    assertTrue(norm(diff) < 1e-100);
end



