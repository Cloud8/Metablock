

  Autobib: Bibliographic Metadata Synchronizer
==================================================

  AutoBib (abd) is a metadata framework for bibliograhical data build around 
  the RDF data model. 
  Most basically its a data crawler able to fetch a RDF description from a 
  data source, analyze the data and write the possibly modified resource 
  description back to a RDF target.

##### RDF Data sources:
  
  - SPARQL service endpoints
  - RDF data files from the filesystem
  - OAI datasources lifted to RDF by XSLT 
  - RDF data files retrieved from the web

##### RDF data targets:

  - Virtuoso RDF storage
  - Jena TDB storage
  - 4store RDF storage
  - RDF files on the local file system
  - Solr search index as defined by the VuFind discovery system
  - System console

##### RDF data analyzers

  - Language analyzer: guesses the language of a resource desription
    from the dcterms abstract or title property
  - PDF Analyzer: extract bibliographic metadata and references from PDF

  - REF Analyzer: extract bibliographic relations like
    <code>paperA dcterms:references paperB</code>

  - CTX Analyzer: find citation context bibliographic citation

##### Search support : write RDF data to a Solr search index

  To build a solr index from a SPARQL service endpoint,
  three steps are required:

  1. Resource Enumeration: List resources 

  2. Resource Dump: Query knowledge about a resource,

  3. Resource Tranformation: Transform RDF data to solr index format.



##### About

  The autobib program is written in Java and controled by a 
  knowledge base written down in turtle together with some 
  command line flags.

  The goal is to have a cronnable tool to drive a RDF based 
  bibliographic metadata hub with VuFind as a discovery frontend.


### Collaboration

  We're *open to pull requests*! If you'd like to collaborate, 
  offer feedback, know better english or best of all - better code, 
  feel free to make use of the issues section on this github repository.

____________________________________________________________________________
