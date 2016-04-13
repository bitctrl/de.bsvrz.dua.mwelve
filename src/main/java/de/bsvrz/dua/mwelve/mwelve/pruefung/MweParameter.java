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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;

/**
 * Korrespondiert mit
 * <code>atg.verkehrsDatenKurzZeitIntervallMessWertErsetzung</code>.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public final class MweParameter implements ClientReceiverInterface {

	/**
	 * Statischer Speicher aller Fahrstreifenparameter der MWE.
	 */
	private static Map<SystemObject, MweParameter> parameterMap = new HashMap<>();

	/**
	 * Maximale Zeitdauer, über die implausible Messwerte ersetzt werden.
	 */
	private long maxErsetzungsDauer = -1;

	/**
	 * Maximale Zeitdauer bis zur Fehlermeldung, wenn wegem fehlendem
	 * Ersatzwert. nicht interpoliert werden kann
	 */
	private long maxWiederholungsZeit = -1;

	/**
	 * Maximale Anzahl von Ersetzungsversuchen bis zur Fehlermeldung, wenn
	 * wegen. fehlendem Ersatzswert nicht interpoliert werden kann.
	 */
	private long maxWiederholungAnzahl = -1;

	/**
	 * Standardkonstruktor.
	 *
	 * @param dav
	 *            Datenverteiler-Verbindung
	 * @param objekt
	 *            das Fahrstreifenobjekt auf dessen Parameter sich angemeldet
	 *            werden soll
	 */
	private MweParameter(final ClientDavInterface dav,
			final SystemObject objekt) {
		final DataDescription datenBeschreibung = new DataDescription(
				dav.getDataModel().getAttributeGroup(
						"atg.verkehrsDatenKurzZeitIntervallMessWertErsetzung"), //$NON-NLS-1$
				dav.getDataModel().getAspect(DaVKonstanten.ASP_PARAMETER_SOLL));
		dav.subscribeReceiver(this, objekt, datenBeschreibung,
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}

	/**
	 * Initialisiert alle Parameteranmeldungen.
	 *
	 * @param dav
	 *            Datenverteiler-Verbindung
	 * @param objekte
	 *            die Fahrstreifenobjekte auf deren Parameter sich angemeldet
	 *            werden soll
	 */
	public static void initialisiere(final ClientDavInterface dav,
			final Collection<SystemObject> objekte) {
		for (final SystemObject objekt : objekte) {
			parameterMap.put(objekt, new MweParameter(dav, objekt));
		}
	}

	/**
	 * Erfragt die aktuellen MWE-Parameter eines Fahrstreifens.
	 *
	 * @param fsObj
	 *            ein Systemobjekt eines Fahrstreifens
	 * @return die aktuellen MWE-Parameter eines Fahrstreifens oder
	 *         <code>null</code>, wenn diese nicht ermittelt werden konnten
	 */
	public static MweParameter getParameter(final SystemObject fsObj) {
		return parameterMap.get(fsObj);
	}

	/**
	 * Erfragt, ob für einen Fahrstreifen gültige Parameter vorhanden sind.
	 *
	 * @param fsObj
	 *            ein Systemobjekt eines Fahrstreifens
	 * @return ob für einen Fahrstreifen gültige Parameter vorhanden sind
	 */
	public static boolean isParameterValide(final SystemObject fsObj) {
		final MweParameter parameter = parameterMap.get(fsObj);
		return (parameter != null) && (parameter.getMaxErsetzungsDauer() != -1);
	}

	/**
	 * Erfragt die Maximale Zeitdauer, über die implausible Messwerte ersetzt
	 * werden.
	 *
	 * @return Maximale Zeitdauer, über die implausible Messwerte ersetzt werden
	 */
	public synchronized long getMaxErsetzungsDauer() {
		return maxErsetzungsDauer;
	}

	/**
	 * Erfragt Maximale Zeitdauer bis zur Fehlermeldung, wenn wegem fehlendem
	 * Ersatzwert nicht interpoliert werden kann.
	 *
	 * @return Maximale Zeitdauer bis zur Fehlermeldung, wenn wegem fehlendem
	 *         Ersatzwert nicht interpoliert werden kann
	 */
	public synchronized long getMaxWiederholungsZeit() {
		return maxWiederholungsZeit;
	}

	/**
	 * Erfragt Maximale Anzahl von Ersetzungsversuchen bis zur Fehlermeldung,
	 * wenn wegen fehlendem Ersatzswert nicht interpoliert werden kann.
	 *
	 * @return Maximale Anzahl von Ersetzungsversuchen bis zur Fehlermeldung,
	 *         wenn wegen fehlendem Ersatzswert nicht interpoliert werden kann
	 */
	public synchronized long getMaxWiederholungAnzahl() {
		return maxWiederholungAnzahl;
	}

	@Override
	public void update(final ResultData[] resultate) {
		if (resultate != null) {
			for (final ResultData resultat : resultate) {
				if ((resultat != null) && (resultat.getData() != null)) {
					synchronized (this) {
						maxErsetzungsDauer = resultat.getData()
								.getTimeValue("MaxErsetzungsDauer").getMillis(); //$NON-NLS-1$
						maxWiederholungsZeit = resultat.getData()
								.getTimeValue("MaxWiederholungsZeit") //$NON-NLS-1$
								.getMillis();
						maxWiederholungAnzahl = resultat.getData()
								.getUnscaledValue("MaxWiederholungAnzahl") //$NON-NLS-1$
								.longValue();
					}
				}
			}
		}
	}
}
