# Algorithmen zur automatisierten Generalisierung durch Zusammenfassung von Linienzügen in OpenStreetMap für konkrete Spezialfälle


##  Vorwort

(evtl.)


##  Inhaltsverzeichnis


##  Verzeichnis der Abbildungen und Tabellen


## 1  Einleitung  (4 %)

Zielgruppe: Leser vom Fach (Kartographie/GIS) ohne besondere Vertiefung in die in dieser Arbeit behandelten Aspekte (z. B. OpenStreetMap, *automatische* Generalisierung)

- erste, grobe Einführung ins Thema
- knapper Abriss des Kontextes der Fragestellung in Grundzügen (vgl. Themenblatt)
- was macht die Fragestellung interessant? (Motivation -- evtl. schon einzelne Anwendungsfälle umreißen)
- Gesamtüberblick der Arbeit einschließlich ihrer Ergebnisse, roter Faden als Orientierung für den Leser
- Eindruck an den Leser: warum soll er diese Arbeit weiterlesen oder welche Kapitel kann er überspringen


## 2  Analyse der Ausgangslage

Zielgruppe: wie für Einleitung


### 2.1  OpenStreetMap: Alles für Alle  (2 %)

- OSM ist Geodatenbank und keine Karte im eigentlichen Sinn
- wie funktioniert OSM?
  + kaum Systematik in Erfassungsgeneralisierung, Ontologie, ...
- welches sind die zu beobachtenden Folgen für Inhalt und Struktur der DB?
  + Way-Fragmentierung
  + OSM hat kein Native-Konzept einer Fläche, Workaround: Linien mit Attribut "Fläche"
  + Änderungen am Schema müssen von der globalen Community akzeptiert und angewandt werden (z. B. [Sch09]), zentrale Entscheidungen Einzelner funktionieren nicht

### 2.2  Kartenherstellung mit OpenStreetMap  (4 %)

- bisher im Wesentlichen nur automatische, (fast) ungeneralisierte Mercator-Tiles fürs Web
- Datenmenge, Maßstäbe, ständige Änderungen und Aktualisierungen etc.
  + manuelle Generalisierung ist nicht zielführend außer für nicht nachzuführende Einzelanfertigungen
- in der Praxis fast nur einfache semantische Modellgeneralisierung unmittelbar im Tile-Renderer, keine kartographische Generalisierung oder Folgekarten bzw. -datenbanken
  + nahe beieinander liegende Punktsignaturen werden willkürlich selektiert, Linearsignaturen überdecken einander
- insbesondere kaum Vereinfachung, Qualitätsumschlag, Zusammenfassung oder Verdrängung (jedoch einige lohnenswerte Ansätze, z. B. [MGW12] oder Haltestellen-Relationen)

### 2.3  Automatisierte Linien-Generalisierung von OpenStreetMap-Daten  (7 %)

- hier: ohne Flächen zu berücksichtigen, obwohl dort evtl. ähnliche Probleme auftreten könnten
- Auswahl, Vergrößern und Betonen funktioniert zufriedenstellend
- Problem bei Verdrängung: der Konflikt (dass z. B. dicht beieinander liegende parallele Linienzüge parallel sind) muss erkannt werden, bevor verdrängt werden kann
- Problem bei Formvereinfachung: Fragmentierung sowie Erhalten der geometrischen Topologie
  + z. B. Fluss außerhalb des (unabhängig gemappten) Flussbetts [Kla11]
  + z. B. ungleichmäßiger Abstand (teils sogar negativ, d. h. Überkreuzen) paralleler Fahrbahnen oder Gleise
- zumindest letzteres Beispiel ist lösbar durch vorhergehendes Zusammenfassen
- Problem bei Zusammenfassung: Linienzüge müssen als zusammengehörig (z. B. parallel) erkannt werden, bevor zusammengefasst werden kann

### 2.4  Zielsetzung der Arbeit  (5 %)

**offener Punkt: Redundanz mit Themenblatt**

