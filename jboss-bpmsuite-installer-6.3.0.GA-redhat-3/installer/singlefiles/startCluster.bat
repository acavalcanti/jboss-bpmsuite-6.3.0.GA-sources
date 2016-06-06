@ECHO OFF
start "Zookeeper One" %~dp0..\zookeeper-one\bin\zkServer.cmd 
start "Zookeeper Two" %~dp0..\zookeeper-two\bin\zkServer.cmd 
start "Zookeeper Three" %~dp0..\zookeeper-three\bin\zkServer.cmd
start "Adding cluster bpms-cluster" /wait cmd.exe /c %~dp0\bin\helix-admin.bat --zkSvr localhost:2181,localhost:2182,localhost:2183 --addCluster bpms-cluster 
start "Adding node nodeOne:12345" /wait cmd.exe /c %~dp0\bin\helix-admin.bat --zkSvr localhost:2181,localhost:2182,localhost:2183 --addNode bpms-cluster nodeOne:12345
start "Adding node nodeTwo:12346" /wait cmd.exe /c %~dp0\bin\helix-admin.bat --zkSvr localhost:2181,localhost:2182,localhost:2183 --addNode bpms-cluster nodeTwo:12346 
start "Adding resources vfs-repo" /wait cmd.exe /c %~dp0\bin\helix-admin.bat --zkSvr localhost:2181,localhost:2182,localhost:2183 --addResource bpms-cluster vfs-repo 1 LeaderStandby AUTO_REBALANCE
start "Rebalancing vfs-repo" /wait cmd.exe /c %~dp0\bin\helix-admin.bat --zkSvr localhost:2181,localhost:2182,localhost:2183  --rebalance bpms-cluster vfs-repo 2
start "Helix Controller" %~dp0\bin\run-helix-controller.bat --zkSvr localhost:2181,localhost:2182,localhost:2183 --cluster bpms-cluster
