tag=$(cat chenile-*-version.txt)
make delete-local-tag tag=$tag
make delete-origin-tag tag=$tag
