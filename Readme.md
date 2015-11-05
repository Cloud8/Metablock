

  Metablock: Preparations for a Semantic Publishing Repository 
================================================================

  Metablock reads RDF data and writes a possibly modified resource
  description back to a RDF target.

##### RDF Data sources:
  
  - SPARQL service endpoints
  - RDF data files 
  - OAI data sources with XSLT lifting
  - RDBMS with XSLT lifting

##### RDF data targets:

  - Virtuoso RDF storage
  - Jena TDB storage
  - RDF data files 
  - Solr search index as defined by VuFind 

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
  The sparql queries and xslt transformations used so far are rather general, 
  but modelling of bibliographic resources may vary and require modification.  


  All configurations are done in lib/seaview.ttl

##### RDF Transporter Test

  [<code>abd -crawl -probe</code>] : get basic information about a data source .

  [<code>abd -crawl -test</code>] : get some URIs based on enumeration query.

  [<code>abd -crawl -test *resource*</code>] : index and transform resource

  [<code>abd -crawl *resource*</code>] : index, transform and write resource

### Collaboration

  We're *open to pull requests*! If you'd like to collaborate, offer feedback, 
  know better english or better code, feel free to make use of the issues 
  section on this github repository.

____________________________________________________________________________
