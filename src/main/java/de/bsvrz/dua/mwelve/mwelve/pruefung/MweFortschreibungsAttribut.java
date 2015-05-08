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

import java.text.SimpleDateFormat;
import java.util.Date;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mwelve.vew.VerwaltungMessWertErsetzungLVE;
import de.bsvrz.sys.funclib.bitctrl.daf.BetriebsmeldungDaten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.operatingMessage.MessageCauser;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageSender;
import de.bsvrz.sys.funclib.operatingMessage.MessageState;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;

/**
 * Diese Klasse beinhaltet alle Informationen, die mit der Fortschreibung eines
 * Attributwertes bzgl. eines Fahrstreifens in Verbindung stehen.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class MweFortschreibungsAttribut {

	/**
	 * das Attribut, dessen Fortschreibungsstatistik hier in Bezug auf ein
	 * bestimmtes Objekt gespeichert werden soll.
	 */
	private MweAttribut attribut = null;

	/**
	 * ein Fahrstreifenobjekt , dessen KZD-Attribut hier untersucht wird.
	 */
	private SystemObject objekt = null;

	/**
	 * speichert den letzten plausiblen Wert dieses Attributs.
	 */
	private MweAttributWert letzterPlausiblerWert = null;

	/**
	 * Der Zeitpunkt, ab dem alte Daten fortgeschrieben werden.
	 */
	private long fortschreibungSeit = -1;

	/**
	 * Zum wievielten Mal wird ein Datum bereits fortgeschrieben.
	 */
	private long fortschreibungMale = 0;

	/**
	 * Standardkonstruktor.
	 *
	 * @param objekt
	 *            ein Fahrstreifenobjekt
	 * @param attribut
	 *            das Attribut, dessen Fortschreibungsstatistik hier in Bezug
	 *            auf ein bestimmtes Objekt gespeichert werden soll
	 */
	protected MweFortschreibungsAttribut(final SystemObject objekt,
			final MweAttribut attribut) {
		this.objekt = objekt;
		this.attribut = attribut;
	}

	/**
	 * Dieser Methode muss <b>jedes</b> für den assoziierten Fahrstreifen
	 * empfangene Datum übergeben werden, damit der jeweils letzte plausible
	 * Wert pro Fahrstreifen ermittelt werden kann.
	 *
	 * @param datum
	 *            ein FS-Datum
	 */
	public final void aktualisiere(final KZDatum datum) {
		final MweAttributWert wert = datum.getAttributWert(attribut);
		if (wert != null) {
			if (!wert.isImplausibel() && !wert.isInterpoliert()
					&& (wert.getWert() >= 0)) {
				letzterPlausiblerWert = wert;
			}
		}
	}

	/**
	 * Manipuliert ein bereits messwertersetztes Datum so, dass alle Werte, die
	 * implausibel und nicht interpoliert sind fortgeschrieben werden so lange
	 * dies nach den Parametern der MWE möglich ist.<br>
	 * Die Fortschreibung erfolgt mit dem letzten Wert für diesen Fahrstreifen,
	 * der nicht implausibel war.
	 *
	 * @param ersetztesDatum
	 *            ein (jedes) Datum, das durch die beiden MWE-Stufen (AFo S.
	 *            111) gegangen ist
	 */
	public final void schreibeGgfFort(final KZDatum ersetztesDatum) {
		final MweAttributWert wert = ersetztesDatum.getAttributWert(attribut);

		if ((wert != null) && wert.isImplausibel() && !wert.isInterpoliert()
				&& (letzterPlausiblerWert != null)
				&& MweParameter.isParameterValide(objekt)) {
			/**
			 * Es wird versucht, ein Datum zu veröffentlichen, das noch
			 * implausible Attributewerte besitzt, die noch nicht interpoliert
			 * wurden
			 */
			if (fortschreibungSeit < 0) {
				fortschreibungSeit = ersetztesDatum.getDatenZeit();
			}
			fortschreibungMale++;

			final MweParameter parameter = MweParameter.getParameter(objekt);
			if (parameter
					.getMaxErsetzungsDauer() >= (ersetztesDatum.getDatenZeit()
							- fortschreibungSeit)) {
				ersetztesDatum.getAttributWert(attribut)
				.setWert(letzterPlausiblerWert.getWert());
				ersetztesDatum.getAttributWert(attribut).setInterpoliert(true);
				ersetztesDatum.getAttributWert(attribut).setImplausibel(false);
				ersetztesDatum.getAttributWert(attribut)
				.setGuete(letzterPlausiblerWert.getGuete());
			}

			if ((parameter.getMaxWiederholungAnzahl() < fortschreibungMale)
					|| (parameter.getMaxWiederholungsZeit() < (ersetztesDatum
							.getDatenZeit() - fortschreibungSeit))) {

				MessageSender.getInstance().sendMessage(
						VerwaltungMessWertErsetzungLVE.getStaticBmvIdKonverter()
						.konvertiere(new BetriebsmeldungDaten(objekt),
								null, new Object[0]),
								MessageType.APPLICATION_DOMAIN, null,
								MessageGrade.WARNING, objekt, MessageState.MESSAGE,
								new MessageCauser(LVEPruefungUndMWE.sDav.getLocalUser(),
										"keine Messwertersetzung moeglich", //$NON-NLS-1$
										"Applikation Messwertersetzung LVE"), //$NON-NLS-1$
						"keine Messwertersetzung möglich"); //$NON-NLS-1$

				// MessageSender
				// .getInstance()
				// .sendMessage(
				// MessageType.APPLICATION_DOMAIN,
				// null,
				// MessageGrade.WARNING, //$NON-NLS-1$
				// this.objekt,
				// new MessageCauser(
				// LVEPruefungUndMWE.sDav.getLocalUser(),
				// "keine Messwertersetzung moeglich", "Applikation
				// Messwertersetzung LVE"), //$NON-NLS-1$ //$NON-NLS-2$
				// "keine Messwertersetzung möglich"); //$NON-NLS-1$
			}
		} else {
			fortschreibungSeit = -1;
			fortschreibungMale = 0;
		}
	}

	@Override
	public String toString() {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				DUAKonstanten.ZEIT_FORMAT_GENAU_STR);
		String s = "Fortschreibung fuer (" + objekt + "): " + attribut; //$NON-NLS-1$ //$NON-NLS-2$

		s += "\nletzter plausibler Wert: " + letzterPlausiblerWert; //$NON-NLS-1$
		s += "\nfortgeschrieben seit: " //$NON-NLS-1$
				+ dateFormat.format(new Date(fortschreibungSeit));
		s += "\nfortgeschrieben zum " + fortschreibungMale + ". mal"; //$NON-NLS-1$ //$NON-NLS-2$

		return s;
	}

}
