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

%{
In this demo we try to refine our guess about the weather during the day
based on our observations of whether or not the grass is dry or wet at
night as well as our knowledge of the relationship between weather today
and weather tomorrow.

We say there is a 30% chance it will rain tomorrow if it rained today and a
5% chance it will rain tomorrow if it did not rain today.

Additionally we assume there is an 80% chance the grass will be wet at
night if it rained during the day and a 10% chance that the grass will be
wet if it did not rain today.

This demo shows several ways to create factors that are convenient when
building HMMs.  Additionaly it uses rolled up Factor Graphs.
%}

setSolver('sumproduct');

%First we define our domains.
weatherDomain = {'rain','no rain'};
grassDomain = {'wet','not wet'};

%Now we create our factor that represents the relationship between today
%and tomorrow's weather.
ftStateTransition = FactorTable(weatherDomain,weatherDomain);

%We want to encode the following CPT (Conditial Probability Table)
%P(rain tommorow | rain today) = .3;
%P(rain tomorrow | not rain today) = .05;

%We use the FactorTable set function to do so.
ftStateTransition.set('rain','rain',.3);
ftStateTransition.set('no rain','rain',.7);
ftStateTransition.set('rain','no rain',.05);
ftStateTransition.set('no rain','no rain',.95);

%Now we create our observation factor.
ftObservation = FactorTable(grassDomain,weatherDomain);

%P(grass wet tonight | rain today) = .8;
%P(grass wet tonight | not rain today) = .1;

%Here we use an alternate set syntax.
ftObservation.set({'wet','rain',.8},...
    {'not wet','rain',.2},...
    {'wet','no rain',.1},...
    {'not wet','no rain',.9});

%Now we build our graph that represents one moment in time.
tomorrow = Discrete(weatherDomain);
today = Discrete(weatherDomain);
grass = Discrete(grassDomain);

ng = FactorGraph(tomorrow,today,grass);
ng.addFactor(ftStateTransition,tomorrow,today);
ng.addFactor(ftObservation,grass,today);

%We create our variable streams.
weather = DiscreteStream(weatherDomain);
grass = DiscreteStream(grassDomain);

%Our Factor Graph
fg = FactorGraph();

%And here we relate our variable streams to the nested graph.
rf = fg.addFactor(ng,weather.getSlice(2),weather.getSlice(1),grass);

%We can optionally set the buffer size.
%rf.BufferSize = 3;

%We set out data source to assume we will look at three days and that each
%day we observe wet grass.
ds = DoubleArrayDataSource(repmat([1 0]',1,3));
grass.DataSource = ds;
weather.DataSink = DoubleArrayDataSink();

%Initialize the graph to set messages to uniform uncertainty.
fg.solve();
disp('retrieve results');
while weather.DataSink.hasNext()
    result = weather.DataSink.getNext();
    disp(result);
end

disp('retrieve dynamic results');
%Here we show that we can add additional data dynamically.
for i = 1:3
    ds.add([1 0]);
    fg.advance();
    fg.solve(false);
    weather.get(1).Belief
end


