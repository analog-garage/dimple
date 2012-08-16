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

%Parameters
snrVal = 4; %noise level
a = 44; %Number of rows to display
b = 24; %Number of cols to display
pa = .1; %Time to pause
numbits=1056; %numbits to diff before displaying success
fs = 70; %fontsize of big red decoded



A = load('matrixout.txt');

[ldpc,x] = createLdpc(A);


msg = randint(704,1);
code = FecEncoding.encode(msg);
[sigma,adjSnr] = FecEncoding.snr2sigma(snrVal);

%Modulate
modulatedsig = FecEncoding.modulateCodeword(code);

%Add Noise
receivedsig = awgn(modulatedsig, adjSnr, 0); % Signal power = 0 dBW

input = (FecEncoding.demodCodeword(receivedsig,sigma));

x.Input = input;
y = reshape(x,44,24);
y2 = reshape(code,44,24);


y = y(1:a,1:b);
v = y2(1:a,1:b);

for i = 1:100
    disp('iterate');
    ldpc.Solver.iterate;
    %tic
    [f boardWidth boardHeight] = drawLDPC(y,v);
    %toc
    pause(pa);

    guesses = x.Value;
    numMsgErrors = sum((guesses(1:numbits) > .5) ~= code(1:numbits));

    if numMsgErrors == 0
        disp('Decoded');
        text(boardWidth/2,boardHeight/2,'Decoded','FontSize',fs,'Color','red',...
            'HorizontalAlignment','center','rotation',-40);
        pause(pa);
        break;
    end
end


disp(numMsgErrors);
