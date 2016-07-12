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

package de.bsvrz.dua.mwelve.mwelve.pruefung;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Container für KDZ-Attribute:<br>
 * <code>qKfz</code>,<br>
 * <code>qLkw</code>,<br>
 * <code>qPkw</code>,<br>
 * <code>vKfz</code>,<br>
 * <code>vLkw</code> und<br>
 * <code>vPkw</code>.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public final class MweAttribut {

	/**
	 * Wertebereich.
	 */
	private static Map<String, MweAttribut> werteBereich = new HashMap<String, MweAttribut>();

	/**
	 * Attribut <code>qKfz</code>.
	 */
	public static final MweAttribut Q_KFZ = new MweAttribut("qKfz"); //$NON-NLS-1$

	/**
	 * Attribut <code>qLkw</code>.
	 */
	public static final MweAttribut Q_LKW = new MweAttribut("qLkw"); //$NON-NLS-1$

	/**
	 * Attribut <code>qPkw</code>.
	 */
	public static final MweAttribut Q_PKW = new MweAttribut("qPkw"); //$NON-NLS-1$

	/**
	 * Attribut <code>vKfz</code>.
	 */
	public static final MweAttribut V_KFZ = new MweAttribut("vKfz"); //$NON-NLS-1$

	/**
	 * Attribut <code>vLkw</code>.
	 */
	public static final MweAttribut V_LKW = new MweAttribut("vLkw"); //$NON-NLS-1$

	/**
	 * Attribut <code>vPkw</code>.
	 */
	public static final MweAttribut V_PKW = new MweAttribut("vPkw"); //$NON-NLS-1$

	/**
	 * der Name des Attributs.
	 */
	private String name = null;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param name
	 *            der Name des Attributs
	 */
	private MweAttribut(final String name) {
		this.name = name;
		werteBereich.put(name, this);
	}

	/**
	 * Erfragt eine mit dem übergebenen Namen korrespondierende statische
	 * Instanz dieser Klasse.
	 * 
	 * @param name
	 *            ein Attribut-Name
	 * @return eine mit dem übergebenen Namen korrespondierende statische
	 *         Instanz dieser Klasse, oder <code>null</code>
	 */
	public static MweAttribut getInstanz(final String name) {
		MweAttribut ergebnis = null;

		if (name != null) {
			ergebnis = werteBereich.get(name);
		}

		return ergebnis;
	}

	/**
	 * Erfragt, ob es sich bei diesem Attribut um die Verkehrsstärke einer
	 * Fahrzeuggruppe handelt.
	 * 
	 * @return ob es sich bei diesem Attribut um die Verkehrsstärke einer
	 *         Fahrzeuggruppe handelt
	 */
	public boolean isQWert() {
		return this.equals(Q_KFZ) || this.equals(Q_LKW) || this.equals(Q_PKW);
	}

	/**
	 * Erfragt alle statischen Instanzen dieser Klasse.
	 * 
	 * @return alle statischen Instanzen dieser Klasse
	 */
	public static Collection<MweAttribut> getInstanzen() {
		return werteBereich.values();
	}

	/**
	 * Erfragt den Namen dieses Attributs.
	 * 
	 * @return der Name dieses Attributs
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public String toString() {
		return this.name;
	}
}
