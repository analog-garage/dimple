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

%Pick an SNR value for this run
snrVal = 3;

%Set the solver
setSolver('sumproduct');


%Create ldpc factor graph
load A;
blockLength = size(A,2);
numCheckEquations = size(A,1);

ldpc = FactorGraph();
x = Bit(blockLength,1);

for i = 1:numCheckEquations
    varIndices = find(A(i,:));
    gd = getNBitXorDef(length(varIndices));
    ldpc.addFactor(gd,x(varIndices));
end

%Create the message
msg = randi(2,704,1)-1;

%Encode
code = FecEncoding.encode(msg);

%Modulate
modulatedsig = FecEncoding.modulateCodeword(code);

%Add Noise
[sigma,adjSnr] = FecEncoding.snr2sigma(snrVal);
receivedsig = awgn(modulatedsig, adjSnr, 0); % Signal power = 0 dBW

%Demodulate to get probabilities
input = FecEncoding.demodCodeword(receivedsig,sigma);

%Set the inputs.
x.Input = input;

%Solve
ldpc.Solver.setNumIterations(100);
tic
ldpc.solve();
toc

%Retrieve guesses and set number of errors.
guesses = x.Value;
numMsgErrors = sum((guesses(1:704) > .5) ~= msg);
disp(numMsgErrors);

