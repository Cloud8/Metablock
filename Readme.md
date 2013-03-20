

Shanghai
========

##### Indexing

  Shanghai can build a solr index for the VuFind discovery system
  out of RDF data sources. <br/>

  The RDF data can be queried from a remote sparql service endpoint
  or from a local Jena TDB triple store. 

  To perform the indexing, three steps are required:

  1. Resource Enumeration: List all resources that should be indexed,

  2. Resource Dump: Query everything the triple store knows about a resource,

  3. Resource Tranformation: Transform RDF/XML to solr index format.


Step 1. and 2. needs a SPARQL query, step 3 works with XSLT. <br/>
  Although the sparql queries and xslt transformations 
  delivered together with shanghai are rathetr general, 
  the concrete modelling of the bibliographic resources may vary
  and requiere modifications.

##### RDF crawling

  Shanghai can be used as a filesystem crawler to collect rdf 
  resource description files and store them into a triple store.

##### OAI harvesting

  Shanghai use the xoai library for harvesting of bibliographic
  records into a local Jena triple store. 
  The storage of XML based records into a RDF storage engine
  requires a XSLT transformation.
  Together with shanghai there a currently two transformations
  defined, from XMetaDissPlus to RDF and from OJS/NLM to RDF.

  The RDF modeling of the records relies on the SPAR Ontologies
  and the Dublin Core Terms vocabulary.

##### About

  The Shanghai program is written in Java and controled by a 
  knowledge base written down in a turtle file together with
  some command line flags.

  The goal is to have a cronnable tool to drive a Jena/RDF based 
  bibliographic metadata hub with a VuFind discovery frontend.

  Shanghai is under development and not feature complete.

###TODO

  - check if SERVICE keyword works as expected to query distributed sources
  - most bibliographic data source have authority and title data
    separated. It may be possible to index RDF data to separate solr cores
    but it is unclear wether VuFind can work with linked data this way. 
  - more performance analysis

###May be later

  - add a knowledge base for indexing well known rdf data sources
  - add an OAI server with XSLT transformer to expose metadata from
    the local store as xml again.

###Collaboration
  We're *open to pull requests*! If you'd like to collaborate, 
  offer feedback, now better english or best of all - better code, 
  feel free to do so. 
  Please use the issues section on this github repository.

____________________________________________________________________________
