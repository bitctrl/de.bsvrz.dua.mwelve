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

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.test.CSVImporter;

/**
 * Liest die Ausgangsdaten eines Fahrstreifens ein.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class TestFahrstreifenImporter extends CSVImporter {

	/**
	 * Verbindung zum Datenverteiler.
	 */
	protected static ClientDavInterface sDav = null;

	/**
	 * T.
	 */
	protected static long intervall = Constants.MILLIS_PER_MINUTE;

	/**
	 * die Zeile, die als letztes eingelesen wurde.
	 */
	private String[] aktuelleZeile = null;

	/**
	 * Standardkonstruktor.
	 *
	 * @param dav
	 *            Datenverteier-Verbindung
	 * @param csvQuelle
	 *            Quelle der Daten (CSV-Datei)
	 * @throws Exception
	 *             falls dieses Objekt nicht vollständig initialisiert werden
	 *             konnte
	 */
	public TestFahrstreifenImporter(final ClientDavInterface dav,
			final String csvQuelle) throws Exception {
		super(csvQuelle);
		if (sDav == null) {
			sDav = dav;
		}

		/**
		 * Tabellenkopf überspringen
		 */
		next();
	}

	/**
	 * Setzt Datenintervall.
	 *
	 * @param t
	 *            Datenintervall
	 */
	public static final void setT(final long t) {
		intervall = t;
	}

	/**
	 * Springt zur naechsten Zeile innerhalb der CSV-Datei.
	 *
	 * @return ob weiter aus der Datei gelesen werden kann
	 */
	public final boolean next() {
		aktuelleZeile = getNaechsteZeile();
		if (aktuelleZeile == null) {
			return false;
		}
		return true;
	}

	/**
	 * Erfragt, ob der Dateizeiger am Ende steht.
	 *
	 * @return ob der Dateizeiger am Ende steht
	 */
	public final boolean isEnde() {
		return aktuelleZeile == null;
	}

	/**
	 * Erfragt einen KZ-Datensatz aus der aktuellen Zeile der importierten
	 * Tabelle fuer den Fahrstreifen mit dem uebergebenen Index.
	 *
	 * @param index
	 *            der Index des Fahrstreifens, dessen Daten gelesen werden
	 *            sollen (1 fuer Fahrstreifen 1, 2 fuer Fahrstreifen 2 und 3
	 *            fuer Fahrstreifen 3)
	 * @return einen KZ-Datensatz aus der aktuellen Zeile der importierten
	 *         Tabelle fuer den Fahrstreifen mit dem uebergebenen Index
	 */
	public final Data getDatensatz(final int index) {
		Data datensatz = sDav.createData(
				sDav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD));

		if (datensatz != null) {

			if (aktuelleZeile != null) {
				try {
					datensatz.getTimeValue("T").setMillis(intervall); //$NON-NLS-1$
					datensatz.getUnscaledValue("ArtMittelwertbildung").set(1); //$NON-NLS-1$
					modifyAttribut("sKfz", DUAKonstanten.NICHT_ERMITTELBAR, //$NON-NLS-1$
							Constants.EMPTY_STRING, datensatz);

					final long qKfz = Integer
							.parseInt(aktuelleZeile[0 + ((index) * 2)]);
					final String qKfzStatus = aktuelleZeile[1 + 0
							+ ((index) * 2)];
					modifyAttribut("qKfz", qKfz, qKfzStatus, datensatz); //$NON-NLS-1$

					final long qPkw = Integer
							.parseInt(aktuelleZeile[6 + ((index) * 2)]);
					final String qPkwStatus = aktuelleZeile[1 + 6
							+ ((index) * 2)];
					modifyAttribut("qPkw", qPkw, qPkwStatus, datensatz); //$NON-NLS-1$

					final long qLkw = Integer
							.parseInt(aktuelleZeile[12 + ((index) * 2)]);
					final String qLkwStatus = aktuelleZeile[1 + 12
							+ ((index) * 2)];
					modifyAttribut("qLkw", qLkw, qLkwStatus, datensatz); //$NON-NLS-1$

					final long vKfz = Integer
							.parseInt(aktuelleZeile[18 + ((index) * 2)]);
					final String vKfzStatus = aktuelleZeile[1 + 18
							+ ((index) * 2)];
					modifyAttribut("vKfz", vKfz, vKfzStatus, datensatz); //$NON-NLS-1$

					final long vPkw = Integer
							.parseInt(aktuelleZeile[24 + ((index) * 2)]);
					final String vPkwStatus = aktuelleZeile[1 + 24
							+ ((index) * 2)];
					modifyAttribut("vPkw", vPkw, vPkwStatus, datensatz); //$NON-NLS-1$

					final long vLkw = Integer
							.parseInt(aktuelleZeile[30 + ((index) * 2)]);
					final String vLkwStatus = aktuelleZeile[1 + 30
							+ ((index) * 2)];
					modifyAttribut("vLkw", vLkw, vLkwStatus, datensatz); //$NON-NLS-1$

					final long vgKfz = Integer
							.parseInt(aktuelleZeile[36 + ((index) * 2)]);
					final String vgKfzStatus = aktuelleZeile[1 + 36
							+ ((index) * 2)];
					modifyAttribut("vgKfz", vgKfz, vgKfzStatus, datensatz); //$NON-NLS-1$

					final long b = Integer
							.parseInt(aktuelleZeile[42 + ((index) * 2)]);
					final String bStatus = aktuelleZeile[1 + 42
							+ ((index) * 2)];
					modifyAttribut("b", b, bStatus, datensatz); //$NON-NLS-1$

					final long tNetto = Integer
							.parseInt(aktuelleZeile[48 + ((index) * 2)]);
					final String tStatus = aktuelleZeile[1 + 48
							+ ((index) * 2)];
					modifyAttribut("tNetto", tNetto, tStatus, datensatz); //$NON-NLS-1$

				} catch (final ArrayIndexOutOfBoundsException ex) {
					datensatz = null;
				}
			} else {
				datensatz = null;
			}
		}

		return datensatz;
	}

	/**
	 * Modifiziert ein Attribut in einem Datensatz.
	 *
	 * @param attributName
	 *            Name des Attributs
	 * @param wert
	 *            Wert des Attributs
	 * @param status
	 *            Status des Attributs
	 * @param datensatz
	 *            der Datensatz
	 */
	private void modifyAttribut(final String attributName, final long wert,
			final String status, final Data datensatz) {
		int nErf = DUAKonstanten.NEIN;
		int wMax = DUAKonstanten.NEIN;
		int wMin = DUAKonstanten.NEIN;
		int wMaL = DUAKonstanten.NEIN;
		int wMiL = DUAKonstanten.NEIN;
		int impl = DUAKonstanten.NEIN;
		int intp = DUAKonstanten.NEIN;
		double guete = 1.0;

		int errCode = 0;

		if (status != null) {
			final String[] splitStatus = status.trim().split(" "); //$NON-NLS-1$

			for (final String splitStatu : splitStatus) {
				if (splitStatu.equalsIgnoreCase("Fehl")) { //$NON-NLS-1$
					errCode = errCode - 2;
				}

				if (splitStatu.equalsIgnoreCase("nErm")) { //$NON-NLS-1$
					errCode = errCode - 1;
				}

				if (splitStatu.equalsIgnoreCase("Impl")) { //$NON-NLS-1$
					impl = DUAKonstanten.JA;
				}

				if (splitStatu.equalsIgnoreCase("Intp")) { //$NON-NLS-1$
					intp = DUAKonstanten.JA;
				}

				if (splitStatu.equalsIgnoreCase("nErf")) { //$NON-NLS-1$
					nErf = DUAKonstanten.JA;
				}

				if (splitStatu.equalsIgnoreCase("wMaL")) { //$NON-NLS-1$
					wMaL = DUAKonstanten.JA;
				}

				if (splitStatu.equalsIgnoreCase("wMax")) { //$NON-NLS-1$
					wMax = DUAKonstanten.JA;
				}

				if (splitStatu.equalsIgnoreCase("wMiL")) { //$NON-NLS-1$
					wMiL = DUAKonstanten.JA;
				}

				if (splitStatu.equalsIgnoreCase("wMin")) { //$NON-NLS-1$
					wMin = DUAKonstanten.JA;
				}

				try {
					guete = Float.parseFloat(splitStatu.replace(",", ".")); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (final Exception e) {
					// kein float Wert
				}
			}
		}

		DUAUtensilien.getAttributDatum(attributName + ".Wert", datensatz) //$NON-NLS-1$
				.asUnscaledValue().set(errCode < 0 ? errCode : wert);
		DUAUtensilien.getAttributDatum(
				attributName + ".Status.Erfassung.NichtErfasst", datensatz) //$NON-NLS-1$
				.asUnscaledValue().set(nErf);
		DUAUtensilien
				.getAttributDatum(attributName + ".Status.PlFormal.WertMax", //$NON-NLS-1$
						datensatz)
				.asUnscaledValue().set(wMax);
		DUAUtensilien
				.getAttributDatum(attributName + ".Status.PlFormal.WertMin", //$NON-NLS-1$
						datensatz)
				.asUnscaledValue().set(wMin);
		DUAUtensilien.getAttributDatum(
				attributName + ".Status.PlLogisch.WertMaxLogisch", datensatz) //$NON-NLS-1$
				.asUnscaledValue().set(wMaL);
		DUAUtensilien.getAttributDatum(
				attributName + ".Status.PlLogisch.WertMinLogisch", datensatz) //$NON-NLS-1$
				.asUnscaledValue().set(wMiL);
		DUAUtensilien.getAttributDatum(
				attributName + ".Status.MessWertErsetzung.Implausibel", //$NON-NLS-1$
				datensatz).asUnscaledValue().set(impl);
		DUAUtensilien.getAttributDatum(
				attributName + ".Status.MessWertErsetzung.Interpoliert", //$NON-NLS-1$
				datensatz).asUnscaledValue().set(intp);
		DUAUtensilien.getAttributDatum(attributName + ".Güte.Index", datensatz) //$NON-NLS-1$
				.asScaledValue().set(guete);
		DUAUtensilien
				.getAttributDatum(attributName + ".Güte.Verfahren", datensatz) //$NON-NLS-1$
				.asUnscaledValue().set(0);
	}

}
