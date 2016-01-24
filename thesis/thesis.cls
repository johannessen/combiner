\NeedsTeXFormat{LaTeX2e}
% for the time being I mostly work on single-chapter drafts, thus I make the ../ path component part of the class name
\ProvidesClass{../thesis}[2012/12/13 HSKA-DA Arne Johannessen]
\LoadClass[
	version=3.19,  % request that Koma typeset this document just like the Koma version it was written with would have
	a4paper,
	draft,  % mark overflowing line boxes
	bibliography=totoc,  % include bibliography in TOC
%	toc=flat,  % don't indent TOC sections
	headings=normal,  % chapter heading font size (default is 'big', 'small' is also available)
	numbers=noendperiod,  % 1.2.3 instead of 1.2.3. (even in the presence of appendices)
	abstract,  % add heading 'Abstract'
%	captions=tableheading,  % (not sure if this works)
]{scrreprt}

% enable UTF-8 input support
\RequirePackage{fontspec}
\XeTeXinputnormalization=1

% enable German style, dictionary and macros
\RequirePackage[ngerman]{babel}
\selectlanguage{ngerman}

% math support
\RequirePackage{units}
%\RequirePackage{mathtools}

% --------------
% https://en.wikibooks.org/wiki/LaTeX/Bibliography_Management

% set bibliography style (default)
\AfterBibliographyPreamble{\raggedright}
\RequirePackage[square]{natbib}
%\bibliographystyle{myamsa}  % path relative to .tex file, so keep this outta here

% enable German bibliography style
% (NB: some of the statements for 'default' above don't work as intended if babelbib is enabled)
%\RequirePackage[varlanguage]{babelbib}
%\RequirePackage{bibgerm}
%\bibliographystyle{babalpha-fl}

% \cfx[p. 34 seq.][cf.]{AB12} ==>> [cf. AB12, p. 34 seq.]
\RequirePackage{xparse}
\RequirePackage{xifthen}
\DeclareDocumentCommand{\cfx}{ O{} O{} m }{%
%\cite[#2][#1]{#3}%
[\ifthenelse{\isempty{#2}}{}{#2 }#3\ifthenelse{\isempty{#1}}{}{, #1}]%
}

\newcommand{\?}{\textbf{??}}
\newcommand{\p}{p.\,}
\newcommand{\f}{ seq.}
\newcommand{\ff}{ seqq.}
\DeclareDocumentCommand{\cf}{ O{} O{} m }{%
\ifthenelse{\isempty{#1}}{\cfx[][#2]{#3}}{\cfx[#1][#2]{#3}}%
}

\DeclareDocumentCommand{\noncf}{ O{} O{} m }{}

% --------------

% http://www.golatex.de/wiki/%5Cnocite

%\RequirePackage{microtype}

% word wrap and hyphenation
%\tolerance=2500  % Paddy
%\hyphenation{Hash-funk-tion Hash-funk-tionen}  % Paddy
\setlength{\emergencystretch}{3em}
% http://www.tex.ac.uk/FAQ-overfull.html
% http://en.wikibooks.org/wiki/LaTeX/Paragraph_Formatting#Hyphenation

% Versuch, lange URLs umzubrechen (funktioniert nicht wirklich)
%\newcommand{\origttfamily}{}% sollte noch nicht definiert sein!
%\let\origttfamily=\ttfamily % alte Definition von \ttfamily sichern
%\renewcommand{\ttfamily}{\origttfamily \hyphenchar\font=`\-}

% increased leading
%\renewcommand{\baselinestretch}{1.4}

% use 'normal' (serif) font for all headers and captions
\setkomafont{sectioning}{\normalfont\bfseries}
\setkomafont{descriptionlabel}{\normalfont\bfseries}

% Schusterjungen, Hurenkinder
\clubpenalty=10000 
\widowpenalty=10000
\displaywidowpenalty=10000

% an eyecatcher is used to introduce key terms in lieu of fine-grained sectioning
\newcommand{\eyecatcher}{\textbf}

% a foreign-language or jargon term is italicised to highlight it
\newcommand{\term}{\textit}

\newcommand{\tag}{\textit}

% drafting aids
\newcommand{\biggap}{\bigskip\bigskip\bigskip\bigskip\bigskip}
%\RequirePackage{comment}

\newcommand{\osm}{OpenStreetMap}

% Hinweis auf fehlende Referenz
%\newcommand{\noref}[1]{[#1\ifthenelse{\isempty{#1}}{}{ }\?] }
\DeclareDocumentCommand{\noref}{ O{} }{%
[#1\ifthenelse{\isempty{#1}}{}{ }\?] %
}

% URLS and links
% should be the last packages imported to avoid issues (says <https://de.wikibooks.org/wiki/LaTeX-W%C3%B6rterbuch:_url> without mentioning specifics)
\RequirePackage{hyperref}
\RequirePackage[all]{hypcap}

% figures and tables
\RequirePackage{chngcntr}
\counterwithout{figure}{chapter}
\counterwithout{table}{chapter}
\RequirePackage[section]{placeins}
%http://texwelt.de/wissen/fragen/2528/wie-funktionieren-gleitobjekte-und-wie-kann-man-ihre-positionierung-beeinflussen
\addtokomafont{caption}{\small}
\addtokomafont{captionlabel}{\bfseries}
\renewcommand*{\captionformat}{.\enskip}
%\addto\captionsngerman{\renewcommand\figurename{Abb.}}

% graphics
\RequirePackage{graphicx}
% scale images that don't fit, but leave others (see l2picfaq 3.3.3)
\makeatletter
\def\ScaleIfNeeded{%
  \ifdim\Gin@nat@width>\linewidth
\linewidth
\else
\Gin@nat@width
\fi
}
\makeatother
