\NeedsTeXFormat{LaTeX2e}
% the ../ path component is part of the class name because all TeX files are in sub-directories
\ProvidesClass{../thesis}[2012/12/13 HSKA-DA Arne Johannessen]
\LoadClass[
	version=3.24,  % request that Koma typeset this document just like the Koma version it was written with would have
	a4paper,
	draft,  % mark overflowing line boxes
	listof=totoc,
	bibliography=totoc,  % include bibliography in TOC
	headings=normal,  % chapter heading font size (default is 'big', 'small' is also available)
	numbers=noendperiod,  % 1.2.3 instead of 1.2.3. (even in the presence of appendices)
]{scrreprt}
\RequirePackage{scrhack}

\RequirePackage{subfiles}
\newcommand{\onlyinsubfile}[1]{#1}
%\newcommand{\notinsubfile}[1]{}
\newcommand{\BibTeXenabled}{}  % disable BibTeX in subfiles for speed

% front matter
\RequirePackage{hyperxmp}
\RequirePackage{adjustbox}
\newcommand{\makededicationpage}[1]{\clearpage\thispagestyle{empty}\vspace*{\stretch{1}}{\centering #1 \par}\vspace{\stretch{3}}\clearpage}
\RequirePackage{pdfpages}

% enable UTF-8 input support
\RequireXeTeX
\RequirePackage[no-logos]{xltxtra}
\XeTeXinputnormalization=1

% enable German style, dictionary and macros
\RequirePackage[ngerman]{babel}
\selectlanguage{ngerman}
%\newcommand{\nolig}{\kern0pt}  % "|  % \usepackage[ngerman]{selnolig}  % keine Ligaturen in Wortfugen, <http://texwelt.de/wissen/fragen/52/wie-kann-ich-ligaturen-verhindern#304>

% this is for figures and tables, but has to go before mathtools to avoid warnings
\RequirePackage[raggedrightboxes]{ragged2e}

% math and symbol support
\RequirePackage{units}
\RequirePackage{mathtools}  % includes amsmath
\RequirePackage{amssymb}  % includes amsfonts
\RequirePackage{textcomp}
\RequirePackage{gensymb}

% own symbols
\newcommand{\eqdef}{\overset{\mathrm{def}}{=\joinrel=}}
%\newcommand{\eqdef}{\stackrel{\text{\tiny def}}{=}}
\NewDocumentCommand{\abs}{o}{|\!\;\!|\IfValueT{#1}{#1\abs}}
\newcommand{\set}[1]{\{#1\}}
\newcommand{\TheEmptySet}{\varnothing}
\newcommand{\ThousandsScale}[1]{1\nobreak:\nobreak #1 \,000}

% --------------
% https://en.wikibooks.org/wiki/LaTeX/Bibliography_Management

% set bibliography style (default)
%\AfterBibliographyPreamble{\raggedright}  % doesn't appear to work
\RequirePackage{csquotes,xpatch}
\RequirePackage[bibencoding=auto,style=../bibliography/thesis,natbib=true]{biblatex}
\input{../bibliography/ref}

% enable German bibliography style
% (NB: some of the statements for 'default' above don't work as intended if babelbib is enabled)
%\RequirePackage[varlanguage]{babelbib}
%\RequirePackage{bibgerm}
%\bibliographystyle{babalpha-fl}

% \cfx[p. 34 seq.][cf.]{AB12} ==>> [cf. AB12, p. 34 seq.]
\RequirePackage{xparse}
\RequirePackage{xifthen}
\NewDocumentCommand{\cfx}{ O{} O{} m }{%
\ifthenelse{\equal{\BibTeXenabled}{1}}%
{\cite[#2][#1]{#3}}%
{[\ifthenelse{\isempty{#2}}{}{#2 }#3\ifthenelse{\isempty{#1}}{}{, #1}]}%
}

\newcommand{\?}{\textbf{??}}
\newcommand{\p}{p.\,}
\newcommand{\f}{ seq.}
\newcommand{\ff}{ seqq.}
\newcommand{\cfabbr}{cf.}
\newcommand{\ibidabbr}{ibid.}
\newcommand{\cfibid}{\cfabbr\ \ibidabbr}
\NewDocumentCommand{\cf}{ O{} O{} m }{%
\ifthenelse{\isempty{#1}}{\cfx[][#2\cfabbr]{#3}}{\cfx[#1][#2\cfabbr]{#3}}%
}

\NewDocumentCommand{\citex}{ O{} O{} m }{%
\ifthenelse{\isempty{#1}}{\cfx[][#2]{#3}}{\cfx[#1][#2]{#3}}%
}

\NewDocumentCommand{\noncf}{ O{} O{} m }{}

% --------------

\RequirePackage{color}

% http://www.golatex.de/wiki/%5Cnocite

%\RequirePackage{microtype}
% http://www.khirevich.com/latex/microtype/

% word wrap and hyphenation
\tolerance=1414
\hbadness=1414
\setlength{\emergencystretch}{1.5em}
\hfuzz=.3pt
\hyphenation{mul-ti-step}
% http://www.tex.ac.uk/FAQ-overfull.html

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

% usage: \osmtag{highway} or \osmtag{highway}[*] or \osmtag{highway}[trunk]
\NewDocumentCommand{\osmtag}{ m O{} }{%
\textit{#1}%
\ifthenelse{\isempty{#2}}{}{%
\textit{\kern+.1em=\kern+.1em \ifthenelse{\equal{#2}{*}}{\kern+.1em$*$}{#2}}%
}}

% code identifiers (e.g. Java class names)
%\definecolor{codebackground}{gray}{0.95}
\newcommand{\code}[1]{%
%\colorbox{codebackground}{
\texttt{#1}}%
%}

% shortcut for frequently-used abbreviation (for use where using the abbreviation in the final text might be appropriate)
\newcommand{\osm}{Open\-Street\-Map}

% Hinweis auf fehlende Referenz
%\newcommand{\noref}[1]{[#1\ifthenelse{\isempty{#1}}{}{ }\?] }
\NewDocumentCommand{\noref}{ O{} }{%
[#1\ifthenelse{\isempty{#1}}{}{ }\?] %
}

% Fußnoten
\deffootnote{1em}{1em}{\thefootnotemark\ }

% URLS and links
% should be the last packages imported to avoid issues (says <https://de.wikibooks.org/wiki/LaTeX-W%C3%B6rterbuch:_url> without mentioning specifics)
% see http://ctan.space-pro.be/tex-archive/macros/latex/contrib/hyperref/doc/manual.html#x1-140004
\RequirePackage{hyperref}
\RequirePackage[all]{hypcap}

% compact lists
\RequirePackage{enumitem}

% figures and tables
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
%\RequirePackage{afterpage}
% \capstart makes in-document links point to top of figures instead of the caption below them; \usepackage[all]{hypcap} might also do the trick
\newcommand{\onedoublefigure}[4]{
	\begin{figure}[#1]%
		\hfill%
		\begin{minipage}[t]{.5\linewidth-1.5mm}
			\centering
			\capstart
			#2
		\end{minipage}\hspace{\stretch{2}}%
		\begin{minipage}[t]{.5\linewidth-1.5mm}
			\centering
			\capstart
			#3
		\end{minipage}%
		\hspace*{\fill}%
		#4
	\end{figure}
}
\newcommand{\twofigures}[3]{\onedoublefigure{#1}{#2}{#3}{}}
\newcommand{\onefigure}[2]{
	\begin{figure}[#1]
		\centering
		\capstart
		#2
	\end{figure}
}
\newcommand{\onetable}[2]{
	\begin{table}[#1]
		\centering
		\capstart
		#2
	\end{table}
}
\RequirePackage{longtable}

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
% fall back to placeholder if the image file is missing
\NewDocumentCommand{\IfImageExists}{mmm}{\IfFileExists{#1}{#2}{\IfFileExists{#1.png}{#2}{\IfFileExists{#1.pdf}{#2}{#3}}}}
\NewDocumentCommand{\includegraphicsmaybe}{O{width=\ScaleIfNeeded} m}{\IfImageExists{#2}{\includegraphics[#1]{#2}}{\includegraphics{../image-missing}}}
% standardised markers in graphics
\RequirePackage[abs]{overpic}
% https://tex.stackexchange.com/questions/7032/good-way-to-make-textcircled-numbers
\RequirePackage{tikz}
\RequirePackage{pifont}
\definecolor{figuremark}{rgb}{1,.1,0}
\NewDocumentCommand{\figuremark}{o m}{%
\IfNoValueF{#1}{\hspace{-1.7mm}\tikz[baseline=(current bounding box.center)]{\fill[#1] (0,0) circle (1.7mm);}\hspace{-1.7mm}}%
\hspace{-1.7mm}\hspace{.18mm}\tikz[baseline=(current bounding box.center)]{\node[shape=circle,draw,color=figuremark,minimum size=2.86mm,line width=.18mm,inner sep=0mm] (char) {\color{figuremark}\textsf{\textbf{\fontsize{7}{7}\selectfont #2}}};}
}
\newcommand{\textfiguremark}[1]{\ding{\number\numexpr#1+171\relax}}
%\newcommand{\textfiguremark}[1]{#1}
\newcommand{\figurestandardsize}{\rule{7cm}{0cm}\rule[-.5ex]{0cm}{4cm}}
% usage: \figureframe{.2}
\newcommand{\figureframe}[1]{\linethickness{.2mm}\color{figuremark}\frame{\scalebox{#1}{\figurestandardsize}}}
%% usage: \figureframe{.2} or \figureframe{.2}[.3] (for scaling not in proportion)
%\NewDocumentCommand{\figureframe}{ m O{} }{\linethickness{.2mm}\color{figuremark}\frame{%
%\ifthenelse{\isempty{#2}}{\scalebox{#1}{\figurestandardsize}}{\scalebox{#1}[#2]{\figurestandardsize}}%
%}}

% algorithms
\RequirePackage[plain]{algorithm}
\newenvironment{algorithmhere}[1]
{\begin{algorithm}[H]}%\caption{#1}}
{\end{algorithm}\vspace{-14pt}}
\floatname{algorithm}{Algorithmus}
\renewcommand{\listalgorithmname}{Algorithmenverzeichnis}
\RequirePackage[noend]{algpseudocode}
% replace english words with symbols, mimicking Sar06
%\def\myAlgMethodSymbol{ $\eqdef$}
\def\myAlgMethodSymbol{ $\equiv$}
\algdef{SE}[PROCEDURE]{Procedure}{EndProcedure}%
   [2]{\textproc{#1}\ifthenelse{\equal{#2}{}}{}{(#2)}\myAlgMethodSymbol}%
   {\algorithmicend}%
\algtext*{EndProcedure}%
\algdef{SE}[FUNCTION]{Function}{EndFunction}%
   [2]{\textproc{#1}\ifthenelse{\equal{#2}{}}{}{(#2)}\myAlgMethodSymbol}%
   {\algorithmicend}%
\algtext*{EndFunction}%
\algrenewcommand{\algorithmicforall}{\textbf{für alle}}
\algrenewcommand{\algorithmicwhile}{\textbf{solange}}
\algrenewcommand{\algorithmicdo}{}
\algrenewcommand{\algorithmicif}{\textbf{falls}}
\algrenewcommand{\algorithmicthen}{}
% own pseudo-code commands
%\newcommand*\Let[2]{\State $#1 \gets #2$}
