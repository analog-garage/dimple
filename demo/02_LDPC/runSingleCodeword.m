%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2011, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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
msg = randint(704,1);

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

