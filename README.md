# Approximation Algorithms for the Quadratic Group Steiner Tree Problem
[![Contributions Welcome](https://img.shields.io/badge/Contributions-Welcome-brightgreen.svg?style=flat-square)](https://github.com/nju-websoft/OpenEA/issues)
[![License](https://img.shields.io/badge/License-Apache-lightgrey.svg?style=flat-square)](https://github.com/nju-websoft/OpenEA/blob/master/LICENSE)
[![language-python3](https://img.shields.io/badge/Language-Java-yellow.svg?style=flat-square)](https://www.python.org/)

This is the source code of the paper 'Approximation Algorithms for the
Quadratic Group Steiner Tree Problem'.

## Directory Structure
Directory /src/main/java contains all the source code based on JDK 11.

+ Directory /src/main/java/graphtheory contains our implementation of algorithms

  'SemKSG' is the implementation of our proposed algorithms

  'DPBF' is the implementation of DPBF

  'B3F' is the implementation of B$^3$F

+ Directory /src/main/java/driver includes some classes to conduct our experiment

  'data' is used to generate dataset

  'work' is used to call our algorithms
+ Directory /src/main/java/mytools consists of some tools convenient for coding

## Getting Started

### Environment

+ Mysql
+ JDK11
+ Maven

### Build

Use maven to build our project:

```shell
cd QGSTP-BO-main
mvn clean package
```

### Data

#### Dataset
Our dataset is available from [DropBox](https://www.dropbox.com/sh/025goup8bi2xjim/AAD0lzaUnWiBcBE3c-vQuOHoa?dl=0), [Onedrive](https://1drv.ms/u/s!AhmzTJHXbVtegmZHH0OMfjGrZX2S?e=MUxQfK) and [Baidu Wangpan](https://pan.baidu.com/s/1Iu0Zt2SMWmTsEWZGOV18zg?pwd=m5xm) (password: m5xm).

Import the data to your mysql database.

#### Generate Hub Label

To run our algorithms, Hub Label should be built ahead. Take `dbpedia_50k` as an example.

Copy a configure file.

```shell
cp .\src\main\resources\config.properties my.properties
```

Assign variables `IP`, `PORT`, `USER` and `PASS` in `my.properties` to connect your database.

Assign `DATABASE=dbpedia_50k` and `SD=Jaccard` (for LUBM `SD` is set to `Rdf2Vec`) .


Then use following command to construct Hub Label:

```shell
java -cp target/QGSTP-jar-with-dependencies.jar:. driver.data.Run1 -c my.properties -p GenerateHubLabel
```


The above command would cost a lot of time. Set `DEBUG=TRUE` if you are concerned about running progress.


### Run Algorithm

Our algorithms will be run to answer the queries stored in database.

First fill `my.properties` according to the instruction inside. Specifically, some variables should be set in the following way:

+ `IP`, `PORT`, `USER`, `PASS` and `database`: connect to your database
+ For DBpedia `SD=Jaccard` and for LUBM `SD=Rdf2Vec`
+ `QUERY_NUM` : the total query in database

  (maybe you can invoke `select count(distint query) from queries` to decide the value)

  if `QUERY_NUM=k`, only the first k queries will be processed.
+ `DEBUG=FALSE`


Now run the algorithm:

```shell
java -cp target/QGSTP-jar-with-dependencies.jar:. driver.work.Run -c my.properties
```

## License
This project is licensed under the GPL License - see the [LICENSE](LICENSE) file for details

## Citation
If you think our algorithms or our experimental results are useful, please kindly cite our paper.