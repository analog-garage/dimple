classdef Sudoku
    properties
        Graph;
        S;
    end
   methods
       function x = Sudoku()
           
           perm = BuildPermutationFactorGraph(9);
           
           vars = Variable(1:9,9,9);
           
           vars4D = reshape(vars,3,3,3,3);
           
           fg = FactorGraph();
           
           for i = 1:3
               for j = 1:3
                   %rows
                   fg.addFactor(perm,reshape(vars4D(:,:,i,j),9,1));
                   %columns
                   fg.addFactor(perm,reshape(vars4D(i,j,:,:),9,1));
                   %boxes
                   fg.addFactor(perm,reshape(vars4D(:,i,:,j),9,1));
               end
           end

           x.Graph = fg;
           x.S = vars;
       end
       function initPuzzle(x,puzzle)
           x.Graph.initialize();
           tmp = reshape(x.S,9*9,1);
           tmp.Input = ones(9*9,9)*1/9;
           for row = 1:9
              for col = 1:9                
                if (puzzle(row,col) ~= 0)
                  ps = zeros(1,9);
                  ps(puzzle(row,col)) = 1;
                  x.S(row,col).Input = ps;
                end
              end
           end
       end
       function setNumIterations(x,num)
           x.Graph.Solver.setNumIterations(num);
       end
       function s = solve(x)           
           x.Graph.solve();
           s = x.S;
       end
       function showSolve(x,maxNumIter)
           x.Graph.initialize();
           for i = 1:maxNumIter
                disp(['iteration: ' num2str(i)]);
                x.Graph.Solver.iterate(1);
                drawSudoku(x.S);
                pause(.2);
                if (x.isValid())
                    break;
                end
           end
           if (x.isValid())
               disp('Solved');
               text(100*4.5,100*4.5,'Solved','FontSize',70,'Color','red',...
                   'HorizontalAlignment','center','rotation',-45);
               pause(.5);
           else
               disp('Failed');
           end
       end
       function isvalid = isValid(x)
           %Check result is valid
            isvalid = 1;
            outputs = x.S.Value;
            %Check rows
            for row = 1:9
              tmpValid = all(sort(outputs(row,:)) == 1:9);
              if (~tmpValid)
                %disp(['row failed: ' num2str(row)]);
                isvalid = 0;
              end
            end

            %Check cols
            for col = 1:9
              tmpValid = all(sort(outputs(:,col))' == 1:9);
              if (~tmpValid)
                %disp(['col failed: ' num2str(col)]);
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
                   %disp(['box failed: ' num2str(i) ' ' num2str(j)]);
                   isvalid = 0;
                 end
               end
            end

            
       end
   end
end
