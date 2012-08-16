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

function testAddFactorErrors( )

    msg = '';
    try

        domain2={[0;0];[0;1];[1;0];[1;1]}; 
        X=Variable(domain2,2,1); 
        fg=FactorGraph(); 
        weirdxor=@(x) 1;
        fg.addFactor(weirdxor,X);
    catch E
        msg = E.message;
    end

    pass = ~isempty(findstr(msg,'Dimple does not currently support'));
    assertTrue(pass);
end

