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

main/main.jar: $(CLASS)
	mkdir -p main
	jar cf $@ -C dlib org

fat: ubcat.jar

ubcat.jar: dlib/manifest main/main.jar
	jar cfm $@ dlib/manifest main lib -C dlib com

dlib/manifest: Makefile
	@echo "Created-By: Nirvana Coorporation" >$@
	@echo "Main-Class: com.simontuffs.onejar.Boot" >>$@
	@echo "One-Jar-Main-Class: org.shanghai.main.Main" >>$@

DIRS:= /srv/archiv/eb /srv/archiv/diss
DIRS:= $(DIRS) /srv/archiv/ep/0002 /srv/archiv/ep/0003
DIRS:= $(DIRS) /srv/archiv/es /srv/archiv/ed
crawl-all:
	rm -f /vol/vol01/data/jena.tdb/*
	@java -cp $(DPATH):$(DPATH) org.shanghai.main.Main -crawl $(DIRS)
	chmod 666 /vol/vol01/data/jena.tdb/*

check:
	@echo CPATH: $(CPATH)
	@echo FILES: $(FILES)

clean:
	@rm -f $(CLASS)
	@rm -rf lib/org

cleaner: clean
	@rm -f lib/shanghai.jar
	@rm -rf dlib/org

