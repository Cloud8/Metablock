

Shanghai
========

##### Indexing

  Shanghai can build a solr index for the VuFind discovery system
  out of RDF data sources. <br/>

  To perform the indexing, three steps are required:

  1. Resource Enumeration: List all resources that should be indexed,

  2. Resource Dump: Query everything the triple store knows about a resource,

  3. Resource Tranformation: Transform RDF/XML to solr index format.


Step 1. and 2. needs a SPARQL query, step 3 works with XSLT. <br/>
  Although the sparql queries and xslt transformations 
  delivered together with shanghai are rather general, 
  the concrete modelling of bibliographic resources may vary
  and require modifications.  
  RDF data can be queried from a remote sparql service endpoint
  or from a local Jena TDB triple store. 

%% See http://journal.code4lib.org/articles/8526

##### Sparql Endpoint Test
  Shanghai does support SPARQL endpoint investigation to a certain
  limit.

  <code>shanghai -probe</code> : get basic information about remote services.

  <code>shanghai -test</code> : get some URIs based on enumeration query.

  <code>shanghai -dump</code> : dump a specific resource

  <code>shanghai -index </code> : load, transform and index.

##### Crawling RDF and Lifting XML to RDF

  Shanghai can be used to collect xml data, lift the data to rdf
  and store everything into a triple store. 

  The storage of XML based records into a RDF storage engine
  requires XSLT transformations.
  Currently two transformations are defined: 
  from XMetaDissPlus to RDF and from OJS/NLM to RDF.

  The RDF modeling of the records relies on the SPAR Ontologies
  and the Dublin Core Terms vocabulary.

    shanghai -crawl oai

##### About

  The Shanghai program is written in Java and controled by a 
  knowledge base written down in turtle together with some 
  command line flags.

  The goal is to have a cronnable tool to drive a Jena/RDF based 
  bibliographic metadata hub with VuFind as a discovery frontend.

  Shanghai is under development and not feature complete.

### Compile
  
  Only Java 7 and upwards...

### Collaboration

  We're *open to pull requests*! If you'd like to collaborate, 
  offer feedback, know better english or best of all - better code, 
  feel free to do so. 
  Please use the issues section on this github repository.

____________________________________________________________________________
