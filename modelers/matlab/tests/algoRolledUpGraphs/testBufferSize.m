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

function testBufferSize()

    %Test specifying buffer size when creating graph
    x = Bit();
    y = Bit();
    ng = FactorGraph(x,y);
    ng.addFactor(@xorDelta,x,y);

    fg = FactorGraph();
    b = BitStream();
    fg.addFactor(ng,5,b,b.getSlice(2));

    assertEqual(length(b.Variables),6);
    assertEqual(length(fg.Variables),6);
    assertEqual(length(fg.Factors),6);
    
    data = repmat([.4 .6],20,1);
    runMMtest(fg,b,data);

    %test specifying buffer size later
    %  test works when making bigger
    fg = FactorGraph();
    b = BitStream();
    fg.addFactor(ng,5,b,b.getSlice(2));

    fgs = fg.FactorGraphStreams{1};
    assertEqual(fgs.BufferSize,5);
    
    fgs.BufferSize = 10;

    assertEqual(length(fg.Variables),11);
    assertEqual(length(fg.Factors),11);

    data = repmat([.4 .6],20,1);    
    runMMtest(fg,b,data);    
    
    %Test changing buffer size on fly
    %  test works when making smaller (data not lost)
    fg = FactorGraph();
    b = BitStream();
    fg.addFactor(ng,10,b,b.getSlice(2));
    fgs = fg.FactorGraphStreams{1};
    
    fgs.BufferSize = 3;

    assertEqual(length(fg.Variables),4);
    assertEqual(length(fg.Factors),4);

    data = repmat([.4 .6],20,1);    
    
    runMMtest(fg,b,data);

    %Now test that we can do this *after* setting the data
    fg = FactorGraph();
    b = BitStream();
    fg.addFactor(ng,10,b,b.getSlice(2));
    
    dataSource = com.analog.lyric.dimple.model.repeated.DoubleArrayDataSource(data);
    b.DataSource = dataSource;

    fgs = fg.FactorGraphStreams{1};
    
    fgs.BufferSize = 3;

    assertEqual(length(fg.Variables),4);
    assertEqual(length(fg.Factors),4);

    data = repmat([.4 .6],20,1);    

    runMMtest(fg,b,data,dataSource);
    
end
