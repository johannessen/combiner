Combiner
========


Installation
------------

	bash -c "$(curl -fsSL https://raw.githubusercontent.com/johannessen/combiner/submission/software/install.sh)" combiner

Paste that line at a Terminal prompt. The [bash script](https://github.com/johannessen/combiner/blob/submission/software/install.sh) will clone the Combiner’s Git repository into a new directory named `combiner` and set up the dependencies and a test dataset (in total, a 196 MB download). No files outside of the new directory will be changed.

Dependencies automatically provided as part of the installation:
- [args4j](http://args4j.kohsuke.org/) 2.33
- [GeoTools](http://www.geotools.org/) 18.2
- [TestNG](http://testng.org/) 6.8


System Requirements
-------------------

Recommended (used during development):
- [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 8
- [Apache Ant](https://ant.apache.org/) 1.10
- [GDAL](http://www.gdal.org/) 2
- Bash shell

Minimum (as far as I know – may require some tinkering):
- Java JDK 7
- Apache Ant *(any)* – for building from source
- GDAL *(any)* – for preparing the test dataset
- POSIX-compliant shell environment

Note that the GeoTools framework [doesn’t support](https://medium.com/codefx-weekly/geotools-on-java-9-e8717e347a1f) newer versions than Java 8. This has been a [known issue](https://osgeo-org.atlassian.net/browse/GEOT-5289) in GeoTools for two years (as of October 2017).


Using the combiner
------------------

To operate on the included test dataset:

	cd software
	./run-combiner.sh


Copying
-------

© 2018 Arne Johannessen

This software and its parts are available for reuse under
the terms of the **3-clause BSD** licence. See [LICENSE](
https://github.com/johannessen/combiner/blob/master/software/LICENSE
) for details.

![](https://licensebuttons.net/i/l/by/transparent/00/00/00/76x22.png)
