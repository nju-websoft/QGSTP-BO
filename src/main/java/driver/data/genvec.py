#!/usr/bin/python
# -*- coding: UTF-8 -*-
from pyrdf2vec.graphs import KG
from pyrdf2vec.samplers import UniformSampler
from pyrdf2vec.walkers import RandomWalker
from pyrdf2vec import RDF2VecTransformer
from pyrdf2vec.embedders import Word2Vec
import rdflib
import pandas as pd
import pymysql

def construct_nodevec(graph,dbname,demension):
    print("Reading from database...")
    conn=pymysql.connect(
        host="localhost",
        database=dbname,
        user="root",
        password="123456",
        port=23333,
        charset="utf8"
    )
    context=pd.read_sql("select id,name from nodes",conn).values
    n=len(context)
    entities=[""]*n
    for row in context:
        entities[row[0]]=row[1]
    
    print("Reading KG...")
    kg = KG(graph, label_predicates=[])
    
    print("Construct RDF2VecTransformer...")
    transformer = RDF2VecTransformer(embedder=Word2Vec(size=demension),walkers=[RandomWalker(2, 50, UniformSampler())])
    print("Generate embeddings...")
# Entities should be a list of URIs that can be found in the Knowledge Graph
    embeddings = transformer.fit_transform(kg, entities)

    print("Write back to database...")
    cur=conn.cursor()
    cur.execute(
        "create table nodevec(" +
        "    `id` int not null," +
        "    `dimension` int not null," +
        "    `value` double not null," +
        "    primary key(`id`,`dimension`)" +
        ")"
    )
    conn.commit()

    sql="insert into nodevec values (%s,%s,%s)"
    val=[]

    for i in range(n):
        for j in range(demension):
            val.append((i,j,float(embeddings[i][j])))
    cur.executemany(sql,val)
    cur.close()
    conn.commit()
    conn.close()
    

    

if __name__ == "__main__":
    graph="lubm_2u"
    pwd="C:\\resources\\"
    construct_nodevec(pwd+"lubm_2u.nt",graph,10)