/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.5 Messwertersetzung LVE
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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

/**
 * CSV-Eingabe-Datei mit Alias.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class TestDatei {

	/**
	 * der Dateiname.
	 */
	private String dateiName = null;

	/**
	 * Alias der Datei (z.B. Tabellenname)
	 */
	private String alias = "kein Alias"; //$NON-NLS-1$

	/**
	 * Standardkonstruktor.
	 *
	 * @param dateiName
	 *            der Dateiname
	 * @param alias
	 *            Alias der Datei (z.B. Tabellenname)
	 */
	public TestDatei(final String dateiName, final String alias) {
		this.dateiName = dateiName;
		if (alias != null) {
			this.alias = alias;
		}
	}

	/**
	 * Erfragt den Dateiname.
	 *
	 * @return der Dateiname
	 */
	public final String getDateiName() {
		return dateiName;
	}

	/**
	 * Erfragt den Alias der Datei (z.B. Tabellenname).
	 *
	 * @return der Alias der Datei (z.B. Tabellenname)
	 */
	public final String getAlias() {
		return alias;
	}

}
