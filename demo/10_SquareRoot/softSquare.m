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

function [xvals zvals] = softSquare(x,z,doplot)
    if nargin < 3
        doplot = 0;
    end
    x_priors = x;
    z_priors = z;
    
    numxbits = length(x);
    numzbits = 2*numxbits;
    %zvec = z;
    numMPerRow = numzbits;

    %dimple_begin;
    g = FactorGraph();
    
    x = Bit(numzbits,1);
    z = Bit(numzbits,1);
    x(1:numxbits).Input = x_priors;
    x(numxbits+1:end).Input = zeros(numzbits-numxbits,1);
    z.Input = z_priors;
    
    m_in = Bit(numzbits,numxbits);
    c_in = Bit(numzbits,numxbits);
    c_out = Bit(1,numxbits);
    
    for xindex = 1:numxbits
        for zindex = 1:numMPerRow
            
            if xindex == numxbits || zindex == 1
                m_in(zindex,xindex).Input = 0;
            end
            
            if zindex == 1
                %set PMF to 0
                c_in(zindex,xindex).Input = 0;
            end
            
            if xindex == 1
                m_out = z(zindex);
            else
                m_out = m_in(zindex+1,xindex-1);
            end
            
            if zindex == numMPerRow
                c_out_tmp = c_out(1,xindex);
                c_out_tmp.Input = 0;
            else
                c_out_tmp = c_in(zindex+1,xindex);
            end
            %Create function node
            %funcNodeName = ['M_' num2str(zindex) '_' num2str(yindex)];
            addFactor(g,@munitDelta,x(zindex),x(xindex), m_in(zindex,xindex),...
                c_in(zindex,xindex), m_out, c_out_tmp);
        end
        numMPerRow = numMPerRow - 1;
    end
    %end graph
    %setNumIterations(g,numxbits*numxbits*3/2);
    g.initialize();
    
    early_terminate=0;
    if doplot
        figure(gcf);
        for i = 1:(numxbits^3)*3/2
            % If we succeed, we run for a few more steps (so that the carry
            % bits can also get computed confidently) and then we terminate
            % early.
            plotMult(x,x(1:numxbits),z,m_in,c_in,c_out);
            if 1
                if i>1
                    xguess=0;
                    zguess=0;
                    for j = 1:numxbits
                        b = x(j).Belief;
                        if b(1)>.5
                            xguess=xguess + 2^(j-1);
                        end
                    end
                    for j = 1:numzbits
                        b = z(j).Belief;
                        if b(1)>.5
                            zguess=zguess + 2^(j-1);
                        end
                    end
                    display(sprintf('Current guess: %d\n', xguess));
                    if xguess^2 == zguess
                        display(sprintf('   Correct answer!\n'));
                        early_terminate=early_terminate+1;
                    else
                        early_terminate=0;
                    end
                end
                if early_terminate>numxbits+1
                    break;
                end
            end
            
            % Otherwise, try another iteration of BP.
            g.Solver.iterate(1);
            display(sprintf('Iteration %2d\n',i));
            pause(1);
            
        end
    else
        g.Solver.setNumIterations(numxbits*numxbits*3/2);
        g.solve();
    end
    
    %Now I should be able to read beliefs of all variables, including xs
    %and ys        
    xvals = zeros(numxbits,1);
    zvals = zeros(numzbits,1);
    for i = 1:numxbits
        b = x(i).Belief;
        xvals(i) = b(1);
    end
    for i = 1:numzbits
        b = z(i).Belief;
        zvals(i) = b(1);
    end
    
    %x = sx;
    %y = sy;
    %z = sz;
end
