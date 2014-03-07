%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012-2014 Analog Devices, Inc.
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

function testRealSourceAndSink()

% This test comes from an example in the user manual

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%                                                                                                
%Build graph                                                                                                                  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%                                                                                                
a = Real();
b = Real();

ng = FactorGraph(a,b);
ng.addFactor('Product',b,a,1.1);

fg = FactorGraph();
s = RealStream();

fg.addFactor(ng,s,s.getSlice(2));

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%                                                                                                
%set data                                                                                                                     
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%                                                                                                

data = [[1; .1] repmat([0 Inf]',1,10)];
dataSource = DoubleArrayDataSource(data);


s.DataSource = dataSource;
s.DataSink = DoubleArrayDataSink();

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%                                                                                                
%Solve                                                                                                                        
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%                                                                                                
fg.solve();


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%                                                                                                
%get belief                                                                                                                        
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%         
out = [];
while s.DataSink.hasNext();
    out = [out; s.DataSink.getNext()];
end

expectedMeans = [ ...
1.000000000000000; ...
1.100000000000000; ...
1.210000000000000; ...
1.331000000000000; ...
1.464100000000001; ...
1.610510000000001; ...
1.771561000000001; ...
1.948717100000001; ...
2.143588810000002];

expectedStds = [ ...
0.100000000000000; ...
0.110000000000000; ...
0.121000000000000; ...
0.133100000000000; ...
0.146410000000000; ...
0.161051000000000; ...
0.177156100000000; ...
0.194871710000000; ...
0.214358881000000];

assertElementsAlmostEqual(out(:,1), expectedMeans);
assertElementsAlmostEqual(out(:,2), expectedStds);

end
