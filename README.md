

Shanghai
========

  Shanghai is an indexer which is capable to build a solr index
  out of rdf data. <br/>
  The rdf data can reside in a jena triple store or queried from
  a remote sparql service endpoint.

  To perform the indexing, three steps are required:

  1. list all resources that should be indexed,

  2. query everything the triple store knows about a resource,

  3. transform the result to solr index format.


  Step 1. and 2. needs a sparql query, step 3 works with XSLT. <br/>
  There are some examples in the book folder to help with the steps.

  The shanghai program is written in the Java programming language
  and controled by a proerty file and some command line flags.
  See Makefile for reference.

  This is the first initial realease and considered test code.

  

##TODO
  - check if SERVICE keyword works as expected to query distributed sources
  - add a OAI client with a XSLT transformer to harvest metadata and
    collect them as rdf data in a local triple store
  - performance analysis
  - add a knowledge base for indexing known rdf data sources
  - check, if SolrCloud  can be used for sort of distributed 
    index 

##Collaboration
  We're *very* open to pull requests! If you'd like to collaborate, 
  offer feedback, or best of all - better code, feel free to do so. 
  Please use the issues section on this github repository.
____________________________________________________________________________
