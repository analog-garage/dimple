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

function testNaNError()
    %We are going to create a FactorGraph which will generate NaNs for
    %the sum product solver.  We'll verify that we catch this and throw
    %an exception.

    a = Variable({1,0});
    b = Variable({1,0});
    c = Variable({1,0});

    fg = FactorGraph();

    %first we set a,b,and c to be equal
    blah = @(x,y,z) (x==y) && (y==z);

    fg.addFactor(blah,a,b,c);
    
    %Now we add a silly constraint that does nothing but make the graph
    %loopy.  This is done simply to make the scheduler pick a flooding
    %scheduler so that we can test that Factor's update() works correctly.
%     fg.addFactor(@(x,y) 1,a,b);   % Removed by jeffb and replaced by setting flooding scheduler
    
    % Force use of a flooding schedule to test update rather than update-edge
    fg.Scheduler = 'FloodingScheduler';


    %It is not sufficient just to set the variables priors to 1 and 0
    %The variable works in log domain so truncates to a min log.  However
    %if we add tons of factors it will sum up the minlog to somet tiny
    %number and induce the NaN.
    for i = 1:100
       fg.addFactor(@(x) x==1,a);
       fg.addFactor(@(x) x==0,b);
    end

    msg = '';
    try
        fg.Solver.iterate(100);
    catch E
        msg = E.message;
       
    end
    
    pass = ~isempty(findstr(msg,'Update failed in SumProduct Solver'));
    
    if ~pass
        msg
    end
    assertTrue(pass);
    
    %Let's do the same thing but without adding hte silly edge so that we
    %can invoke the tree scheduler and test updateEdge.
    a = Variable({1,0});
    b = Variable({1,0});
    c = Variable({1,0});

    fg = FactorGraph();

    blah = @(x,y,z) (x==y) && (y==z);

    fg.addFactor(blah,a,b,c);

    %a.Input = 1;
    %b.Input = 0;
    for i = 1:100
       fg.addFactor(@(x) x==1,a);
       fg.addFactor(@(x) x==0,b);
    end
    
    % Force use of udpate-edge
    scheduler = com.analog.lyric.dimple.schedulers.TreeOrFloodingScheduler();
    scheduler.useOnlyEdgeUpdates;
    fg.setScheduler(scheduler);
    

    msg = '';
    try
        fg.solve();
    catch E
        msg = E.message;
       
    end
    
    pass = ~isempty(findstr(msg,'UpdateEdge failed in SumProduct Solver'));
    
    if ~pass
        msg
    end
    assertTrue(pass);
end
