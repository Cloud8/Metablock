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

commit:
	git add --all
	git commit -m "javadoc"
	git merge --no-edit -s ours remotes/doc/gh-pages

push:
	pit push doc master:gh-pages
 	git push doc master:gh-pages

