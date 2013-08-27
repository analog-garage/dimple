function [Adj_matrix,V2,C2]=voronoi_box(num_countries)

% Fixes (through a horrible hack) matlab's representation of voronoin so that unbounded patch can be drawn locally
% (voronoid in matlab gives enough information for each patch to be drawn
% independently, unless it is unbounded, in which case the unbounded lines
% might be encoded in another, neighboring polygon).
% Assumes you live in the unit square (otherwise just rescale your data)

iscontained=@(point) (point(:,1)>=0).*(point(:,1)<=1).*(point(:,2)>=0).*(point(:,2)<=1);
%figure(1);


%The fix will fail if a country has only point in the box. Makes sure this
%is not the case.

success=0;

while ~success
    x=rand(num_countries,1);
    y=rand(num_countries,1);
    [V2,C2]=voronoin([[x;-100;-100;100;100] [y;-100;100;-100;100]]);
    successes=zeros(num_countries,1);
    for idx=1:num_countries
        coords=V2(C2{idx},:);
        inbox=iscontained(coords);
        successes(idx)=(sum(inbox)>=2);
    end
    success=all(successes);
end


%voronoi(x,y); 
pt_init=size(V2,1)+1;
V2=[V2;0 0; 0 1; 1 0; 1 1];
pt_counter=size(V2,1);


for idx=1:num_countries
    coords=V2(C2{idx},:);
    inbox=iscontained(coords);
    
    if sum(inbox==0)>0 %drawing boundaries
        
        if inbox(end)==1
            boxshift=find(inbox==0);
            boxshift=boxshift(end);
            boxshift=length(inbox)-boxshift;
        else
            boxshift=find(inbox==1);
            boxshift=-boxshift(1);
        end
        
        
        C2{idx}=circshift(C2{idx}',boxshift)'; %circshift to start inside the cube
        coords=V2(C2{idx},:);
        inbox=iscontained(coords);
        
        first_outbox=find(inbox==0);
        first_outbox=first_outbox(1);
        C2{idx}=C2{idx}([1:first_outbox length(C2{idx})]);
        coords=V2(C2{idx},:);
        
        % First point!!
        pt2=coords(first_outbox,:);
        pt1=coords(first_outbox-1,:);
        
        direction=[(pt2(1)-pt1(1))>0 , (pt2(2)-pt1(2))>0];
        if isequal(direction,[1 1])
            dangle=1;
        elseif isequal(direction,[0 1])
            dangle=2;
        elseif isequal(direction,[0 0])
            dangle=3;
        elseif isequal(direction,[1 0])
            dangle=4;
        end
        
        switch dangle
            
            case 1 %NE - intercepts x=1 and y=1
                targx=1;
                targy=1;
                
                
                
            case 2 %NW - intercepts x=0 and y=1
                targx=0;
                targy=1;
                
            case 3 %SW - intercepts x=0 and y=0
                targx=0;
                targy=0;
                
            case 4 %SE - intercepts x=1 and y=0
                targx=1;
                targy=0;
        end
        
        t1=(targx-pt1(1))/(pt2(1)-pt1(1));
        t2=(targy-pt1(2))/(pt2(2)-pt1(2));
        t=min(t1,t2);
        if t1<t2
            newx=targx;
            newy=pt1(2)+t*(pt2(2)-pt1(2));
        else
            newx=pt1(1)+t*(pt2(1)-pt1(1));
            newy=targy;
        end
        V2=[V2;newx newy];
        pt_counter=pt_counter+1;
        C2temp=C2{idx};
        C2temp(first_outbox)=pt_counter;
        C2{idx}=C2temp;
        
        
        
        
        
        % Second point!!
        coords=V2(C2{idx},:);
        pt1=coords(1,:);
        pt2=coords(length(C2{idx}),:);
        
        direction=[(pt2(1)-pt1(1))>0 , (pt2(2)-pt1(2))>0];
        if isequal(direction,[1 1])
            dangle=1;
        elseif isequal(direction,[0 1])
            dangle=2;
        elseif isequal(direction,[0 0])
            dangle=3;
        elseif isequal(direction,[1 0])
            dangle=4;
        end
        
        switch dangle
            
            case 1 %NE - intercepts x=1 and y=1
                targx=1;
                targy=1;
                
                
                
            case 2 %NW - intercepts x=0 and y=1
                targx=0;
                targy=1;
                
            case 3 %SW - intercepts x=0 and y=0
                targx=0;
                targy=0;
                
            case 4 %SE - intercepts x=1 and y=0
                targx=1;
                targy=0;
        end
        
        t1=(targx-pt1(1))/(pt2(1)-pt1(1));
        t2=(targy-pt1(2))/(pt2(2)-pt1(2));
        t=min(t1,t2);
        if t1<t2
            newx=targx;
            newy=pt1(2)+t*(pt2(2)-pt1(2));
        else
            newx=pt1(1)+t*(pt2(1)-pt1(1));
            newy=targy;
        end
        V2=[V2;newx newy];
        pt_counter=pt_counter+1;
        C2temp=C2{idx};
        C2temp(length(C2temp))=pt_counter;
        C2{idx}=C2temp;
        
        %last pass
        pt1_idx=C2{idx}(end);
        pt2_idx=C2{idx}(end-1);
        pt1=V2(pt1_idx,:);
        pt2=V2(pt2_idx,:);
        
        if not(or(pt1(1)==pt2(1),pt1(2)==pt2(2)))
            C2temp=C2{idx};
            extrapt=(or((pt1==1),(pt1==0)).*pt1)+(or((pt2==1),(pt2==0)).*pt2);
            if isequal(extrapt,[0 0])
                C2temp=[C2temp(1:end-1) pt_init C2temp(end)];
            elseif isequal(extrapt,[0 1])
                C2temp=[C2temp(1:end-1) pt_init+1 C2temp(end)];
            elseif isequal(extrapt,[1 0])
                C2temp=[C2temp(1:end-1) pt_init+2 C2temp(end)];
            elseif isequal(extrapt,[1 1])
                C2temp=[C2temp(1:end-1) pt_init+3 C2temp(end)];
            end
            C2{idx}=C2temp;
        end
        
        
        
        
    end
    
    
    
end

hold on;
figure(1);

for idx=1:num_countries
    coords=V2(C2{idx},:);
    patch(coords(:,1),coords(:,2),rand(1,3));
end

Adj_matrix=zeros(num_countries,num_countries);
for ii=1:num_countries-1
    for jj=(ii+1):num_countries
        cell1=C2{ii};
        cell2=C2{jj};
        if sum(ismember(cell1,cell2))>=1
            Adj_matrix(ii,jj)=1;
            Adj_matrix(jj,ii)=1;
        end
    end
end
hold off;