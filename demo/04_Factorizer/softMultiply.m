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

function [xvals,yvals,zvals] = softMultiply(x,y,z,doplot)
    if nargin < 4
        doplot = 0;
    end
    x_priors = x;
    y_priors = y;
    z_priors = z;
    
    %z = 323;
    numxbits = length(x);
    numybits = length(y);
    numzbits = numxbits + numybits;
    %zvec = z;
    numMPerRow = numzbits;

    %dimple_begin;
    g = FactorGraph();
    
        x = Bit(numzbits,1);
        y = Bit(numybits,1);
        z = Bit(numzbits,1);
        x(1:numxbits).Input = x_priors;
        x(numxbits+1:end).Input = zeros(numzbits-numxbits,1);
        y.Input = y_priors;
        z.Input = z_priors;
    
        m_in = Bit(numzbits,numybits);
        c_in = Bit(numzbits,numybits);
        c_out = Bit(1,numybits);
        
        for yindex = 1:numybits
           for zindex = 1:numMPerRow

              if yindex == numybits || zindex == 1
                  m_in(zindex,yindex).Input = 0;
              end

              %c_in
              if zindex == 1
                  %set PMF to 0
                  c_in(zindex,yindex).Input = 0;
              end

              if yindex == 1
                m_out = z(zindex);
              else
                m_out = m_in(zindex+1,yindex-1);
              end
                                       
              if zindex == numMPerRow
                 c_out_tmp = c_out(1,yindex);
                 c_out_tmp.Input = 0;
              else
                  c_out_tmp = c_in(zindex+1,yindex);
              end
              %Create function node
              %funcNodeName = ['M_' num2str(zindex) '_' num2str(yindex)];
              addFactor(g,@munitDelta,x(zindex),y(yindex), m_in(zindex,yindex),...
                  c_in(zindex,yindex), m_out, c_out_tmp);
           end
           numMPerRow = numMPerRow - 1;
        end
    %end graph
    %setNumIterations(g,numxbits*numybits*3/2);
    g.initialize();
    
    if doplot
        figure(gcf);
        for i = 1:numxbits*numybits*3/2
            plotMult(x,y,z,m_in,c_in,c_out);
            g.Solver.iterate(1);
            pause(1);

        end
    else
        g.NumIterations=numxbits*numybits*3/2;
        g.solve();
    end
    
    %Now I should be able to read beliefs of all variables, including xs and ys
    xvals = zeros(numxbits,1);
    yvals = zeros(numybits,1);
    zvals = zeros(numzbits,1);
    for i = 1:numxbits
        b = x(i).Belief;
        xvals(i) = b(1);
    end
    for i = 1:numybits
        b = y(i).Belief;
        yvals(i) = b(1);
    end
    for i = 1:numzbits
        b = z(i).Belief;
        zvals(i) = b(1);
    end
    
    %x = sx;
    %y = sy;
    %z = sz;
end
