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


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Markov Model
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Build nested graph
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%Here we build a graph that connects two variables by an xor equation
%An xor equation with only two variables is the equivalent of an equals
%constraint.

in = Bit();
out = Bit();
ng = FactorGraph(in,out);
ng.addFactor(@xorDelta,in,out);


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%create rolled up graph.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%Now we build a FactorGraph that creates an infinite chain of variables.
bs = BitStream();
fg = FactorGraph();

%Passing variable streams as arguments to addFactor will result in a rolled up
%graph.  Passing in a slice of a variable stream specifies a relative offset for
%where the nested graph should be connected to the variable stream.
fs = fg.addFactor(ng,5,bs,bs.getSlice(2));

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%create data source
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
data = repmat([.4 .6],10,1);
ds = DoubleArrayDataSource(data);
bs.DataSource = ds;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%solve and get beliefs
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%We have to initialize now since we will avoid initializing on every call
%to solve.
fg.initialize();

while 1
    
    %The "false" argument specifies that we should avoid re-initializing
    %the messages on the graph.  This is necessary since we don't want to
    %lose information from the past.
    fg.solve(false);
    
    %There are multiple ways to retrieve variables from variable streams.
    %The "FirstVar" property will retrieve the oldest variable in the
    %buffer.
    disp(bs.FirstVar.Belief);

    %hasNext returns true if there is data for every VariableStream
    %connected to a data source in this FactorGraph.
    if ~fg.hasNext()
        break;
    end
    
    %Advance moves the buffer forward, retrieves data for new variables,
    %and keeps track of messages coming from the past.
    fg.advance();
    
end

