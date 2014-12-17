

Seaview
========

##### Crawling : from OAI to RDF to RDF

  Before publishing RDF data, data sources should be crawled

##### Indexing : from RDF to solr

  To build a solr index from a SPARQL service endpoint,
  three steps are required:

  1. Resource Enumeration: List all resources that should be indexed,

  2. Resource Dump: Query everything the triple store knows about a resource,

  3. Resource Tranformation: Transform RDF/XML to solr index format.


Step 1. and 2. need a SPARQL query, step 3 works with XSLT. <br/>
  The sparql queries and xslt transformations from lib/sparql and lib/xslt
  are rather general, but modelling of bibliographic resources may vary and 
  require modifications.  

%% <!-- See http://journal.code4lib.org/articles/8526 -->

  All configurations are done in lib/seaview.ttl

##### Sparql Endpoint Test
  RDF data investigation is supported to a certain limit:

  <code>abd -probe</code> : get basic information about remote services.

  <code>abd -test</code> : get some URIs based on enumeration query.

  <code>abd -dump</code> : dump a specific resource

  <code>abd -index </code> : load, transform and index.

##### Crawling RDF and Lifting XML to RDF

  The seaview program can be used to crawl xml data, lift data to rdf
  and store everything into a triple store. 

    abd -crawl oai : load oai data as defined by configuration file

##### About

  The seaview program is written in Java and controled by a 
  knowledge base written down in turtle together with some 
  command line flags.

  The goal is to have a cronnable tool to drive a RDF based 
  bibliographic metadata hub with VuFind as a discovery frontend.


### Collaboration

  We're *open to pull requests*! If you'd like to collaborate, 
  offer feedback, know better english or best of all - better code, 
  feel free to make use of the issues section on this github repository.

____________________________________________________________________________
