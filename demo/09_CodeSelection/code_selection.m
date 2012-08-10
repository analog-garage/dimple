% The code selection problem 
%clc;

% Fix random seed
rand('twister', 5489);

%%%%%%%%%%%%%%%%%%%%%%%
% Specify the codes  %
%%%%%%%%%%%%%%%%%%%%%%%

%  Generate new codes based on the FEC H matrix; list_of_codes(1,:,:)= the
%  regular H matrix.
load H_FEC;  % Loads "H" matrix
load G_FEC;  % Loads "G" matrix
sz=size(H);
H=sparse(H);
% We are choosing between K codes, with N bit codewords and M (independent) 
% check equations
K=10;
M=sz(1);
N=sz(2);
%list_of_codes = get_check_mats_target(H, K);

disp(sprintf('Choosing between %d codes, each %d bits long, with %d check equations.', K, N, M));


%%%%%%%%%%%%%%%%%%%%%%%%%%
% Make a noisy codeword  %
%%%%%%%%%%%%%%%%%%%%%%%%%%
G2=gf(G,1);

info_bits=randi(2,N-M,1)-1;

codeword=G2*info_bits;
codeword_priors=zeros(N,1); % Prior prob that the bit is a 1.
% "p" is the actual probability of flipping a bit
p=.012;
% "q" is the claimed prior probability.
q=.012;
initial_wrong=0;
for i=1:N
    if rand(1,1)<p
        initial_wrong=initial_wrong+1;
        if(codeword(i)==0)
            codeword_priors(i)=1-q;
        else
            codeword_priors(i)=q;
        end
    else
        if(codeword(i)==1)
            codeword_priors(i)=1-q;
        else
            codeword_priors(i)=q;
        end
    end
end


%%%%%%%%%%%%%%
% Dimple stuff %
%%%%%%%%%%%%%%

code_graph=FactorGraph();

% "which_code" is a K-ary variable determining which code is active.
domain=1:K;
which_code = Variable(domain);

% We have a bit for each bit of the codeword.
x=Bit(N,1);
x(1:N).Input=codeword_priors;

% We have a bit for each equation.
%tic
y=Bit(K*M,1);
%toc

% For each code,
disp('Inserting each code into factor graph');
for code=1:K
    disp(code);
    % Choose a random permutation of the rows, to produce different
    % check matrices
    if code==1
        col_perm=(1:N);
    else
        col_perm=randperm(N);
    end
    
    % For each check equation,
    %tic;
    for eq=1:M
        varIndices = find(H(eq,:));
        eq_density=length(varIndices);
        permVarIndices = zeros(eq_density,1);
        for j=1:eq_density
            permVarIndices(j)=col_perm(varIndices(j));
        end
        
        gd=getNBitXorDef(eq_density+1);        
        code_graph.addFactor(gd,x(permVarIndices),y((code-1)*M+eq));
        code_graph.addFactor(@matchDelta,which_code,code,y((code-1)*M+eq));
        
        %code_graph.addFactor(@xorMDelta,which_code,code,x(permVarIndices));
    end
    %toc;
end

for i=1:10
    code_graph.Solver.iterate(1);
    disp(sprintf('Step %3d: Belief in correct code = %f',i,which_code.Belief(1)));
    if which_code.Belief(1)>.999 
        break;
    end
end

code_graph.Solver.setNumIterations(20);
% code_graph.iterate
tic;
code_graph.solve();
toc;
disp('Final belief in which code generated the codeword (first is correct):');
disp(which_code.Belief);
X_back=x.Value;
wrong=0;
for i=1:N
    if X_back(i)~= codeword(i)
        wrong=wrong+1;
    end
end
disp(sprintf('Before decoding: %4d bits wrong', initial_wrong));
disp(sprintf('After  decoding: %4d bits wrong', wrong));

