#
# Makefile Forever Agency
#

JDIRS := $(shell find src -type d)
FILES := $(foreach d,$(JDIRS),$(wildcard $(d)/*.java))
CLASS := $(patsubst src/%.java,dlib/%.class,$(FILES))
CPATH := dlib:lib:$(subst $() $(),:,$(wildcard lib/*.jar))
JOPTS := -encoding UTF8

.PHONY: deploy

dlib/%.class: src/%.java
	@echo $<
	@javac $(JOPTS) -cp src:$(CPATH) -d dlib $<

default: compile
	@echo "All compiled now, I believe."

dlib:
	mkdir -p dlib

compile: $(CLASS)

check:
	@echo CPATH: $(CPATH)
	@echo FILES: $(FILES)

clean:
	@rm -f $(CLASS)
	@rm -rf lib/org

cleaner: clean
	@rm -f lib/shanghai.jar
	@rm -rf dlib/org dlib/com dlib/manifest main

