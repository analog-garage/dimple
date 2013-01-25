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

function testBasics()

    solvers = {'minsum','sumproduct'};
    
    for sindex = 1:length(solvers)
    
        setSolver(solvers{sindex});

        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        %Simple HMM with buffersize 1


        N = 10;
        data = repmat([.4 .6],N,1);

        %Create a data source
        dataSource = DoubleArrayDataSource(data);

        %Create a variable stream.
        %TODO: don't couple data source with stream here.  Do it later.
        vs = DiscreteStream(DiscreteDomain({0,1}));

        vs.DataSource = dataSource;
        vs.DataSink = DoubleArrayDataSink();

        %Create our nested graph
        in = Bit();
        out = Bit();
        ng = FactorGraph(in,out);
        ng.addFactor(@xorDelta,in,out);

        %Create our main factor graph
        fg = FactorGraph();

        %Build the repeated graph
        bufferSize = 1;
        fgs = fg.addFactor(ng, bufferSize, vs,vs.getSlice(2));

        %Initialize our messages
        fg.initialize();

        fg2 = FactorGraph();
        b = Bit(N,1);
        b.Input = repmat([.4],N,1);

        %Solve all at once.
        fg.solve();


        i = 1;
        while vs.DataSink.hasNext();

            fg2.addFactor(@xorDelta,b(i),b(i+1));
            fg2.solve();      

            %Get the belief for the first variable
            belief = vs.DataSink.getNext();
            belief2 = b(i).Belief;

            assertElementsAlmostEqual(belief(1),belief2(1));


            i = i+1;

        end

        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        %simple HMM with larger buffer size

        N = 10;
        data = repmat([.4 .6],N,1);

        %Create a data source
        dataSource = DoubleArrayDataSource(data);

        %Create a variable stream.
        %TODO: don't couple data source with stream here.  Do it later.
        vs = DiscreteStream({0,1});
        vs.DataSource = dataSource;

        %Get slices of the variable stream
        %slice1 = vs.getSlice(1,Inf);
        slice2 = vs.getSlice(2);

        %Create our nested graph
        in = Bit();
        out = Bit();
        ng = FactorGraph(in,out);
        ng.addFactor(@xorDelta,in,out);

        %Create our main factor graph
        fg = FactorGraph();

        %Build the repeated graph
        bufferSize = 2;
        fgs = fg.addFactor(ng, bufferSize, vs,slice2);

        vs.get(1).Name = 'v1';
        vs.get(2).Name = 'v2';
        vs.get(3).Name = 'v3';

        %Initialize our messages
        fg.initialize();

        fg2 = FactorGraph();
        b = Bit(N,1);
        b.Input = repmat([.4],N,1);
        fg2.addFactor(@xorDelta,b(1),b(2));

        %Chunk through the data
        tic
        i = 1;
        fg.NumSteps = 0;
        while 1

            %fg2.addFactor(@xorDelta,b(i),b(i+1));
            fg2.addFactor(@xorDelta,b(i+1),b(i+2));

            %Solve the current time step
            fg.solve(false);
            fg2.solve();

            %Get the belief for the first variable
            belief = vs.get(1).Belief;
            belief2 = b(i).Belief;

            assertElementsAlmostEqual(belief(1),belief2(1));


            %If there's data, keep going.
            %TODO: This should ask the graph rather than the graph stream
            if fgs.hasNext
                fg.advance();
            else
                break;
            end

            i = i+1;

            %Let's add more data to make this an infinite stream.
            %dataSource.add([.2 .8]);
        end

    end

end			
		
