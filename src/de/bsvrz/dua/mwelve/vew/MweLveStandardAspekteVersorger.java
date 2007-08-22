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

import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.StandardAspekteVersorger;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Diese Klasse repräsentiert die Versorgung des Moduls
 * Messwertersetzung LVE (innerhalb der SWE Messwertersetzung LVE)
 * mit Standard-Publikationsinformationen (Zuordnung von
 * Objekt-Datenbeschreibung-Kombination zu Standard-
 * Publikationsaspekt).
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class MweLveStandardAspekteVersorger 
extends StandardAspekteVersorger{

	
	/**
	 * {@inheritDoc}
	 */
	public MweLveStandardAspekteVersorger(IVerwaltung verwaltung)
	throws DUAInitialisierungsException {
		super(verwaltung);
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void init()
	throws DUAInitialisierungsException{

		this.standardAspekte = new StandardAspekteAdapter(
				new StandardPublikationsZuordnung[] {
						new StandardPublikationsZuordnung(
								DUAKonstanten.TYP_FAHRSTREIFEN,
								DUAKonstanten.ATG_KZD,
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_MESSWERTERSETZUNG),
						new StandardPublikationsZuordnung(
								DUAKonstanten.TYP_FAHRSTREIFEN,
								DUAKonstanten.ATG_LZD,
								DUAKonstanten.ASP_EXTERNE_ERFASSUNG,
								DUAKonstanten.ASP_MESSWERTERSETZUNG) });
					
	}	
}
