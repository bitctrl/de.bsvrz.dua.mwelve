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

import java.util.Collection;
import java.util.HashSet;

import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.dua.mwelve.mwelve.MessWertErsetzungLVE;
import de.bsvrz.dua.mwelve.plformal.PlFormMweLveStandardAspekteVersorger;
import de.bsvrz.dua.mwelve.pllovlve.PlLogMweLveStandardAspekteVersorger;
import de.bsvrz.dua.plformal.plformal.PlPruefungFormal;
import de.bsvrz.dua.plformal.vew.PPFStandardAspekteVersorger;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapterMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.DuaVerkehrsNetz;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.FahrStreifen;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Implementierung des Moduls Verwaltung der SWE Messwertersetzung LVE.
 * Seine Aufgabe besteht in der Auswertung der Aufrufparameter, der Anmeldung
 * beim Datenverteiler und der entsprechenden Initialisierung der Module PL-Prüfung formal
 * (1 u. 2), PL-Prüfung logisch LVE (1 u. 2), Messwertersetzung LVE sowie Publikation.
 * Weiter ist das Modul Verwaltung für die Anmeldung der zu prüfenden Daten zuständig.
 * Die Verwaltung gibt ein Objekt des Moduls PL-Prüfung formal als Beobachterobjekt an,
 * an das die zu überprüfenden Daten durch den Aktualisierungsmechanismus weitergeleitet
 * werden. Weiterhin stellt die Verwaltung die Verkettung der Module PL-Prüfung formal
 * (1), PL-Prüfung logisch LVE (1), Messwertersetzung LVE, PL-Prüfung formal (2),
 * PL-Prüfung logisch LVE (2) sowie Publikation in dieser Reihenfolge her.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class VerwaltungMessWertErsetzungLVE
extends AbstraktVerwaltungsAdapterMitGuete{

	/**
	 * Instanz des Moduls PL-Prüfung formal (1)
	 */
	private PlPruefungFormal plForm1  = null;
	
	/**
	 * Instanz des Moduls PL-Prüfung formal (2)
	 */
	private PlPruefungFormal plForm2  = null;

	/**
	 * Instanz des Moduls PL-Prüfung logisch LVE (1)
	 */
	private PlPruefungLogischLVE plLog2 = null;
	
	/**
	 * Instanz des Moduls PL-Prüfung logisch LVE (2)
	 */
	private PlPruefungLogischLVE plLog1 = null;
	
	/**
	 * Instanz des Moduls Messwertersetzung LVE
	 */
	private MessWertErsetzungLVE mwe = null;	
	
	
	/**
	 * {@inheritDoc}
	 */
	public SWETyp getSWETyp() {
		return SWETyp.SWE_MESSWERTERSETZUNG_LVE;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiere()
	throws DUAInitialisierungsException {
		
		DuaVerkehrsNetz.initialisiere(this.verbindung);
				
		Collection<SystemObject> alleFsObjImKB = DUAUtensilien.getBasisInstanzen(
				this.verbindung.getDataModel().getType(DUAKonstanten.TYP_FAHRSTREIFEN),
				this.verbindung, this.getKonfigurationsBereiche());
		
		/**
		 * Filtere die Fahrstreifen heraus, die keinen Nachbarfahrstreifen haben.
		 * Diese sind entweder keinem Messquerschnitt zugeordnet oder der Messquerschnitt
		 * an sich ist einspurig
		 */
		Collection<SystemObject> alleFsObjImKBmitNachbar = new HashSet<SystemObject>();
		for(SystemObject fsObjImKB:alleFsObjImKB){
			FahrStreifen fs = FahrStreifen.getInstanz(fsObjImKB);
			if(fs != null){
				if(fs.getNachbarFahrStreifen() != null){
					alleFsObjImKBmitNachbar.add(fsObjImKB);
				}
			}else{
				LOGGER.warning("Fahrstreifen " + fsObjImKB + " konnte nicht identifiziert werden" ); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}				
		this.objekte = alleFsObjImKBmitNachbar.toArray(new SystemObject[0]);

		String infoStr = Konstante.LEERSTRING;
		for(SystemObject obj:this.objekte){
			infoStr += obj + "\n"; //$NON-NLS-1$
		}
		LOGGER.config("---\nBetrachtete Objekte:\n" + infoStr + "---\n"); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		this.plForm1 = new PlPruefungFormal(
				new PlFormMweLveStandardAspekteVersorger(this).getStandardPubInfos());
		this.plForm1.setPublikation(true);
		this.plForm1.initialisiere(this);
		
		this.plLog1 = new PlPruefungLogischLVE(null);
				//new PlLogMweLveStandardAspekteVersorger(this).getStandardPubInfos());
		this.plLog1.setPublikation(true);
		this.plLog1.initialisiere(this);
		
		this.mwe = new MessWertErsetzungLVE();
		this.mwe.initialisiere(this);

		this.plForm2 = new PlPruefungFormal(
				new PlFormMweLveStandardAspekteVersorger(this).getStandardPubInfos());
		this.plForm2.initialisiere(this);
		
		this.plLog2 = new PlPruefungLogischLVE(
				new PlLogMweLveStandardAspekteVersorger(this).getStandardPubInfos());
		this.plLog2.initialisiere(this);
				
		
		DataDescription anmeldungsBeschreibungKZD = new DataDescription(
				this.verbindung.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				this.verbindung.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				(short)0);			
		
		this.verbindung.subscribeReceiver(this, this.objekte, anmeldungsBeschreibungKZD,
					ReceiveOptions.normal(), ReceiverRole.receiver());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		this.plForm1.aktualisiereDaten(resultate);
	}
	
	
	/**
	 * Startet diese Applikation
	 * 
	 * @param args Argumente der Kommandozeile
	 */
	public static void main(String argumente[]){
        Thread.setDefaultUncaughtExceptionHandler(new Thread.
        				UncaughtExceptionHandler(){
            public void uncaughtException(@SuppressWarnings("unused")
			Thread t, Throwable e) {
            	e.printStackTrace();
                LOGGER.error("Applikation wird wegen" +  //$NON-NLS-1$
                		" unerwartetem Fehler beendet", e);  //$NON-NLS-1$
                Runtime.getRuntime().exit(0);
            }
        });
		StandardApplicationRunner.run(
					new VerwaltungMessWertErsetzungLVE(), argumente);
	}

	
	/**
	 * {@inheritDoc}.<br>
	 * 
	 * Standard-Gütefaktor für Ersetzungen (90%)<br>
	 * Wenn das Modul Messwertersetzung LVE einen Messwert ersetzt, so
	 * vermindert sich die Güte des Ausgangswertes um diesen Faktor (wenn
	 * kein anderer Wert über die Kommandozeile übergeben wurde)
	 */
	@Override
	public double getStandardGueteFaktor() {
		return 0.9;
	}
	
}
