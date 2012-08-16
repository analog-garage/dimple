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

function [ error_rate ] = recover_text( t1,t2,t3, prob_trigrams,...
    hint_rate, verbosity, overlap_length)
% Given three texts t1,t2,t3, represented as ASCII streams, suppose
% we only observe the XOR of any pair of texts.  Our goal is to recover the
% original texts from the XORs.  To make life easier, we cheat and reveal
% every 'hint_rate' byte.  'prob_trigrams' is a matrix listing
% probabilities of the most common trigrams.  It's culled from Google's
% n-gram data and has some weird features (only a-z,A-Z,"."," ", and it
% tokenizes such that periods are always separated by white space on both
% sides).



calc_overlap_length=min(min(length(t1),length(t2)),length(t3));

if nargin < 7 || overlap_length > calc_overlap_length
    overlap_length = calc_overlap_length;
end

t1=t1(1,1:overlap_length);
t2=t2(1,1:overlap_length);
t3=t3(1,1:overlap_length);

% Type coersion:
%   char=>int      a+0
%   int=>char      char(a)

% XOR text:
observed12=bitxor(t1+0,t2+0);
observed13=bitxor(t1+0,t3+0);
observed23=bitxor(t2+0,t3+0);

% Build factor graph.
% The domain is {space, period, A-Z, a-z}
fg=FactorGraph();
domain_arr=char([32 46 65:90 97:122]);
domain_cell=cell(size(domain_arr));
for i=1:length(domain_arr)
    domain_cell{i}=domain_arr(i);
end
inv_domain=-ones(122,1);
for i=1:length(domain_cell)
    inv_domain(0+domain_cell{i})=i-1;
end
plaintext=Discrete(domain_cell, overlap_length,3);
% Add in XOR constraints
for i=1:overlap_length
    fg.addFactor(@ascii_xor,plaintext(i,1), plaintext(i,2),observed12(i));
    fg.addFactor(@ascii_xor,plaintext(i,1), plaintext(i,3),observed13(i));
    fg.addFactor(@ascii_xor,plaintext(i,2), plaintext(i,3),observed23(i));
end
% Add in trigram constraints
val = prob_trigrams(:,1);
ind = inv_domain(prob_trigrams(:,2:4));
tbl = fg.createTable(ind,val,plaintext.Domain,plaintext.Domain,plaintext.Domain);
for i=1:overlap_length-2
    fg.addFactor(tbl,plaintext(i,1),plaintext(i+1,1),plaintext(i+2,1));
    fg.addFactor(tbl,plaintext(i,2),plaintext(i+1,2),plaintext(i+2,2));
    fg.addFactor(tbl,plaintext(i,3),plaintext(i+1,3),plaintext(i+2,3));
end

for i=1:hint_rate:overlap_length
    prior=zeros(length(domain_cell),1);
    alph1=inv_domain(0+t1(i))+1;
    prior(alph1)=1;
    plaintext(i,1).Input=prior;
end

fg.NumIterations=100;
%fg.setSolver('gp5',pwd)
fg.solve;

% Display results

error_rate = calculate_results(t1,t2,t3,plaintext,verbosity,overlap_length,hint_rate);






end

