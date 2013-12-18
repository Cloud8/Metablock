

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

  See http://journal.code4lib.org/articles/8526

##### Sparql Endpoint Test
  Shanghai does support SPARQL endpoint investigation to a certain
  limit.
  You can formulate a probe-query which is intended as a "Hello World"
  use case, to just make sure that everything works.
  If that works, try

  <code>shanghai -test</code> : get some URIs based on enumeration query.

  <code>shanghai -dump</code> : dump a specific resource

  <code>shanghai -index 0 1</code> : dump first resource, transform and index.

##### RDF crawling && OAI harvesting

  Shanghai can be used as a filesystem crawler to collect rdf 
  resource description files and store them into a triple store.

  Shanghai uses the xoai library for harvesting bibliographic
  records into a local Jena triple store. 
  The storage of XML based records into a RDF storage engine
  requires XSLT transformations.
  Together with shanghai there a currently two transformations
  defined, from XMetaDissPlus to RDF and from OJS/NLM to RDF.

  The RDF modeling of the records relies on the SPAR Ontologies
  and the Dublin Core Terms vocabulary.

##### About

  The Shanghai program is written in Java and controled by a 
  knowledge base written down turtle together with some 
  command line flags.

  The goal is to have a cronnable tool to drive a Jena/RDF based 
  bibliographic metadata hub with VuFind as a discovery frontend.

  Shanghai is under development and not feature complete.

### Compile
  
  Java 7 and upwards...

### May be later

  - add an OAI server to expose metadata from the local store as RDF/XML

### Collaboration
  We're *open to pull requests*! If you'd like to collaborate, 
  offer feedback, know better english or best of all - better code, 
  feel free to do so. 
  Please use the issues section on this github repository.

____________________________________________________________________________
