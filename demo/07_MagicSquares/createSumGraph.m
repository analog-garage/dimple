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


function [sg,bits,sumToBits] = createSumGraph(N,numBits,numSumToBits)
    %[g,x,y,z,c] = createAdder(numInBits,numOutBits)
    bits = Bit(N,numBits);
    sumToBits = Bit(numSumToBits,1);
    sg = FactorGraph(bits,sumToBits);

    numExtraBits = numSumToBits-numBits;
    extraBits = Bit(numExtraBits,1);
    extraBits.Input = zeros(numExtraBits,1);
    
    prevResultBits = [bits(1,:) extraBits];

    aGraph = createAdder(numSumToBits,numSumToBits);
    
    for i = 2:N
       
       %Get x y z and c
       x = prevResultBits;
       extraBits = Bit(numExtraBits,1);
       extraBits.Input = zeros(numExtraBits,1);
       y = [bits(i,:) extraBits];
       
       if i == N
           z = sumToBits;
       else
           z = Bit(numSumToBits,1);
       end
       
       c = Bit(numSumToBits+1,1);
       c(1).Input = 0;
       c(end).Input = 0;
       
       sg.addFactor(aGraph,x,y,z,c);
       
       prevResultBits = z;
    end
    
    sg.Solver.setNumIterations(100);
end
