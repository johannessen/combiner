\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{thesis}[2012-12-13 HSKA-DA Arne Johannessen]
\LoadClass[
	version=3.11,  % request that Koma typeset this document just like the Koma version it was written with would have
	a4paper,
	draft,  % mark overflowing line boxes
	bibliography=totoc,  % include bibliography in TOC
%	toc=flat,  % don't indent TOC sections
	headings=normal,  % chapter heading font size (default is 'big')
	numbers=noendperiod,  % 1.2.3 instead of 1.2.3. (even in the presence of appendices)
	abstract,  % add heading 'Abstract'
]{scrreprt}

% enable UTF-8 input support
\RequirePackage{fontspec}
\XeTeXinputnormalization=1

% enable German style, dictionary and macros
\RequirePackage[ngerman]{babel}
\selectlanguage{ngerman}

% set bibliography style (default)
%\RequirePackage{cite}
\AfterBibliographyPreamble{\raggedright}
%\bibliographystyle{myamsa}  % path relative to .tex file, so keep this outta here

% enable German bibliography style
% (NB: some of the statements for 'default' above don't work as intended if babelbib is enabled)
%\RequirePackage[varlanguage]{babelbib}
%\RequirePackage{bibgerm}
%\bibliographystyle{babalpha-fl}

%\RequirePackage{microtype}

% Versuch, lange URLs umzubrechen (funktioniert nicht wirklich)
%\newcommand{\origttfamily}{}% sollte noch nicht definiert sein!
%\let\origttfamily=\ttfamily % alte Definition von \ttfamily sichern
%\renewcommand{\ttfamily}{\origttfamily \hyphenchar\font=`\-}

% use 'normal' (serif) font for all headers and captions
\setkomafont{sectioning}{\normalfont\bfseries}
\setkomafont{descriptionlabel}{\normalfont\bfseries}

% Schusterjungen, Hurenkinder
\clubpenalty = 10000 
\widowpenalty = 10000
\displaywidowpenalty = 10000

% an eyecatcher is used to introduce key terms in lieu of fine-grained sectioning
\newcommand{\eyecatcher}{\textbf}

% a foreign-language or jargon term is italicised to highlight it
\newcommand{\term}{\textit}

% drafting aids
\newcommand{\biggap}{\bigskip\bigskip\bigskip\bigskip\bigskip}
%\RequirePackage{comment}
