#!/bin/bash
export DIR=$( dirname "${BASH_SOURCE}[0]" )
cd $DIR
ant build
if [ $? -eq 0 ]; then
  rm -rf bin
  mkdir -p bin/resolver/lib
  mkdir -p bin/resolver/log
  mkdir -p bin/resolver/feeds
  cp Resolver/scripts/* bin/resolver/
  cp Resolver/staging/* bin/resolver/lib
  cp eventFeeds/* bin/resolver/feeds/
  cd bin
  zip -r resolver.zip resolver
fi