- "Algorithmen zur automatisierten Generalisierung durch Zusammenfassung von Linienzügen in OpenStreetMap"
- Beispiel: Eisenbahnkarte mit untauglicher Darstellung von ein- und mehrgleisigen Strecken
- Beispiel: unregelmäßige Lücken zwischen Autobahn-Richtungsfahrbahnen
- Beispiele: untaugliche Formvereinfachungen (siehe vorgenannte Probleme)
- ...
- Visionen:
  + kartographische Generalisierung => besseres Kartenbild
  + Verringerung der Datenmenge
  + MRDB
  + Vereinfachung der Weiternutzung
  + ...
- *nicht* Teil der Arbeit sind insbesondere:
  + die Formvereinfachung selbst
  + eine wie auch immer geartete Verdrängung (obgleich eine solche durch diese Arbeit vielleicht erleichtert werden könnte)


### 2.5  Diskussion existierender Ansätze zur automatisierten Linien-Generalisierung  (13 %)

- Puffer [OHS+10]
- OS MasterMap [CM05]
- Strokes [Tho06b], [EM00]
- graphbasiert [JC04], [MM99], [HAS05], [TR95], [Kne09]
- Skeleton [LM96], [Mig12], [All11]
- Relational Constraints [TBD+12]
- Conflict Detection [KP98], [Tho06a]
- ...

(jeweils einschließlich Anwendbarkeit auf die vorliegende Fragestellung, ggf. Vor- und Nachteilen, ggf. Bezug der darin verwendeten Fachsprache zur in dieser Arbeit verwendeten Terminologie)


## 3  Spezifikation der zu untersuchenden Fälle

Zielgruppe: Leser mit Verständnis der Fragestellung samt ihrer Grenzen und mit einem Überblick über einige existierende Lösungsansätze für ähnlich gelagerte Probleme


### 3.1  Vergleich verschiedener Problemfälle der automatisierten Linien-Generalisierung  (5 %)

- mehrgleisige Eisenbahnstrecken
- Richtungsfahrbahnen im Straßenraum mit baulicher Trennung
- parallele Fahrbahnen/Wege für unterschiedliche Verkehrsmittel/-arten
  + z. B. zweibahnige Straße außerorts mit parallelem Zweirichtungsradweg
  + z. B. Nebenfahrbahnen (Frontage Roads)
  + z. B. straßenbündiger Bahnkörper (Straße mit Gleisen für Straßenbahn oder Street-Running)
  + z. B. komplexe innerstädtische Straße mit mehreren parallelen getrennten Radwegen, Gehwegen, Richtungsfahrbahnen, Nebenfahrbahnen, besonderem Stadtbahn-Gleiskörper (mehrgleisig), Adressinterpolationen, PLZ-Grenze und unterirdischer Fernwärmeleitung
  + z. B. Rampen für Anschlussstellen vom Typ SPUI (der "Fall Kriegsstraße") oder Diamant

(Vergleich unter dem Gesichtspunkt der Eignung als "Spezialfall" für diese DA, vgl. Themenblatt)


### 3.2  Auswahl der in dieser Arbeit zu behandelnden Spezialfälle  (2 %)

- Welcher konkrete Spezialfall wird in dieser Arbeit stellvertretend untersucht? Gibt es einen wesentlichen, die Auswahl entscheidenden Grund?
- Kann das Ergebnis dieser Arbeit auf andere Spezialfälle oder auf die Gesamtheit verallgemeinert werden? (kurze, grobe Abschätzung auf Basis der zuvor benannten Beispiele)


## 4  Algorithmen zur Generalisierung

Zielgruppe: wie für Spezifikation

### 4.1  Vorgehensweise  (2 %)

### 4.2  Beschreibung der Algorithmen  (17 %)

(Unterteilung nach Bedarf, hier vorerst nur beispielhaft)

#### 4.2.1  Identifikation parallel verlaufender Linien-Fragmente

#### 4.2.2  Generalisierung durch Zusammenfassung

#### 4.2.3  Verknüpfung von Linienfragmenten zu einem einzigen kontinuierlichen Linienzug


## 5  Implementierung

Zielgruppe: Leser, die sowohl mit dem fachlichen Hintergrund als auch den bisherigen Ergebnissen dieser Arbeit eng vertraut sind und zumindest über ein grundlegendes Verständnis von Softwareentwicklung verfügen

