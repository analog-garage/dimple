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

%clc

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Choose numbers and set priors
xval = 108;
yval = -8;

num_bits = 8;

xpriors = num2vec(xval,num_bits);
ypriors = num2vec(yval,num_bits);
zpriors = ones(num_bits,1)*.5;

xpriors(4) = .8;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Create 8 bit ripple adder
g = FactorGraph();
x = Bit(1,num_bits);
y = Bit(1,num_bits);
z = Bit(1,num_bits);
c = Bit(1,num_bits+1);
            
for i = 1:num_bits
    g.addFactor(@adderUnitDelta,x(i),y(i),c(i),c(i+1),z(i)); 
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Initialize graph, set priors and solve
g.initialize();
            
            
c(1).Input = 0;
x.Input = xpriors;
y.Input = ypriors;
            
g.Solver.setNumIterations(num_bits+2);
g.solve();

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Get back results
xvals = x.Value;
yvals = y.Value;
zvals = z.Value;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Convert beliefs to number and display
z = vec2num(zvals,1);
xandy = ([xpriors; ypriors]);
disp('x and y');
disp(xandy);
disp('z guesses');
disp(zvals');
disp('result');
disp([num2str(xval) ' + ' num2str(yval) ' = ' num2str(z)]);

