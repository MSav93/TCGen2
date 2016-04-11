#!/bin/bash
STR=$(ps -ax | grep sbcl | grep "start.lisp")
if [ "$STR" != "" ];
then
    kill -9 $(echo "$STR" | sed 's/\([0-9][0-9]*\).*/\1/')
fi
STR=$(ps -ax | grep "gksudo ./start.sh")
if [ "$STR" != "" ];
then
    kill -9 $(echo "$STR" | sed 's/\([0-9][0-9]*\).*/\1/')
fi
STR=$(ps -ax | grep "./start.sh")
if [ "$STR" != "" ];
then
    kill -9 $(echo "$STR" | sed 's/\([0-9][0-9]*\).*/\1/')
fi

