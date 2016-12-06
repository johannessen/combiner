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

\RequirePackage{subfiles}
\newcommand{\onlyinsubfile}[1]{#1}
%\newcommand{\notinsubfile}[1]{}

% enable UTF-8 input support
\RequirePackage{fontspec}
\XeTeXinputnormalization=1

% enable German style, dictionary and macros
\RequirePackage[ngerman]{babel}
\selectlanguage{ngerman}

% math and symbol support
\RequirePackage{units}
\RequirePackage{mathtools}  % includes amsmath
\RequirePackage{amssymb}  % includes amsfonts

% --------------
% https://en.wikibooks.org/wiki/LaTeX/Bibliography_Management

% set bibliography style (default)
%\AfterBibliographyPreamble{\raggedright}  % doesn't appear to work
\RequirePackage[square,numbers]{natbib}  % no clue what "numbers" does in there, but adding it removes spurious warnings
\bibliographystyle{../thesis}  % path relative to .tex files

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

% switch the comment symbol above to control BibTeX!

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

\newcommand{\osmtag}{\textit}

% drafting aids
\newcommand{\biggap}{\bigskip\bigskip\bigskip\bigskip\bigskip}
%\RequirePackage{comment}

% shortcut for frequently-used abbreviation (for use where using the abbreviation in the final text might be appropriate)
\newcommand{\osm}{Open\-Street\-Map}

% Hinweis auf fehlende Referenz
%\newcommand{\noref}[1]{[#1\ifthenelse{\isempty{#1}}{}{ }\?] }
\DeclareDocumentCommand{\noref}{ O{} }{%
[#1\ifthenelse{\isempty{#1}}{}{ }\?] %
}

% URLS and links
% should be the last packages imported to avoid issues (says <https://de.wikibooks.org/wiki/LaTeX-W%C3%B6rterbuch:_url> without mentioning specifics)
% see http://ctan.space-pro.be/tex-archive/macros/latex/contrib/hyperref/doc/manual.html#x1-140004
\RequirePackage[colorlinks=false]{hyperref}
\RequirePackage[all]{hypcap}
% break URLs at arbitrary positions to avoid ugly word spacing in justified text:
\expandafter\def\expandafter\UrlBreaks\expandafter{\UrlBreaks%  save the current one
  \do\a\do\b\do\c\do\d\do\e\do\f\do\g\do\h\do\i\do\j%
  \do\k\do\l\do\m\do\n\do\o\do\p\do\q\do\r\do\s\do\t%
  \do\u\do\v\do\w\do\x\do\y\do\z\do\A\do\B\do\C\do\D%
  \do\E\do\F\do\G\do\H\do\I\do\J\do\K\do\L\do\M\do\N%
  \do\O\do\P\do\Q\do\R\do\S\do\T\do\U\do\V\do\W\do\X%
  \do\Y\do\Z\do\0\do\1\do\2\do\3\do\4\do\5\do\6\do\7\do\8\do\9}

% figures and tables
\RequirePackage[raggedrightboxes]{ragged2e}
%\usepackage{caption}
%\captionsetup{format=plain,font=small,labelfont=bf,labelsep=colon,justification=RaggedRight}
\RequirePackage{chngcntr}
\counterwithout{figure}{chapter}
\counterwithout{table}{chapter}
\RequirePackage[section]{placeins}
%http://texwelt.de/wissen/fragen/2528/wie-funktionieren-gleitobjekte-und-wie-kann-man-ihre-positionierung-beeinflussen
\addtokomafont{caption}{\small}
\addtokomafont{captionlabel}{\bfseries}
%\renewcommand*{\captionformat}{.\enskip}  % keep the colon! -TA
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

% algorithms
\RequirePackage[plain]{algorithm}
\floatname{algorithm}{Algorithmus}
\renewcommand{\listalgorithmname}{Algorithmenverzeichnis}
\RequirePackage[noend]{algpseudocode}
% replace english words with symbols, mimicking Sar06
%\def\myAlgMethodSymbol{ $\stackrel{\text{\tiny def}}{=}$}
\def\myAlgMethodSymbol{ $\equiv$}
\algdef{SE}[PROCEDURE]{Procedure}{EndProcedure}%
   [2]{\textproc{#1}\ifthenelse{\equal{#2}{}}{}{(#2)}\myAlgMethodSymbol}%
   {\algorithmicend}%
\algtext*{EndProcedure}%
\algdef{SE}[FUNCTION]{Function}{EndFunction}%
   [2]{\textproc{#1}\ifthenelse{\equal{#2}{}}{}{(#2)}\myAlgMethodSymbol}%
   {\algorithmicend}%
\algtext*{EndFunction}%
%\algrenewcommand{\algorithmicforall}{\textbf{$\forall$}}
\algrenewcommand{\algorithmicforall}{\textbf{für alle}}
\algrenewcommand{\algorithmicdo}{}
%\algnewcommand\algorithmicdo{\textbf{do}}
% own pseudo-code commands
%\newcommand*\Let[2]{\State $#1 \gets #2$}
