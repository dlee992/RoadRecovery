#!/usr/bin/env bash

name="outputs-10201-10300-0302.zip"

cd src/test/resources/
zip -r $name outputs/
git status
#git add -f $name
#git commit -am "automatic compression."
#git push -u origin master