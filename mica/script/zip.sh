#!/bin/bash

cd $1
zip -r -D -X $2

mv $3 $4

cd $4
rm -r $5
