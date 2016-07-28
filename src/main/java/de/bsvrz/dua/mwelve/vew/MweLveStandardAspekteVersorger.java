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

package de.bsvrz.dua.mwelve.vew;

import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.StandardAspekteVersorger;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse repräsentiert die Versorgung des Moduls Messwertersetzung LVE
 * (innerhalb der SWE Messwertersetzung LVE) mit
 * Standard-Publikationsinformationen (Zuordnung von
 * Objekt-Datenbeschreibung-Kombination zu Standard- Publikationsaspekt).
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class MweLveStandardAspekteVersorger extends StandardAspekteVersorger {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Standardkonstruktor.
	 * 
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @throws DUAInitialisierungsException
	 *             wird weitergereicht
	 */
	public MweLveStandardAspekteVersorger(final IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		super(verwaltung);
	}

	@Override
	protected void init() throws DUAInitialisierungsException {
		final List<StandardPublikationsZuordnung> zuordnungen = new ArrayList<StandardAspekteVersorger.StandardPublikationsZuordnung>();
		zuordnungen.add(new StandardPublikationsZuordnung(
				DUAKonstanten.TYP_FAHRSTREIFEN, DUAKonstanten.ATG_KZD,
				DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
				DUAKonstanten.ASP_MESSWERTERSETZUNG));
		if (isVerarbeiteLangzeitdaten()) {
			LOGGER.config("Langzeitdaten werden verarbeitet.");
			zuordnungen.add(new StandardPublikationsZuordnung(
					DUAKonstanten.TYP_FAHRSTREIFEN, DUAKonstanten.ATG_LZD,
					DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
					DUAKonstanten.ASP_MESSWERTERSETZUNG));
		} else {
			LOGGER.config("Langzeitdaten werden ignoriert.");
		}
		standardAspekte = new StandardAspekteAdapter(
				zuordnungen.toArray(new StandardPublikationsZuordnung[0]));
	}

	private boolean isVerarbeiteLangzeitdaten() {
		final String arg = verwaltung.getArgument("ignoriereLangzeitdaten");
		if (arg == null) {
			return true;
		}

		final boolean ignoriereLangzeitdaten = Boolean.parseBoolean(arg);
		return !ignoriereLangzeitdaten;
	}

}
