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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.MesswertMarkierung;

/**
 * Korrespondiert mit einem Attributwert eines KZ- oder LZ-Datums.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class MweAttributWert extends MesswertMarkierung implements
		Comparable<MweAttributWert> {

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
		this.wert = datenSatz.getItem(attr.getName())
				.getUnscaledValue("Wert").longValue(); //$NON-NLS-1$
		this.nichtErfasst = datenSatz.getItem(attr.getName())
				.getItem("Status").getItem("Erfassung").//$NON-NLS-1$//$NON-NLS-2$
				getUnscaledValue("NichtErfasst").intValue() == DUAKonstanten.JA; //$NON-NLS-1$
		this.implausibel = datenSatz.getItem(attr.getName())
				.getItem("Status").getItem("MessWertErsetzung").//$NON-NLS-1$//$NON-NLS-2$
				getUnscaledValue("Implausibel").intValue() == DUAKonstanten.JA; //$NON-NLS-1$
		this.interpoliert = datenSatz.getItem(attr.getName())
				.getItem("Status").getItem("MessWertErsetzung").//$NON-NLS-1$//$NON-NLS-2$
				getUnscaledValue("Interpoliert").intValue() == DUAKonstanten.JA; //$NON-NLS-1$

		this.formalMax = datenSatz.getItem(attr.getName())
				.getItem("Status").getItem("PlFormal").//$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("WertMax").intValue() == DUAKonstanten.JA; //$NON-NLS-1$
		this.formalMin = datenSatz.getItem(attr.getName())
				.getItem("Status").getItem("PlFormal").//$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("WertMin").intValue() == DUAKonstanten.JA; //$NON-NLS-1$

		this.logischMax = datenSatz.getItem(attr.getName())
				.getItem("Status").getItem("PlLogisch").//$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("WertMaxLogisch").intValue() == DUAKonstanten.JA; //$NON-NLS-1$
		this.logischMin = datenSatz.getItem(attr.getName())
				.getItem("Status").getItem("PlLogisch").//$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("WertMinLogisch").intValue() == DUAKonstanten.JA; //$NON-NLS-1$

		this.guete = new GWert(datenSatz, attr.getName());
	}

	/**
	 * Kopierkonstruktor. Das Datum, das durch diesen Konstruktor erzeugt wird,
	 * ist als <code>veraendert</code> markiert
	 * 
	 * @param vorlage
	 *            das zu kopierende Datum
	 */
	public MweAttributWert(MweAttributWert vorlage) {
		this.veraendert = true;
		this.attr = vorlage.attr;
		this.wert = vorlage.wert;
		this.nichtErfasst = vorlage.nichtErfasst;
		this.implausibel = vorlage.implausibel;
		this.interpoliert = vorlage.interpoliert;
		this.formalMax = vorlage.formalMax;
		this.formalMin = vorlage.formalMin;
		this.logischMax = vorlage.logischMax;
		this.logischMin = vorlage.logischMin;
		this.guete = vorlage.guete == null ? null : new GWert(vorlage.guete);
	}

	/**
	 * Erfragt das Attribut.
	 * 
	 * @return das Attribut
	 */
	public final MweAttribut getAttribut() {
		return this.attr;
	}

	/**
	 * Setzt den Wert dieses Attributs.
	 * 
	 * @param wert
	 *            der Wert dieses Attributs
	 */
	public final void setWert(final long wert) {
		this.veraendert = true;
		this.wert = wert;
	}

	/**
	 * Erfragt den Wert dieses Attributs.
	 * 
	 * @return der Wert dieses Attributs
	 */
	public final long getWert() {
		return this.wert;
	}

	public int compareTo(MweAttributWert that) {
		return new Long(this.getWert()).compareTo(that.getWert());
	}

	/**
	 * Erfragt die Guete dieses Attributwertes.
	 * 
	 * @return die Guete dieses Attributwertes
	 */
	public final GWert getGuete() {
		return this.guete;
	}

	/**
	 * Setzte die Guete dieses Attributwertes.
	 * 
	 * @param guete
	 *            die Guete dieses Attributwertes
	 */
	public final void setGuete(final GWert guete) {
		this.veraendert = true;
		this.guete = guete;
	}

	@Override
	public String toString() {
		return "Attribut: " + this.attr + "\nWert: " + this.wert + //$NON-NLS-1$ //$NON-NLS-2$
				"\nGuete: " + this.guete + //$NON-NLS-1$
				"\nVeraendert: " + (this.veraendert ? "Ja" : "Nein") + //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
				"\n" + super.toString(); //$NON-NLS-1$
	}

}
