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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Assert;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mwelve.mwelve.MessWertErsetzungLVE;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;

/**
 * Test nach PrSpez.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class MessWertErsetzungLVETest1 {

	/**
	 * Soll das JUnit-Assertion-Framewort benutzt und die Applikation beendet
	 * werden, wenn ein Fehler aufgetreten ist.
	 */
	private static final boolean USE_ASSERTION = false;

	/**
	 * Verbose.
	 */
	private static final boolean VERBOSE = false;

	/**
	 * letzte empfangene Antwort.
	 */
	private ResultData antwort = null;

	/**
	 * Testet die die SWE 4.5 nach PrSpez.
	 *
	 * @throws Exception
	 *             wird weitergereicht
	 */
	@org.junit.Test
	public void testAlles() throws Exception {

		/**
		 * Initialisierung
		 */
		final ClientDavInterface dav = Initialisierung.getVerwaltung()
				.getVerbindung();
		final MessWertErsetzungLVE testModul = Initialisierung.getMWE();

		/**
		 * Anmeldung
		 */
		final SystemObject fahrstreifen1 = dav.getDataModel()
				.getObject("Fahrstreifen1"); //$NON-NLS-1$
		final DataDescription ddAntwort = new DataDescription(
				dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD),
				dav.getDataModel()
						.getAspect(DUAKonstanten.ASP_MESSWERTERSETZUNG));
		dav.subscribeReceiver(new ClientReceiverInterface() {

			@Override
			public void update(final ResultData[] results) {
				if (results != null) {
					for (final ResultData result : results) {
						if ((result != null) && (result.getData() != null)) {
							antwort = result;
							synchronized (dav) {
								dav.notifyAll();
							}
						}
					}
				}
			}

		}, new SystemObject[] { fahrstreifen1 }, ddAntwort,
		ReceiveOptions.normal(), ReceiverRole.receiver());

		/**
		 * Hole Daten
		 */
				final Vergleich vergleich = new Vergleich(dav,
						Initialisierung.INPUT_CSV, Initialisierung.OUTPUT_CSV);

		/**
		 * eigentlicher Test
		 */
						final GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(0);
		ResultData[] frage = null;

		while ((frage = vergleich.getFrage(cal.getTimeInMillis())) != null) {
			cal.add(Calendar.MINUTE, 1);

			final ResultData[] dummyFrage = frage;
			new Thread() {

				@Override
				public void run() {
					testModul.aktualisiereDaten(dummyFrage);
				}

			}.start();

			synchronized (dav) {
				if (VERBOSE) {
					for (final ResultData f : frage) {
						System.out.println("Sende: " + f); //$NON-NLS-1$
					}
				}
				dav.wait();
				if (VERBOSE) {
					System.out.println("Empfange: " + antwort); //$NON-NLS-1$
				}
			}

			final Vergleich.Ergebnis ergebnis = vergleich
					.getErgebnis(new ResultData[] { antwort });

			if (USE_ASSERTION) {
				Assert.assertTrue(ergebnis.toString(),
						ergebnis.passtZusammen());
			} else {
				if (ergebnis.passtZusammen()) {
					System.out.println(ergebnis.toString() + ": OK"); //$NON-NLS-1$
				} else {
					System.out.println("Fehler:"); //$NON-NLS-1$
					if (!VERBOSE) {
						for (final ResultData f : frage) {
							System.out.println("Sende: " + f); //$NON-NLS-1$
						}
						System.out.println("Empfange: " + antwort); //$NON-NLS-1$
					}
					System.out.println(ergebnis.toString());
				}
			}
		}

		dav.disconnect(false, "Alles ok."); //$NON-NLS-1$
	}

}
