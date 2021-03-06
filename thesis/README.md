Directory Organisation
----------------------

All TeX files are in subdirectories one level down, which seems to make
working with file paths and the `subfile` module a little easier. Aside
from this readme, the top level only contains the document class
`thesis.cls` and a few general files.

- `appendices`: the appendices' contents
- `bibliography`: bibliography databases, styles and supporting files
- `chapter*`: the chapters' contents
- `main`: the main document `thesis.tex`, topic statement and supporting
  files


Copying
-------

© 2018 Arne Johannessen, Detlef Günther-Diringer, Frederik Ramm

All repository contents in the directory `thesis` and its
subdirectories are available for reuse under the terms of the
[Creative Commons Attribution-ShareAlike 4.0](
https://creativecommons.org/licenses/by-sa/4.0/
) licence (CC-BY-SA 4.0).

[![](https://licensebuttons.net/l/by-sa/4.0/80x15.png)![](https://licensebuttons.net/i/l/by-sa/transparent/00/00/00/76x22.png)](https://creativecommons.org/licenses/by-sa/4.0/)


Producing the PDF
-----------------

I currently use the [MacTeX-2017](https://www.tug.org/mactex/)
distribution for typesetting, which is based on TeX Live 2017 and
provides:

- XeLaTeX 3.14159265-2.6-0.99998

With `tlmgr update --all`:

- KOMA-Script 3.24
- BibLaTeX 3.10
- Biber 2.10

To typeset the document run `typeset.sh` from any directory. The PDF
will be produced alongside some TeX log files as `thesis.pdf` inside
the `main` subdirectory.


Missing Images
--------------

For legal reasons not all images could be included in this repository.
A small number of copyrighted images were used without a licence under
the conditions of [§ 51 UrhG](https://dejure.org/gesetze/UrhG/51.html),
which allows this for scientific use under certain conditions. Since
I am unable to licence use of these images to others, the printed or
PDF form of the final thesis *cannot* allow derivatives (and is as such
licensed only under CC-BY-ND). This repository, however, does *not*
contain these unlicensed images specifically to ensure reusability
under CC-BY-SA.

If images are missing for this reason, they will be replaced with
a placeholder during typesetting.

List of affected images:

- `chapter2/approach-strokes_51`  
  source:
  [The ‘Good Continuation’ Principle of Perceptual Organization applied
  to the Generalization of Road Networks (Thomson and Richardson,
  1999)](https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.202.4737&rep=rep1&type=pdf),
  figure 2

- `chapter2/graph-edges-parallel_51`  
  source:
  [Automating the Detection and Simplification of Junctions
  in Road Networks (Mackaness and Mackechnie,
  1999)](https://www.geos.ed.ac.uk/homes/wam/MackMack1999.pdf),
  figure 3

- `chapter3/administrative-borders_51`  
  source:
  [Topografische Karten – Kartengrafik und Generalisierung
  (Schweizerische Gesellschaft für Kartografie,
  2002)](http://kartografie.ch/wp-content/uploads/demo_no16_en.pdf),
  page 83
