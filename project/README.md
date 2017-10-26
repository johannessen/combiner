Combiner
========


Installation
------------

	bash -c "$(curl -fsSL https://raw.githubusercontent.com/johannessen/combiner/master/project/install.sh)" combiner

Paste that line at a Terminal prompt. The [bash script](project/install.sh) will clone the Combiner's Git repository into a new directory named `combiner` and set up the dependencies and a test dataset (in total, a 196 MB download).


System Requirements
-------------------

Recommended (used during development):
- [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 8
- [Apache Ant](https://ant.apache.org/) 1.10
- [GDAL](http://www.gdal.org/) 2
- Bash shell

Minimum (as far as I know – may require some tinkering):
- Java JDK 6
- Apache Ant *(any)* – for building from source
- GDAL *(any)* – for preparing the test dataset
- POSIX-compliant shell environment


Using the combiner
------------------

To operate on the included test dataset:

	cd project
	./run-combiner.sh
