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
package de.bsvrz.dua.mwelve.mwelve;

import stauma.dav.clientside.ResultData;
import de.bsvrz.dua.mwelve.mwelve.pruefung.LVEPruefungUndMWE;
import de.bsvrz.dua.mwelve.mwelve.pruefung.MweParameter;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltungMitGuete;

/**
 * Das Modul Messwertersetzung LVE meldet sich auf alle benötigten Parameter
 * der vorgesehenen Objekte an. Es führt für die über die Methode <code>aktualisiereDaten(..)</code>
 * übergebenen Kurzzeitdaten ggf. eine attributbezogene Messwertersetzung durch. Bei nicht
 * durchführbarer Messwertersetzung wird ggf. eine Betriebsmeldung generiert. Nach der Ersetzung
 * werden die Daten je nach Parametrierung unter dem Aspekt asp.messWertErsetzung publiziert
 * und an den nächsten Bearbeitungsknoten weitergereicht
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class MessWertErsetzungLVE 
extends AbstraktBearbeitungsKnotenAdapter{
	
	/**
	 * Die Guete dieses Moduls innerhalb dieser SWE
	 */
	public static double GUETE = 0.9;

	/**
	 * Dieses Submodul führt die eigentliche Prüfung durch
	 */
	private LVEPruefungUndMWE pruefung = null;
	
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
	throws DUAInitialisierungsException {
		
		if(dieVerwaltung instanceof IVerwaltungMitGuete){
			GUETE = ((IVerwaltungMitGuete)dieVerwaltung).getGueteFaktor();
		}else{
			throw new RuntimeException("Dieses Modul benötigt Informationen" + //$NON-NLS-1$
					" zum Guetefaktor der angeschlossenen SWE"); //$NON-NLS-1$
		}
		
		/**
		 * Auf Parameter aller betrachteten Fahrstreifen anmelden
		 */
		MweParameter.initialisiere(dieVerwaltung.getVerbindung(), dieVerwaltung.getSystemObjekte());
		
		this.pruefung = new LVEPruefungUndMWE();
		this.pruefung.initialisiere(dieVerwaltung);
		this.pruefung.setNaechstenBearbeitungsKnoten(this.knoten);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		this.pruefung.aktualisiereDaten(resultate);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return null;
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// hier wird nicht publiziert
	}

}