### 5.1  Entwicklungsumgebung  (1 %)

- Systemvoraussetzungen (z. B. Java, PostGIS, ...)
- Versionen verwendeter Komponenten, Kompatibilität
- Abhängigkeiten von Frameworks etc. (z. B. Geotools)

### 5.2  Systemkonzept  (7 %)

- konzeptueller Überblick der entwickelten Software in dem Umfang, der für den Leser dieser Arbeit zum Verständnis notwendig ist
  + z. B. Modulabhängigkeiten
  + z. B. Klassenstrukturen
  + z. B. Interaktionswege
- Bezug zu Kapitel 4 herstellen

### 5.3  Schwierigkeiten bei der Umsetzung  (5 %)

- Erläuterung wichtiger Designentscheidungen
- Unterschiede zu den entworfenen Algorithmen
- interessante konkrete Schwierigkeiten oder Erfolge bei der Implementierung aufzeigen


## 6  Ergebnisuntersuchung im Anwendungskontext

Zielgruppe: Leser, die sowohl mit dem fachlichen Hintergrund als auch mit der Ausgangsfrage dieser Arbeit eng vertraut sind

### 6.1  Beurteilungskriterien  (2 %)

- allgemeine Kriterien zur qualitativen Kritik des Ergebnisses (= des entwickelten Algorithmus / der entwickelten Software) beschreiben
- falls möglich, quantitative Metriken für die Beurteilung definieren
- erwartete Ergebnisse ausgehend von Analyse und Spezifikation rekapitulieren

### 6.2  Ergebnisbeurteilung  (13 %)

- Beurteilung anhand der zuvor beschriebenen Kriterien
- Diskussion von signifikanten Abweichungen des beobachteten vom erwarteten Ergebnis
- Untersuchung der Anwendung des Algorithmus auf unterschiedliche Beispiele (unterschiedliche Regionen der Welt etc.) der spezifizierten Spezialfälle sowie auf andere als diese spezifizierten Fälle (highways statt railways etc.)
- abschließende quantitative Gesamtbeurteilung des Ergebnisses


## 7  Schlussfolgerung und Ausblick

Zielgruppe: wie für Ergebnisuntersuchung

### 7.1  Praktische Anwendbarkeit  (5 %)

- abschließende qualitative Gesamtbeurteilung der Arbeit auf Basis der Ergebnisuntersuchung in Bezug auf:
  + Praxistauglichkeit
  + Übertragbarkeit auf andere als die spezifizierten Spezialfälle
  + Übertragbarkeit auf andere, ähnlich gelagerte, aber nicht identische Fragestellungen (z. B. Generalisierung durch Verdrängen)
  + evtl. in Relation zu existierenden Lösungsansätzen (-> Analyse)

### 7.2  Ungelöste Problemfälle  (2 %)

- vorliegende Algorithmen und vorliegende Software

### 7.3  Mögliche Ansätze zur Weiterentwicklung  (2 %)

- nächste Schritte
- neue Probleme


## 8  Zusammenfassung  (2 %)

Zielgruppe: Leser, welche die Arbeit nicht oder nur flüchtig gelesen haben, jedoch neben Fachkenntnissen (Kartographie/GIS) auch über vertieften Einblick in die in dieser Arbeit behandelten Aspekte verfügen (z. B. OpenStreetMap, *automatische* Generalisierung), soweit zum Verständnis nötig

- kurze und prägnante Zusammenfassung der Arbeit (aller Teile und als Ganzes)
  + Präzision ist wünschenswert, Detailreichtum jedoch unnötig
  + ggf. Querverweise in die einzelnen Kapitel


##  Literaturverzeichnis


## Anhänge

### A  Glossar

- Fachsprache (sofern für das Definieren in der Einleitung und der Analyse nicht ausreichend Platz zur Verfügung stand oder dies dort den Lesefluss empfindlich gestört hätte)

### B  Abkürzungsverzeichnis

(sofern erforderlich)

### C  Software-Dokumentation

(sofern nicht besser direkt bei der Software aufgehoben)

