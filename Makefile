#
# Makefile Forever Agency
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

default: deploy 
	@echo "All compiled now, I believe."

deploy: lib/shanghai.jar

lib/shanghai.jar: $(CLASS) lib/shanghai.properties
	jar cf $@ -C lib org -C lib shanghai.properties -C lib log4j.properties

compile: $(CLASS)

check:
	@echo CPATH: $(CPATH)
	@echo FILES: $(FILES)

clean:
	@rm -f $(CLASS)
	@rm -rf lib/org

cleaner:
	@rm -f lib/shanghai.jar

