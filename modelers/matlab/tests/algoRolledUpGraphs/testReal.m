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

function testReal()

    solver = getSolver();
    
    setSolver('Gaussian');

    y = Real();
    x = Real();
    ng = FactorGraph(x,y);

    ng.addFactor(@constmult,y,x,1.1);


    %TODO: allow same args as Real variable
    vars = RealStream();

    fg = FactorGraph();

    fg.addFactor(ng,vars,vars.getSlice(2));

    data = ones(10,2);

    vars.DataSource = com.analog.lyric.dimple.model.repeated.DoubleArrayDataSource(data);


    fg.initialize();

    fg2 = FactorGraph();
    r = Real(size(data,1),1);

    for i = 1:(size(data,1)-1)
       fg2.addFactor(@constmult,r(i+1),r(i),1.1); 
    end

    i = 1;
    while fg.hasNext()
        r(i).Input = [1 1];
        r(i+1).Input = [1 1];

        fg.solve(false);
        fg2.solve();


        assertElementsAlmostEqual(vars.FirstVar.Belief,r(i).Belief);

        fg.advance();
        i = i + 1;
    end

    setSolver(solver);
end
