#!/bin/bash

mkdir -p $PREFIX/lib/jars
cp jars/*.jar $PREFIX/lib/jars
cd src
pip install . --no-deps --no-build-isolation
