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

import java.util.HashMap;
import java.util.Map;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;

/**
 * Korrespondiert mit einer Instanz eines KZD-<code>ResultData</code>-Objektes
 * und hält alle innerhalb der MWE betrachteten Attribute (veränderbar) bereit.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id$
 */
public class KZDatum {

	/**
	 * das originale KZD-<code>ResultData</code>-Objekt.
	 */
	private ResultData originalDatum = null;

	/**
	 * alle zur MWE vorgesehenen Werte.
	 */
	private final Map<MweAttribut, MweAttributWert> attributWerte = new HashMap<MweAttribut, MweAttributWert>();

	/**
	 * Zeigt für dieses Datum an, dass es ein bestimmtes Modul verlassen hat und
	 * einem anderen Modul zur Verfügung gestellt wurde.
	 */
	private boolean bereitsWiederFreigegeben = false;

	/**
	 * Standardkonstruktor.
	 *
	 * @param resultat
	 *            ein KZD-<code>ResultData</code>-Objekt (<code>!= null</code>)
	 */
	public KZDatum(final ResultData resultat) {
		if (resultat == null) {
			throw new NullPointerException("Uebergebenes KZ-Datum ist <<null>>"); //$NON-NLS-1$
		}

		originalDatum = resultat;
		if (resultat.getData() != null) {
			for (final MweAttribut attribut : MweAttribut.getInstanzen()) {
				attributWerte.put(attribut, new MweAttributWert(attribut,
						resultat.getData()));
			}
		}
	}

	/**
	 * Erfragt den Datenzeitstempel des Originaldatums, mit dem dieses Datum
	 * assoziiert ist.
	 *
	 * @return der Datenzeitstempel des Originaldatums, mit dem dieses Datum
	 *         assoziiert ist
	 */
	public final long getDatenZeit() {
		return originalDatum.getDataTime();
	}

	/**
	 * Erfragt den Defektionszustand des mit diesem Datum assoziierten
	 * Fahstreifens für den Datenzeitpunkt dieses Datums<br>
	 * Der Fahrstreifen bzw. dieses Datum ist <code>defekt</code>, wenn:<br>
	 * 1. der Datensatz keine Nutzdaten enthält<br>
	 * .
	 *
	 * @return ob der mit diesem Datum assoziierte Fahstreifen im Moment dieses
	 *         Datenzeitstempels als <code>defekt</code> zu interpretieren ist
	 */
	public final boolean isDefekt() {
		return originalDatum.getData() == null;
	}

	/**
	 * Erfragt ob dieses Datum ein bestimmtes Modul verlassen hat und einem
	 * anderen Modul wieder zur Verfügung gestellt wurde.
	 *
	 * @return ob dieses Datum ein bestimmtes Modul verlassen hat und einem
	 *         anderen Modul wieder zur Verfügung gestellt wurde
	 */
	public final boolean isBereitsWiederFreigegeben() {
		return bereitsWiederFreigegeben;
	}

	/**
	 * Setzt, ob dieses Datum ein bestimmtes Modul verlassen hat und einem
	 * anderen Modul wieder zur Verfügung gestellt wurde.
	 *
	 * @param bereitsWiederFreigegeben
	 *            ob dieses Datum ein bestimmtes Modul verlassen hat und einem
	 *            anderen Modul wieder zur Verfügung gestellt wurde
	 */
	public final void setBereitsWiederFreigegeben(
			final boolean bereitsWiederFreigegeben) {
		this.bereitsWiederFreigegeben = bereitsWiederFreigegeben;
	}

	/**
	 * Erfragt den Wert eines bestimmten Attributs.
	 *
	 * @param attribut
	 *            das Attribut
	 * @return den Wert eines bestimmten Attributs
	 */
	public final MweAttributWert getAttributWert(final MweAttribut attribut) {
		return attributWerte.get(attribut);
	}

	/**
	 * Erfragt den Wert eines bestimmten Attributs.
	 *
	 * @param attributWert
	 *            das Attribut
	 */
	public final void setAttributWert(final MweAttributWert attributWert) {
		attributWerte.put(attributWert.getAttribut(), attributWert);
	}

	/**
	 * Erfragt, ob dieses Datum in allen für die MWE relevanten Werten plausibel
	 * ist<br>
	 * . <b>Achtung:</b> Die Methode gibt auch dann <code>true</code> zurück,
	 * wenn das mit diesem Objekt assoziierte Datum keine Nutzdaten enthält
	 *
	 * @return ob dieses Datum in allen für die MWE relevanten Werten plausibel
	 *         ist
	 */
	public final boolean isVollstaendigPlausibel() {
		boolean plausibel = true;

		if (!isDefekt()) {
			for (final MweAttribut attribut : MweAttribut.getInstanzen()) {
				if (getAttributWert(attribut).isImplausibel()) {
					plausibel = false;
					break;
				}
			}
		}

		return plausibel;
	}

	/**
	 * Erfragt das ggf. veränderte KZD-<code>ResultData</code>-Objekt
	 *
	 * @return das ggf. veränderte KZD-<code>ResultData</code>-Objekt
	 */
	public final ResultData getDatum() {
		ResultData ergebnis = originalDatum;

		if (originalDatum.getData() != null) {
			boolean veraendert = false;
			for (final MweAttribut attribut : MweAttribut.getInstanzen()) {
				if (attributWerte.get(attribut).isVeraendert()) {
					veraendert = true;
					break;
				}
			}

			if (veraendert) {
				final Data kopie = originalDatum.getData()
						.createModifiableCopy();

				for (final MweAttribut attribut : MweAttribut.getInstanzen()) {
					modifiziereGgfDatenSatz(attributWerte.get(attribut), kopie);
				}

				ergebnis = new ResultData(originalDatum.getObject(),
						originalDatum.getDataDescription(),
						originalDatum.getDataTime(), kopie);
			}
		}

		return ergebnis;
	}

