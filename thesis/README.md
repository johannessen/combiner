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
