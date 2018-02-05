Bildqualität
============


- ursprüngliche Versionen mit 72 dpi völlig untauglich
- auch 96 dpi erheblich gröber als erwartet

   
- PDF-Export von <https://osm.org> funktioniert im Prinzip recht gut, hat aber gerade für kleine Maßstäbe sehr viele Details, so dass die Dateigröße in die Höhe schießt
- ⇒ PDF-Export in Photoshop rastern, um eine PNG-Datei mit hoher Auflösung zu erhalten
- (kleiner) Nachteil: die Schrift ist in Anbetracht der hohen Auflösung größer als nötig und eigentlich erwünscht

   
- Auflösung im Druck: ca. 60 l/cm
- Qualitätsfaktor: mindestens 2
- benötigte PNG-Auflösung (für Karten, mit Reserve): ca. 160 px/cm ≈ 400 ppi
- benötigte JPG-Auflösung (für Fotos, ohne Reserve): ca. 120 px/cm ≈ 300 ppi



Schritte für Kartenausschnitte
------------------------------

1. mit Photoshop passenden 7×4–Bildausschnitt auf <https://osm.org> finden
2. über `coord xy2latlon+en.gcx` Eckkoordinaten ermitteln
   (Pixel-Ursprung links oben)
3. PDF-Anfrage auf render.openstreetmap.org
   (passender Maßstab: try&error, vergleiche Screenshot aus 1.)
4. PDF in Photoshop rendern (height 640 px, 160 px/cm, no anti-aliasing)
5. (optional) Crop Canvas Size derart, dass es exakt zum Screenshot passt
6. Crop Canvas Size derart, dass die Breite 1120 px ist (für 7×4 cm²)
7. Indexed Color
8. `ImageOptim.app` (lossless)



Maßstab für Kartenausschnitte
-----------------------------

Der Export als PDF auf <https://osm.org> erfolgt mit dem Maßstab der
„Web Mercator“-Kartennetzabbildung mit φ₀ = 0°. Der als URL-Parameter
enthaltene `scale` ist also eine Äquatormaßstabszahl.

Ungefährer Skalierungsfaktor (für die Kugel), um die geladene Karte
in den eigentlich mit `scale` angefragten Maßstab zu bekommen, sollte
demnach cos(φ) sein. Allerdings scheinen die Karten so immer noch ein
Drittel zu groß zu sein. Anscheinend rendert Mapnik für 96 ppi, während
im weiteren Verlauf der PDF-Herstellung mit dem PostScript-Default von
72 ppi gearbeitet wird. Dies ist vermutlich ein Bug.

Derzeitige Formel zur Skalierung der geladenen Karte auf den angefragten Maßstab:
`scale`×cos(`latitude`×π/180)×72/96
