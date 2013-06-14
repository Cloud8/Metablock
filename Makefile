#
# Makefile Forever Agency
#

JDIRS := $(shell find src -type d)
FILES := $(foreach d,$(JDIRS),$(wildcard $(d)/*.java))
CLASS := $(patsubst src/%.java,dlib/%.class,$(FILES))
CPATH := dlib:$(subst $() $(),:,$(wildcard lib/*.jar))
JOPTS := -encoding UTF8

.PHONY: deploy fat

dlib/%.class: src/%.java
	@echo $<
	@javac $(JOPTS) -cp src:$(CPATH) -d dlib $<

default: compile
	@echo "All compiled now, I believe."

dlib:
	mkdir -p dlib

deploy: 
	@#rsync -av shanghai.jar archiv@archiv:/usr/local/opus/Shanghai
	rsync -av --delete lib dlib shanghai archiv@archiv:/usr/local/opus/Shanghai/

compile: $(CLASS)

shanghai.jar: dlib/manifest $(CLASS)
	jar cfm $@ dlib/manifest
	@#rm -rf dlib/META-INF dlib/manifest
	jar uf $@ -C dlib org
	jar uf $@ lib

dlib/manifest: Makefile
	@echo "Created-By: Nirvana Coorporation" >$@
	@#echo "Class-Path: . lib $(wildcard lib/*)" >>$@
	@echo "Class-Path: . lib " >>$@
	@echo "Main-Class: org.shanghai.main.Main" >>$@

fat: shanghai.jar
	
check:
	@echo CPATH: $(CPATH)
	@echo FILES: $(FILES)

clean:
	@rm -f $(CLASS)
	@rm -rf lib/org

cleaner:
	@rm -f lib/shanghai.jar

