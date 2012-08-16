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

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testCoreTreeSchedulerBasic()

%disp('++testTreeSchedulerBasic')

if strcmp(class(com.analog.lyric.dimple.solvers.sumproduct.Solver), class(getSolver()))
    
    bits = 8;
    M = 2^bits;
    xDomain = 0:M-1;
    
    g = FactorGraph();
    x = Variable(xDomain,1,1);
    b = Bit(1,bits);
    g.addFactor(@F,x,b);
    bn = Bit(1,bits);
    for i=1:bits
        g.addFactor(@Not,b(i),bn(i));
    end
    bnn = Bit(1,bits);
    for i=1:bits
        g.addFactor(@Not,bn(i),bnn(i));
    end
    
    g.setScheduler(com.analog.lyric.dimple.schedulers.TreeOrFloodingScheduler());
    
    u1 = 100;
    div1 = 10;
    s1 = M/div1;
    w1 = .4;
    u2 = 50;
    div2 = 20;
    s2 = M/div2;
    w2 = .6;
    y = w1 * normpdf(xDomain,u1,s1) + w2 * normpdf(xDomain,u2,s2);
    y = y/sum(y);
    x.Input = y;
    
    g.solve();
    
    bBelief = bnn.Belief;
    
    
    %%%%%% Compare with direct projection onto f's
    
    f = genF(bits);
    fproj = y * f';
    
    
    %%%%%% The beliefs should be nearly equal to the projections
    
    assertElementsAlmostEqual(bBelief,fproj);
    
    
    %disp('--testTreeSchedulerBasic')
else
    %disp('--testTreeSchedulerBasic skipped')
end

end



function valid = F(x,b)
bits = length(b);

sum = 0;
for bit=0:bits-1
    sum = sum + b(bit+1)*2^bit;
end

if (sum == x)
    valid = true;
else
    valid = false;
end
end


function valid = Not(x,y)
valid = (x ~= y);
end

% Probability density function for a normal distribution
function p=normpdf(y,u,s)
p = (1/(s * sqrt(2*pi))) * exp(-(y - u).^2 ./ (2 * s^2));
end


% Explicitly generate the f functions
function f=genF(bits)
M = 2^bits;
f = ones(bits,M);
for bit = 1:bits
    mod = 2^(bit-1); %for 8 bits = 1, 2, 4, 8, 16, 32, 64, 128
    for i = 1:2*mod:(M-mod+1)
        for j = 0:mod-1
            f(bit,i+j)=0;
        end
    end
end

end
