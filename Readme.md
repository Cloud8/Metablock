

Shanghai
========

  Shanghai can build a solr index out of RDF data sources. <br/>

  The RDF data can be queried from a remote sparql service endpoint
  or reside in a local Jena TDB triple store. 

  To perform the indexing, three steps are required:

  1. Resource Enumeration: List all resources that should be indexed,

  2. Resource Dump: Query everything the triple store knows about a resource,

  3. Resource Tranformation: Transform RDF/XML to solr index format.

  Step 1. and 2. needs a SPARQL query, step 3 works with XSLT. <br/>
  There are some examples in the book folder to help with the steps,
  but the solution is to break the RDF index problem into this parts.

  The Shanghai program is written in Java and controled by a property 
  file and some command line flags.

  *This is the first initial realease and considered test code.*

##TODO
  - check if SERVICE keyword works as expected to query distributed sources
  - some performance analysis, may be there are too much model.commits()

##May be later
  - add a knowledge base for indexing well known rdf data sources
  - add an OAI client with XSLT transformer to harvest metadata and
    collect them as rdf data in a local triple store

##Collaboration
  We're *open to pull requests*! If you'd like to collaborate, 
  offer feedback, now better english or best of all - better code, 
  feel free to do so. 
  Please use the issues section on this github repository.

____________________________________________________________________________
