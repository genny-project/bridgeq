#!/bin/bash
jps -l | grep bridgeq | cut -d" " -f1 | xargs kill -9

