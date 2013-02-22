#
# Make Intelligence
#

JDIRS := $(shell find src -type d)
JDIRS := $(filter-out src/%/old,$(JDIRS))
FILES := $(foreach d,$(JDIRS),$(wildcard $(d)/*.java))
CLASS := $(patsubst src/%.java,lib/%.class,$(FILES))

# copy
JENAH := $(wildcard $(HOME)/data/jena/apache-jena-2.7.4/lib/*.jar)
VFLIB := /usr/local/vufind/solr/jetty/webapps/WEB-INF/lib
SOLRH := $(VFLIB)/apache-solr-solrj-3.5.0.jar
SOLRH := $(SOLRH) $(VFLIB)/commons-httpclient-3.1.jar

# compile
CPATH := lib:$(subst $() $(),:,$(wildcard lib/*.jar))
JOPTS := -Xlint:deprecation -Xlint:unchecked -Xmaxerrs 9 -encoding UTF8
J4OPT := -Dlog4j.configuration=lib/log4j.properties
PACKT := shanghai

# run
DIRS := /srv/archiv/diss/2013 /srv/archiv/eb/2013 /srv/archiv/es/2013
MAIN := org.shanghai.rdf.Main -prop book/gnd.properties
MAIN := org.shanghai.rdf.Main -prop book/opus.properties

.PHONY: deploy 

lib/%.class: src/%.java
	javac $(JOPTS) -cp src:$(CPATH) -d lib $<

def: lib compile 
	@echo "All compiled now, I believe."
	@echo "make zero : delete solr index"
	@echo "make index : build solr index from jena triple store"

zero: # solr clean up : throws away the index
	@java -cp $(CPATH) $(MAIN) -clean

crawl: # crawl file system and update jena store with rdf files
	@java -cp $(CPATH) $(MAIN) -crawl $(DIRS)

test: # fetch some record URIs from triple store
	@java -cp $(CPATH) $(MAIN) -probe 22

probe: # fetch a random record from triple store
	@java -cp $(CPATH) $(MAIN) -probe

get: # retrieve description about uri 1=$1 from store to file 2=$2
	@java -cp $(CPATH) $(MAIN) -get $1 $2

put: # jena update rdf file 1=$1 with description to triple store
	@java -cp $(CPATH) $(MAIN) -upd $1

del: # remove description about uri 1=$1 from store
	@java -cp $(CPATH) $(MAIN) -del $1

post: # post description about uri 1=$1 to solr
	@java -cp $(CPATH) $(MAIN) -post $1

index: # jena store index
	@java -cp $(CPATH) $(MAIN) -index

# copy like mad
copy: 
	@mkdir -p lib
	@cp $(JENAH) lib/
	@cp $(SOLRH) lib/

compile: $(CLASS)

jar: 
	jar cf shanghai.jar lib 
	@#jar ufm shanghai.jar lib/manifest 

check:
	@#echo FILES: $(FILES)
	@#echo CPATH: $(CPATH)
	@#echo JENAH: $(JENAH)

clean:
	@rm -f $(CLASS)
	@rm -rf lib/org

