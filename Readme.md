
## Metablock: Semantic Publishing Tools 

  [Metablock](http://cloud8.github.io/Metablock) reads RDF data and 
  writes a possibly modified resource description back to a RDF target.

##### RDF Data sources:
  
  - SPARQL service endpoints
  - RDF data files 
  - OAI data sources with XSLT lifting
  - RDBMS with XSLT lifting

##### RDF data targets:

  - Virtuoso RDF storage
  - Jena TDB storage
  - RDF data files 
  - Solr search index with XSLT transformation

##### Compile && Configure

  - configured by lib/metablock.ttl 
  - make 
  - java -jar metablock.jar

##### Indexing : write RDF data to a Solr search index

  To build a Solr search index from a SPARQL service endpoint,
  three steps are required:

  1. Resource Enumeration: List all resources that should be indexed,

  2. Resource Dump: Query everything the triple store knows about a resource,

  3. Resource Tranformation: Transform RDF/XML to solr index format.


Step 1. and 2. need a SPARQL query, step 3 works with XSLT. <br/>
  The sparql queries and xslt transformations used so far are rather general, 
  but modelling of bibliographic resources may vary and require modification.  

  All configurations are done in turtle (see lib/metablock.ttl)

##### RDF Crawling

  RDF data sources configured in *lib/metablock.ttl* can be tested with

    java -jar metablock.jar -s [source] -t [target] -test

  Copy from RDF *source* to RDF *target*

    java -jar metablock.jar -s [source] -t [target]


##### RDF analyzers

  PDF Analyzer: utilizes Grobid / CERMINE to extract metadata and
  bibliographical references from scientific articles 

    java -jar metablock.jar -s [source] -t [target] -e pdf

  Reference Analysis: use external libraries to find citation context and
  determine citation polarity

    java -jar metablock.jar -s [source] -t [target] -e sen


##### Examples

   1. Index a directory of PDF files, enable pdf engine to extract metadata 
      and write to VuFind:

        java -jar metablock.jar -crawl -s files -t solr1 -e pdf Documents

   2. Write DSpace metadata from DSpace REST API to Virtuoso triplestore 
      (experimental):
   
        java -jar metablock.jar -crawl -s dspace -t virt

   3. Crawl OAI sources to a jena TDB store:

        java -jar metablock.jar -crawl -s oai -t tdb

   4. Build a search index for a TDB triple store

        java -jar metablock.jar -crawl -s tdb -t solr1

##### Javadoc Documentation
  
  [Javadoc](http://cloud8.github.io/Metablock/doc) is available online.
   


------------------------------------------------------------------------

____________________________________________________________________________
