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

clc;

N = 4;

%%%%%%%%%%%%%%
%Create Graph
%%%%%%%%%%%%%%

[msGraph,b] = createMagicSquaresGraph(N);


%%%%%%%%%%%%%%
%Set Input
%%%%%%%%%%%%%%
priors = rand(N,N,4);
for i = 1:N
    for j = 1:N
        for k = 1:4
            setBitInput(b(i,j,k),rand);
        end
    end
end

%%%%%%%%%%%%
%Solve
%%%%%%%%%%%%
setNumIterations(msGraph,100);

tic
solve(msGraph);
toc
beliefs = getBelief(b);

guess = zeros(N,N);
for i = 1:N
    for j = 1:N
        vec = getBelief(b(i,j,:));
        num = vec2num(vec);
        %num = softbyte2gauss(vec);
        guess(i,j) = num;
    end
end

guess

sum(guess,1)
sum(guess,2)
sum(diag(guess))
sum(diag(fliplr(guess)))
