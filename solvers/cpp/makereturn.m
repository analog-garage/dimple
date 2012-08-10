function makereturn(debug)


mypath = mfilename('fullpath');
loc = findstr(mypath,'makereturn');
directory = mypath(1:loc-1);

oldpath = pwd;

if nargin < 1
    debug = 0;
end

cd(directory);
try
    make(debug);
catch 
    
end
cd(oldpath);
