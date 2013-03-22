#
# Makefile Forever Agency
#

JDIRS := $(shell find src -type d)
JDIRS := $(filter-out src/%/view,$(JDIRS))
FILES := $(foreach d,$(JDIRS),$(wildcard $(d)/*.java))
CLASS := $(patsubst src/%.java,lib/%.class,$(FILES))
CPATH := lib:$(subst $() $(),:,$(wildcard lib/*.jar))
JOPTS := -encoding UTF8

.PHONY: deploy 

lib/%.class: src/%.java
	@echo $<
	@javac $(JOPTS) -cp src:$(CPATH) -d lib $<

default: compile
	@echo "All compiled now, I believe."

deploy: 
	rsync -av --delete --exclude .git ./ archiv@archiv:/usr/local/opus/Shanghai

lib/shanghai.jar: $(CLASS) 
	jar cf $@ -C lib org -C lib log4j.properties

compile: lib/shanghai.jar

check:
	@echo CPATH: $(CPATH)
	@echo FILES: $(FILES)

clean:
	@rm -f $(CLASS)
	@rm -rf lib/org

cleaner:
	@rm -f lib/shanghai.jar

