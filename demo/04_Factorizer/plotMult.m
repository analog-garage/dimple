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

function plotMult(xbits,ybits,zbits,m_in,c_in,c_out)
    hold off;
    plot(0,0,'.');
    hold on;
    
    
    axis([-.5 prod(size(ybits))+1.5 -prod(size(zbits))-1 1.5]);
    lr = -1;
    ud = -1;
    
    %{
    function c = getColor(x,y)
        c = 'b';
        %if (inMult(x,y) ~= .5 && (inMult(x,y) > .5) ~= (idealMultIn(x,y) > .5)) || ...
        %   (outMult(x,y) ~= .5) && (outMult(x,y) >.5) ~= (idealMultOut(x,y) > .5)
        %    c = 'r';
        %end
 
    end
    %}



    numSize = .2;

    MPerRow = (length(zbits)-length(ybits)+1);
    for xpos = 1:prod(size(ybits))
        for ypos = 1:MPerRow
            plot(xpos,-ypos,'.');

            %M edges
            plot([xpos xpos+.5],[-ypos -ypos-.5]);
            plot([xpos xpos-.5],[-ypos -ypos+.5]);
            
            %C edges
            plot([xpos xpos],[-ypos -ypos+.5]);
            plot([xpos xpos],[-ypos -ypos-.5]);
            
            %xs and ys
            %plot([xpos xpos+.4], [-ypos -ypos]);
            %plot([xpos xpos-.4], [-ypos -ypos]);
            
            zindex = ypos;
            yindex = prod(size(ybits))-xpos+1;
            b = m_in(zindex,yindex).Belief;
            %text(xpos-.5,-ypos+.5,num2str(b(1)),'HorizontalAlign','center');

            drawBit(b(1),xpos-.5,-ypos+.5,numSize);
            b = c_in(zindex,yindex).Belief;
            %text(xpos, -ypos+.5,num2str(b(1)),'HorizontalAlign','center');
            drawBit(b(1),xpos,-ypos+.5,numSize);
            if (ypos == MPerRow)
                b = c_out(1,yindex).Belief;
                %text(xpos,-ypos-.5,num2str(b(1)),'HorizontalAlign','center');
                drawBit(b(1),xpos,-ypos-.5,numSize);
            end
            
        end
        MPerRow = MPerRow + 1;
    end
    
    for zpos = 1:length(zbits)
        b = zbits(zpos).Belief;
        %zpos
        %num2str(zpos)
        text(prod(size(ybits))+.75 -.07,-zpos-.5,['z_{' num2str(zpos-1) '}'],'HorizontalAlign','center');
        drawBit(b(1),prod(size(ybits))+1,-zpos-.5,numSize);

        
        b = xbits(zpos).Belief;
        text(-.3,-zpos,['x_{' num2str(zpos-1) '}'],'HorizontalAlign','center');
        %text(0,-zpos,num2str(b(1)),'HorizontalAlign','center');
        drawBit(b(1),0,-zpos,numSize);
    end
    
    for xpos = 1:prod(size(ybits))
        yindex = prod(size(ybits))-xpos+1;
        b = ybits(yindex).Belief;
        text(xpos,1,['y_{' num2str(yindex-1) '}'],'HorizontalAlign','center');
        %text(xpos,.25,num2str(b(1)),'HorizontalAlign','center');
        drawBit(b(1),xpos,.25,numSize);
    end
   
    
end
