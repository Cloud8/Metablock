

  Autobib: Bibliographic Metadata Synchronizer
==================================================

  This is a data crawler able to fetch a description from a RDF data source, 
  analyze the data and write the possibly modified resource description back 
  to a RDF target.

##### RDF Data sources:
  
  - SPARQL service endpoints
  - RDF data files 
  - OAI datasources with XSLT lifting

##### RDF data targets:

  - Virtuoso RDF storage
  - Jena TDB storage
  - 4store RDF storage
  - RDF files 
  - Solr search index as defined by the VuFind discovery system

##### RDF data analyzers

  - Language analyzer: guesses the language of a resource desription
    from dcterms abstract or title property
  - PDF Analyzer: uses external libraries (grobid, cermine) to extract 
    bibliographic metadata and references from PDF documents

##### Indexing : writing RDF data to a Solr search index

  To build a solr index from a SPARQL service endpoint,
  three steps are required:

  1. Resource Enumeration: List all resources that should be indexed,

  2. Resource Dump: Query everything the triple store knows about a resource,

  3. Resource Tranformation: Transform RDF/XML to solr index format.


Step 1. and 2. need a SPARQL query, step 3 works with XSLT. <br/>
  The sparql queries and xslt transformations from lib/sparql and lib/xslt
  are rather general, but modelling of bibliographic resources may vary and 
  require modifications.  


  All configurations are done in lib/seaview.ttl

##### RDF Transporter Test

  [<code>abd -crawl -probe</code>] : get basic information about a data source .

  [<code>abd -crawl -test</code>] : get some URIs based on enumeration query.

  [<code>abd -crawl -dump</code>] : dump a random resource

  [<code>abd -crawl -dump *resource*</code>] : dump a specific resource

  [<code>abd -crawl -test *resource*</code>] : index and transform resource

  [<code>abd -crawl *resource*</code>] : index, transform and write resource

##### About

  The autobib program is written in Java and controled by a 
  knowledge base written down in turtle

  The goal is to have a cronnable tool to drive a RDF based 
  bibliographic metadata hub with VuFind as a discovery frontend.


### Collaboration

  We're *open to pull requests*! If you'd like to collaborate, 
  offer feedback, know better english or best of all - better code, 
  feel free to make use of the issues section on this github repository.

____________________________________________________________________________
