function addDimpleTestDir(d)
    dirs = getDimpleTestDir();
    m = containers.Map();
    for i = 1:length(dirs)
       m(dirs{i}) = 1; 
    end
    
    tmp = getenv('DimpleTESTDIR');
    if ~m.isKey(d)
       if ~isempty(tmp)
           tmp = [tmp ';' d];
       else
           tmp = d;
       end
       setenv('DimpleTESTDIR',tmp);
    end
    
end
