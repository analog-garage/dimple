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

%Load the Check Matrix
H = blockVandermonde();

%Create the ldpc Factor Graph
[ldpc,x] = createLdpc(H);

%Create an all zeros codeword since we don't have a generator matrix
numCols = size(H,2);
input = zeros(numCols,1);

%Flip N bits for our decoder to correct.
noisy_input = input;
N = 10;
bitsToFlip = randi(numCols,N,1);
noisy_input(bitsToFlip) = 1;

%Assign the noisy bits as input.  This is a little funny since
%we're providing 100% certainty of the flipped bits.
x.Input = noisy_input;

%Run BP
ldpc.Solver.setNumIterations(100);
ldpc.solve();

%Retrieve our best guesses
guesses = x.Value;

numMsgErrors = sum(guesses ~= input);

%Display results
disp(['Num Errors after decoding: ' num2str(numMsgErrors)]);
disp(['Corrected: ' num2str(N-numMsgErrors) ' bits']);
