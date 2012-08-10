% Suppose we have three texts, viewed as ASCII streams.  We observe the
% bitwise XOR of each pair of texts.  Our goal is to recover the original
% texts.

% First, load some sample texts
load ('data/Dickens.mat');
t1=tok_3{1};  % Tale of Two Cities
load ('data/Orwell.mat');
t2=tok_4{1};  % Orwell's 1984
load ('data/Kafka.mat');
t3=tok_5{1};  % Kafka's Metamorphosis
clear 'tok_3' 'tok_4' 'tok_5';

% Load trigram stats (instantiates matrix "pure_prob_trigrams")
load ('data/prob_matrix.mat');

% To give the process a hint, we reveal the correct letter every
% 'hint_rate' bytes.
hint_rate=10;

% Verbosity=2, 1, or 0
verbosity=2;


overlap_length = 10;

tic;
error_rate = recover_text(t1,t2,t3, pure_prob_trigrams, hint_rate,verbosity,overlap_length);
toc;

   
