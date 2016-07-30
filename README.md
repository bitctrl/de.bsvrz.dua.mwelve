[![Build Status](https://travis-ci.org/bitctrl/de.bsvrz.dua.mwelve.svg?branch=develop)](https://travis-ci.org/bitctrl/de.bsvrz.dua.mwelve)
[![Build Status](https://api.bintray.com/packages/bitctrl/maven/de.bsvrz.dua.mwelve/images/download.svg)](https://bintray.com/bitctrl/maven/de.bsvrz.dua.mwelve)

# Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.5 Messwertersetzung LVE

Version: ${version}

## Übersicht

Aufgabe der SWE Messwertersetzung LVE ist es, alle empfangenen Kurzzeitdatensätze einer
formalen und logischen Plausibilisierung zuzuführen. Die aus diesen Plausibilisierungsstufen
als Implausibel hervorgegangenen Attributwerte werden dann einer Messwertersetzung unterzogen.
Eine genaue Beschreibung der Ersetzungsmethoden erfolgt in [AFo]. Nach dieser Prüfung bzw.
Ersetzung werden die Daten nochmals logisch und formal geprüft und unter dem Aspekt
asp.messWertErsetzung publiziert.


## Versionsgeschichte

### 2.0.2

Release-Datum: 28.07.2016

de.bsvrz.dua.mwelve.mwelve.pruefung.MweAttributWert:

- unnötige equals-Funktion entfernt, da hashCode gefehlt hat und Daten der Klasse von
  außen geändert werden können

de.bsvrz.dua.mwelve.tests.DuAMweLveTestBase
- der Member "_messWertErsetzungLVE" sollte nicht statisch sein, der er bei jedem Test neu initialisiert wird

 - Obsolete SVN-Tags aus Kommentaren entfernt
- Obsolete inheritDoc-Kommentare entfernt

### 2.0.1

Release-Datum: 22.07.2016

- Umpacketierung gemäß NERZ-Konvention

### 2.0.0

Release-Datum: 31.05.2016

#### Neue Abhängigkeiten

Die SWE benötigt nun das Distributionspaket de.bsvrz.sys.funclib.bitctrl.dua in
Mindestversion 1.5.0 und de.bsvrz.sys.funclib.bitctrl in Mindestversion 1.4.0.

#### Änderungen

Folgende Änderungen gegenüber vorhergehenden Versionen wurden durchgeführt:

- Die SWE setzt keine Betriebsmeldungen mehr ab.
– Die SWE Pl Logisch LVE wird allerdings implizit mitgestartet und setzt noch
  Betriebsmeldungen ab.
- Der Ersetzungsalgorithmus wurde vollständig überarbeitet:
– Alle 6 Attribute qKfz, qLkw, qPkw, vKfz, vLkw, vPkw werden jetzt nur noch
  gemeinsam ersetzt.
– Nur noch der Parameter MaxErsetzungsDauer ist relevant und er bezieht sich
  auf beide Verfahren.
– Damit Werte vom Ersatzfahrstreifen übernommen werden, müssen die Erfassungsintervalle
  beider Fahrstreifen übereinstimmen.
– Es werden keine bereits interpolierten Werte vom Ersatzfahrstreifen zur Ersetzung
  verwendet.
– Die Güte wird jetzt um 5% reduziert, wenn ein Attribut vom Ersatzfahrstreifen
  übernommen wird.
– Die Güte wird jetzt um 10% reduziert, wenn ein Attribut fortgeschrieben
  wird.

#### Fehlerkorrekturen

Folgende Fehler gegenüber vorhergehenden Versionen wurden korrigiert:

- Mögliche NullPointerException beim Kopieren von Mess- und Gütewerten korrigiert.

### 1.5.0

- Umstellung auf Java 8 und UTF-8

### 1.4.1

- Kompatibilität zu DuA-2.0 hergestellt

### 1.4.0

- Umstellung auf Funclib-Bitctrl-Dua

### 1.3.0

- Umstellung auf Maven-Build

### 1.2.0

- Neues Kommandozeilenargument -ignoriereLangzeitdaten. Der Defaultwert
  "false" behält das aktuelle Verhalten bei, bei dem die Langzeitdaten von der
  SW-Einheit verarbeitet werden

### 1.1.4

- Senden von reinen Betriebsmeldungen in DUA um die Umsetzung von Objekt-PID/ID nach
  Betriebsmeldungs-ID erweitert.  

### 1.1.3

- FIX: Null-Ueberpruefung vorm Zugriff auf den lokalen Puffer.

### 1.1.2

- FIX: Sämtliche Konstruktoren DataDescription(atg, asp, sim) ersetzt durch
       DataDescription(atg, asp)

### 1.1.0

- Logging veraendert:
  Ausgabe "Systemobjekt kann nicht identifiziert werden?" entfernt.
  Dieser Zustand kann/darf im Prinzip nicht auftreten. Sollte dies dennoch geschehen,
  so wird jetzt eine Laufzeit-Exception geworfen. 
  
### 1.0.0

- Erste Auslieferung

## Bemerkungen

Diese SWE ist eine eigenständige Datenverteiler-Applikation, welche über die Klasse
de.bsvrz.dua.mwelve.VerwaltungMessWertErsetzungLVE mit folgenden Parametern gestartet werden kann
(zusaetzlich zu den normalen Parametern jeder Datenverteiler-Applikation):
	-KonfigurationsBereichsPid=pid(,pid)

Der Datenfluss durch diese Applikation ist wie folgt:
externe Erfassung 
	--> formale Prüfung (Publikation asp.plPrüfungFormal) 
		-->	logische Prüfung (Publikation unter asp.plPrüfungLogisch)
			--> Messwertersetzung 
				--> formale Prüfung 
					-->	logische Prüfung (Publikation unter asp.messwertErsetzung)
	
## Kontakt

BitCtrl Systems GmbH
Weißenfelser Straße 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
