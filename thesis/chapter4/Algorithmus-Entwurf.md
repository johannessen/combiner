Der Algorithmus
===============

(nach frühem "Entwurf" auf Papier)


0. Daten einlesen
-----------------


1. Segmentierung
----------------


2. Regionalisierung
-------------------

_defer_

- ∀ Segmente : Liste der "nahen" anderen Segmente

→ Spatial Index / Array (sortiert)


3. Orientierungs-Ausschluss
---------------------------

- ∀ Segmente : Liste der "nahen und geometrisch parallelen" Segmente


4. Splitten ("re-entrant") in Fragmente
---------------------------------------

Liste B aller `LinePart` als Basis (initial: alle `LineSegment`)
- _∀ B : P₁, P₂_
    - _∀ P_ als p
        - _∀ `splitTargets()`_ : (_target noch nicht gesplittet_ -> next;)
            - Fußpunkt PF für p auf target suchen
            - falls nicht außerhalb target (✐) / falls ≠ target.P₁, target.P₂: *Split* target bei PF
                - neue Fragmente f₁, f₂ mit P₁–PF und PF–P₂ erzeugen
                - beide f₁, f₂ an Controller melden (für Basis-Iterator: hinten anhängen an Liste zu splittender Segmente)
                - Basis und target als gesplittet / "fertig" markieren
                - … mehr an Controller melden…?


5. Analyse der Fragmente
------------------------

∀ Fragmente:
- Ähnlichkeitsmaß zu allen anderen in Frage kommenden Fragmenten bestimmen (⇐ `closeParallels`), sortieren
- aussortieren über `Analyser`
- ∀ verbleibenden anderen Fragmente: best match(es) L+R finden, falls vorhanden (teilen 2 Segmente einen Node, ist es kein Match! (sonst klappt die L/R-Punktfindung nicht richtig)
- best match(es) für _Segmente_ eintragen in eine Liste (und zwar reziprok) (→ eigentlich unnötig??) (∀ Segmente: ∃ 2 Listen "paralleler" Segmente) L+R (|||)


6. Links/Rechts filtern
-----------------------

(Albaufstieg vs. Verteilerfahrbahnen)

_defer_

Basis: Vergleichsfaktor  
z. B. _R_ ignorieren, falls Distanz zu _R_ *m*-fach Distanz zu _L_ ist (*m* ≈ 2,5)


7. Punktzuordnungen erzeugen (Vorstufe Generalisierung)
-------------------------------------------------------

- ∀ Segmente "S" : S hat Parallele und S noch nicht "fertig 7"
    - ∀ Punkte (Start/End "T₁"), ∀ Seiten von S (L/R) "A"
        - ∀ Parallelen von S "P" auf Seite A
            - nahegelehensten Punkt finden "T₂"
            - falls nichts gefunden (= keine Parallele auf dieser Seite), näcshtes A
            - neue Punktzuordung: T₁ ↔︎ T₂
    - S markieren als "fertig 7"

_defer:_
- ∀ Parallelen von S: Rekursion
- alle Segmente/Zuordnungen für je ein ursprüngliches Segment als Teil eines "Blocks" markieren (könnte später evtl. Auffinden eines Anfangs zur Generalisierung erleichtern)


8. Generalisierung
------------------

für "tiefen" Baum, mit > 2 Parallelen:
- geeigneten Start-Punkt finden/wählen ⇒ current point CP
- in Segment-Richtung am weitesten "hinten" liegenden R-Punkt finden (Vektor), CP darauf setzen
- Zuordnungsbaum (→ L) durchlaufen ("hinten"-first, -> Vektor) (von einem "geschickten" Baumdurchlauf hängt ab, ob dieser funktioniert und wie gut der Output ist); … ⇒ Mittelpunkt ermittelt in _minimaler Anzahl_, aber mind. 1 pro L/R-Punkt
- CP auf Endpunkt der Segmente setzen, weiter wie oben, (kleine OPtimierung: denselben Baum nicht mehrfach durchlaufen)
- Abbruch: wenn sich die Breite des Zuordnungsbaums geändert hat (auf ∅ → totaler Abbruch, sonst letzten Punkt als Verknüpfnung merken)
- Abbruch: wenn sich die Tags der Segmente "inakzeptabel" ändern
Währenddessen: innerhalb des Baums (nach dessen Abarbeitung) alle Segmente als "archiviert" markieren; beim Baumwechsel die zwischenliegenden Segmente als "archiviert" markieren  
=> nach Fertigstellung nur neue Gen-Linien und _nicht_ archivierte Segmente erhalten  
-> Problem: "verwaiste" Segmente: ✐
- einzelne Segmente löschen
- Segmente, die Teil eines Ways sind, in dem Segmente _sowohl davor als auch dahinter "archiviert" sind" löschen (?)
- … (noch nicht 100% gelöst)
_defer_ (außerdem Node-Replacement-Liste für ✐)
