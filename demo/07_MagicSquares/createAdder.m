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

function [g,x,y,z,c] = createAdder(numInBits,numOutBits)
    x = Bit(numInBits,1);
    y = Bit(numInBits,1);
    z = Bit(numOutBits,1);
    c = Bit(numOutBits+1,1);
    g = FactorGraph(x,y,z,c);
    
    numExtraInBits = numOutBits-numInBits;
    if numExtraInBits > 0
        extraXBits = Bit(numExtraInBits,1);
        extraYBits = Bit(numExtraInBits,1);
        x = [x extraXBits];
        y = [y extraYBits];
        setBitInput(extraXBits,zeros(numExtraInBits,1));
        setBitInput(extraYBits,zeros(numExtraInBits,1));
    end
    
    
    for i = 1:numOutBits
       addFactor(g,@adderUnitDelta,x(i),y(i),c(i),c(i+1),z(i)); 
    end
    
    g.Solver.setNumIterations(numOutBits+2);
end
