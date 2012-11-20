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

function testGuess()

    b = Bit(10,2);
    fg = FactorGraph();
    fg.addFactorVectorized(@xorDelta,{b 1});

    %Try setting with cell
    input = cell(10,2);
    for i = 1:numel(input)
        input{i} = randi(2)-1;
    end
    b.Guess = input;
    output = b.Guess;

    assertEqual(input,output)

    input = randi(2,10,2)-1;
    b.Guess = input;
    output = b.Guess;
    assertEqual(num2cell(input),output);
end