#!/bin/bash
cd BTAnalyser
/usr/local/bin/sbcl --dynamic-space-size 4096 --load "start.lisp"
