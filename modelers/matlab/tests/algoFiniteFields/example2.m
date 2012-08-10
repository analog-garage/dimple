% Choose a representation of a finite field with 2^d elements.
degree=7;
prim_poly=2^7 + 2^1 + 2^0;

% Make 3 finite field variables
fg = FactorGraph();
ff = FiniteFieldVariable(prim_poly,3,1);

% Set hard priors on ff(1) so that ff(1) = 0000010.
x=zeros(128,1);
x(2)=1;
ff(1).Input=x;
% ff(2) = 0000010 * ff1 (so, hopefully, ff(2) = 0000100)
fg.addFactor(@finiteFieldMult,2,ff(1),ff(2));
% ff(3) = 0000010 * ff2 (so, hopefully, ff(3) = 0001000)
fg.addFactor(@finiteFieldMult,2,ff(2),ff(3));

% Project bits from ff1, ff2, and ff3
bits=Bit(degree,3);
paired=cell(3,degree);
for j=1:3
    for i=0:degree-1
        paired{j,2*i+1}=i;
        paired{j,2*i+2}=bits(i+1,j);
    end
    fg.addFactor(@finiteFieldProjection,ff(j),paired{j,:});
end


% Solve
fg.Solver.setNumIterations(10);
fg.solve();

% Print output beliefs, etc
v(1)=ff(1).Value;
v(2)=ff(2).Value;
v(3)=ff(3).Value;
a(1,:)=ff(1).Beliefs;
a(2,:)=ff(2).Beliefs;
a(3,:)=ff(3).Beliefs;

for i=1:3
    fprintf('Variable "ff%d"\n',i);
    fprintf('  Value=%d; Beliefs=',v(i));
    for j=1:6
        fprintf(' %.2f',a(i,j));
    end
    fprintf(' ...\n  Projections:     ');
    for j=1:degree
        fprintf(' %.2f',bits(j,i).Beliefs);
    end
    fprintf('\n\n');
end







