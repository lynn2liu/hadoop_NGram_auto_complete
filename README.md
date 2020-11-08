# hadoop_NGram_auto_complete
##Implementation
###1. set up MAMP for mac
set up Mysql
Note: Start MySQL server before run MySQL
```
sudo systemctl start mysql
cd /usr/local/mysql/bin/
./mysql -uroot -pyour_password
```

Create 'Test' database and 'output' table in MySQL.
```
create database Test;
use Test;
create table output(starting_phrase VARCHAR(250), following_word VARCHAR(250), count INT);

```

Get port number of MySQL database. (We need it when MapReduce writing into MySQL database.)
```
SHOW VARIABLES WHERE Variable_name = 'port';
```


###2. Set up Hadoop and HDFS
Use Docker to set up the Hadoop environment with 1 namenode (master machine) and 2 datanodes (slave machines).

Create Hadoop network.
```
sudo docker network create --driver=bridge hadoop
```


Modify start-container.sh to ensure you sync '/~/src' codes in your localhost with '/root/src' on docker container.
Start container. 
The correct output is
```
start hadoop-master container...
start hadoop-slave1 container...
start hadoop-slave2 container...
root@hadoop-master:~# 
```


Set up HDFS.
```
hdfs dfs -mkdir /mysql
hdfs dfs -put mysql-connector-java-*.jar /mysql/ #hdfs path to mysql-connector*
```


###3. set up parameters
In the container, find the auto-complete folder. It should be /root/src/auto-complete. Set input path for HDFS.

```
hdfs dfs -mkdir -p input
```

Make sure HDFS output path not exist.
```
hdfs dfs -rm -r /output 
```

Upload input files into HDFS.
```
hdfs dfs -put bookList/* input/
```

###4.Run Auto-complete program

```
hadoop com.sun.tools.javac.Main *.java
jar cf ngram.jar *.class
```

Set nGram_size(2), threshold_size(3), and topK(4).
```
hadoop jar ngram.jar Driver input /output 2 3 4
```


In localhost, check whther the results have been written into database.
```
mysql -uroot -pyour_password
use Test
select * from output limit 10;
```

the final result show below:
![avatar](/web/image/screenshot.png)