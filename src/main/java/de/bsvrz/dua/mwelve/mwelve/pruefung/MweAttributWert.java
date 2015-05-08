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

package de.bsvrz.dua.mwelve.mwelve.pruefung;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.MesswertMarkierung;

/**
 * Korrespondiert mit einem Attributwert eines KZ- oder LZ-Datums.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class MweAttributWert extends MesswertMarkierung
		implements Comparable<MweAttributWert> {

	/**
	 * das Attribut.
	 */
	private MweAttribut attr = null;

	/**
	 * der Wert dieses Attributs.
	 */
	private long wert = -4;

	/**
	 * die Guete.
	 */
	private GWert guete = null;

	/**
	 * Standardkonstruktor.
	 *
	 * @param attr
	 *            das Attribut
	 * @param datenSatz
	 *            der Datensatz in dem der Attributwert steht
	 */
	public MweAttributWert(final MweAttribut attr, final Data datenSatz) {
		if (attr == null) {
			throw new NullPointerException("Attribut ist <<null>>"); //$NON-NLS-1$
		}
		if (datenSatz == null) {
			throw new NullPointerException("Datensatz ist <<null>>"); //$NON-NLS-1$
		}
		this.attr = attr;
		wert = datenSatz.getItem(attr.getName()).getUnscaledValue("Wert") //$NON-NLS-1$
				.longValue();
		nichtErfasst = datenSatz.getItem(attr.getName()).getItem("Status") //$NON-NLS-1$
				.getItem("Erfassung").//$NON-NLS-1$
				getUnscaledValue("NichtErfasst").intValue() == DUAKonstanten.JA; //$NON-NLS-1$
		implausibel = datenSatz.getItem(attr.getName()).getItem("Status") //$NON-NLS-1$
				.getItem("MessWertErsetzung").//$NON-NLS-1$
				getUnscaledValue("Implausibel").intValue() == DUAKonstanten.JA; //$NON-NLS-1$
		interpoliert = datenSatz.getItem(attr.getName()).getItem("Status") //$NON-NLS-1$
				.getItem("MessWertErsetzung").//$NON-NLS-1$
				getUnscaledValue("Interpoliert").intValue() == DUAKonstanten.JA; //$NON-NLS-1$

		formalMax = datenSatz.getItem(attr.getName()).getItem("Status") //$NON-NLS-1$
				.getItem("PlFormal").//$NON-NLS-1$
				getUnscaledValue("WertMax").intValue() == DUAKonstanten.JA; //$NON-NLS-1$
		formalMin = datenSatz.getItem(attr.getName()).getItem("Status") //$NON-NLS-1$
				.getItem("PlFormal").//$NON-NLS-1$
				getUnscaledValue("WertMin").intValue() == DUAKonstanten.JA; //$NON-NLS-1$

		logischMax = datenSatz.getItem(attr.getName()).getItem("Status") //$NON-NLS-1$
				.getItem("PlLogisch").//$NON-NLS-1$
				getUnscaledValue("WertMaxLogisch") //$NON-NLS-1$
				.intValue() == DUAKonstanten.JA;
		logischMin = datenSatz.getItem(attr.getName()).getItem("Status") //$NON-NLS-1$
				.getItem("PlLogisch").//$NON-NLS-1$
				getUnscaledValue("WertMinLogisch") //$NON-NLS-1$
				.intValue() == DUAKonstanten.JA;

		guete = new GWert(datenSatz, attr.getName());
	}

	/**
	 * Kopierkonstruktor. Das Datum, das durch diesen Konstruktor erzeugt wird,
	 * ist als <code>veraendert</code> markiert
	 *
	 * @param vorlage
	 *            das zu kopierende Datum
	 */
	public MweAttributWert(final MweAttributWert vorlage) {
		veraendert = true;
		attr = vorlage.attr;
		wert = vorlage.wert;
		nichtErfasst = vorlage.nichtErfasst;
		implausibel = vorlage.implausibel;
		interpoliert = vorlage.interpoliert;
		formalMax = vorlage.formalMax;
		formalMin = vorlage.formalMin;
		logischMax = vorlage.logischMax;
		logischMin = vorlage.logischMin;
		guete = new GWert(vorlage.guete);
	}

	/**
	 * Erfragt das Attribut.
	 *
	 * @return das Attribut
	 */
	public final MweAttribut getAttribut() {
		return attr;
	}

	/**
	 * Setzt den Wert dieses Attributs.
	 *
	 * @param wert
	 *            der Wert dieses Attributs
	 */
	public final void setWert(final long wert) {
		veraendert = true;
		this.wert = wert;
	}

	/**
	 * Erfragt den Wert dieses Attributs.
	 *
	 * @return der Wert dieses Attributs
	 */
	public final long getWert() {
		return wert;
	}

	@Override
	public int compareTo(final MweAttributWert that) {
		return new Long(getWert()).compareTo(that.getWert());
	}

	/**
	 * Erfragt die Guete dieses Attributwertes.
	 *
	 * @return die Guete dieses Attributwertes
	 */
	public final GWert getGuete() {
		return guete;
	}

	/**
	 * Setzte die Guete dieses Attributwertes.
	 *
	 * @param guete
	 *            die Guete dieses Attributwertes
	 */
	public final void setGuete(final GWert guete) {
		veraendert = true;
		this.guete = guete;
	}

	@Override
	public boolean equals(final Object obj) {
		boolean ergebnis = false;

		if ((obj != null) && (obj instanceof MweAttributWert)) {
			final MweAttributWert that = (MweAttributWert) obj;
			ergebnis = getAttribut().equals(that.getAttribut())
					&& (getWert() == that.getWert())
					&& (isNichtErfasst() == that.isNichtErfasst())
					&& (isImplausibel() == that.isImplausibel())
					&& getGuete().equals(that.getGuete())
					&& (isInterpoliert() == that.isInterpoliert());
		}

		return ergebnis;
	}

	@Override
	public String toString() {
		return "Attribut: " + attr + "\nWert: " + wert + //$NON-NLS-1$ //$NON-NLS-2$
				"\nGuete: " + guete + //$NON-NLS-1$
				"\nVeraendert: " + (veraendert ? "Ja" : "Nein") + //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
				"\n" + super.toString(); //$NON-NLS-1$
	}

}
