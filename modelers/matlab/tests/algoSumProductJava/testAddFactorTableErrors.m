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

function testAddFactorTableErrors()
    fg = FactorGraph();

    fail = false;

    try
        table = fg.createTable([0 0; 0 0; 1 1],[1 1 1],DiscreteDomain({1,0}),DiscreteDomain({1,0}));
    catch E
        assertTrue(findstr(E.message,'Table Factor contains multiple rows with same set of indices') > 0);
        fail = true;
    end

    assertTrue(fail);



    fg = FactorGraph();
    fail = false;
    try
        table = fg.createTable([0 0; 2 2; 1 1],[1 1 1],DiscreteDomain({1,0}),DiscreteDomain({1,0}));
    catch E
        assertTrue(findstr(E.message,'Index 2 out of bounds for domain 0 with size 2')>0);
        fail = true;
    end

    assertTrue(fail);

end
