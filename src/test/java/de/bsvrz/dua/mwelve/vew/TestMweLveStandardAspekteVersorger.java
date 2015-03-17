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

import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.StandardAspekteVersorger;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Diese Klasse repraesentiert die Versorgung des Moduls Messwertersetzung LVE
 * (innerhalb des Tests des Moduls Messwertersetzung LVE) mit
 * Standard-Publikationsinformationen (Zuordnung von
 * Objekt-Datenbeschreibung-Kombination zu Standard- Publikationsaspekt).
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class TestMweLveStandardAspekteVersorger extends
		StandardAspekteVersorger {

	/**
	 * Standardkonstruktor.
	 * 
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @throws DUAInitialisierungsException wird weitergereicht
	 */
	public TestMweLveStandardAspekteVersorger(IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		super(verwaltung);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void init() throws DUAInitialisierungsException {

		this.standardAspekte = new StandardAspekteAdapter(
				new StandardPublikationsZuordnung[] { new StandardPublikationsZuordnung(
						DUAKonstanten.TYP_FAHRSTREIFEN, DUAKonstanten.ATG_KZD,
						DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH,
						DUAKonstanten.ASP_MESSWERTERSETZUNG) });
	}
}