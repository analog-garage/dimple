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

puzzle = [4 0 0 0 0 8 3 0 5; ...
          5 6 8 0 0 0 0 0 0; ...
          7 0 0 5 0 0 9 0 4; ...
          0 0 5 0 1 2 6 0 0; ...
          8 0 0 0 5 0 0 0 3; ...
          0 0 1 8 4 0 2 0 0; ...
          2 0 4 0 0 5 0 0 6; ...
          0 0 0 0 0 0 8 4 1; ...
          1 0 7 6 0 0 0 0 9];
%{
4,1,9,2,7,8,3,6,5,
5,6,8,4,3,9,1,7,2,
7,2,3,5,6,1,9,8,4,
3,4,5,7,1,2,6,9,8,
8,7,2,9,5,6,4,1,3,
6,9,1,8,4,3,2,5,7,
2,8,4,1,9,5,7,3,6,
9,5,6,3,2,7,8,4,1,
1,3,7,6,8,4,5,2,9,
          %}
      
%Hard puzzle
%puzzle = [8 5 0 0 0 2 4 0 0; ...
%          7 2 0 0 0 0 0 0 9; ...
%          0 0 4 0 0 0 0 0 0; ...
%          0 0 0 1 0 7 0 0 2; ...
%          3 0 5 0 0 0 9 0 0; ...
%          0 4 0 0 0 0 0 0 0; ...
%          0 0 0 0 8 0 0 7 0; ...
%          0 1 7 0 0 0 0 0 0; ...
%          0 0 0 0 3 6 0 4 0];
      
%puzzle = [8 5 9 6 1 2 4 3 7; ...
%            7 2 3 8 5 4 1 6 9; ...
%            1 6 4 3 7 9 5 2 8; ...
%            9 8 6 1 4 7 3 5 2; ...
%            3 7 5 2 6 8 9 1 4; ...
%            2 4 1 5 9 3 7 8 6; ...
%            4 3 2 9 8 1 6 7 5; ...
%            6 1 7 4 2 5 8 9 3; ...
%            5 9 8 7 3 6 2 4 1];
      
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Define Factor Graph
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


ssize = 9;
sdomain = 1:ssize;

%TODO: Support priors for more than one dimension.

%Create definition
sgd = newGraph();
    %Create Ss
    s = addVariable(sgd,ssize,ssize);
    setDomain(s,sdomain);

    %Create vals
    vals = addVariable(sgd,ssize,1);
    for i = 1:length(vals)
        setDomain(vals(i),i);
    end

    %Create L1s
    for i = 1:ssize
        L1{i} = addBit(sgd,ssize,ssize);
        setBitInput(reshape(L1{i},9*9,1),ones(9*9,1)*1/9);
    end

    %Create gs
    for num = 1:ssize
        for i = 1:ssize
            for j = 1:ssize
                addFactor(sgd,@g,L1{num}(i,j),s(i,j),vals(num));
            end
        end
    end
     
    %Create L2s
    %Create L3s
    for num = 1:ssize
       L2Cols{num} = addBit(sgd,3,ssize);
       setBitInput(reshape(L2Cols{num},3*ssize,1),ones(3*ssize,1)*1/3);
       L3Cols{num} = addVariable(sgd,1,ssize);
       setDomain(L3Cols{num},1);
       L2Rows{num} = addBit(sgd,ssize,3);
       setBitInput(reshape(L2Rows{num},ssize*3,1),ones(ssize*3,1)*1/3);
       L3Rows{num} = addVariable(sgd,ssize,1);
       setDomain(L3Rows{num},1);
       %L3BoxesRows{num} = addVariable(sgd,3,3);
       %setDomain(L3BoxesRows{num},1);
       L3BoxesCols{num} = addVariable(sgd,3,3);
       setDomain(L3BoxesCols{num},1);
    end

    for num = 1:9
  
        %Setup trees across columns
        for row = 1:3
            for col = 1:9
                startRow = (row-1)*3+1;
                endRow = startRow + 2;
	 
                addFactor(sgd,@f,L2Cols{num}(row,col),L1{num}(startRow:endRow,col));
            end
        end
        for col = 1:9
            addFactor(sgd,@f,L3Cols{num}(1,col), L2Cols{num}(:,col));
        end
      
        %Setup trees across rows
        for row = 1:9
            for col = 1:3
                startCol = (col-1)*3+1;
                endCol = startCol + 2;
	 
                addFactor(sgd,@f,L2Rows{num}(row,col), L1{num}(row,startCol:endCol));
            end
        end
        for row = 1:9
            addFactor(sgd,@f,L3Rows{num}(row,1), L2Rows{num}(row,:));
        end
        
        %Setup boxes
        %{
        for row = 1:3
            for col = 1:3
                startRow = (row-1)*3+1;
                endRow = startRow+2;
                newFunc(sgd,@f,[L3BoxesRows{num}(row,col); L2Rows{num}(startRow:endRow,col)]);
            end
        end
        %}
        for row = 1:3
            for col = 1:3
                startCol = (col-1)*3+1;
                endCol = startCol+2;
                addFactor(sgd,@f,L3BoxesCols{num}(row,col), L2Cols{num}(row,startCol:endCol));
            end
        end
        
    end

     
%end definition
   
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Run Algorithm
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%Create variables

for row = 1:9
  for col = 1:9
    if (puzzle(row,col) ~= 0 )
      ps = zeros(1,9);
      ps(puzzle(row,col)) = 1;
      setInput(s(row,col),ps);
    end
  end
end

setNumIterations(sgd,25);

%tic
%solve(sgd);      
%toc

figure(gcf);
initGraph(sgd);
for i = 1:25
    disp(['iteration: ' num2str(i)]);
    iterate(sgd);
    drawSudoku(s);
    pause(.2);
end


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Get Results
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

outputs = getValue(s);      
disp(outputs);


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Check results
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


%Check result is valid
isvalid = 1;

%Check rows
for row = 1:9
  tmpValid = all(sort(outputs(row,:)) == 1:9);
  if (~tmpValid)
    disp(['row failed: ' num2str(row)]);
    isvalid = 0;
  end
end

%Check cols
for col = 1:9
  tmpValid = all(sort(outputs(:,col))' == 1:9);
  if (~tmpValid)
    disp(['col failed: ' num2str(col)]);
    isvalid = 0;
  end
end

%check boxes
for i = 1:3
   for j = 1:3
     rstart = (i-1)*3+1;
     rend = rstart+2;
     cstart = (j-1)*3+1;
     cend = cstart+2;
     tmpValid = all(sort(reshape(outputs(rstart:rend,cstart:cend),1,9)) == 1:9);
     if (~tmpValid)
       disp(['box failed: ' num2str(i) ' ' num2str(j)]);
       isvalid = 0;
     end
   end
end

isvalid
drawSudoku(s);
