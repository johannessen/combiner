# Algorithmen zur automatisierten Generalisierung durch Zusammenfassung von Linienzügen in OpenStreetMap für konkrete Spezialfälle

       Vorwort
       Inhaltsverzeichnis  
         Verzeichnis der Abbildungen und Tabellen
    1  Einleitung
    2  Analyse der Ausgangslage
    3  Spezifikation der zu untersuchenden Fälle
    4  Synthese
    5  Implementierung
    6  Ergebnisuntersuchung im Anwendungskontext
    7  Schlussfolgerung und Ausblick
    8  Zusammenfassung
       Literaturverzeichnis
       Anhänge



Vorwort
=======

(evtl.)



Inhaltsverzeichnis
==================


Verzeichnis der Abbildungen und Tabellen
----------------------------------------



Einleitung
==========

Neben einer ersten, groben Einführung ins Thema soll die Einleitung dem Leser erklären, warum er diese Arbeit weiterlesen sollte oder welche Kapitel er überspringen kann.

Zielgruppe: Leser vom Fach (Kartographie/GIS) ohne besondere Vertiefung in die in dieser Arbeit behandelten Aspekte (z. B. OpenStreetMap, *automatische* Generalisierung)

- knapper Abriss des Kontextes der Fragestellung in Grundzügen (vgl. Themenblatt)

- Was macht die Fragestellung interessant? (Motivation -- evtl. einzelne Anwendungsfälle umreißen)

- Gesamtüberblick der Arbeit einschließlich ihrer Ergebnisse, roter Faden als Orientierung für den Leser

- Fachsprache definieren (soweit hier verwendet und hier sinnvoll möglich, sonst auf Glossar verweisen)



Analyse der Ausgangslage
========================

Die Analyse soll die ersten beiden Punkte des Themenblatts abdecken: Ausgangslage und Literaturrecherche. Es geht zunächst um die präzise Formulierung des Themas samt Abgrenzung von anderen Fragestellungen im selben Umfeld. Damit einher geht die Untersuchung des status quo samt Untersuchung, was davon evtl. hier weiterverwendet werden kann.

Zielgruppe: wie für Einleitung

**Offener Punkt: Redundanz mit Themenblatt?**

- Einführung in den Stand der Technik, Problemstellung aufzeigen (detaillierter als in Einleitung)

- Auf welche Weise soll diese Arbeit den Stand der Technik weiterbringen? Anwendungsfälle beschreiben.

- Fachsprache definieren (soweit hier verwendet und hier sinnvoll möglich, sonst auf Glossar verweisen)

- Welche abgeschlossenen oder noch laufenden anderen Arbeiten beschäftigen sich mit verwandten Themen?

- Welche Ergebnisse oder Ansätze daraus sind auf die vorliegende Fragestellung anwendbar? Welche Probleme ergeben sich dabei? Vor- und Nachteile diskutieren.

- Fachsprache der anderen Arbeiten definieren (soweit nötig) und Bezug zur in dieser Arbeit verwendeten Terminologie herstellen (ggf. auf Glossar verweisen)



Spezifikation der zu untersuchenden Fälle
=========================================

Die im Themenblatt festgehaltene Aufgabenstellung für die Diplomarbeit macht keine Angaben über das konkrete zu lösende Problem. Aufgrund der vergleichsweise guten Überschaubarkeit ihrer Schwierigkeit sind zwei naheliegende und bereits diskutierte Problemfälle mehrgleisige Eisenbahnstrecken sowie im Straßenraum Richtungsfahrbahnen mit baulicher Trennung.

Zielgruppe: Leser mit Verständnis der Fragestellung samt ihrer Grenzen und mit einem Überblick über einige existierende Lösungsansätze für ähnlich gelagerte Probleme

- Welche Vor- und Nachteile bestehen bei einzelnen konkreten Spezialfällen? (anhand von Beispielen)

- Welcher konkrete Spezialfall wird in dieser Arbeit stellvertretend untersucht? Gibt es einen wesentlichen, die Auswahl entscheidenden Grund?

- Kann das Ergebnis dieser Arbeit auf andere Spezialfälle oder auf die Gesamtheit verallgemeinert werden? (kurze, grobe Abschätzung auf Basis der zuvor benannten Beispiele)



Synthese
========

(Arbeitstitel) (evtl. noch in mehr als ein Kapitel zu teilen)

