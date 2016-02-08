#
# Makefile Security Agency
#

JDIRS := $(shell find src -type d)
FILES := $(foreach d,$(JDIRS),$(wildcard $(d)/*.java))
CLASS := $(patsubst src/%.java,lib/%.class,$(FILES))
CPATH := lib:$(subst $() $(),:,$(wildcard lib/*.jar))
JOPTS := -encoding UTF8

lib/%.class: src/%.java
	@echo $<
	@javac $(JOPTS) -cp src:$(CPATH) -d lib $<

default: lib/seaview.jar

compile: $(CLASS) 

lib/seaview.jar: $(CLASS) lib/seaview.ttl
	jar cf $@ -C lib org 

metablock.jar: lib/seaview.jar lib/xslt lib/sparql lib/Manifest.txt
	jar cfm $@ lib/Manifest.txt 
	jar uf $@ -C lib com -C lib xslt -C lib sparql lib/seaview.ttl
	jar uf $@ lib/languages
	jar uf $@ lib/*.jar 
	cd lib; jar uf ../$@ *.properties

fat: metablock.jar
	java -jar metablock.jar

doc: $(CLASS)
	javadoc -cp $(CPATH) -d doc -tag date:a:"Date:" -tag title:a:"Title:" -tag license:a:"License:" -tag abstract:a:"Abstract:"  $(FILES)

test: 
	@java -cp lib:lib/* org.seaview.cite.Main

check:
	@echo CPATH: $(CPATH)
	@echo FILES: $(FILES)

clean:
	@rm -f $(CLASS)
	@rm -rf lib/org
	@rm -rf lib/com
	@rm -f metablock.jar

cleaner: clean
	@rm -f lib/seaview.jar

