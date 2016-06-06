#!/bin/sh
../zookeeper-one/bin/zkServer.sh start &
../zookeeper-two/bin/zkServer.sh start & 
../zookeeper-three/bin/zkServer.sh start &
./bin/helix-admin.sh --zkSvr localhost:2181,localhost:2182,localhost:2183 --addCluster bpms-cluster 
./bin/helix-admin.sh --zkSvr localhost:2181,localhost:2182,localhost:2183 --addNode bpms-cluster nodeOne:12345
./bin/helix-admin.sh --zkSvr localhost:2181,localhost:2182,localhost:2183  --addNode bpms-cluster nodeTwo:12346 
./bin/helix-admin.sh --zkSvr localhost:2181,localhost:2182,localhost:2183  --addResource bpms-cluster vfs-repo 1 LeaderStandby AUTO_REBALANCE
./bin/helix-admin.sh --zkSvr localhost:2181,localhost:2182,localhost:2183  --rebalance bpms-cluster vfs-repo 2
./bin/run-helix-controller.sh --zkSvr localhost:2181,localhost:2182,localhost:2183 --cluster bpms-cluster 2>&1 > /tmp/controller.log &
