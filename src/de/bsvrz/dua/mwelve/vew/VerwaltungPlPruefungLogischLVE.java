/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.2 Pl-Prüfung logisch LVE
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

import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.dua.plformal.plformal.PlPruefungFormal;
import de.bsvrz.dua.plformal.vew.PPFStandardAspekteVersorger;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapterMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Implementierung des Moduls Verwaltung der SWE Pl-Prüfung logisch LVE.
 * Dieses Modul erfragt die zu überprüfenden Daten aus der Parametrierung
 * und initialisiert damit die Module Pl-Prüfung formal und Pl-Prüfung logisch LVE,
 * die dann die eigentliche Prüfung durchführen.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class VerwaltungPlPruefungLogischLVE
extends AbstraktVerwaltungsAdapterMitGuete{

	/**
	 * Instanz des Moduls PL-Prüfung formal
	 */
	private PlPruefungFormal plPruefungFormal = null;

	/**
	 * Instanz des Moduls PL-Prüfung logisch LVE
	 */
	private PlPruefungLogischLVE plPruefungLogischLVE = null;
	
	
	
	
	/**
	 * {@inheritDoc}
	 */
	public SWETyp getSWETyp() {
		return SWETyp.PL_PRUEFUNG_LOGISCH_LVE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiere()
	throws DUAInitialisierungsException {
		
		String infoStr = Konstante.LEERSTRING;
		Collection<SystemObject> plLogLveObjekte = DUAUtensilien.getBasisInstanzen(
				this.verbindung.getDataModel().getType(DUAKonstanten.TYP_FAHRSTREIFEN),
				this.verbindung, this.getKonfigurationsBereiche());
		this.objekte = plLogLveObjekte.toArray(new SystemObject[0]);
		
		for(SystemObject obj:this.objekte){
			infoStr += obj + "\n"; //$NON-NLS-1$
		}
		LOGGER.config("---\nBetrachtete Objekte:\n" + infoStr + "---\n"); //$NON-NLS-1$ //$NON-NLS-2$
		
		this.plPruefungFormal = new PlPruefungFormal(
				new PPFStandardAspekteVersorger(this).getStandardPubInfos());
		this.plPruefungFormal.setPublikation(true);
		this.plPruefungFormal.initialisiere(this);

		this.plPruefungLogischLVE = new PlPruefungLogischLVE(
				new MweLveStandardAspekteVersorger(this).getStandardPubInfos());
		this.plPruefungLogischLVE.setPublikation(true);
		this.plPruefungLogischLVE.initialisiere(this);
		
		this.plPruefungFormal.setNaechstenBearbeitungsKnoten(this.plPruefungLogischLVE);		
		
		DataDescription anmeldungsBeschreibungKZD = new DataDescription(
				this.verbindung.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				this.verbindung.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				(short)0);
		DataDescription anmeldungsBeschreibungLZD = new DataDescription(
				this.verbindung.getDataModel().getAttributeGroup(DUAKonstanten.ATG_LZD),
				this.verbindung.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG),
				(short)0);
			
		this.verbindung.subscribeReceiver(this, this.objekte, anmeldungsBeschreibungKZD,
					ReceiveOptions.normal(), ReceiverRole.receiver());
		this.verbindung.subscribeReceiver(this, this.objekte, anmeldungsBeschreibungLZD,
					ReceiveOptions.delayed(), ReceiverRole.receiver());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		this.plPruefungFormal.aktualisiereDaten(resultate);
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
					new VerwaltungPlPruefungLogischLVE(), argumente);
	}

	
	/**
	 * {@inheritDoc}.<br>
	 * 
	 * Standard-Gütefaktor für Ersetzungen (90%)<br>
	 * Wenn das Modul Pl-Prüfung logisch LVE einen Messwert ersetzt (eigentlich
	 * nur bei Wertebereichsprüfung) so vermindert sich die Güte des Ausgangswertes
	 * um diesen Faktor (wenn kein anderer Wert über die Kommandozeile übergeben wurde)
	 */
	@Override
	public double getStandardGueteFaktor() {
		return 0.9;
	}
	
}
