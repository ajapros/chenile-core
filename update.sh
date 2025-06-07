read -p "Comments:" comment
new_version=$(cat chenile-core-version.txt)
git add .
if [[ -z $comment ]]
then
	git commit 
else
	git commit -m "${new_version}: ${comment}"
fi
git push origin main
make tag tag=$new_version
make push-tags

