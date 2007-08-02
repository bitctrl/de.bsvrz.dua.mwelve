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
package de.bsvrz.dua.mwelve.mwelve.pruefung;

import de.bsvrz.sys.funclib.bitctrl.dua.lve.FahrStreifen;
import stauma.dav.clientside.ResultData;

/**
 * TODO<br>
 * Analogon zu <code>ErsetzungsTabelle</code> aus der Feinspezifikation 
 * 
 * IDee: Fahrstreifen initial auf Kaputt setzen, damit hier keine NO_DATA und kein Quelle
 * mehr hineingesopeist werden muss
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class FSDatenPuffer {

	/**
	 * der Fahrstreifen, dessen Daten hier gepuffert werden
	 */
	private FahrStreifen fs = null;
	
	/**
	 * zeigt an, ob dieser Fahrstreifen gerade als defekt eingeschätzt wird.
	 * Am Anfang ist jeder Fahstreifen defekt.
	 */
	private boolean aktuellDefekt = true;
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param fs der Fahrstreifen, dessen Daten hier gepuffert werden sollen
	 */	
	protected FSDatenPuffer(final FahrStreifen fs){
		if(fs == null){
			throw new NullPointerException("Als Fahrstreifen wurde <<null>> uebergeben"); //$NON-NLS-1$
		}
		this.fs = fs;
	}
	
	
	/**
	 * Erfragt den Fahrstreifen, dessen Daten hier gepuffert werden
	 * 
	 * @return der Fahrstreifen, dessen Daten hier gepuffert werden
	 */
	public final FahrStreifen getFahrStreifen(){
		return this.fs;
	}
	
	
	/**
	 * Erfragt, ob dieser Fahstreifen anhand der Daten, die er zuletzt geliefert hat
	 * im Moment als defekt eingestuft wird
	 * 
	 * @return  ob dieser Fahstreifen als defekt eingestuft wird
	 */
	public final boolean isAktuellDefekt(){
		return this.aktuellDefekt;
	}
	
	
	public final void aktuelisiereDaten(ResultData datum){
		
	}

}
