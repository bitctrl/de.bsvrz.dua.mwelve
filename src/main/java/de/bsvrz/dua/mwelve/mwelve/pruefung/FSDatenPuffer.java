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

import de.bsvrz.sys.funclib.bitctrl.dua.lve.FahrStreifen;

/**
 * Analogon zu <code>ErsetzungsTabelle</code> aus der Feinspezifikation. Eine
 * Instanz dieser Klasse ist jeweils mit einem Fahrstreifen assoziiert. Für
 * diesen werden jeweils drei historische Werte in einem Ringpuffer
 * bereitgehalten. Außerdem wird für jedes in der MWE betrachtete Attribut die
 * Historie seiner Fortschreibung gespeichert
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class FSDatenPuffer {

	/**
	 * die Groesse des Ringpuffers.
	 */
	private static final int PUFFER_KAPAZITAET = 3;

	/**
	 * der Fahrstreifen, dessen Daten hier gepuffert werden.
	 */
	private FahrStreifen fs = null;

	/**
	 * die letzten drei emfangenen Datensätze im Ringpuffer.
	 */
	private final KZDatum[] ringPuffer = new KZDatum[PUFFER_KAPAZITAET];

	/**
	 * Mappt alle innerhalb der MWE betrachteten Attribute auf ihre
	 * Eigenschaften bezüglich der Fortschreibung von Messwerten.
	 */
	private final Map<MweAttribut, MweFortschreibungsAttribut> messWertFortschreibung = new HashMap<>();

	/**
	 * aktueller Index auf den Ringpuffer.
	 */
	private int ringPufferIndex = 0;

	/**
	 * Standardkonstruktor.
	 *
	 * @param fs
	 *            der Fahrstreifen, dessen Daten hier gepuffert werden sollen
	 */
	protected FSDatenPuffer(final FahrStreifen fs) {
		if (fs == null) {
			throw new NullPointerException(
					"Als Fahrstreifen wurde <<null>> uebergeben"); //$NON-NLS-1$
		}
		this.fs = fs;
		for (final MweAttribut attribut : MweAttribut.getInstanzen()) {
			messWertFortschreibung.put(attribut, new MweFortschreibungsAttribut(
					fs.getSystemObject(), attribut));
		}
	}

	/**
	 * Dieser Methode sollte alle Datensätze übergeben werden, die so
	 * veröffentlicht werden sollen. Sie überprüft in allen (innerhalb der MWE)
	 * betrachteten Attributen, ob diese ersetzt wurden und versucht sie ggf.
	 * fortzuschreiben.
	 *
	 * @param kzDatum
	 *            das zur Veröffentlichung stehende KZ-Datum
	 */
	public final void schreibeDatenFortWennNotwendig(final KZDatum kzDatum) {
		for (final MweAttribut attribut : MweAttribut.getInstanzen()) {
			final MweFortschreibungsAttribut fortschreibung = messWertFortschreibung
					.get(attribut);
			fortschreibung.schreibeGgfFort(kzDatum);
		}
	}

	/**
	 * Erfragt den unmittelbar vor dem übergebenen Datum in diesen Puffer
	 * eingespeisten Datensatz.
	 *
	 * @param kzDatum
	 *            ein Datensatz
	 * @return das Vorgängerdatum oder <code>null</code>, wenn dieses nicht
	 *         existiert
	 */
	public final KZDatum getVorgaengerVon(final KZDatum kzDatum) {
		KZDatum ergebnis = null;

		for (int i = 0; i < PUFFER_KAPAZITAET; i++) {
			if (ringPuffer[i] != null) {
				if (ringPuffer[i].getDatum().getDataTime() == kzDatum.getDatum()
						.getDataTime()) {
					ergebnis = ringPuffer[((i + PUFFER_KAPAZITAET) - 1)
					                      % PUFFER_KAPAZITAET];
					break;
				}
			}
		}

		return ergebnis;
	}

	/**
	 * Erfragt den Fahrstreifen, dessen Daten hier gepuffert werden.
	 *
	 * @return der Fahrstreifen, dessen Daten hier gepuffert werden
	 */
	public final FahrStreifen getFahrStreifen() {
		return fs;
	}

	/**
	 * Erfragt, ob in diesem Puffer Daten für mehr als ein Intervall gespeichert
	 * sind, die noch nicht wieder freigegeben wurden. Dieser Zustand indiziert,
	 * dass ein Intervall abgelaufen ist<br>
	 * Es wird angenommen, dass ein Intervall dann abgelaufen ist, wenn
	 * innerhalb dieses Puffers nicht freigegebene Daten von mehr als einem
	 * Intervall stehen
	 *
	 * @return ob in diesem Puffer Daten für mehr als ein Intervall gespeichert
	 *         sind, die noch nicht wieder freigegeben wurden
	 */
	public final boolean isIntervallAbgelaufen() {
		int nichtWiederFreiGegebenZaehler = 0;

		for (final KZDatum kzDatum : ringPuffer) {
			if (kzDatum != null) {
				if (!kzDatum.isBereitsWiederFreigegeben()) {
					nichtWiederFreiGegebenZaehler++;
				}

				if (nichtWiederFreiGegebenZaehler > 1) {
					break;
				}
			}
		}

		return nichtWiederFreiGegebenZaehler > 1;
	}

	/**
	 * Erfragt, ob in diesem Puffer wenigstens ein Datum gespeichert ist, das
	 * noch nicht wieder an ein anderes Modul weitergegeben wurde.
	 *
	 * @return ob in diesem Puffer wenigstens ein Datum gespeichert ist, das
	 *         noch nicht wieder an ein anderes Modul weitergegeben wurde
	 */
	public final boolean habeNochNichtFreigegebenesDatum() {
		for (final KZDatum kzDatum : ringPuffer) {
			if (kzDatum != null) {
				if (!kzDatum.isBereitsWiederFreigegeben()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Befreit das in diesem Puffer stehende Datum, das zu einem bereits
	 * abgelaufenen Intervall gehören muss. Dies ist das älteste Datum, das noch
	 * nicht wieder freigegeben wurde
	 *
	 * @return das in diesem Puffer stehende Datum, das zu einem bereits
	 *         abgelaufenen Intervall gehören muss
	 */
	public final KZDatum befreieAeltestesAbgelaufenesDatum() {
		KZDatum abgelaufenesDatum = null;

		for (final KZDatum kzDatum : ringPuffer) {
			if ((kzDatum != null) && !kzDatum.isBereitsWiederFreigegeben()) {
				if (abgelaufenesDatum == null) {
					abgelaufenesDatum = kzDatum;
				} else {
					if (abgelaufenesDatum.getDatum().getDataTime() > kzDatum
							.getDatum().getDataTime()) {
						abgelaufenesDatum = kzDatum;
					}
				}
			}
		}

		abgelaufenesDatum.setBereitsWiederFreigegeben(true);
		return abgelaufenesDatum;
	}

	/**
	 * Erfragt, ob dieser Fahrstreifen aktuell als <code>defekt</code>
	 * eingeschätzt wird. Er ist <code>defekt</code>, wenn das letzte Datum
	 * keine Nutzdaten enthielt, oder noch nie ein Datum angekommen ist.
	 *
	 * @return ob dieser Fahrstreifen aktuell als <code>defekt</code>
	 *         eingeschätzt wird
	 */
	public final boolean isAktuellDefekt() {
		boolean defekt = true;

		final KZDatum aktuellesDatum = getDatumAktuell();
		if (aktuellesDatum != null) {
			defekt = aktuellesDatum.isDefekt();
		}

		return defekt;
	}

	/**
	 * Aktualisiert dieses Objekt mit einem aktuellen (Roh-)Datensatz für den
	 * assoziierten Fahrstreifen<br>
	 * .
	 *
	 * @param kzDatum
	 *            ein KZD des assoziierten Fahrstreifens (muss
	 *            <code>!= null</code> sein)
	 */
	public final void aktualisiereDaten(final KZDatum kzDatum) {
		for (final MweAttribut attribut : MweAttribut.getInstanzen()) {
			final MweFortschreibungsAttribut fortschreibung = messWertFortschreibung
					.get(attribut);
			if (fortschreibung != null) {
				fortschreibung.aktualisiere(kzDatum);
			}
		}
		ringPuffer[ringPufferIndex] = kzDatum;
		ringPufferIndex = (ringPufferIndex + 1) % PUFFER_KAPAZITAET;
	}

	/**
	 * Erfragt das letzte in diesen Puffer eingespeiste Datum.
	 *
	 * @return das letzte in diesen Puffer eingespeiste Datum oder
	 *         <code>null</code>, wenn noch nie ein Datum eingespeist wurde
	 */
	public final KZDatum getDatumAktuell() {
		return ringPuffer[((ringPufferIndex + PUFFER_KAPAZITAET) - 1)
		                  % PUFFER_KAPAZITAET];
	}

	/**
	 * Erfragt das Datum innerhalb dieses Puffers, das den übergebenen
	 * Zeitstempel besitzt.
	 *
	 * @param zeitStempel1
	 *            der Zeitstempel des Datums
	 * @return das Datum innerhalb dieses Puffers, das den übergebenen
	 *         Zeitstempel besitzt oder <code>null</code>, wenn kein Datum
	 *         diesen Zeitstempel besitzt
	 */
	public final KZDatum getDatumMitZeitStempel(final long zeitStempel1) {
		KZDatum ergebnis = null;

		for (int i = 0; i < PUFFER_KAPAZITAET; i++) {
			final KZDatum dummy = ringPuffer[i];
			if (dummy != null) {
				if (dummy.getDatum().getDataTime() == zeitStempel1) {
					ergebnis = dummy;
					break;
				}
			}
		}

		return ergebnis;
	}

	/**
	 * Ersetzt ein in diesem Puffer gespeichertes Datum durch ein
	 * messwertersetztes Datum.
	 *
	 * @param mweDatum
	 *            das neue Datum
	 */
	public final void ersetzeDatum(final KZDatum mweDatum) {
		for (int i = 0; i < PUFFER_KAPAZITAET; i++) {
			final KZDatum dummy = ringPuffer[i];
			if (dummy != null) {
				if (dummy.getDatum().getDataTime() == mweDatum.getDatum()
						.getDataTime()) {
					ringPuffer[i] = mweDatum;
					break;
				}
			}
		}
	}

	@Override
	public String toString() {
		String s = fs.toString();

		int t = 0;
		for (int i = ((ringPufferIndex + PUFFER_KAPAZITAET) - 1)
				% PUFFER_KAPAZITAET; i != ringPufferIndex; i = (i + 1)
				% PUFFER_KAPAZITAET) {
			t++;
			s += "\nPuffer[T-" + t + "]:\n" //$NON-NLS-1$//$NON-NLS-2$
					+ (ringPuffer[i] == null ? "<<null>>" : ringPuffer[i]); //$NON-NLS-1$
		}
		s += "\nAttributfortschreibung:"; //$NON-NLS-1$
		for (final MweAttribut attribut : MweAttribut.getInstanzen()) {
			s += "\n" + attribut.toString() + ": " //$NON-NLS-1$//$NON-NLS-2$
					+ messWertFortschreibung.get(attribut);
		}

		return s;
	}

}
