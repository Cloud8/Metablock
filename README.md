Shanghai
========

  This is about building a solr index from a jena triple store.
  Indexing from jena to solr can be done in three steps:

  1. list all resources that should be indexed,

  2. query everything the triple store knows about a resource,

  3. transform the result to solr index format.

  There is an example in the book folder whith some sparql queries.

  Step 1. and 2. needs a sparql query, step 3 works with XSLT.


  This is the first initial realease and test code.

____________________________________________________________________________
