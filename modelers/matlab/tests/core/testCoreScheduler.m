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

function testCoreScheduler()
    %disp('++testCoreScheduler')
    d = com.analog.lyric.dimple.schedulers.DefaultScheduler();
    f = com.analog.lyric.dimple.schedulers.FloodingScheduler();
    s = com.analog.lyric.dimple.schedulers.SequentialScheduler();
    tf = com.analog.lyric.dimple.schedulers.TreeOrFloodingScheduler();
    ts = com.analog.lyric.dimple.schedulers.TreeOrSequentialScheduler();
        fg = FactorGraph();
        fg.setScheduler(d);
        fg.setScheduler(f);
        fg.setScheduler(s);
        fg.setScheduler(tf);
        fg.setScheduler(ts);
        fg.Scheduler = 'DefaultScheduler';
        fg.Scheduler = 'FloodingScheduler';
        fg.Scheduler = 'SequentialScheduler';
        fg.Scheduler = 'TreeOrFloodingScheduler';
        fg.Scheduler = 'TreeOrSequentialScheduler';
        errorThrown = false;
        try
            fg.Scheduler = 'NonExistantScheduler';
        catch e
            errorThrown = true;
        end
        assert(errorThrown);
    %disp('--testCoreScheduler')
end
