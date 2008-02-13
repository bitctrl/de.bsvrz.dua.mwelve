/**
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.5 Messwertersetzung LVE
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
 * Wei�enfelser Stra�e 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.mwelve.vew;

import java.util.Collection;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mwelve.mwelve.MessWertErsetzungLVE;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.PublikationsModul;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapterMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.DuaVerkehrsNetz;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;


/**
 * Statischer Speicher fuer die Initialisierungsdaten
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class Initialisierung {
	
/////////////////////////////////////////////////////////////////////////////////////
	
	/***
	 * ANPASSEN:
	 * 
	 * Wurzelverzeichnis der Testdaten
	 * 
	 */
	public static final String WURZEL = ".\\res\\testDaten\\"; //$NON-NLS-1$
	
	/**
	 * Datei aus der die Eingabedaten fuer den Test ausgelesen werden
	 */
	public static final TestDatei INPUT_CSV = new TestDatei("PL_Pruef_LVE_Grenz.csv", ".xls)PL-Pruef LVE Grenz");   //$NON-NLS-1$   //$NON-NLS-2$
	
//	/**
//	 * Datei aus der die erwarteten Ergebnisdaten fuer den Test ausgelesen werden
//	 */
//	public static final TestDatei OUTPUT_CSV = new TestDatei("Messwerters_PL_gepr.csv", ".xls)Messwerters. PL-gepr");   //$NON-NLS-1$   //$NON-NLS-2$

	/**
	 * Datei aus der die erwarteten Ergebnisdaten fuer den Test ausgelesen werden
	 */
	public static final TestDatei OUTPUT_CSV = new TestDatei("Messwerters_LVE.csv", ".xls)Messwerters. Ers. LVE");   //$NON-NLS-1$   //$NON-NLS-2$

	/**
	 * Erfassungsintervall der Kurzzeitdaten in ms
	 */
	public static final long INTERVALL_LAENGE = Konstante.MINUTE_IN_MS;

	/**
	 * ANPASSEN:
	 * 
	 * Verbindungsdaten
	 * 
	 */
	private static final String[] CON_DATA = new String[] {
		"-datenverteiler=localhost:8083",   //$NON-NLS-1$
		"-benutzer=Tester",  //$NON-NLS-1$
		"-authentifizierung=c:\\passwd",  //$NON-NLS-1$
		"-debugLevelStdErrText=WARNING",	//$NON-NLS-1$
		"-debugLevelFileText=WARNING",	//$NON-NLS-1$
		"-KonfigurationsBereichsPid=kb.duaMweTest"}; //$NON-NLS-1$
	
/////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * statische Datenverteiler-Verbindung
	 */
	private static IVerwaltung VERWALTUNG = null;
	
	/**
	 * statische Instanz der Messwertersetzung
	 */
	private static MessWertErsetzungLVE MESSWERTERSETZUNGLVE = null;
	
	
	/**
	 * Erfragt bzw. initialisiert eine statische Instanz eines Moduls
	 * Messwertersetzung LVE
	 * 
	 * @return eine statische Instanz eines Moduls Messwertersetzung LVE
	 * @throws Exception falls die Verbindung nicht
	 * hergestellt werden konnte
	 */
	public static final MessWertErsetzungLVE getMWE()
	throws Exception{
		
		if(VERWALTUNG == null){			
			StandardApplicationRunner.run(VERWALTUNG = new AbstraktVerwaltungsAdapterMitGuete(){

				@Override
				protected void initialisiere()
				throws DUAInitialisierungsException {
					super.initialisiere();
					
					DuaVerkehrsNetz.initialisiere(this.verbindung);
							
					Collection<SystemObject> alleFsObjImKB = DUAUtensilien.getBasisInstanzen(
							this.verbindung.getDataModel().getType(DUAKonstanten.TYP_FAHRSTREIFEN),
							this.verbindung, this.getKonfigurationsBereiche());
					this.objekte = alleFsObjImKB.toArray(new SystemObject[0]);

					String infoStr = Konstante.LEERSTRING;
					for(SystemObject obj:this.objekte){
						infoStr += obj + "\n"; //$NON-NLS-1$
					}
					LOGGER.config("---\nBetrachtete Objekte:\n" + infoStr + "---\n"); //$NON-NLS-1$ //$NON-NLS-2$
					
					MESSWERTERSETZUNGLVE = new MessWertErsetzungLVE();
					PublikationsModul pub = new PublikationsModul(
							new TestMweLveStandardAspekteVersorger(this).getStandardPubInfos(), 
							ModulTyp.MESSWERTERSETZUNG_LVE);

					MESSWERTERSETZUNGLVE.setNaechstenBearbeitungsKnoten(pub);
					MESSWERTERSETZUNGLVE.initialisiere(this);

					pub.initialisiere(this);
					pub.setPublikation(true);					
				}

				public SWETyp getSWETyp() {
					return SWETyp.SWE_MESSWERTERSETZUNG_LVE;
				}

				public void update(ResultData[] results) {
					// wird hier nicht benutzt
				}

				@Override
				public double getStandardGueteFaktor() {
					return 0.9;
				}
				
			}, CON_DATA);
		}
		
		return MESSWERTERSETZUNGLVE;
	}
	
	
	/**
	 * Erfragt bzw. initialisiert eine
	 * Verbindung zum Verwaltungsmodul
	 * 
	 * @return die Datenverteiler-Verbindung
	 * @throws Exception falls die Verbindung nicht
	 * hergestellt werden konnte
	 */
	public static final IVerwaltung getVerwaltung()
	throws Exception {
		
		if(VERWALTUNG == null) {
			getMWE();
		}
		
		return VERWALTUNG;
	}
	
}