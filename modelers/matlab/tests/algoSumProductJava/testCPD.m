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

function testCPD


    %Try different domains for Z

    %N = 3; %Number of possible sources
    ZDomains = {{1,2},{2,3},{3,4,5}};
    N = length(ZDomains);

    [fg,y,a,zs] = makeCustomFactorCPD(ZDomains);
    vars = struct('fg',fg,'y',y,'a',a,'zs',{zs},'type','custom factor');
    [fg,y,a,zs] = makeMultiplexerCPD(ZDomains);
    vars(2) = struct('fg',fg,'y',y,'a',a,'zs',{zs},'type','multiplexer CPD');
    [fg,y,a,zs] = makeNestedMultiplexerCPD(ZDomains);
    vars(3) = struct('fg',fg,'y',y,'a',a,'zs',{zs},'type','nested multiplexer CPD');

    [fg,y,a,zs] = makeFullCPD(ZDomains);

    rand('seed',1);

    for v = 1:length(vars)

        %Test downard message
        aInput = rand(N,1);
        zInputs = cell(N,1);

        for i = 1:N
           zInputs{i} = rand(1,length(ZDomains{i})); 
           zInputs{i} = zInputs{i} / sum(zInputs{i});
        end

        a.Input = aInput;
        vars(v).a.Input = aInput;

        y.Input = ones(length(y.Domain.Elements),1);

        for i = 1:N
           zs{i}.Input = zInputs{i}; 
           vars(v).zs{i}.Input = zInputs{i};
        end

        fg.solve();
        vars(v).fg.solve();

        belief = y.Belief;


        assertTrue(norm(vars(v).y.Belief-belief) < 1e-15);


        %Test message to a
        yInput = rand(length(y.Domain.Elements),1);
        a.Input = ones(N,1);
        y.Input = yInput;
        vars(v).a.Input = a.Input;
        vars(v).y.Input = yInput;

        fg.solve();
        vars(v).fg.solve();


        assertTrue(norm(vars(v).a.Belief-a.Belief) < 1e-15);

        %Test message to zi
        aInput = rand(N,1);
        a.Input = aInput;
        vars(v).a.Input = aInput;

        %disp('Calculating messages to Z');

        for x = 1:N
            for i = 1:length(zInputs)
                zInputs{i} = rand(1,length(ZDomains{i}));
                zInputs{i} = zInputs{i} / sum(zInputs{i});

                zs{i}.Input = zInputs{i};
                vars(v).zs{i}.Input = zInputs{i};
            end

            fg.solve();
            vars(v).fg.solve();


            assertTrue(norm(zs{x}.Belief - vars(v).zs{x}.Belief) < 1e-15);
        end

    end

    %%Test errors
    fg = FactorGraph();
    Y = Discrete({1,2});
    A = Discrete({1,2});
    Z1 = Discrete({1,2});
    Z2 = Discrete({1,2});

    message = '';
    try
        fg.addFactor(@multiplexerCPD,Y);
    catch e
        message = e.message;
    end

    assertTrue(~ isempty(strfind(message,'Must specify at least Y and A')));
    
    
    message = '';
    try
        fg.addFactor(@multiplexerCPD,Y,A,Z1);
    catch e
        message = e.message;
    end
    
    assertTrue(~ isempty(strfind(message,'Must specify 2 Zs')));
    
    