Wesentliche Punkte der einzelnen Unterkapitel
=============================================

(zur Vorbereitung des Kapitels „Zusammenfassung“)

- __2.1__  (OSM-Einführung: Idee, Datenmodell, Erfassung durch Community als VGI)
- __2.1__  Fragmentierung durch Attribut-Änderungen
- __2.1__  keine Erfassung der Straßenachse
- __2.2__  Verarbeitung und Darstellung in aller Regel voll automatisiert
- __2.3__  kaum kartographische Generalisierung (viele Beispiele)  
  __2.3.4__  u.a. Formvereinfachung: bei parallelen Linienzügen problematisch (kann zum Überschneiden führen)  
  __2.3.6__  u.a. Zusammenfassung: oft kartographisch unbefriedigende Darstellung von Richtungsfahrbahnen
- __2.4__  (Zielabgrenzung: keine Formvereinfachung, Verdrängung o.ä.)
- __2.5__  es existieren bereits zahlreiche Ansätze zur automatisierten Linien-Generalisierung  
  __2.5.2__  u.a. Skelettlinien: (für DA zu) aufwändig, aber funktioniert zumindest halbwegs  
  __2.5.4__  u.a. Strokes: möglichst lange Linienzüge erzeugen, dann zusammenfassen [Thom]  
  __2.5.5__  u.a. Graphenanalyse: evtl. entfallende Parallelen durch Zusammenfassung von Kreuzungen  
   
- __3.1__  (Diskussion von Spezialfällen für die Zusammenfassung)
- __3.2__  (Auswahl eines Spezialfalls: "baulich getrennte Richtungsfahrbahnen im Straßenraum")  
   
- __4.1__  (Diskussion der Tauglichkeit einiger Ansätze aus 2.5 für den gewählten Spezialfall)
- __4.1__  Skelett nicht wegen Kreuzungsproblemen, Strokes nicht wegen fehlenden Attributen [Thom]
- __4.1__  statt möglichst langer Linien (Strokes) auch gleichmäßig kurze Linien denkbar
- __4.2__  Prinzip der "Erkennung paralleler Linienzüge auf der Basis eines geometrischen Vergleichs möglichst kurzer Fragmente"
- __4.3__  (Formale Beschreibung der Algorithmen)
- __4.4__  theoretische Bewertung des Prinzips (Groß-O-Notation)  
   
- __5.1__  (Einführung in die Implementierung: Entwicklungsumgebung)
- __5.2__  Systemarchitektur: Grundsätzliches zu UI, I/O, räumlicher Indizierung, Bibliotheken
- __5.3__  Datenmodell der Bibliothek GeoTools hier schlecht geeignet
- __5.3__  eigenes Datenmodell als Alternative, auf "möglichst kurze Fragmente" zugeschnitten
- __5.4__  Implementierung des Datenmodells und I/O mit Java aufwändiger als erwartet; Fokusverschiebung  
   
- __6.1__  der Combiner und damit das entwickelte Prinzip funktioniert grundsätzlich
- __6.2__  OSM-Datenqualität hinsichtlich Attributen problematisch
- __6.3__  im Ergebnis Topologielücken und Chaos an Straßenkreuzungen
- __6.4__  Performance zufriedenstellend, jedoch Speicherverbrauch offenbar optimierungsfähig
- __6.5__  auch auf andere Spezialfälle anwendbar, aber derzeit mit erheblichen Einschränkungen  
   
- __7.1__  Praxistauglichkeit beschränkt aufgrund der Kreuzungsprobleme (aus Zeitgründen)
- __7.2__  mögliche Weiterentwicklungen  
  __7.2.3__  u.a. Kreuzungserkennung: [MM99] etc., ferner: [CTGV14] (s. 7.3)  
  __7.2.5__  u.a. Softwarequalität: I/O, UTM, Speicherverbrauch, UI, Tests, OOP, API, Doku
- __7.3__  auch neuere Ansätze haben Probleme mit Kreuzungen
- __7.3__  bisher keine offensichtliche Lösung der Zusammenfassung mit allgemeiner Anwendbarkeit
- __7.3__  mit zuverlässiger Kreuzungserkennung jedoch wahrscheinlich leicht lösbar
