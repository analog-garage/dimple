## Support Note

## What is Dimple?

Dimple is an open-source API for probabilistic modeling and inference.
Dimple allows the user to specify probabilistic models in the form of
graphical models (factor graphs, Bayesian networks, or Markov networks),
and performs inference on the model using a variety of supported
algorithms.

Probabilistic graphical models unify a great number of models from
machine learning, statistical text processing, vision, bioinformatics,
and many other fields concerned with the analysis and understanding of
noisy, incomplete, or inconsistent data. Graphical models reduce the
complexity inherent in complex statistical models by dividing them into
a series of logically (and statistically) independent components. By
factoring the problem into sub-problems with known and simple
interdependencies, and by adopting a common language to describe each
sub-problem, one can considerably simplify the task of creating complex
probabilistic models. A [short introduction to factor graphs](appendices/_IntroductionToFactorGraphs.md)
can be found in the Appendices.

An important attribute of Dimple is that it allows the user to construct
probabilistic models in a form that is largely independent of the
algorithm used to perform inference on the model. This modular
architecture benefits those who create probabilistic models by freeing
them from the complexities of the inference algorithms, and it benefits
those who develop new inference algorithms by allowing these algorithms
to be implemented independently from any particular model or
application.

Key features of Dimple:

-   Supports both undirected and directed graphs.

-   Supports a variety of *solvers* for performing inference, including
    sum-product and Gaussian belief propagation (BP), min-sum BP,
    particle BP, discrete junction tree, linear programming (LP), and
    Gibbs sampling.

-   Supports both discrete and continuous variables.

-   Supports arbitrary factor functions as well as a growing library of
    standard distributions and mathematical functions.

-   Supports nested graphs.

-   Supports rolled-up graphs (repeated HMM-like structures).

-   Some support for parameter estimation (including the EM
    algorithm).

-   The core implementation is in Java[^2], with interfaces in Python (under construction)
    and MATLAB[^1] (deprecated).

Using MATLAB to program Dimple requires an existing licensed version of
MATLAB, of version at least 2013b.

[^1]: MATLAB is a registered trademark of The Mathworks, Inc.

[^2]: Java is a registered trademark of Oracle and/or its affiliates.
