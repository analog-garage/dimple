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

function addDimplePath(DimpleDirectory, XUnitDirectory, JavaDir, BerToolDirectory, TestFilesDirectory)

    if nargin < 1
       f = fileparts(mfilename('fullpath')); 
       DimpleDirectory = fullfile(f, '..');
    end

    setenv('_Dimple_START_PATH', DimpleDirectory);

    dimple_base = DimpleDirectory;
    loc = strfind(dimple_base,fullfile([filesep 'modelers'], 'matlab'));
    dimple_base = dimple_base(1:loc-1);
    
    bLog = false;

    Paths = {};

	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    Paths{length(Paths) + 1} = fullfile(DimpleDirectory, 'core');
    Paths{length(Paths) + 1} = fullfile(DimpleDirectory, 'lib');
    Paths{length(Paths) + 1} = fullfile(DimpleDirectory, 'factorfunctions');
    Paths{length(Paths) + 1} = fullfile(DimpleDirectory, 'lib', 'GraphViz2Mat1.2');
    Paths{length(Paths) + 1} = fullfile(DimpleDirectory, 'util');
    Paths{length(Paths) + 1} = fullfile(DimpleDirectory, 'tests');
    
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	LyricPath = fullfile(DimpleDirectory, 'lyric');
    if exist(LyricPath, 'dir')
		Paths{length(Paths) + 1} = LyricPath;
		bLog = true;
    end
    
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	CPPPath = fullfile(dimple_base, 'solvers', 'cpp');
	
	if exist(CPPPath, 'dir')
		Paths{length(Paths) + 1} = CPPPath;
		Paths{length(Paths) + 1} = fullfile(DimpleDirectory, 'modelfactory');
		bLog = true;
	end
	
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	ActualXUnitDirectory = fullfile(DimpleDirectory, 'lib', 'xunit_dist', 'matlab_xunit', 'xunit');
	if nargin > 1
		ActualXUnitDirectory = XUnitDirectory;
	end
	Paths{length(Paths) + 1} = ActualXUnitDirectory;

	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    
    JavaBuildDir = fullfile(dimple_base, 'solvers', 'java', 'build');
    JavaClassDir = fullfile(JavaBuildDir, 'classes', 'main');
    
    if exist(JavaClassDir,'dir')
        ActualJavaDir = JavaClassDir;
    else
        ActualJavaDir = fullfile(dimple_base, 'solvers', 'lib', 'dimple.jar');
    end
    
    JavaBuildJarsDir = fullfile(JavaBuildDir, 'external-libs');
    if exist(JavaBuildJarsDir, 'dir')
        lib_dir = JavaBuildJarsDir;
    else
        lib_dir = fullfile(dimple_base, 'solvers', 'lib');
    end
    
	if nargin > 2
		ActualJavaDir = JavaDir;
	end
    javaaddpath(ActualJavaDir);
    
    JavaTestClassDir = fullfile(JavaBuildDir, 'classes', 'test');
    if exist(JavaTestClassDir, 'dir')
        javaaddpath(JavaTestClassDir);
    end
    
    jars = dir(lib_dir);
    for i = 1:length(jars)
        j = jars(i).name;
        if ~isempty(strfind(j,'jar'))
            javaaddpath(fullfile(lib_dir, j));
        end
    end


	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	ActualBerToolDirectory = fullfile(LyricPath, 'LyricBerTool');
	if nargin > 3
		ActualBerToolDirectory = BerToolDirectory;
	end
    if exist(ActualBerToolDirectory, 'dir')
		Paths{length(Paths) + 1} = ActualBerToolDirectory;
    end

	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	ActualTestFilesDirectory = fullfile(LyricPath, 'TesterFiles');
	if nargin > 4
		ActualTestFilesDirectory = TestFilesDirectory;
	end
    if exist(ActualBerToolDirectory, 'dir')
		Paths{length(Paths) + 1} = ActualTestFilesDirectory;
    end
        
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    for idx = 1:length(Paths)
        addpath(Paths{idx});
    end
    
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    if bLog
        fprintf('Dimple initialized with paths:\n');
        for idx = 1:length(Paths)
            fprintf('\t%u: {%s}\n', idx, Paths{idx});
        end
        fprintf('\tjava: {%s}\n', ActualJavaDir);
    end
end

%--------------------------------------------------------------------------
