
fileparts(mfilename('fullpath'))

dimple_base = fileparts(mfilename('fullpath'));
dimple_path = [dimple_base '/modelers/matlab/util'];
addpath(dimple_path);
addDimplePath();
