************************************************************************************
*  Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.5 Messwertersetzung LVE  *
************************************************************************************

Version: ${version}

Übersicht
=========

Aufgabe der SWE Messwertersetzung LVE ist es, alle empfangenen Kurzzeitdatensätze einer
formalen und logischen Plausibilisierung zuzuführen. Die aus diesen Plausibilisierungsstufen
als Implausibel hervorgegangenen Attributwerte werden dann einer Messwertersetzung unterzogen.
Eine genaue Beschreibung der Ersetzungsmethoden erfolgt in [AFo]. Nach dieser Prüfung bzw.
Ersetzung werden die Daten nochmals logisch und formal geprüft und unter dem Aspekt
asp.messWertErsetzung publiziert.


Versionsgeschichte
==================

1.5.0
=====
- Umstellung auf Java 8 und UTF-8

1.4.1
- Kompatibilität zu DuA-2.0 hergestellt

1.4.0
- Umstellung auf Funclib-Bitctrl-Dua

1.3.0
- Umstellung auf Maven-Build

1.2.0

  - Neues Kommandozeilenargument -ignoriereLangzeitdaten. Der Defaultwert
    "false" behält das aktuelle Verhalten bei, bei dem die Langzeitdaten von der
    SW-Einheit verarbeitet werden

1.1.4

  - Senden von reinen Betriebsmeldungen in DUA um die Umsetzung von Objekt-PID/ID nach
    Betriebsmeldungs-ID erweitert.  

1.1.3

  - FIX: Null-Ueberpruefung vorm Zugriff auf den lokalen Puffer.

1.1.2

  - FIX: Sämtliche Konstruktoren DataDescription(atg, asp, sim) ersetzt durch
         DataDescription(atg, asp)

1.1.0

  - Logging veraendert:
 	Ausgabe "Systemobjekt kann nicht identifiziert werden?" entfernt.
 	Dieser Zustand kann/darf im Prinzip nicht auftreten. Sollte dies dennoch geschehen,
 	so wird jetzt eine Laufzeit-Exception geworfen. 
  
  
1.0.0

  - Erste Auslieferung


Bemerkungen
===========

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
	
- Tests:

	Die automatischen Tests, die in Zusammenhang mit der Prüfspezifikation durchgeführt
	werden, sind noch nicht endgültig implementiert.
	
	Für die Tests wird eine Verbindung zum Datenverteiler mit einer Konfiguration mit dem
	Testkonfigurationsbereich "kb.duaTestObjekteSWE4.5" benötigt.
	Die Verbindung wird über die statische Variable CON_DATA der Klasse
	de.bsvrz.dua.mwelve.VerwaltungMessWertErsetzungLVETest hergestellt.
	Die Testdaten befinden sich im Verzeichnis extra.

	/**
	 * Verbindungsdaten
	 */
	public static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083", //$NON-NLS-1$ 
			"-benutzer=Tester", //$NON-NLS-1$
			"-authentifizierung=c:\\passwd"}; //$NON-NLS-1$
	
	Das Wurzelverzeichnis mit den Testdaten (csv-Dateien) muss ebenfalls innerhalb dieser Datei verlinkt sein:
	
	public static final String ROOT = "...\\de.bsvrz.dua.aggrlve\\extra\\"; //$NON-NLS-1$
	

- Logging-Hierarchie (Wann wird welche Art von Logging-Meldung produziert?):

	ERROR:
	- DUAInitialisierungsException --> Beendigung der Applikation
	- Fehler beim An- oder Abmelden von Daten beim Datenverteiler
	- Interne unerwartete Fehler
	
	WARNING:
	- Fehler, die die Funktionalität grundsätzlich nicht
	  beeinträchtigen, aber zum Datenverlust führen können
	- Nicht identifizierbare Konfigurationsbereiche
	- Probleme beim Explorieren von Attributpfaden 
	  (von Plausibilisierungsbeschreibungen)
	- Wenn mehrere Objekte eines Typs vorliegen, von dem
	  nur eine Instanz erwartet wird
	- Wenn Parameter nicht korrekt ausgelesen werden konnten
	  bzw. nicht interpretierbar sind
	- Wenn inkompatible Parameter übergeben wurden
	- Wenn Parameter unvollständig sind
	- Wenn ein Wert bzw. Status nicht gesetzt werden konnte
	
	INFO:
	- Wenn neue Parameter empfangen wurden
	
	CONFIG:
	- Allgemeine Ausgaben, welche die Konfiguration betreffen
	- Benutzte Konfigurationsbereiche der Applikation bzw.
	  einzelner Funktionen innerhalb der Applikation
	- Benutzte Objekte für Parametersteuerung von Applikationen
	  (z.B. die Instanz der Datenflusssteuerung, die verwendet wird)
	- An- und Abmeldungen von Daten beim Datenverteiler
	
	FINE:
	- Wenn Daten empfangen wurden, die nicht weiterverarbeitet 
	  (plausibilisiert) werden können (weil keine Parameter vorliegen)
	- Informationen, die nur zum Debugging interessant sind 
	

Disclaimer
==========

Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.5 Messwertersetzung LVE
Copyright (C) 2007 BitCtrl Systems GmbH 

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51
Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.


Kontakt
=======

BitCtrl Systems GmbH
Weißenfelser Straße 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