„Entwicklung praxistauglicher Algorithmen zur Identifikation und zur Generalisierung durch Zusammenfassung kontinuierlicher, parallel verlaufender OSM-Linienzüge und, soweit dazu nötig, zur Verknüpfung der Fragmente (ways) dieser kontinuierlichen Linienzüge“



Implementierung
===============

Weder dieses Kapitel noch die Arbeit als Ganzes sind Teil der Software-Dokumentation oder umgekehrt. Wohl gibt es aber teilweise Überschneidungen. Es soll in diesem Kapitel ein Verständnis für den bei der Entwicklung betriebenen Aufwand und die inneren Struktur geweckt werden mit dem Ziel, sich mit Hilfe der Software-Dokumentation schnell im Quelltext zurechtfinden zu können.

Zielgruppe: Leser, die sowohl mit dem fachlichen Hintergrund als auch den bisherigen Ergebnissen dieser Arbeit eng vertraut sind und zumindest über ein grundlegendes Verständnis von Softwareentwicklung verfügen

- knappe Erläuterung von Entwicklungsumgebung, Versionen, Abhängigkeiten (Frameworks etc.), beteiligten Entscheidungen

- konzeptueller Überblick der Software (Klassenstrukturen, Modulabhängigkeiten, Interaktionswege etc.), soweit sich dies nicht direkt aus den zuvor in der „Synthese“ entwickelten Algorithmen ergibt

- falls signifikante Unterschiede zwischen dem Ergebnis der „Synthese“ und deren Implementierung bestehen: diese genau beschreiben

- ggf. wichtige Software-Designentscheidungen erläutern

- interessante konkrete Schwierigkeiten oder Erfolge bei der Implementierung aufzeigen



Ergebnisuntersuchung im Anwendungskontext
=========================================

Selbstkritik

Zielgruppe: Leser, die sowohl mit dem fachlichen Hintergrund als auch mit der Ausgangsfrage dieser Arbeit eng vertraut sind

- Wie kann in Bezug auf die Fragestellung hinreichend objektiv die Qualität des Ergebnisses (= des entwickelten Algorithmus / der entwickelten Software) geprüft werden? Welche Metriken drängen sich dazu auf?

- Welche Ergebnisse waren zu erwarten ausgehend von Analyse und Spezifikation? Was wäre gut / was wäre schlecht?

- Diskussion von signifikanten Abweichungen des beobachteten vom erwarteten Ergebnis

- Untersuchung der Anwendung des Algorithmus auf unterschiedliche Beispiele (unterschiedliche Regionen der Welt etc.) der spezifizierten Spezialfälle sowie auf andere als diese spezifizierten Fälle (highways statt railways etc.)

- abschließende quantitative Gesamtbeurteilung des Ergebnisses



Schlussfolgerung und Ausblick
=============================

Was hat's gebracht? Wie geht's weiter?

Zielgruppe: wie für Ergebnisuntersuchung

- abschließende qualitative Gesamtbeurteilung der Arbeit auf Basis der Ergebnisuntersuchung in Bezug auf:
  - Praxistauglichkeit
  - Übertragbarkeit auf andere als die spezifizierten Spezialfälle
  - Übertragbarkeit auf andere, ähnlich gelagerte, aber nicht identische Fragestellungen (z. B. Generalisierung durch Verdrängen)
  - evtl. in Relation zu existierenden Lösungsansätzen (-> Analyse)

- Welche Teilprobleme / Use Cases sind nun gelöst, welche noch nicht?

- Welche neuen Probleme / nächste Schritte ergeben sich?



Zusammenfassung
===============

Kurze und prägnante Zusammenfassung der Arbeit (aller Teile und als Ganzes). Präzision ist wünschenswert, Detailreichtum jedoch unnötig, statt dessen ggf. Querverweise in die einzelnen Kapitel.

Zielgruppe: Leser, welche die Arbeit nicht oder nur flüchtig gelesen haben, jedoch neben Fachkenntnissen (Kartographie/GIS) auch über vertieften Einblick in die in dieser Arbeit behandelten Aspekte verfügen (z. B. OpenStreetMap, *automatische* Generalisierung), soweit zum Verständnis nötig



Literaturverzeichnis
====================



Anhänge
=======


Glossar
-------

(sofern für das Definieren der Fachsprache in der Einleitung und der Analyse nicht ausreichend Platz zur Verfügung stand oder dies dort den Lesefluss empfindlich gestört hätte)


Abkürzungsverzeichnis
---------------------

(sofern erforderlich)


Software-Dokumentation
----------------------

(sofern nicht besser direkt bei der Software aufgehoben)

