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

function testGibbsTableFactor

N = 100;
bufferSize = 1;
seed = 0;
rs=RandStream('mt19937ar');
RandStream.setGlobalStream(rs);
reset(rs,seed);

actual_input = zeros(1,N);
actual_input(1) = randi(2)-1;
for i = 2:N
    %t = [0.1, 0.9; 0.8, 0.2];
    t = [0, 1; 1, 0];
    if rand() > t(actual_input(i-1)+1,1)
        actual_input(i) = 1;
    else
        actual_input(i) = 0;
    end
end

%sigma = 1;
%input = actual_input + randn(size(actual_input))*sigma;
%input(input<0) = 0;
%input(input>1) = 1;
input = actual_input;


source1 = DoubleArrayDataSource([input; 1-input]);
sink1 = DoubleArrayDataSink();
[fg1, x1] = createGraph(source1, sink1, bufferSize);
fg1.Solver = 'SumProduct';
fg1.solve();
b1 = sink1.Array;

source2 = DoubleArrayDataSource([input; 1-input]);
sink2 = DoubleArrayDataSink();
[fg2, x2] = createGraph(source2, sink2, bufferSize);
fg2.Solver = 'Gibbs';
fg2.solve();
%{
%fg2.Solver.setNumRestarts(200);
fg2.initialize();
fg2.Solver.saveAllSamples();
while 1
   fg2.solveOneStep();
   if ~ fg2.hasNext();
       break;
   end
   
   fg2.advance();
end
%}


b2 = sink2.Array;

diff = b2(:,(end-10):end) - b1(:,(end-10):end);
ndiff = norm(diff);
assertTrue(ndiff < 1e-100);


end

function [fg,x] = createGraph(source, sink, bufferSize)

xi = Bit();
xo = Bit();
sg = FactorGraph(xi,xo);
sg.addFactor(@myFactor,xi,xo);

fg = FactorGraph();
x = BitStream();
fg.addFactor(sg,bufferSize,x,x.getSlice(2));
x.DataSource = source;
x.DataSink = sink;

end

function v = myFactor(in,out)

t = [0, 1; 1, 0];
v = t(in+1,out+1);

end