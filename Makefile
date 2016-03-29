#
# GH201603
#

#   mv ../Metablock/doc Pages/
#   cd Pages/

def:
	@echo "make init"
	@echo "make commit"
	@echo "make push"

init:
	git init
	git remote add doc https://Cloud8@github.com/Cloud8/Metablock.git
	git fetch --depth=1 doc gh-pages

sync:
	rsync -av --delete ../../autobib/dlib/doc/ doc
	rsync -av --delete ../../autobib/book/09Readme.txt Readme.md

commit:
	git add --all
	git commit -m "javadoc"
	git merge --no-edit -s ours remotes/doc/gh-pages

push:
	git push doc master:gh-pages
	git push Readme.md master:gh-pages

