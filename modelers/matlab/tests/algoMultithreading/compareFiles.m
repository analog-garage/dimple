function aresame = compareFiles(file1,file2)
    f1 = fopen(file1,'r');
    f2 = fopen(file2,'r');
    
    done = false;
    
    aresame = true;
    
    while ~done
       s1 = fgets(f1);
       s2 = fgets(f2);
       
       if ~isequal(s1,s2)
           aresame = false;
           break;
       end
       
       if s1 == -1;
           break;
       end
       
    end
    
end