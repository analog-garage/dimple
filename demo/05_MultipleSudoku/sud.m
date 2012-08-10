function fff = sud(filename)
%usage: sudoku(filename)
% filename is a string which is exactly the name of the sudoku data file.
% This is the SUDOKU solver which only tries to find one solution. 
% If the solution is unique, it definitely gives you the answer.
%Input your sudoku like this:
% 0 0 0 1 2 3 0 0 0
% 1 0 5 0 0 4 0 6 0
% *****
% The blank is represented by '0'. All numbers are separated by blank.
% save the file and use this function like this;
% sud('filename')
% Make sure matlab can find your file.
% clc
%tic
fff = 0;
global su a
su = load(filename);
% su = e2;
a = zeros([9, 9, 9]);
sudoku1;
ind = (su==0);
f = sum(sum(ind>0));
if (f < 1)
    %disp('Well done!')
    %disp('-----------------------SUDOKU--------------------------')
    %disp(su)
    %toc
    fff = 1;
%     save sudokoA su
    return
end
if (f<15)
    %disp('Something wrong');
    %su
    return
end
V = 0;

K = zeros(9,9,f);
if(f>15)
    su1 = su;
    a1 = a;
    [r,c]=find(ind>0);
    for i=1:length(r)
        t = reshape(a1(r(i),c(i),:),[1,9]);
        [r1,c1] = find(t>0);
        for j = 1:length(r1)
            a = a1;
            su = su1;
            su(r(i),c(i)) = t(c1(j));
            sudoku1;
            ind = (su==0);
            f = sum(sum(ind>0));
            if (f<1)
                %disp('-----------------------SUDOKU--------------------------')
                %disp(su)
                %toc
                fff = 1;
                save sudokoA su
                return
            end
        end
    end
end
if(V<1)
    disp('Not found!')
    return;
end
save K K
for i=1:V
    %disp('-----------------------SUDOKU--------------------------')
    %disp(reshape(K(:,:,i),[9,9]))
end

%toc

%-------------------------------
function sudoku1
global su a
ind = (su==0);

for i= 1:9
    a(:,:,i) = i*(1-ind);
end
f = sum(sum(sum(ind)));
f1 = f+1;
while(f1>f)
    while( f1 > f)
        f1 = f;
        SUDO(ind);
        ind = (su==0);
        SUD(ind);
        ind = (su==0);
        f = sum(sum(ind));
    end
    if (f>0)
        for i=0:2
            for j=0:2
                ind1 = zeros(9);
                ind1(3*i+1:3*i+3, 3*j+1:3*j+3) = ind(3*i+1:3*i+3, 3*j+1:3*j+3);
                [r3, c3] = find(ind1>0);
                if (length(r3)>0)
                    Decide(r3,c3);
                end
            end
        end
    end

    ind = (su==0);
    f = sum(sum(ind));
end




%--------------------
function SUDO(ind)
global su a

[r,c] = find(ind > 0);
flag = 0;
for i=1:length(r)
    y = FIND(r(i),c(i));
    if (sum(y>0)==1)
        flag = 1;
        su(r(i),c(i)) = sum(y);
        a(r(i), c(i),:) = 0;
        a(r(i), :,sum(y)) = 0;
        a(:, c(i),sum(y)) = 0;
    else
        a(r(i), c(i),:) = y;
    end
end
%-------------------------
function SUD(ind)
global su a
for i =1:9
    [r1,c1] = find(ind(i,:)>0);
    r1 = r1*0+i;
    Decide(r1,c1);
end

ind = (su==0);
for i=1:9
    [r2,c2] = find(ind(:,i)>0);
    c2 = c2*0 + i;
    Decide(r2,c2);
end


%-------------------
function Decide(r,c)
global su a
s = [];
for i=1:length(r)
    t = reshape(a(r(i),c(i),:),[1,9]);
    s = [s; t];
end
for i=1:9
    if (sum(sum((s==i))) == 1)
        [t1,t2] = find((s-i)==0);
        su(r(t1),c(t1)) = i;
        a(r(t1),:,i) = 0;
        a(:,c(t1),i) = 0;
        a(r(t1),c(t1),:) = 0;

    end
end



%--------------------
function y = FIND(i,j)
global su
y = 1:9;
m = ceil(i/3-0.1)-1;
n = ceil(j/3-0.1)-1;
xC = reshape(su(3*m+1:3*m+3, 3*n+1:3*n+3)',[1,9]);
y1 = sort([su(i,:), su(:,j)', xC]);
y2 = [1,y1(2:end) - y1(1:end-1)];
y1 = y1(y2~=0);
if (y1(1) == 0)
    y1(1) = [];
end
y(y1) = 0;