	/**
	 * Modifiziert den übergebenen DAV-Datensatz ggf. nach dem übergebenen
	 * Verkehrsdaten-Attributwert (ggf. heißt, nur wenn sich der Attributwert
	 * verändert hat)
	 *
	 * @param attr
	 *            der Attributwert
	 * @param datenSatz
	 *            der Datensatz, der modifiziert werden soll
	 */
	private void modifiziereGgfDatenSatz(final MweAttributWert attr,
			final Data datenSatz) {
		if (attr.isVeraendert()) {

			if (DUAUtensilien.isWertInWerteBereich(
					datenSatz.getItem(attr.getAttribut().getName()).getItem(
							"Wert"), attr.getWert())) { //$NON-NLS-1$
				datenSatz.getItem(attr.getAttribut().getName())
				.getUnscaledValue("Wert").set(attr.getWert()); //$NON-NLS-1$
				datenSatz.getItem(attr.getAttribut().getName())
				.getItem("Status").getItem("MessWertErsetzung").//$NON-NLS-1$//$NON-NLS-2$
				getUnscaledValue("Implausibel").set(attr.isImplausibel() ? DUAKonstanten.JA : DUAKonstanten.NEIN); //$NON-NLS-1$
				datenSatz.getItem(attr.getAttribut().getName())
				.getItem("Status").getItem("MessWertErsetzung").//$NON-NLS-1$//$NON-NLS-2$
				getUnscaledValue("Interpoliert").set(attr.isInterpoliert() ? DUAKonstanten.JA : DUAKonstanten.NEIN); //$NON-NLS-1$
				datenSatz.getItem(attr.getAttribut().getName())
				.getItem("Güte").//$NON-NLS-1$
				getUnscaledValue("Index").set(attr.getGuete().getIndexUnskaliert()); //$NON-NLS-1$
				datenSatz
				.getItem(attr.getAttribut().getName())
				.getItem("Güte").//$NON-NLS-1$
				getUnscaledValue("Verfahren").set(attr.getGuete().getVerfahren().getCode()); //$NON-NLS-1$
			} else {
				datenSatz
				.getItem(attr.getAttribut().getName())
				.getUnscaledValue("Wert").set(DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT); //$NON-NLS-1$
				datenSatz.getItem(attr.getAttribut().getName())
						.getItem("Status").getItem("MessWertErsetzung").//$NON-NLS-1$//$NON-NLS-2$
						getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				datenSatz
						.getItem(attr.getAttribut().getName())
				.getItem("Status").getItem("MessWertErsetzung").//$NON-NLS-1$//$NON-NLS-2$
				getUnscaledValue("Interpoliert").set(DUAKonstanten.NEIN); //$NON-NLS-1$
				datenSatz.getItem(attr.getAttribut().getName())
				.getItem("Güte").//$NON-NLS-1$
				getUnscaledValue("Index").set(0.0); //$NON-NLS-1$
				datenSatz
				.getItem(attr.getAttribut().getName())
				.getItem("Güte").//$NON-NLS-1$
				getUnscaledValue("Verfahren").set(attr.getGuete().getVerfahren().getCode()); //$NON-NLS-1$
			}

			datenSatz
					.getItem(attr.getAttribut().getName())
			.getItem("Status").getItem("PlFormal").//$NON-NLS-1$ //$NON-NLS-2$
			getUnscaledValue("WertMax").set(attr.isFormalMax() ? DUAKonstanten.JA : DUAKonstanten.NEIN); //$NON-NLS-1$
			datenSatz
					.getItem(attr.getAttribut().getName())
			.getItem("Status").getItem("PlFormal").//$NON-NLS-1$ //$NON-NLS-2$
			getUnscaledValue("WertMin").set(attr.isFormalMin() ? DUAKonstanten.JA : DUAKonstanten.NEIN); //$NON-NLS-1$
			datenSatz
					.getItem(attr.getAttribut().getName())
			.getItem("Status").getItem("PlLogisch").//$NON-NLS-1$ //$NON-NLS-2$
			getUnscaledValue("WertMaxLogisch").set(attr.isLogischMax() ? DUAKonstanten.JA : DUAKonstanten.NEIN); //$NON-NLS-1$
			datenSatz
					.getItem(attr.getAttribut().getName())
			.getItem("Status").getItem("PlLogisch").//$NON-NLS-1$ //$NON-NLS-2$
			getUnscaledValue("WertMinLogisch").set(attr.isLogischMin() ? DUAKonstanten.JA : DUAKonstanten.NEIN); //$NON-NLS-1$
			datenSatz
					.getItem(attr.getAttribut().getName())
			.getItem("Status").getItem("Erfassung").//$NON-NLS-1$//$NON-NLS-2$
			getUnscaledValue("NichtErfasst").set(attr.isNichtErfasst() ? DUAKonstanten.JA : DUAKonstanten.NEIN); //$NON-NLS-1$
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String s = "Original: " + originalDatum; //$NON-NLS-1$

		for (final MweAttribut attribut : MweAttribut.getInstanzen()) {
			s += "\n" + attribut.toString() + ": " + attributWerte.get(attribut); //$NON-NLS-1$ //$NON-NLS-2$
		}
		s += "\n" + (bereitsWiederFreigegeben ? "freigegeben" : "nicht freigegeben"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return s;
	}

}
