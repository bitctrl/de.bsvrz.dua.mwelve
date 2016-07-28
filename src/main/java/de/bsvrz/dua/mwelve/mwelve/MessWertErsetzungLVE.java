/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Messwertersetzung LVE
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.mwelve.
 * 
 * de.bsvrz.dua.mwelve is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.mwelve is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.mwelve.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.mwelve.mwelve;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.mwelve.mwelve.pruefung.LVEPruefungUndMWE;
import de.bsvrz.dua.mwelve.mwelve.pruefung.MweParameter;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Das Modul Messwertersetzung LVE meldet sich auf alle benötigten Parameter der
 * vorgesehenen Objekte an. Es führt für die über die Methode
 * <code>aktualisiereDaten(..)</code> übergebenen Kurzzeitdaten ggf. eine
 * attributbezogene Messwertersetzung durch. Bei nicht durchführbarer
 * Messwertersetzung wird ggf. eine Betriebsmeldung generiert. Nach der
 * Ersetzung werden die Daten je nach Parametrierung unter dem Aspekt
 * asp.messWertErsetzung publiziert und an den nächsten Bearbeitungsknoten
 * weitergereicht
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class MessWertErsetzungLVE extends AbstraktBearbeitungsKnotenAdapter {
	
	/**
	 * Dieses Submodul führt die eigentliche Prüfung durch.
	 */
	private LVEPruefungUndMWE pruefung = null;

	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {

		/**
		 * Auf Parameter aller betrachteten Fahrstreifen anmelden
		 */
		MweParameter.initialisiere(dieVerwaltung.getVerbindung(), dieVerwaltung
				.getSystemObjekte());

		this.pruefung = new LVEPruefungUndMWE();
		this.pruefung.setNaechstenBearbeitungsKnoten(this.knoten);
		this.pruefung.initialisiere(dieVerwaltung);
	}

	public void aktualisiereDaten(ResultData[] resultate) {
		this.pruefung.aktualisiereDaten(resultate);
	}

	public ModulTyp getModulTyp() {
		return null;
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// hier wird nicht publiziert
	}

}
