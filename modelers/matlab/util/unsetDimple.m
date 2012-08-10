function unsetDimple

    %Get the path
    pathstr = path;
    teststr = '/chmpl';

    %find the dimple path root
    parts = regexp(path,':','split');
    dimple_root = [];
    
    dimple_root = findDimpleRoot(parts,teststr);

    while ~isempty(dimple_root)


        %loop through the path and remove anything that has the root path in
        %it.
        for i = 1:length(parts)
           if findstr(parts{i},dimple_root) >= 0
               rmpath(parts{i});
           end
        end

        
        pathstr = path;
        parts = regexp(path,':','split');        
        dimple_root = findDimpleRoot(parts,teststr);

    end

    %do the java stuff

    parts = javaclasspath();
    teststr = '/solvers/java/bin';
    dimple_root = findDimpleRoot(parts,teststr);
    
    while ~isempty(dimple_root)
       
        for i = 1:length(parts)
           if findstr(parts{i},dimple_root) >= 0
               javarmpath(parts{i});
           end
        end
        
        parts = javaclasspath();
        dimple_root = findDimpleRoot(parts,teststr);
    
    end
    
end

function dimple_root = findDimpleRoot(parts,teststr)

    dimple_root = [];
    
    for i = 1:length(parts)
       loc = findstr(parts{i},teststr);
       if loc >= 0
           dimple_root = parts{i};
           dimple_root = dimple_root(1:loc-1);
           break;
       end
    end
end
