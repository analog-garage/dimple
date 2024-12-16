Dimple is an open-source software tool for probabilistic modeling, inference, 
and learning developed at the [Analog Garage][garage] 
of Analog Devices Inc.  Dimple allows models to be specified in the form of 
probabilistic graphical models, and can perform inference on those models 
using a variety of algorithms, including various forms of belief propagation 
and Gibbs sampling.

Dimple allows a user to construct probabilistic models in a form largely 
independent of the algorithm used to perform inference on it. This is intended 
to allow rapid prototyping of complicated probabilistic models, freeing the user 
from the complexities of the inference algorithms.

Features include:

* MATLAB and Java versions the API.
* Supports a variety of solvers for performing inference, including sum-product 
  and Gaussian belief propagation (BP), min-sum BP, particle BP, 
  discrete junction tree, linear programming (LP), and Gibbs sampling.
* Supports both undirected and directed graphs.
* Supports both discrete and continuous variables.
* Supports arbitrary factor functions as well as a library of standard 
  distributions and mathematical functions.
* Supports nested graphs.
* Supports rolled-up graphs (repeated HMM-like structures).

## Documentation

* Dimple User Manual MATLAB Interface - build from source
* Dimple User Manual Java Interface - build from source
* Java API - build from source
* [Accelerating Inference: towards a full Language, Compiler and Hardware Stack]
  (https://arxiv.org/abs/1212.2991)

## Download

* Source Code: [release_0.07.2.zip](https://github.com/analog-garage/dimple/archive/release_0.07.2.zip)
* Dimple on Github: https://github.com/analog-garage/dimple
* Dimple Demos on Github: https://github.com/analog-garage/dimpleDemos

Checkout the latest snapshot using:

~~~bash
$ git clone git@github.com:analog-garage/dimple.git
~~~

## Installation

### Requirements 

In order to use Dimple with MATLAB, you need:

* Java runtime environment (JRE - version 1.7 or later)
* MATLAB (version 2013b or later) - Not necessary if you only want to use Dimple 
  as a Java API.

If you would like to build Dimple from source you also need

* Java development kit (JDK - version 1.7 or later)
* gradle (version 2.10 or later 2.x version)

### Step-by-step instructions for using Dimple with MATLAB

1. Download Dimple
2. Unzip and put it in whatever directory you want.
3. Start MATLAB
4. In MATLAB, navigate to your new Dimple directory.
5. Execute the startup.m script in the Dimple directory.
6. Verify dimple has been installed properly by executing "testDimple".  
   This MATLAB function will run a suite of tests.  If you are missing some 
   toolboxes (like the Communications or Statistics toolboxes) some tests will 
   fail.
7. Learn about Dimple.  

Read the documentation in the doc directory and check out the code in the 
demos directory.

### Using Dimple as a Java library

If you want to use Dimple as a java API, you need to download dimple and include 
all of the jar files in `solvers/lib`.

### Importing Dimple from JCenter Maven repository using gradle

**NOTE: JCenter/Bintray is defunct. This no longer works**

Starting with version 0.06, Dimple is published as a project on Bintray and can 
be imported from the associated JCenter Maven repository. If you are using 
gradle to build your project, you will only need to add JCenter to your list of 
repositories and specify Dimple as a library dependency. For example:

~~~groovy
    repositories {
       jcenter()
       // ... other repositories ...
    }

    dependencies {
       compile "com.analog.dimple:dimple:0.06"
       // ... other dependencies ...
    }
~~~

[]: # (------- hidden links ------------)
[garage]: https://www.analog.com/en/incubators/analog-garage.html
[github]: https://github.com/analog-garage/dimple
[issues]: https://github.com/analog-garage/dimple/issues
