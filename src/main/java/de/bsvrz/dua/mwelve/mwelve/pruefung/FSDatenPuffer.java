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

import de.bsvrz.dua.guete.GWert;
import de.bsvrz.dua.guete.GueteException;
import de.bsvrz.sys.funclib.bitctrl.dua.GanzZahl;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.FahrStreifen;
import de.bsvrz.sys.funclib.debug.Debug;

import static de.bsvrz.dua.guete.GueteVerfahren.STANDARD;
import static de.bsvrz.dua.guete.GueteVerfahren.produkt;

/**
 * Analogon zu <code>ErsetzungsTabelle</code> aus der Feinspezifikation. Eine
 * Instanz dieser Klasse ist jeweils mit einem Fahrstreifen assoziiert. Für
 * diesen werden jeweils drei historische Werte in einem Ringpuffer
 * bereitgehalten. Außerdem wird für jedes in der MWE betrachtete Attribut die
 * Historie seiner Fortschreibung gespeichert
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
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
	private KZDatum[] ringPuffer = new KZDatum[PUFFER_KAPAZITAET];

	/**
	 * aktueller Index auf den Ringpuffer.
	 */
	private int ringPufferIndex = 0;
	
	private static final Debug _debug = Debug.getLogger();

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
	public final void schreibeDatenFortWennNotwendig(KZDatum kzDatum) {
		if(kzDatum.isVollstaendigPlausibel()){
			return;
		}
		final GanzZahl sweGueteWert = GanzZahl.getGueteIndex();
		sweGueteWert.setSkaliertenWert(0.9);
		final GWert sweGuete = new GWert(sweGueteWert, STANDARD, false);
		
		KZDatum vorgaenger = getVorgaengerVon(kzDatum);
		if(vorgaenger == null) {
			// Daten fortschreiben nicht möglich, kein Vorgänger ermittelbar
			return;
		}
		for (MweAttribut attribut : MweAttribut.getInstanzen()) {
			MweAttributWert attributWert = new MweAttributWert(vorgaenger.getAttributWert(attribut));
			if(!vorgaenger.istBereitsGueteReduziert()) {

				try {
					attributWert.setGuete(produkt(attributWert.getGuete(), sweGuete));
				}
				catch(GueteException e) {
					_debug.warning("Kann Güte nicht reduzieren", e);
				}
			}
			attributWert.setInterpoliert(true);
			kzDatum.setAttributWert(attributWert);
		}
		
		kzDatum.setBereitsGueteReduziert(true);
		kzDatum.setErsetzungsDauer(vorgaenger.getErsetzungsDauer() + kzDatum.getT());
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
	public final KZDatum getVorgaengerVon(KZDatum kzDatum) {
		KZDatum ergebnis = null;

		for (int i = 0; i < PUFFER_KAPAZITAET; i++) {
			if (this.ringPuffer[i] != null) {
				if (this.ringPuffer[i].getDatum().getDataTime() == kzDatum
						.getDatum().getDataTime()) {
					ergebnis = this.ringPuffer[(i + PUFFER_KAPAZITAET - 1)
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
		return this.fs;
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

		for (KZDatum kzDatum : this.ringPuffer) {
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
		for (KZDatum kzDatum : this.ringPuffer) {
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

		for (KZDatum kzDatum : this.ringPuffer) {
			if (kzDatum != null && !kzDatum.isBereitsWiederFreigegeben()) {
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

		KZDatum aktuellesDatum = this.getDatumAktuell();
		if (aktuellesDatum != null) {
			defekt = aktuellesDatum.isDefekt();
		}

		return defekt;
	}

	/**
	 * Aktualisiert dieses Objekt mit einem aktuellen (Roh-)Datensatz für den
	 * assoziierten Fahrstreifen<br>.
	 * 
	 * @param kzDatum
	 *            ein KZD des assoziierten Fahrstreifens (muss
	 *            <code>!= null</code> sein)
	 */
	public final void aktualisiereDaten(KZDatum kzDatum) {
		this.ringPuffer[ringPufferIndex] = kzDatum;
		ringPufferIndex = (ringPufferIndex + 1) % PUFFER_KAPAZITAET;
	}

	/**
	 * Erfragt das letzte in diesen Puffer eingespeiste Datum.
	 * 
	 * @return das letzte in diesen Puffer eingespeiste Datum oder
	 *         <code>null</code>, wenn noch nie ein Datum eingespeist wurde
	 */
	public final KZDatum getDatumAktuell() {
		return this.ringPuffer[(ringPufferIndex + PUFFER_KAPAZITAET - 1)
				% PUFFER_KAPAZITAET];
	}

	/**
	 * Erfragt das Datum innerhalb dieses Puffers, das den übergebenen
	 * Zeitstempel besitzt.
	 * 
	 * @param zeitStempel1 der Zeitstempel des Datums
	 * @return das Datum innerhalb dieses Puffers, das den übergebenen
	 *         Zeitstempel besitzt oder <code>null</code>, wenn kein Datum
	 *         diesen Zeitstempel besitzt
	 */
	public final KZDatum getDatumMitZeitStempel(final long zeitStempel1) {
		KZDatum ergebnis = null;

		for (int i = 0; i < PUFFER_KAPAZITAET; i++) {
			KZDatum dummy = this.ringPuffer[i];
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
	public final void ersetzeDatum(KZDatum mweDatum) {
		for (int i = 0; i < PUFFER_KAPAZITAET; i++) {
			KZDatum dummy = this.ringPuffer[i];
			if (dummy != null) {
				if (dummy.getDatum().getDataTime() == mweDatum.getDatum()
						.getDataTime()) {
					this.ringPuffer[i] = mweDatum;
					break;
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String s = this.fs.toString();

		int t = 0;
		for (int i = (this.ringPufferIndex + PUFFER_KAPAZITAET - 1)
				% PUFFER_KAPAZITAET; i != this.ringPufferIndex; i = (i + 1)
				% PUFFER_KAPAZITAET) {
			t++;
			s += "\nPuffer[T-" + t + "]:\n" + (this.ringPuffer[i] == null ? "<<null>>" : this.ringPuffer[i]); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		}

		return s;
	}

}
