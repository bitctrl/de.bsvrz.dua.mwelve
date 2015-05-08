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

import java.text.SimpleDateFormat;
import java.util.Date;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;

/**
 * Liest alle Eingangs- und Ausgangsdaten fuer den Test nach PrSpez und
 * Vergleicht die Ergebnisse.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class Vergleich {

	/**
	 * Fahrstreifen1.
	 */
	private SystemObject fahrstreifen1 = null;

	/**
	 * Fahrstreifen2.
	 */
	private SystemObject fahrstreifen2 = null;

	/**
	 * Fahrstreifen3.
	 */
	private SystemObject fahrstreifen3 = null;

	/**
	 * alle hier betrachteten Fahrstreifen.
	 */
	private SystemObject[] fahrstreifen = null;

	/**
	 * Importer fuer die Eingangsdaten.
	 */
	private TestFahrstreifenImporter inputImporter = null;

	/**
	 * Importer fuer die erwarteten Ausgangsdaten.
	 */
	private TestFahrstreifenImporter outputImporter = null;

	/**
	 * Datenbeschreibung der logisch ueberprueften LVE-Daten.
	 */
	private DataDescription ddPlLog = null;

	/**
	 * Datenbeschreibung der messwertersetzten LVE-Daten..
	 */
	private DataDescription ddPlMwe = null;

	/**
	 * letzte erwartete Ergebnisse.
	 */
	private ResultData[] erwarteteErgebnisse = null;

	/**
	 * Informationen zu den Eingabedateien.
	 */
	private String dateiInfo = null;

	/**
	 * Standardkonstruktor.
	 *
	 * @param dav
	 *            Datenverteiler-Verbindung
	 * @param inputCsv
	 *            Dateiname der CSV-Datei mit den Eingangsdaten
	 * @param outputCsv
	 *            Dateiname der CSV-Datei mit den erwarteten Ausgangsdaten
	 * @throws Exception
	 *             wird weitergereicht
	 */
	public Vergleich(final ClientDavInterface dav, final TestDatei inputCsv,
			final TestDatei outputCsv) throws Exception {
		fahrstreifen1 = dav.getDataModel().getObject("Fahrstreifen1"); //$NON-NLS-1$
		fahrstreifen2 = dav.getDataModel().getObject("Fahrstreifen2"); //$NON-NLS-1$
		fahrstreifen3 = dav.getDataModel().getObject("Fahrstreifen3"); //$NON-NLS-1$
		fahrstreifen = new SystemObject[] { fahrstreifen1, fahrstreifen2,
				fahrstreifen3 };
		TestFahrstreifenImporter.setT(Initialisierung.INTERVALL_LAENGE);
		inputImporter = new TestFahrstreifenImporter(dav,
				Initialisierung.WURZEL + inputCsv.getDateiName());
		outputImporter = new TestFahrstreifenImporter(dav,
				Initialisierung.WURZEL + outputCsv.getDateiName());
		dateiInfo = "Eingabe: \"" + inputCsv.getAlias() + "\", Soll-Werte: \"" //$NON-NLS-1$//$NON-NLS-2$
				+ outputCsv.getAlias() + "\""; //$NON-NLS-1$
		ddPlLog = new DataDescription(
				dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				dav.getDataModel()
				.getAspect(DUAKonstanten.ASP_PL_PRUEFUNG_LOGISCH));
		ddPlMwe = new DataDescription(
				dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				dav.getDataModel()
				.getAspect(DUAKonstanten.ASP_MESSWERTERSETZUNG));
	}

	/**
	 * Indikator für Anfang.
	 */
	private static int a = 0;

	/**
	 * Erfragt eine Menge von Eingangdatensaetzen fuer den Test fuer alle hier
	 * behandelten Fahrstreifen (mit dem uebergebenen Zeitstempel).
	 *
	 * @param zeitStempel
	 *            der geforderte Zeitstempel der Daten
	 * @return eine Menge von Eingangdatensaetzen fuer den Test fuer alle hier
	 *         behandelten Fahrstreifen (mit dem uebergebenen Zeitstempel)
	 */
	public final ResultData[] getFrage(final long zeitStempel) {
		ResultData[] frage = null;

		if (inputImporter.next() && outputImporter.next()) {
			erwarteteErgebnisse = new ResultData[3];
			frage = new ResultData[3];

			for (int i = 0; i < 3; i++) {
				erwarteteErgebnisse[i] = new ResultData(fahrstreifen[i],
						ddPlMwe, zeitStempel, outputImporter.getDatensatz(i));

				frage[i] = new ResultData(fahrstreifen[i], ddPlLog, zeitStempel,
						inputImporter.getDatensatz(i));
			}
		}

		if (a++ == 0) {
			final ResultData[] reverse = new ResultData[3];
			reverse[0] = frage[2];
			reverse[1] = frage[1];
			reverse[2] = frage[0];
			return reverse;
		}
		return frage;
	}

	/**
	 * Erfragt das Ergebnis des Vergleichs von tatsaechlichem Ergebnis und
	 * erwartetem Ergebnis.
	 *
	 * @param ergebnisse
	 *            die tatsaechlich eingetroffenen Ergebnisse
	 * @return das Ergebnis des Vergleichs von tatsaechlichem Ergebnis und
	 *         erwartetem Ergebnis
	 */
	public final Ergebnis getErgebnis(final ResultData[] ergebnisse) {
		return new Ergebnis(inputImporter.getZeilenNummer() + 1, dateiInfo,
				erwarteteErgebnisse, ergebnisse);
	}

	/**
	 * Ergebnis der Ueberpruefung von Soll- und Ist-Wert.
	 *
	 * @author BitCtrl Systems GmbH, Thierfelder
	 *
	 */
	protected static class Ergebnis {

		/**
		 * indiziert, dass Soll- und Ist-Wert uebereinstimmen.
		 */
		private boolean passt = false;

		/**
		 * illustriert die fehlerhafte Uebereinstimmung.
		 */
		private String info = null;

		/**
		 * Standardkonstruktor.
		 *
		 * @param zeilenNummer
		 *            die Zeilennummer in der CSV-Eingabe-Datei in der der
		 *            Fehler aufgetacht ist
		 * @param dateiInfo
		 *            Informationen zu den Test-Dateien
		 * @param erwarteteErgebnisse
		 *            Menge von erwarteten <code>ResultData</code>-Datensaetzen
		 * @param ergebnisse
		 *            Menge von tatsaechlich erhaltenen <code>ResultData</code>
		 *            -Datensaetzen
		 */
		protected Ergebnis(final int zeilenNummer, final String dateiInfo,
				final ResultData[] erwarteteErgebnisse,
				final ResultData[] ergebnisse) {

			final SimpleDateFormat dateFormat = new SimpleDateFormat(
					DUAKonstanten.ZEIT_FORMAT_GENAU_STR);

			for (final ResultData ist : ergebnisse) {
				for (final ResultData soll : erwarteteErgebnisse) {
					if (ist.getObject().equals(soll.getObject())) {
						final KurzZeitDatum kzdSoll = new KurzZeitDatum(soll);
						final KurzZeitDatum kzdIst = new KurzZeitDatum(ist);
						passt = (soll.getDataTime() == ist.getDataTime())
								&& kzdSoll.equals(kzdIst);

						info = "Zeile: " + zeilenNummer + " (" + dateiInfo //$NON-NLS-1$ //$NON-NLS-2$
								+ ")"; //$NON-NLS-1$
						if (!passt) {
							info += "\nSoll != Ist\n" + kzdSoll.compare(kzdIst); //$NON-NLS-1$
							info += "\nSoll-Zeit: " + dateFormat //$NON-NLS-1$
									.format(new Date(soll.getDataTime()));
							info += "\nIst-Zeit:  " + dateFormat //$NON-NLS-1$
									.format(new Date(ist.getDataTime()));
						}
						break;
					}
				}
			}
		}

		/**
		 * Erfragt, ob Soll- und Ist-Wert uebereinstimmen.
		 *
		 * @return ob Soll- und Ist-Wert uebereinstimmen
		 */
		protected final boolean passtZusammen() {
			return passt;
		}

		@Override
		public String toString() {
			return info;
		}

	}
}
