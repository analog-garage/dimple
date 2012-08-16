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

function runMMtest(fg,stream,data,dataSource)
    
    bufferSize = fg.FactorGraphStreams{1}.BufferSize;

    if nargin < 4        
        dataSource = DoubleArrayDataSource(data);
        stream.DataSource = dataSource;
    end
    
    
    dataSize = size(data,1);

    fg.initialize();
    
    fg2 = FactorGraph();
    b = Bit(dataSize,1);
    
    for i = 1:(dataSize-1);
        fg2.addFactor(@xorDelta,b(i),b(i+1));
    end
    
    for i = 1:(bufferSize+1)
        b(i).Input = data(i,2);
    end
    
    i = 1;
    while dataSource.hasNext()
        fg.solve(false);
        fg2.solve();
        
        assertEqual(stream.FirstVar.Belief(2),b(i).Belief);
        
        fg.advance();
        b(bufferSize+i+1).Input = data(bufferSize+i+1,2);
        i = i + 1;
    end
end
