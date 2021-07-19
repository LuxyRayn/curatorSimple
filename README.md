# zookeeperTestDemo

~~~~
To start qsuedo zookeeper cluster:
1. download zookeeper
   zookeeper-3.6.3 https://downloads.apache.org/zookeeper/stable/apache-zookeeper-3.6.3-bin.tar.gz

2. unzip the zip file and enter the folder, create two folder: /data and /log
   the final folders tree should look like this:
   zookeeper
   |
   |--data--zoo-1
   |     |--zoo-2
   |     |--zoo-3
   |
   |--log --zoo-1
         |--zoo-2
         |--zoo-3

3. add "myid" file to /data/zoo-* folder
   each myid file contains only one digits that represent the id of the zookeeper server
   for exampe: 1 means zkid is 1
   data--zoo-1
      |     |--myid
      |--zoo-2
      |     |--myid
      |--zoo-3
            |--myid

4. find the config files in this project(/src/main/resources/zkConfig)
   a. change the .cfg files
      the zoo-1.cfg for example:
      _set new value of dataDir and dataLogDir, which is the folder you just create in step 2_
      
      _change the clientPort(each node's port has to different from the others in cluster)
      in this project, we set (2181, 2182, 2183)_
      
      _add admin.serverPort at the end of the file, set admin.serverPort value
      likely, we have to set different port for different node
      in this project, we set (8888, 8889, 8890)
      Tips: admin.serverPort is for zk manager(the new feature of recent version)_
    
      _add server.* (* means zkserver id, which you just set in myid file) to the end of the file:
          server.1 = 127.0.0.1:2888:3888
          server.2 = 127.0.0.1:2889:3889
          server.3 = 127.0.0.1:2890:3890_
   
   b. change the .cmd files
      the zkServer-1.cmd for example:
      _set new value for "call", like "call D:/developTool/apache-zookeeper-3.6.3-bin/bin/zkEnv.cmd"
      the address is located at the /bin file of the zookeeper folder_
      
      _set new value for "set ZOOCFG", like "set ZOOCFG=D:\developTool\apache-zookeeper-3.6.3-bin\conf\im_cluster\zoo-3.cfg"
      the file address is the zoo-*.cfg file's address_
      
5. just start each zkServer-*.cmd in command line

6. you can use jps tool to check whether the process is succeed
   if everything ok, you should see the picture below
   ![avatar](/readmeimg/img.png)
   
   or you can use the offcial tool zkCli.cmd in the zk/bin folder
   ./zkCli.cmd -server 127.0.0.1:2181 

      