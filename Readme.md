

  The AutoBib RDF Metadata Framework
=====================================

  AutoBib (abd) is a metadata framework for bibliograhical data build around 
  the RDF data model. 
  In its most basic functionality, it is a data crawler able to fetch a
  RDF description from a data source, analyze the data and write the
  possibly modified resource description back to a RDF target.

##### RDF Data sources:
  
  - SPARQL service endpoints
  - RDF data files from the filesystem
  - OAI datasources
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
  - NLM Analyzer: set urn identifier property, transform NLM to RDF 
  - DOI Analyzer: register a DOI for a resource
  - PDF Analyzer: extract bibliographic metadata and references from PDF

  - REF Analyzer: some bibliographic relations are reflexive like so:
    <code>paperA dcterms:references paperB</code> vs. 
    <code>paperB dcterms:isReferencedBy paperA</code>. 
    This Analyzer trys to update a referenced knowledge base with the inverse 
    relation (experimental).

  - CTX Analyzer: find citation context and set c4o:hasContext and 
    c4o:hasInTextCitationFrequency property for a dct:bibliographicCitation

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

%% <!-- See http://journal.code4lib.org/articles/8526 -->

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
  knowledge base written down in turtle together with some 
  command line flags.

  The goal is to have a cronnable tool to drive a RDF based 
  bibliographic metadata hub with VuFind as a discovery frontend.


### Collaboration

  We're *open to pull requests*! If you'd like to collaborate, 
  offer feedback, know better english or best of all - better code, 
  feel free to make use of the issues section on this github repository.

____________________________________________________________________________
