Der Algorithmus
===============

(nach frühem "Entwurf" auf Papier)


0. Daten einlesen
-----------------


1. Segmentierung
----------------

[in `AbstractLine.add` als Teil des Einlesens]


2. Regionalisierung
-------------------

- ∀ Segmente : Liste der "nahen" anderen Segmente

→ Spatial Index / Array (sortiert)

[in `Combiner.regionaliseSegments`]


3. Orientierungs-Ausschluss
---------------------------

- ∀ Segmente : Liste der "nahen und geometrisch parallelen" Segmente

[in `LineSegment.closeParallels`]


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

(`splitTargets()`: Liste der "nahen" anderen Segmente)

[in `AbstractLinePart.splitCloseParallels`]


5. Analyse der Fragmente
------------------------

∀ Fragmente:
- Ähnlichkeitsmaß zu allen anderen in Frage kommenden Fragmenten bestimmen (⇐ `closeParallels`), sortieren
- aussortieren über `Analyser`
- ∀ verbleibenden anderen Fragmente: best match(es) L+R finden, falls vorhanden (teilen 2 Segmente einen Node, ist es kein Match! (sonst klappt die L/R-Punktfindung nicht richtig)
- best match(es) für _Segmente_ eintragen in eine Liste (und zwar reziprok) (→ eigentlich unnötig??) (∀ Segmente: ∃ 2 Listen "paralleler" Segmente) L+R (|||)

[in `Combiner.analyseSegments` / `AbstractLinePart.analyse`]


6. Links/Rechts filtern
-----------------------

(Albaufstieg vs. Verteilerfahrbahnen)

_defer_

Basis: Vergleichsfaktor  
z. B. _R_ ignorieren, falls Distanz zu _R_ *m*-fach Distanz zu _L_ ist (*m* ≈ 2,5)

[??]


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

[in `CorrelationGraph.createGraph`]


8. Generalisierung
------------------

"trivialer Fall":
1. zufällig Segment S wählen, Edge E (ES, ET) (Bedingung: Zähler E < 2)
2. Richtung D zufällig wählen (-> S); {F,R}
    1. gegenüberliegendes Segment T (für Start): dasjenige der beiden (<- trivialer Fall) Segment von ET, welches -- so gedreht, dass der Start-Node == ES ist -- in die gleiche Richtung zeigt wie S
3. ∀ D :
    1. wiederhole, solange ∃ E
4. M-Punkt setzen
    5. Segmente A,B von E in Richtung D: nächste Nodes N {X,Y} finden (falls ∄: N:= aktueller Node
    6. ∀ N:
        7. Edge F von N zurück zu E? (X->ET, Y->ES)
           ∃: F ist nächstes E; Zähler E + 1, Zähler F + 1
           ∄: continue 6, sonst: Edge F(X,Y)
              ∃: F ist nächstes E
      E=F <=> ∄: Zähler E + 1, continue 3

[in `GeneralisedLines.traverse`]
