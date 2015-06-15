#
# Makefile Security Agency
#

JDIRS := $(shell find src -type d)
FILES := $(foreach d,$(JDIRS),$(wildcard $(d)/*.java))
CLASS := $(patsubst src/%.java,lib/%.class,$(FILES))
CPATH := lib:$(subst $() $(),:,$(wildcard lib/*.jar))
JOPTS := -encoding UTF8

.PHONY: deploy

lib/%.class: src/%.java
	@echo $<
	@javac $(JOPTS) -cp src:$(CPATH) -d lib $<

default: lib/autobib.jar

compile: $(CLASS) 

lib/autobib.jar: $(CLASS) lib/seaview.ttl
	jar cf $@ -C lib org 

autobib.jar: lib/autobib.jar lib/xslt lib/sparql lib/Manifest.txt
	jar cfm $@ lib/Manifest.txt 
	jar uf $@ -C lib com -C lib xslt -C lib sparql lib/seaview.ttl
	jar uf $@ lib/languages
	jar uf $@ lib/*.jar 
	cd lib; jar uf ../$@ *.properties

fat: autobib.jar
	@#java -Done-jar.verbose -jar autobib.jar
	java -jar autobib.jar

test: 
	@java -cp lib:lib/* org.shanghai.rdf.Main

check:
	@echo CPATH: $(CPATH)
	@echo FILES: $(FILES)

clean:
	@rm -f $(CLASS)
	@rm -rf lib/org
	@rm -rf lib/com
	@rm -f autobib.jar

cleaner:
	@rm -f lib/autobib.jar

