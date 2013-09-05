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

function testDataSource

    data = .1:.1:1;
    data = [data; 1-data];


    %ability to set data source from beginning
    dataSource = DoubleArrayDataSource(data);
    b = BitStream();
    b.DataSource = dataSource;

    x = Bit();
    y = Bit();
    ng = FactorGraph(x,y);
    ng.addFactor(@xorDelta,x,y);

    fg = FactorGraph();
    fg.addFactor(ng,b,b.getSlice(2));

    assertElementsAlmostEqual(b.get(1).Input,[.1 .9]');
    assertElementsAlmostEqual(b.get(2).Input,[.2 .8]');

    %ability to set data source after first graph built
    dataSource = DoubleArrayDataSource(data);
    b = BitStream();

    x = Bit();
    y = Bit();
    ng = FactorGraph(x,y);
    ng.addFactor(@xorDelta,x,y);

    fg = FactorGraph();
    fg.addRepeatedFactor(ng,b,b.getSlice(2));

    assertElementsAlmostEqual(b.get(1).Input,[.5 .5]');
    assertElementsAlmostEqual(b.get(2).Input,[.5 .5]');

    b.DataSource = dataSource;

    assertElementsAlmostEqual(b.get(1).Input,[.1 .9]');
    assertElementsAlmostEqual(b.get(2).Input,[.2 .8]');


    %ability to set data source after we've already progressed
    dataSource = DoubleArrayDataSource(data);
    b = BitStream();

    x = Bit();
    y = Bit();
    ng = FactorGraph(x,y);
    ng.addFactor(@xorDelta,x,y);

    fg = FactorGraph();
    fg.addRepeatedFactor(ng,b,b.getSlice(2));

    assertElementsAlmostEqual(b.get(1).Input,[.5 .5]');
    assertElementsAlmostEqual(b.get(2).Input,[.5 .5]');

    fg.advance();


    assertElementsAlmostEqual(b.get(1).Input,[.5 .5]');
    assertElementsAlmostEqual(b.get(2).Input,[.5 .5]');

    %fg.reset();
    
    b.DataSource = dataSource;

    assertElementsAlmostEqual(b.get(1).Input,[.1 .9]');
    assertElementsAlmostEqual(b.get(2).Input,[.2 .8]');



    %ability to reset data source after we've already progressed
    dataSource = DoubleArrayDataSource(data);
    b = BitStream();
    b.DataSource = dataSource;

    x = Bit();
    y = Bit();
    ng = FactorGraph(x,y);
    ng.addFactor(@xorDelta,x,y);

    fg = FactorGraph();
    fg.addRepeatedFactor(ng,b,b.getSlice(2));

    assertElementsAlmostEqual(b.get(1).Input,[.1 .9]');
    assertElementsAlmostEqual(b.get(2).Input,[.2 .8]');

    fg.advance();
    assertElementsAlmostEqual(b.get(1).Input,[.2 .8]');
    assertElementsAlmostEqual(b.get(2).Input,[.3 .7]');

    dataSource = DoubleArrayDataSource(data);
    %fg.reset();
    b.DataSource = dataSource;

    assertElementsAlmostEqual(b.get(1).Input,[.1 .9]');
    assertElementsAlmostEqual(b.get(2).Input,[.2 .8]');
    
    
    %not enough data when set in advance    
    dataSource = DoubleArrayDataSource([.8 .2]');
    b = BitStream();
    b.DataSource = dataSource;

    x = Bit();
    y = Bit();
    ng = FactorGraph(x,y);
    ng.addFactor(@xorDelta,x,y);

    fg = FactorGraph();
    
    message = '';
    try
        fg.addRepeatedFactor(ng,b,b.getSlice(2));
    catch err
        message = err.message;
    end

    assertFalse(isempty(strfind(message,'out of data')));
    
    
    %not enough data when set later;
    dataSource = DoubleArrayDataSource([.8 .2]');
    b = BitStream();

    x = Bit();
    y = Bit();
    ng = FactorGraph(x,y);
    ng.addFactor(@xorDelta,x,y);

    fg = FactorGraph();
    fg.addRepeatedFactor(ng,b,b.getSlice(2));

    message = '';
    try
        b.DataSource = dataSource;
    catch err
        message = err.message;
    end
    assertFalse(isempty(strfind(message,'not enough data in data source')));

   

end
