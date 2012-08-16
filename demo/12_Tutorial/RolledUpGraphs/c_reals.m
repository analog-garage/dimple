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


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Set solver type so we can create reals
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
setSolver('Gaussian');

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Build graph
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
a = Real();
b = Real();

ng = FactorGraph(a,b);
ng.addFactor(@constmult,b,a,1.1);

fg = FactorGraph();
s = RealStream();

fg.addFactor(ng,s,s.getSlice(2));

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%set data
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

data = [1 .1; repmat([0 Inf],10,1)];
dataSource = DoubleArrayDataSource(data);

s.DataSource = dataSource;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Solve
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

fg.initialize();

while fg.hasNext()
    fg.solve(false);    
    disp(s.FirstVar.Belief(1));    
    fg.advance();
end
