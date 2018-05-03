#!/bin/bash

files=$(echo *.java)

javac $files
java Adhoc
