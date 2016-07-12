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

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;

import java.util.HashMap;
import java.util.Map;

/**
 * Korrespondiert mit
 * <code>atg.verkehrsDatenKurzZeitIntervallMessWertErsetzung</code>.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public final class MweParameter implements ClientReceiverInterface {

	/**
	 * Statischer Speicher aller Fahrstreifenparameter der MWE.
	 */
	private static Map<SystemObject, MweParameter> parameterMap = new HashMap<SystemObject, MweParameter>();

	/**
	 * Maximale Zeitdauer, über die implausible Messwerte ersetzt werden.
	 */
	private long maxErsetzungsDauer = -1;

	/**
	 * Standardkonstruktor.
	 * 
	 * @param dav
	 *            Datenverteiler-Verbindung
	 * @param objekt
	 *            das Fahrstreifenobjekt auf dessen Parameter sich angemeldet
	 *            werden soll
	 */
	private MweParameter(final ClientDavInterface dav, final SystemObject objekt) {
		DataDescription datenBeschreibung = new DataDescription(
				dav.getDataModel().getAttributeGroup(
						"atg.verkehrsDatenKurzZeitIntervallMessWertErsetzung"), //$NON-NLS-1$
				dav.getDataModel().getAspect(DaVKonstanten.ASP_PARAMETER_SOLL));
		dav.subscribeReceiver(this, objekt, datenBeschreibung, ReceiveOptions
				.normal(), ReceiverRole.receiver());
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
			final SystemObject[] objekte) {
		for (SystemObject objekt : objekte) {
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
		MweParameter parameter = parameterMap.get(fsObj);
		return parameter != null && parameter.getMaxErsetzungsDauer() != -1;
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
	 * {@inheritDoc}.
	 */
	public void update(ResultData[] resultate) {
		if (resultate != null) {
			for (ResultData resultat : resultate) {
				if (resultat != null && resultat.getData() != null) {
					synchronized (this) {
						this.maxErsetzungsDauer = resultat.getData()
								.getTimeValue("MaxErsetzungsDauer").getMillis(); //$NON-NLS-1$
					}
				}
			}
		}
	}
}
