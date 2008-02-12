/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.5 Messwertersetzung LVE
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */
package de.bsvrz.dua.mwelve.vew;

import java.sql.Date;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;

/**
 * Liest alle Eingangs- und Ausgangsdaten fuer den Test nach PrSpez und
 * Vergleicht die Ergebnisse
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class Vergleich {
	
	/**
	 * Fahrstreifen1
	 */
	private SystemObject fahrstreifen1 = null;

	/**
	 * Fahrstreifen2
	 */
	private SystemObject fahrstreifen2 = null;
	
	/**
	 * alle hier betrachteten Fahrstreifen
	 */
	private SystemObject[] fahrstreifen = null;
	
	/**
	 * Importer fuer die Eingangsdaten
	 */
	private TestFahrstreifenImporter inputImporter = null;

	/**
	 * Importer fuer die erwarteten Ausgangsdaten
	 */
	private TestFahrstreifenImporter outputImporter = null;
	
	/**
	 * Datenbeschreibung der logisch ueberprueften LVE-Daten
	 */
	private DataDescription ddPlLog = null;
	
	/**
	 * Datenbeschreibung der messwertersetzten LVE-Daten
	 */
	private DataDescription ddPlMwe = null;
		
	/**
	 * letzte erwartete Ergebnisse
	 */
	private ResultData[] erwarteteErgebnisse = null;
	
	/**
	 * Informationen zu den Eingabedateien
	 */
	private String dateiInfo = null;
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param dav Datenverteiler-Verbindung
	 * @param inputCsv Dateiname der CSV-Datei mit den Eingangsdaten
	 * @param outputCsv Dateiname der CSV-Datei mit den erwarteten Ausgangsdaten
	 * @throws Exception wird weitergereicht
	 */
	public Vergleich(ClientDavInterface dav, 
					 final TestDatei inputCsv,
					 final TestDatei outputCsv)
	throws Exception{
		this.fahrstreifen1 = dav.getDataModel().getObject("Fahrstreifen1"); //$NON-NLS-1$
		this.fahrstreifen2 = dav.getDataModel().getObject("Fahrstreifen2"); //$NON-NLS-1$
		this.fahrstreifen = new SystemObject[]{this.fahrstreifen1, this.fahrstreifen2};
		TestFahrstreifenImporter.setT(Initialisierung.INTERVALL_LAENGE);
		this.inputImporter = new TestFahrstreifenImporter(dav, Initialisierung.WURZEL + inputCsv.getDateiName());
		this.outputImporter = new TestFahrstreifenImporter(dav, Initialisierung.WURZEL + outputCsv.getDateiName());
		this.dateiInfo = "Eingabe: \"" + inputCsv.getAlias() + "\", Soll-Werte: \"" + outputCsv.getAlias() + "\"";  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		this.ddPlLog = new DataDescription(
				dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				dav.getDataModel().getAspect(DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH));
		this.ddPlMwe = new DataDescription(
				dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				dav.getDataModel().getAspect(DUAKonstanten.ASP_MESSWERTERSETZUNG));
	}	

	
	/**
	 * Erfragt eine Menge von Eingangdatensaetzen fuer den Test fuer alle
	 * hier behandelten Fahrstreifen (mit dem uebergebenen Zeitstempel)
	 * 
	 * @param zeitStempel der geforderte Zeitstempel der Daten
	 * @return eine Menge von Eingangdatensaetzen fuer den Test fuer alle
	 * hier behandelten Fahrstreifen (mit dem uebergebenen Zeitstempel)
	 */
	public final ResultData[] getFrage(final long zeitStempel){
		ResultData[] frage = null;
		
		if(this.inputImporter.next() && this.outputImporter.next()){
			this.erwarteteErgebnisse = new ResultData[2];
			frage = new ResultData[2];
			
			for(int i = 0; i<2; i++){
				this.erwarteteErgebnisse[i] = new ResultData( 
							this.fahrstreifen[i],
							this.ddPlMwe,
							zeitStempel,
							this.outputImporter.getDatensatz(i));

				frage[i] = new ResultData(
						this.fahrstreifen[i],
						this.ddPlLog,
						zeitStempel,
						this.inputImporter.getDatensatz(i));
			}
		}
		
		return frage;
	}
	
	
	/**
	 * Erfragt das Ergebnis des Vergleichs von tatsaechlichem Ergebnis und
	 * erwartetem Ergebnis
	 * 
	 * @param ergebnisse die tatsaechlich eingetroffenen Ergebnisse
	 * @return das Ergebnis des Vergleichs von tatsaechlichem Ergebnis und
	 * erwartetem Ergebnis
	 */
	public final Ergebnis getErgebnis(ResultData[] ergebnisse){
		return new Ergebnis(this.inputImporter.getZeilenNummer() + 1, this.dateiInfo, this.erwarteteErgebnisse, ergebnisse);
	}
	
	
	/**
	 * Ergebnis der Ueberpruefung von Soll- und Ist-Wert 
	 * 
	 * @author BitCtrl Systems GmbH, Thierfelder
	 *
	 */
	protected static class Ergebnis{
		
		/**
		 * indiziert, dass Soll- und Ist-Wert uebereinstimmen
		 */
		private boolean passt = false;
		
		/**
		 * illustriert die fehlerhafte Uebereinstimmung
		 */
		private String info = null;
		
		
		/**
		 * Standardkonstruktor
		 * 
		 * @param zeilenNummer die Zeilennummer in der CSV-Eingabe-Datei in der der Fehler aufgetacht ist
		 * @param dateiInfo Informationen zu den Test-Dateien
		 * @param erwarteteErgebnisse Menge von erwarteten <code>ResultData</code>-Datensaetzen
		 * @param ergebnisse Menge von tatsaechlich erhaltenen <code>ResultData</code>-Datensaetzen
		 */
		protected Ergebnis(final int zeilenNummer, final String dateiInfo,
				ResultData[] erwarteteErgebnisse, ResultData[] ergebnisse){
			for(int i = 0; i<ergebnisse.length; i++){
				for(int j = 0; j<erwarteteErgebnisse.length; j++){
					if(ergebnisse[i].getObject().equals(erwarteteErgebnisse[j].getObject())){
						ResultData soll = erwarteteErgebnisse[j];
						ResultData ist = ergebnisse[i];
						passt = soll.getDataTime() == ist.getDataTime() &&
						soll.getData().toString().equals(ist.getData().toString());
						this.info = "Zeile: " + zeilenNummer + " (" + dateiInfo + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if(!passt){
							this.info += "\nSoll-Zeit: " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(soll.getDataTime())); //$NON-NLS-1$
							this.info += "\nIst-Zeit:  " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(ist.getDataTime())); //$NON-NLS-1$
							this.info += "\nSoll-Data: " + soll.getData().toString(); //$NON-NLS-1$
							this.info += "\nIst-Data:  " + ist.getData().toString() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
						}
						break;
					}
				}
			}
		}
		
		
		/**
		 * Erfragt, ob Soll- und Ist-Wert uebereinstimmen
		 * 
		 * @return ob Soll- und Ist-Wert uebereinstimmen
		 */
		protected final boolean passtZusammen(){
			return passt;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return this.info;
		}
		
	}
}
