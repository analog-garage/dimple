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

% This program demonstrates LDPC decoding over non-binary fields.
% * Generate a random LDPC parity check matrix H over the
%        finite field with q=2^k elements
% * Construct a noisy codeword:
%   * Imagine that we are using the all-zeros codeword,
%   * add some random Gaussian noise
%   * calculate priors on bits
% * Try to recover the codeword:
%   * Build the corresponding factor graph
%   * Run BP on the factor graph
%   * Graphically illustrate the prior and posterior probabilities using
%     a heatmap.
%   * Check if our recovery worked or not.
%
% The heatmap shows the prior probability on symbols.  Each row corresponds
% to one symbol of the codeword; each column corresponds to one of the
% possible finite field elements.  The color intensity of each entry is
% proportional to the probability of that symbol.
%
% The left heatmap shows the received ("prior") probabilities, and the
% right heatmap shows the error-corrected ("posterior") probabilities.
% We transmit the all-zeroes codeword, so the true probability lies along
% the leftmost column.

% Do Not Modify. For use with automated testing.
global Dimple_DEMO_RESULT;

% Check For Required Toolbox
if isempty(which('awgn'))
    disp('Communications toolbox is required.');
    Dimple_DEMO_RESULT = -1;
    return;
end

%%%%%%%%
% Set some parameters
%%%%%%%%
% We will operate over the field with q=2^k elements
k=5;
q=2^k;
% We have codewords of length n symbols, and with m parity-check equations
% over the finite field.
n=128;
%m=16;
m=64;
% We can also measure the size of the corresponding matrix of bits:
N=n*k;
M=m*k;

SNR=2; % in dB

fprintf('\nOperating over the finite field with 2^%d elements\n',k);
fprintf('Codeword length: %d symbols (%d bits)\n', n,N);
fprintf('Check equations over the finite field: %d\n',m);
fprintf('Code rate: %.3f\n',1-m/n);

%%%%%%%%%
% Generate a random LDPC "parity" check matrix H over the
%        finite field with 2^k elements
%%%%%%%%%

%    We construct an m by n matrix H, whose values encode elements of 
%    the finite field, and H_proto, which just tells us which entries
%    are non-zero.  We use "randperm" to choose distinct elements.  We
%    preferentially select rows with lower density, to keep all the
%    row densities about the same.
H=zeros(m,n);
H_proto=zeros(m,n);
column_density=2;

for col=1:n
    % Find some under-represented rows
    row_counts=sum(H_proto,2);
    sorted_counts=sort(row_counts);
    threshold=sorted_counts(column_density);
    
    under_rows=find(row_counts<=threshold);
    perm=randperm(length(under_rows));
    for i=1:column_density
        rand_row=under_rows(perm(i));
        H(rand_row,col)=randi(q-2)+1;
        H_proto(rand_row,col)=1;
    end
end

sorted_row_densities = sort(sum(H_proto,2));
min_row_density=sorted_row_densities(1);
max_row_density=sorted_row_densities(m);
fprintf('Minimum row density is %d\n',min_row_density);

%%%%%%%%%
% * Construct a noisy codeword:
%%%%%%%%%
%   * Imagine that we are using the all-zeros message, which happens
%     to encode to the all-zeros codeword both over F_q and over bits.
true_codeword_bit = zeros(N,1);

%   * (BPSK modulate and) add some random Gaussian noise
modulated_codeword = ones(N,1) - 2*true_codeword_bit;
if ~which('awgn')
    fprintf('Communications toolbox is required.');
    return;
end
noisy_modulated_codeword = awgn(modulated_codeword,SNR);

%   * calculate priors on bits
cond_prob_0 = exp(-(noisy_modulated_codeword-ones(N,1)).^2);
cond_prob_1 = exp(-(noisy_modulated_codeword+ones(N,1)).^2);
prior=cond_prob_1 ./(cond_prob_0 + cond_prob_1);

%%%%%%%%%%%%
% * Try to recover the codeword:
%%%%%%%%%%%%
%   * Build the corresponding factor graph

setSolver(com.lyricsemi.dimple.solvers.sumproduct.Solver());
fg = FactorGraph();

%     Build bits and set priors
bits=Bit(N,1);
bits.Input=prior;
%bits.Input(1:5)=.5;

%     Build symbols and set priors (by projection)
primPoly = findPrimPoly(k);
ff_codeword = FiniteFieldVariable(primPoly, n,1);
for b=1:n
    start_index = (b-1)*k+1;
    end_index = start_index + k-1;
    fg.addFactor(@finiteFieldProjection,ff_codeword(b),0:k-1,bits(start_index:end_index));
end

% Add "parity" check equations
ff_mults=FiniteFieldVariable(primPoly, m,max_row_density);
for row=1:m
    varIndices = find(H(row,:));
    row_density= length(varIndices);
    for j=1:row_density
        x=H(row,varIndices(j));
        fg.addFactor(@finiteFieldMult, x, ff_codeword(varIndices(j)),...
            ff_mults(row,j));
    end
    
    ff_equation= getNVarFiniteFieldPlus(primPoly, row_density);
    fg.addFactor(ff_equation,ff_mults(row,1:row_density));
end    
    
%   * Run BP on the factor graph
fg.Solver.setNumIterations(1);
fg.solve();

%   * Graphically illustrate the prior and posterior probabilities using a
%     heatmap.
a=ff_codeword.Belief;
subplot(1,2,1);
imagesc(a);
title('Prob Before ECC Recovery');
xlabel(sprintf('FF_{%d} element',q));
ylabel('Codeword symbol');
colorbar;


fg.Solver.setNumIterations(50);
fg.solve();
b=ff_codeword.Belief;

subplot(1,2,2);
imagesc(b);
title('Prob After ECC Recovery');
xlabel(sprintf('FF_{%d} element',q));
ylabel('Codeword symbol');
colorbar;

%   * Check if our recovery worked or not.
% All correct bits are zero in this case.
num_bad_bits=0;
correct_bit=0;
for i=1:n
    % Maximum likelihood symbol:
    symbol=ff_codeword(i).Value;
    % It's also handy to look at "ff_codeword(i).Belief;"
    for j=0:k-1
        num_bad_bits=num_bad_bits +...
            bitxor(correct_bit,bitand(2^j,symbol)/2^j);
    end
end

if num_bad_bits==0
    fprintf('Recovery succeeded!\n');
    
    % Do Not Modify. For use with automated testing.
    Dimple_DEMO_RESULT = 0;
else
    fprintf('Recovery failed.\n');
    
    % Do Not Modify. For use with automated testing.
    Dimple_DEMO_RESULT = 1;
end
