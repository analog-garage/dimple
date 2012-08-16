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

function [msGraph,b] = createMagicSquaresGraph(N)
    %%%%%%%%%%%%%%%%%%%%%%%%%
    % Setup parameters
    %%%%%%%%%%%%%%%%%%%%%%%%%
    maxNum = N^2-1;
    numBits = ceil(log2(maxNum));
    sumToVal = maxNum/2*N;

    sumToValNumBits = ceil(log2(sumToVal));

    % Create Bits
    b = Bit(N,N,numBits);

    %%%%%%%%%%%%%%%%%%%%%%%%%
    % Create Graph
    %%%%%%%%%%%%%%%%%%%%%%%%%
    msGraph = FactorGraph(b);


    bitxors = Bit(N^2,N^2,numBits);
    oneVar = Variable([1]);

    % Create constraints for N choose K pairs not being equal

    %setBitInput(b(1,1,:),[0.01 0.01 0.01 .99]);

    for i = 1:(N^2-1)
        for j = i+1:N^2
            for k = 1:numBits
                [x,y] = ind2sub([N N],i);
                b1 = b(x,y,k);
                [x,y] = ind2sub([N N],j);
                b2 = b(x,y,k);
                x = bitxors(i,j,k);
                msGraph.addFactor(@xorDelta,[b1 b2 x]);
            end

            msGraph.addFactor(@orDelta,bitxors(i,j,:),oneVar);
        end
    end

    addToBits = Bit(sumToValNumBits,1);
    vec = num2vec(sumToVal,sumToValNumBits);
    addToBits.Input = vec;

    sg = createSumGraph(N,numBits,sumToValNumBits);

    dosumconstraints = 1;

    if dosumconstraints

        %Create constraints that columns must sum to some value
        for columnNum=1:N
            bs = b(:,columnNum,:);
            bs = reshape(bs,N,numBits);
            addFactor(msGraph,sg,bs,addToBits);
        end

        %Create constraints that rows must sum to some value
        for rowNum=1:N
            bs = b(rowNum,:,:);
            bs = reshape(bs,N,numBits);
            addFactor(msGraph,sg,bs,addToBits);
        end

        %Create constraints that diagonals must sum to some value
        for i = 1:N
            bs(i,:) = b(i,i,:);
        end
        addFactor(msGraph,sg,bs,addToBits);

        for i = 1:N
            bs(i,:) = b(i,end-i+1,:);
        end
        addFactor(msGraph,sg,bs,addToBits);
    end

    msGraph.Solver.setNumIterations(100);
 
end
