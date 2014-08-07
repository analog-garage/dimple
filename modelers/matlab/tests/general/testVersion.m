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

function testVersion
    % NOTE: this can fail if LONG_VERSION is absent which can happen either
    % because gradle was not run.
    dv = dimpleVersion();
    assertTrue(length(regexp(dv,'\d+\.\d+ [^\s]+ \d+-\d+-\d+ \d+:\d+:\d+ [+-]\d+','match')) > 0);
    firstpart = fileparts(mfilename('fullpath'));
    filename = [firstpart filesep() fullfile('..','..','..','..','LONG_VERSION')];
    movefile(filename,'LONG_VERSION_OLD');
    dv = dimpleVersion();
    movefile('LONG_VERSION_OLD',filename);
    assertTrue(length(regexp(dv,'\d+\.\d+ UNKNOWN')) > 0);    
   
    % Verify that the Dimple java layer can detect that we are inside MATLAB.
    assertTrue(com.analog.lyric.dimple.environment.DimpleEnvironment.loadedFromMATLAB());
end