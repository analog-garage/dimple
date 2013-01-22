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

function testCustomScheduleVectors()

    %Create a Factor Graph with a vector of variables and vector of factors
    fg = FactorGraph();
    b = Bit(4,4);
    f = fg.addFactorVectorized(@xorDelta,{b,[1]});
    
    %Set the schedule using vector objects.
    fg.Schedule = {b,f};

    %Figure out the expected serialized schedule
    schedule = cell(8,1);
    for i = 1:16
        schedule{i} = b(i).VectorObject.getModelerNode(0);
    end
    for i = 1:4
        schedule{16+i} = f(i).VectorObject.getModelerNode(0);
    end
    
    %Compare with the actual schedule
    it = fg.VectorObject.getModelerNode(0).getSchedule().iterator();
    
    i = 1;
    while it.hasNext()
       tmp = it.next(); 
       n = tmp.getNode();
       assertEqual(n,schedule{i});
       i = i+1;
    end
end