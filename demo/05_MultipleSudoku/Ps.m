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

function x = Ps(n)
%usage Ps(n)
% input a number n (0.1~0.9) to creat a SUDOKU puzzle
% n < 0.4 --> easy
% n < 0.55 --> Medium
% n < 0.65 --> Hard
% n > 0.7  --> Evil
% SUDOKU solver is used to make sure it's solvable and unique. However, I can not make sure it is unique when n > 0.5
% when n>0.7, it make take a few minutes because the solver tries to find a solution at least by guess.
%close all
if (nargin < 1)
    n = 0.5;
end
x = Cs(n);
%{
su = load('sudo');
figure
hold on
s1 = num2str(ceil(rand*100000));
if(n<0.4)
    s = 'SUDOKO  EASY';
    s2 = ['Easy',s1];
else if (n < 0.56)
        s = 'SUDOKO  MEDIUM';
        s2 = ['Medium',s1];
    else if ( n < 0.7 )
            s = 'SUDOKO  HARD';
            s2 = ['Hard',s1];
        else
            s = 'SUDOKO  EVIL';
            s2 = ['Evil',s1];
        end
    end
end
s = [s,'    ( ', s1,' )'];
title(s)
for i = 0:9
    plot([i,i],[0,9],'k');
    plot([0,9],[i,i],'k');
end
axis([-0.5,9.5,-0.5,9.5])
for i = 0:3
    plot([3*i,3*i],[0,9],'k','linewidth',2)
    plot([0,9],[3*i,3*i],'k','linewidth',2);
end
su=su';
for i=1:9
    for j=1:9
        if (su(i,j)> 0)
            text(i-0.5,9 - j+0.5,num2str(su(i,j)))
        end
    end
end
axis equal
axis off
save(s2,'su','-ascii');

su = load('sudoA');
figure
hold on
axis([-0.5,9.5,-0.5,9.5])
title(s)
for i = 0:9
    plot([i,i],[0,9],'k');
    plot([0,9],[i,i],'k');
end
axis([-1,10,-1,10])
for i = 0:3
    plot([3*i,3*i],[0,9],'k','linewidth',2)
    plot([0,9],[3*i,3*i],'k','linewidth',2);
end
su = su';
for i=1:9
    for j=1:9
        if (su(i,j)> 0)
            text(i-0.5,9 - j+0.5,num2str(su(i,j)))
        end
    end
end
axis equal
axis off
save([s2,'A'],'su','-ascii');

%}
%%-----------------
function x = Cs(n)
% hard: n = 0 ~ 1
% 1 column
% 2 row
% 1 - 3 which line or row
% 1 - 3 which two
%clc
load sudokoA
m = 10000;
a = zeros(4, m);
a(1,:) = ceil(2*rand(1,m));
a(2,:) = ceil(3*rand(1,m));
a(3,:) = ceil(3*rand(1,m));
a(4,:) = ceil(3*rand(1,m));
L = [3*(a(2,:)-1) + a(3,:); 3*(a(2,:)-1) + a(4,:)];
ind = (L(1,:) == L(2,:));
L(:,ind) = [];
a = a(1,ind==0);
a1 = size(L);
for i = 1:a1(2)
    L1 = L(1,i);
    L2 = L(2,i);
    if (a(i) == 1)
        t = su(:,L1);
        su(:,L1) = su(:,L2);
        su(:,L2) = t;
    else
        t = su(L1,:);
        su(L1,:) = su(L2,:);
        su(L2,:) = t;
    end    
end
a = rand(9);
ind = a > n;
su1 = ind.*su;
save sudo su1 -ascii
save sudoA su -ascii
fff = sud('sudo');
if (fff<0.5)
    Cs(n);
end
x = su1;
