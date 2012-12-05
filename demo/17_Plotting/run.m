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



%First we build a Factor Graph to use for plotting examples
fg = FactorGraph();
b = Bit(6,1);
for i = 1:6
    b(i).Name = sprintf('b%d',i);
end

%We use Label rather than Name for the factors so that we can assign
%them the same Label.  Name must be a unique identifier,
%Label is just used for printing/plotting.
f1 = fg.addFactor(@xorDelta,b(1:4));
f1.Label = 'f';
f2 = fg.addFactor(@xorDelta,b(4:6));
f2.Label = 'f';

pause_time = 1;

%Calling plot with no arguments shows no labels.  It draws variables as
%circles and factors as squares.
fg.plot();

pause(pause_time);

%The following is equivalent to the previous plot command.  We are simply
%explicitly turning off labels.
fg.plot('labels',false);

pause(pause_time);

%Now we turn on the labels.  Now we see the names we assigned to the
%various nodes and variables of the Factor Graph.
fg.plot('labels',true);

pause(pause_time)

%We can specify a subset of nodes to plot
fg.plot('labels',true,'nodes',{b(1:2),f1});

pause(pause_time)

%By can set a global color for all the nodes in the graph.
fg.plot('labels',true,'color','g');

pause(pause_time)

%We can specify a color for one node in the graph.
fg.plot('labels',true,'nodes',{b(1:2),f1},'color',b(1),'g');

pause(pause_time)

%We can specify colors for multiple nodes in the graph.
fg.plot('labels',true,'nodes',{b(1:2),f1},'color',{b(2),f1},{'r','c'});

pause(pause_time)

%We can mix setting a global color, colors for a single node mutliple
%times, and colors for multiple nodes.  The global color is used in all
%cases where a color has not explicitly been set for a node.
fg.plot('labels',true,'color',b(1),'g','color',b(2),'r','color',{b(3),b(4)},{'y','c'},'color','b');

pause(pause_time)

%We can also specify a root node on which we perform a depth first search
%up to a specific depth and then only print nodes up to that depth.
for depth = 0:5

    %Furthermore, we color the root node green so we know which is the root
    %node.
    fg.plot('labels',true,'depth',b(1),depth,'color',b(1),'g','color','b');
    
    pause(pause_time);
end

%The following shows how using the depth feature we might be able to find
%out interesting information.  Here we increase the depth until we visually
%see a loop.
H = blockVandermonde();
[ldpc,vars] = createLdpc(H);
v = vars(1);

for depth = 0:6
    ldpc.plot('depth',v,depth);
    
    pause(pause_time);
end


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Plotting with Nesting
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%By default, the plotting method ignores hierarchy and plots the flattened
%graph.  If the user specifies the 'nesting' parameter, however, they can
%specify how deep to descend into the hierarchy before considering
%NestedGraphs to be Factors and plotting them as such.
%
%When labels are turned off, nested graphs are displayed as triangles

%First let's build a graph with three levels of nesting.
b = Bit(2,1);
template1 = FactorGraph(b);
iv = Bit();
template1.addFactor(@xorDelta,b(1),iv);
template1.addFactor(@xorDelta,b(2),iv);

b = Bit(2,1);
template2 = FactorGraph(b);
iv = Bit();
template2.addFactor(template1,b(1),iv);
template2.addFactor(template1,b(2),iv);

template2.plot();

b = Bit(2,1);
fg = FactorGraph(b);
iv = Bit();
fg.addFactor(template2,b(1),iv);
fg.addFactor(template2,b(2),iv);


%Here we show the graph plotted with various levels of nesting.
for i = 0:2
    fg.plot('nesting',i);
    pause(pause_time);
end


%Wwe can retrieve an instance of a nested graph
fg.NestedGraphs{1}.plot('nesting',0);
pause(pause_time);


%Here we mix depth first search with nesting
for i = 0:2
    fg.plot('nesting',0,'depth',iv,i);
    pause(pause_time);
end


