Shanghai
========

  This is about building a solr index from a jena triple store.
  Indexing can be done in three steps:

  1. list all resources that should be indexed,

  2. query everything the triple store knows about a resource,

  3. transform the result to solr index format.

  There is an example in the book folder whith some sparql queries.


  To find out information about a dataset, you may want to:

  1. make props : list all predicates
  2. make fetch : fetch some identifiers to test if index.sparql works
  3. make probe : fetch a random record  to test if about.sparql works
  4. make index : start indexing process

____________________________________________________________________________
