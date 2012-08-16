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

function setSolver(solver,varargin)
    global g_dimpleSolver;
    global g_dimpleModeler;
    
    if ischar(solver)
       registry = getSolverRegistry();
       solver = registry.get(solver);
       solver = solver(varargin{:});
    end
    
    if strcmp(class(solver),'CSolver')==1
        g_dimpleModeler = ModelFactory();
    else
        g_dimpleModeler = com.analog.lyric.dimple.matlabproxy.ModelFactory();
    end
    g_dimpleModeler.setSolver(solver);
    g_dimpleSolver = solver;
end
