/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.5 Messwertersetzung LVE
 * Copyright (C) 2007 BitCtrl Systems GmbH 
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.FahrStreifen;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Hier findet die eigentliche Messwertersetzung statt. Die Daten werden hier
 * zwei Intervalle zwischengespeichert und messwertersetzt, sobald alle Daten
 * zur Messwertersetzung vorliegen (spätestens aber zum Beginn des nächsten
 * Intervalls)
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class LVEPruefungUndMWE extends AbstraktBearbeitungsKnotenAdapter {

	/**
	 * statische Verbindung zum Datenverteiler.
	 */
	public static ClientDavInterface sDav = null;

	/**
	 * Alle Attribute, die in Vorschrift 1.1 verarbeitet werden.<br>
	 * <code>qKfz</code>,<br>
	 * <code>qPkw</code>,<br>
	 * <code>vKfz</code> und<br>
	 * <code>vPkw</code>
	 */
	private static final MweAttribut[] Q_V_PKW_KFZ_LKW = new MweAttribut[] {
			MweAttribut.Q_KFZ, MweAttribut.Q_PKW, MweAttribut.V_KFZ,
			MweAttribut.V_PKW, MweAttribut.Q_LKW, MweAttribut.V_LKW };

	/**
	 * Menge aller Fahrstreifen, die sowohl einen Nachbar- wie auch einen
	 * Ersatzfahrstreifen besitzen und somit hier plausibilisiert werden.
	 */
	private Set<FahrStreifen> pruefungsFahrstreifen = new HashSet<FahrStreifen>();

	/**
	 * Mapt alle (hier relevanten) Objekte vom Typ <code>FahrStreifen</code>
	 * auf deren Puffer-Objekte. Relevant sind sowohl die Objekte, die hier
	 * direkt plausibilisiert werden, wie auch deren Nachbar- bzw.
	 * Ersatzfahrstreifen
	 */
	private Map<FahrStreifen, FSDatenPuffer> fsAufDatenPuffer = new HashMap<FahrStreifen, FSDatenPuffer>();

	/**
	 * Assoziiert einen Fahrstreifen <code>a</code> mit den Fahrstreifen
	 * <code>x</code>, an die er über die Relationen
	 * <code>a = istNachbarVon(x)</code>, <code>a = istErsatzVon(x)</code>
	 * und <code>Identitaet</code> gebunden ist.
	 */
	private Map<FahrStreifen, Collection<FahrStreifen>> triggerListe = new HashMap<FahrStreifen, Collection<FahrStreifen>>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		sDav = dieVerwaltung.getVerbindung();

		/**
		 * Puffere alle Fahrstreifen, die sowohl einen Nachbar- als auch einen
		 * Ersatzfahrtstreifen haben (und deren Nachbar- und
		 * Ersatzfahrtstreifen)
		 */
		for (SystemObject fsObjekt : dieVerwaltung.getSystemObjekte()) {
			FahrStreifen fs = FahrStreifen.getInstanz(fsObjekt);
			if (fs != null) {
				if (fs.getErsatzFahrStreifen() != null /**
														 * &&
														 * fs.getNachbarFahrStreifen() !=
														 * null
														 */
				) {
					this.pruefungsFahrstreifen.add(fs);
					this.fsAufDatenPuffer.put(fs, new FSDatenPuffer(fs));
					/**
					 * this.fsAufDatenPuffer.put(fs.getNachbarFahrStreifen(),
					 * new FSDatenPuffer(fs.getNachbarFahrStreifen()));
					 */
					this.fsAufDatenPuffer.put(fs.getErsatzFahrStreifen(),
							new FSDatenPuffer(fs.getErsatzFahrStreifen()));
				}
			} else {
				throw new DUAInitialisierungsException(
						"Fahrstreifenkonfiguration von " + fsObjekt + //$NON-NLS-1$
								" konnte nicht ausgelesen werden"); //$NON-NLS-1$
			}
		}

		/**
		 * Erstelle die Trigger-Liste mit Verweisen von einem Fahrstreifen a auf
		 * eine Menge von Fahrstreifen x, die bei der Ankunft eines Datums für a
		 * unter Umständen alle Daten zur Berechung dieses Intervalls haben<br>
		 * Insbesondere triggert jeder Fahrstreifen sich selbst und die
		 * Fahrstreifen, für die er entweder Nachbar- oder Ersatzfahrstreifen
		 * ist
		 */
		for (FahrStreifen fs : this.pruefungsFahrstreifen) {

			/**
			 * jeder Fahrstreifen triggert sich selbst
			 */
			Collection<FahrStreifen> triggerFuerFS = this.triggerListe.get(fs);
			if (triggerFuerFS == null) {
				triggerFuerFS = new HashSet<FahrStreifen>();
				triggerFuerFS.add(fs);
				this.triggerListe.put(fs, triggerFuerFS);
			} else {
				triggerFuerFS.add(fs);
			}

			// /**
			// * jeder Fahrstreifen triggert die Fahrstreifen, für die er
			// Nachbar ist
			// */
			// FahrStreifen nachbar = fs.getNachbarFahrStreifen();
			//			
			// Collection<FahrStreifen> triggerFuerNachbar =
			// this.triggerListe.get(nachbar);
			// if(triggerFuerNachbar == null){
			// triggerFuerNachbar = new HashSet<FahrStreifen>();
			// triggerFuerNachbar.add(fs);
			// this.triggerListe.put(nachbar, triggerFuerNachbar);
			// }else{
			// triggerFuerNachbar.add(fs);
			// }

			/**
			 * jeder Fahrstreifen trigger die Fahrstreifen, für die er
			 * Ersatzfahrstreifen ist
			 */
			FahrStreifen ersatz = fs.getErsatzFahrStreifen();

			Collection<FahrStreifen> triggerFuerErsatz = this.triggerListe
					.get(ersatz);
			if (triggerFuerErsatz == null) {
				triggerFuerErsatz = new HashSet<FahrStreifen>();
				triggerFuerErsatz.add(fs);
				this.triggerListe.put(ersatz, triggerFuerErsatz);
			} else {
				triggerFuerErsatz.add(fs);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		if (resultate != null) {
			/**
			 * die weiterzuleitenden Resultate müssen die selbe
			 * Datenidentifikation haben wie die eingetroffenen Resultate
			 */
			List<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();

			for (ResultData resultat : resultate) {
				if (resultat != null) {
					FahrStreifen fs = FahrStreifen.getInstanz(resultat
							.getObject());
					if (fs != null) {

						/**
						 * Aktualisiere den Datenpuffer des Fahrstreifens
						 */
						FSDatenPuffer pufferFS = this.fsAufDatenPuffer.get(fs);
						if (pufferFS != null) {
							KZDatum kzDatum = new KZDatum(resultat);
							pufferFS.aktualisiereDaten(kzDatum);

							if (!this.pruefungsFahrstreifen.contains(fs)
									|| kzDatum.isDefekt()
									|| kzDatum.isVollstaendigPlausibel()) {
								/**
								 * Wenn das Datum zu einem Fahrstreifen gehört,
								 * der hier nur gespeichert wird, weil er
								 * Ersatz- oder Nachbarfahrstreifen eines hier
								 * plausibilisierten Fahrstreifens ist (er hier
								 * aber nicht plausibilisiert wird), oder der
								 * Datensatz keine Nutzdaten enhält, kann er
								 * einfach so wieder freigegeben werden
								 */
								weiterzuleitendeResultate.add(resultat);
								kzDatum.setBereitsWiederFreigegeben(true);
							}

							/**
							 * Versuche eine Berechnung an allen mit dem
							 * Fahrstreifen assoziierten Fahrstreifen
							 */
							for (FahrStreifen triggerFS : this.triggerListe
									.get(fs)) {
								pufferFS = this.fsAufDatenPuffer.get(triggerFS);
								ResultData plausibilisiertesDatum = plausibilisiere(pufferFS);
								if (plausibilisiertesDatum != null) {
									/**
									 * baue Datum mit alter Datenidentifikation
									 * zusammen
									 */
									ResultData weiterzuleitendesResultat = new ResultData(
											triggerFS.getSystemObject(),
											resultat.getDataDescription(),
											plausibilisiertesDatum
													.getDataTime(),
											plausibilisiertesDatum.getData());
									weiterzuleitendeResultate
											.add(weiterzuleitendesResultat);
								}
							}
						} else {
							/**
							 * Fahrstreifen wird hier nicht betrachtet
							 */
							weiterzuleitendeResultate.add(resultat);
						}

					} else {
						Debug.getLogger()
								.warning("Fahrstreifen-Datum zu " + resultat.getObject() + //$NON-NLS-1$
										" konnte nicht extrahiert werden"); //$NON-NLS-1$
						weiterzuleitendeResultate.add(resultat);
					}
				}
			}

			/**
			 * Daten und ggf. Ergebnisse an den nächsten Bearbeitungsknoten
			 * weiterleiten
			 */
			if (this.knoten != null && !weiterzuleitendeResultate.isEmpty()) {
				this.knoten.aktualisiereDaten(weiterzuleitendeResultate
						.toArray(new ResultData[0]));
			}
		}
	}

	/**
	 * Hier findet die MWE für einen Fahrstreifen in Bezug auf die aktuell im
	 * Datenpuffer enthaltenen Daten statt. Diese Methode muss gewährleisten,
	 * dass die eintreffenden Daten entweder sofort oder innerhalb des nächsten
	 * Intervalls wieder freigegeben werden.
	 * 
	 * @param fahrStreifenPuffer
	 *            ein Fahrstreifenpuffer
	 * @return ein messwertersetztes Datum
	 */
	private ResultData plausibilisiere(FSDatenPuffer fahrStreifenPuffer) {
		KZDatum zielDatum = null;

		if (fahrStreifenPuffer.habeNochNichtFreigegebenesDatum()) {
			/**
			 * Betrachte nur Puffer, die empfangene Datensätze gespeichert
			 * haben, für die noch keine assoziierten Datensätze unter dem
			 * Aspekt MWE publiziert wurden
			 */
			if (fahrStreifenPuffer.isIntervallAbgelaufen()) {
				/**
				 * gebe hier das Datum nur frei und mache nichts anderes
				 */
				zielDatum = fahrStreifenPuffer
						.befreieAeltestesAbgelaufenesDatum();
			} else {
				/**
				 * das Intervall ist also nicht abgelaufen.
				 */
				// FahrStreifen nachbar =
				// fahrStreifenPuffer.getFahrStreifen().getNachbarFahrStreifen();
				FahrStreifen ersatz = fahrStreifenPuffer.getFahrStreifen()
						.getErsatzFahrStreifen();
				// FSDatenPuffer nachbarPuffer =
				// this.fsAufDatenPuffer.get(nachbar);
				FSDatenPuffer ersatzPuffer = this.fsAufDatenPuffer.get(ersatz);

				if (!ersatzPuffer.isAktuellDefekt()) {
					zielDatum = this.plausibilisiereNachVorschrift1(
							fahrStreifenPuffer, ersatzPuffer);
				} else {
					zielDatum = fahrStreifenPuffer
							.befreieAeltestesAbgelaufenesDatum();
				}
			}

			/**
			 * wenn das Zieldatum jetzt nicht leer ist, dann ist entweder ein
			 * Intervall abgelaufen und ein altes Datum wird freigegeben oder
			 * eine der beiden Plausibilisierungsvorschriften hat zugeschlagen
			 */
			if (zielDatum != null) {
				fahrStreifenPuffer.schreibeDatenFortWennNotwendig(zielDatum);
				zielDatum.setBereitsWiederFreigegeben(true);
				fahrStreifenPuffer.ersetzeDatum(zielDatum);
			}
		}

		return zielDatum == null ? null : zielDatum.getDatum();
	}

	/**
	 * Führt eine MWE durch für den Fall, dass der Ersatzfahrstreifen nicht
	 * defekt ist.
	 * 
	 * @param fahrStreifenPuffer
	 *            Datenpuffer des Fahrstreifens, der plausibibilisiert werden
	 *            soll
	 * @param ersatzPuffer
	 *            Datenpuffer des Ersatzfahrstreifens
	 * @return das plausibilisierte Datum des Fahrstreifens oder
	 *         <code>null</code>, wenn nicht alle Daten zur Berechnung
	 *         vorhanden sind
	 */
	private KZDatum plausibilisiereNachVorschrift1(
			FSDatenPuffer fahrStreifenPuffer, FSDatenPuffer ersatzPuffer) {
		KZDatum ergebnisDatum = null;
		KZDatum zielDatum = fahrStreifenPuffer.getDatumAktuell();

		if (zielDatum != null) {
			KZDatum ersatzZielDatum = ersatzPuffer
					.getDatumMitZeitStempel(zielDatum.getDatum().getDataTime());
			if (ersatzZielDatum != null) {
				/**
				 * Es sind alle Daten zur Berechnung des Zielintervalls da!
				 */
				ergebnisDatum = new KZDatum(zielDatum.getDatum());

				/**
				 * Versuche Ersetzung von qKfz, qPkw, vKfz und vPkw
				 */
				for (MweAttribut attribut : Q_V_PKW_KFZ_LKW) {
					this.ersetzeAttributWertNachVerfahren1(attribut,
							ergebnisDatum, ersatzZielDatum, fahrStreifenPuffer,
							ersatzPuffer);
				}

				// /**
				// * Ersetze qLkw
				// */
				// MweAttributWert attributWert =
				// ergebnisDatum.getAttributWert(MweAttribut.Q_LKW);
				// MweAttributWert attributWertQKfz =
				// ergebnisDatum.getAttributWert(MweAttribut.Q_KFZ);
				// MweAttributWert attributWertQKfzErsatz =
				// ersatzZielDatum.getAttributWert(MweAttribut.Q_KFZ);
				// MweAttributWert attributWertQLkwErsatz =
				// ersatzZielDatum.getAttributWert(MweAttribut.Q_LKW);
				//
				// if(attributWert.isImplausibel() &&
				// !attributWertQKfz.isImplausibel() &&
				// attributWertQKfz.getWert() >= 0 &&
				// !attributWertQKfzErsatz.isImplausibel() &&
				// attributWertQKfzErsatz.getWert() > 0 &&
				// !attributWertQLkwErsatz.isImplausibel() &&
				// attributWertQLkwErsatz.getWert() >= 0 &&
				// ergebnisDatum.getDatum().getData().getTimeValue("T").getMillis()
				// == //$NON-NLS-1$
				// ersatzZielDatum.getDatum().getData().getTimeValue("T").getMillis()){
				// //$NON-NLS-1$
				//
				// double qLkwErsatz = (double)attributWertQLkwErsatz.getWert();
				// GWert gueteQLkwErsatz = attributWertQLkwErsatz.getGuete();
				// double qKfzErsatz = (double)attributWertQKfzErsatz.getWert();
				// GWert gueteQKfzErsatz = attributWertQKfzErsatz.getGuete();
				// double qKfz = (double)attributWertQKfz.getWert();
				// GWert gueteQKfz = attributWertQKfz.getGuete();
				//
				// /**
				// * Ersatzwert berechnen
				// */
				// double neuerWert = qKfz * qLkwErsatz / qKfzErsatz;
				//
				// /**
				// * Güteberechnung des Wertes
				// */
				// GWert neueGuete =
				// GWert.getNichtErmittelbareGuete(attributWert.getGuete().getVerfahren());
				// try {
				// neueGuete = GueteVerfahren.quotient(
				// GueteVerfahren.produkt(gueteQKfz, gueteQLkwErsatz),
				// gueteQKfzErsatz
				// );
				// } catch (GueteException e) {
				// LOGGER.error("Guete fuer qLkw kann nicht ermittelt werden",
				// e); //$NON-NLS-1$
				// e.printStackTrace();
				// }
				//
				// /**
				// * Wert verändern
				// */
				// ergebnisDatum.getAttributWert(MweAttribut.Q_LKW).setWert((long)(neuerWert
				// + 0.5));
				// ergebnisDatum.getAttributWert(MweAttribut.Q_LKW).setGuete(neueGuete);
				// ergebnisDatum.getAttributWert(MweAttribut.Q_LKW).setInterpoliert(true);
				// }

				// /**
				// * Ersetze vLkw
				// */
				// MweAttributWert vLkw =
				// ergebnisDatum.getAttributWert(MweAttribut.V_LKW);
				// MweAttributWert vLkwErsatz =
				// ersatzZielDatum.getAttributWert(MweAttribut.V_LKW);
				//
				// if(vLkw.isImplausibel() &&
				// !vLkwErsatz.isImplausibel() &&
				// vLkwErsatz.getWert() >= 0 &&
				// ergebnisDatum.getDatum().getData().getTimeValue("T").getMillis()
				// == //$NON-NLS-1$
				// ersatzZielDatum.getDatum().getData().getTimeValue("T").getMillis()){
				// //$NON-NLS-1$
				// ergebnisDatum.getAttributWert(MweAttribut.V_LKW).setWert(
				// ersatzZielDatum.getAttributWert(MweAttribut.V_LKW).getWert());
				// ergebnisDatum.getAttributWert(MweAttribut.V_LKW).setGuete(
				// ersatzZielDatum.getAttributWert(MweAttribut.V_LKW).getGuete());
				// ergebnisDatum.getAttributWert(MweAttribut.V_LKW).setInterpoliert(true);
				// ergebnisDatum.getAttributWert(MweAttribut.V_LKW).setImplausibel(false);
				// }
			}
		}

		return ergebnisDatum;
	}

	/**
	 * Führt eine Messwertersetzung nach Vorschrift Nr. 1 für ein bestimmtes
	 * Attribut innerhalb eines KZ-Datensatzes durch (Stand: 05.03.2008)
	 * 
	 * @param attribut
	 *            das Attribut, für das die MWE durchgeführt werden soll
	 * @param ergebnisDatum
	 *            das Datum, innerhalb dem der Attributwert mit dem zu
	 *            ersetzenden Datum steht
	 * @param ersetzungsDatum
	 *            das Datum, durch das das zu ersetzenden Datum ersetzt werden
	 *            soll
	 * @param fahrStreifenPuffer
	 *            der Datenpuffer des Fahrstreifens, der MW-ersetzt werden soll
	 * @param ersetzungsPuffer
	 *            der Datenpuffer des Fahrstreifens, durch den die Ersetzung
	 *            durchgeführt werden soll
	 * @return eine veränderte Version von <code>ergebnisDatum</code>, für
	 *         die ggf. der Wert des Attributs <code>attribut</code> ersetzt
	 *         und gekennzeichnet wurde
	 */
	private KZDatum ersetzeAttributWertNachVerfahren1(
			MweAttribut attribut, KZDatum ergebnisDatum,
			KZDatum ersetzungsDatum, FSDatenPuffer fahrStreifenPuffer,
			FSDatenPuffer ersetzungsPuffer) {
		MweAttributWert attributWert = ergebnisDatum.getAttributWert(attribut);
		MweAttributWert attributWertErsetzung = ersetzungsDatum
				.getAttributWert(attribut);

		if (attributWert.isImplausibel()
				&& !attributWertErsetzung.isInterpoliert()
				&& !attributWertErsetzung.isImplausibel()
				&& !attributWertErsetzung.isFormalMax()
				&& !attributWertErsetzung.isFormalMin()
				&& !attributWertErsetzung.isLogischMax()
				&& !attributWertErsetzung.isLogischMin()
				&& attributWertErsetzung.getWert() >= 0) {

			double wertErsetzung = (double) attributWertErsetzung.getWert();
			GWert gueteErsetzung = attributWertErsetzung.getGuete();

			/**
			 * ggf. Intervallanpassung
			 */
			if (attribut.isQWert()
					&& ergebnisDatum.getDatum().getData()
							.getTimeValue("T").getMillis() != //$NON-NLS-1$
					ersetzungsDatum.getDatum().getData()
							.getTimeValue("T").getMillis()) { //$NON-NLS-1$
				double faktor = ergebnisDatum.getDatum().getData()
						.getTimeValue("T").getMillis() / //$NON-NLS-1$
						ersetzungsDatum.getDatum().getData()
								.getTimeValue("T").getMillis(); //$NON-NLS-1$
				wertErsetzung = wertErsetzung * faktor;
			}

			/**
			 * Wertberechnung
			 */
			double neuerWert = wertErsetzung;

			/**
			 * Güteberechnung des Wertes
			 */
			GWert neueGuete = gueteErsetzung;

			/**
			 * Wert verändern
			 */
			ergebnisDatum.getAttributWert(attribut).setWert(
					Math.round(neuerWert));
			ergebnisDatum.getAttributWert(attribut).setGuete(neueGuete);
			ergebnisDatum.getAttributWert(attribut).setInterpoliert(true);
			ergebnisDatum.getAttributWert(attribut).setImplausibel(false);
			ergebnisDatum.getAttributWert(attribut).setFormalMax(false);
			ergebnisDatum.getAttributWert(attribut).setFormalMin(false);
			ergebnisDatum.getAttributWert(attribut).setLogischMax(false);
			ergebnisDatum.getAttributWert(attribut).setLogischMin(false);
			ergebnisDatum.getAttributWert(attribut).setNichtErfasst(
					attributWertErsetzung.isNichtErfasst());
		}

		return ergebnisDatum;
	}

	// Implementierung von veralteten Anforderungen
	//	
	// /**
	// * Führt eine Messwertersetzung nach Vorschrift Nr. 1 für ein bestimmtes
	// * Attribut innerhalb eines KZ-Datensatzes durch
	// *
	// * @param attribut das Attribut, für das die MWE durchgeführt werden soll
	// * @param ergebnisDatum das Datum, innerhalb dem der Attributwert mit dem
	// zu ersetzenden
	// * Datum steht
	// * @param ersetzungsDatum das Datum, durch das das zu ersetzenden Datum
	// ersetzt werden soll
	// * @param fahrStreifenPuffer der Datenpuffer des Fahrstreifens, der
	// MW-ersetzt werden soll
	// * @param ersetzungsPuffer der Datenpuffer des Fahrstreifens, durch den
	// die Ersetzung durchgeführt
	// * werden soll
	// * @return eine veränderte Version von <code>ergebnisDatum</code>, für die
	// * ggf. der Wert des Attributs <code>attribut</code> ersetzt und
	// gekennzeichnet
	// * wurde
	// */
	// private final KZDatum ersetzeAttributWertNachVerfahren1(MweAttribut
	// attribut,
	// KZDatum ergebnisDatum,
	// KZDatum ersetzungsDatum,
	// FSDatenPuffer fahrStreifenPuffer,
	// FSDatenPuffer ersetzungsPuffer){
	// MweAttributWert attributWert = ergebnisDatum.getAttributWert(attribut);
	// MweAttributWert attributWertErsetzung =
	// ersetzungsDatum.getAttributWert(attribut);
	//		
	// if(attributWert.isImplausibel() &&
	// !attributWertErsetzung.isImplausibel()&&
	// !attributWertErsetzung.isFormalMax() &&
	// !attributWertErsetzung.isFormalMin() &&
	// !attributWertErsetzung.isLogischMax() &&
	// !attributWertErsetzung.isLogischMin() &&
	// attributWertErsetzung.getWert() >= 0){
	//			
	// double wertErsetzung = (double)attributWertErsetzung.getWert();
	// GWert gueteErsetzung = attributWertErsetzung.getGuete();
	//			
	// double alterWertErsetzung = 1.0;
	// KZDatum altesDatumErsetzung =
	// ersetzungsPuffer.getVorgaengerVon(ersetzungsDatum);
	// GWert alteGueteErsetzung =
	// GWert.getMaxGueteWert(GueteVerfahren.STANDARD);
	//
	// if(altesDatumErsetzung != null &&
	// !altesDatumErsetzung.isDefekt() &&
	// !altesDatumErsetzung.getAttributWert(attribut).isImplausibel() &&
	// altesDatumErsetzung.getAttributWert(attribut).getWert() >= 0){
	//				
	// alterWertErsetzung =
	// altesDatumErsetzung.getAttributWert(attribut).getWert();
	// alteGueteErsetzung =
	// altesDatumErsetzung.getAttributWert(attribut).getGuete();
	// }
	//
	// double alterWert = 1.0;
	// KZDatum altesDatum = fahrStreifenPuffer.getVorgaengerVon(ergebnisDatum);
	// GWert alteGuete = GWert.getMaxGueteWert(GueteVerfahren.STANDARD);
	//			
	// if(altesDatum != null &&
	// !altesDatum.isDefekt() &&
	// !altesDatum.getAttributWert(attribut).isImplausibel() &&
	// altesDatum.getAttributWert(attribut).getWert() >= 0){
	// alterWert = altesDatum.getAttributWert(attribut).getWert();
	// alteGuete = altesDatum.getAttributWert(attribut).getGuete();
	// }
	//			
	// /**
	// * ggf. Intervallanpassung
	// */
	// if(attribut.isQWert() &&
	// ergebnisDatum.getDatum().getData().getTimeValue("T").getMillis() !=
	// //$NON-NLS-1$
	// ersetzungsDatum.getDatum().getData().getTimeValue("T").getMillis()){
	// //$NON-NLS-1$
	// double faktor =
	// ergebnisDatum.getDatum().getData().getTimeValue("T").getMillis() /
	// //$NON-NLS-1$
	// ersetzungsDatum.getDatum().getData().getTimeValue("T").getMillis();
	// //$NON-NLS-1$
	// if(alterWertErsetzung != 1.0){
	// alterWertErsetzung = alterWertErsetzung * faktor;
	// }
	// wertErsetzung = wertErsetzung * faktor;
	// }
	//			
	// /**
	// * Wertberechnung
	// */
	// double neuerWert = (alterWert * wertErsetzung) / alterWertErsetzung;
	//			
	// /**
	// * Güteberechnung des Wertes
	// */
	// GWert neueGuete =
	// GWert.getNichtErmittelbareGuete(GueteVerfahren.STANDARD);
	// try {
	// neueGuete = GueteVerfahren.quotient(
	// GueteVerfahren.produkt(alteGuete, gueteErsetzung),
	// alteGueteErsetzung
	// );
	// } catch (GueteException e) {
	// LOGGER.error("Guete von " + attribut + " konnte nicht ermittelt werden",
	// e); //$NON-NLS-1$ //$NON-NLS-2$
	// e.printStackTrace();
	// }
	//			
	// /**
	// * Wert verändern
	// */
	// ergebnisDatum.getAttributWert(attribut).setWert(Math.round(neuerWert));
	// ergebnisDatum.getAttributWert(attribut).setGuete(neueGuete);
	// ergebnisDatum.getAttributWert(attribut).setInterpoliert(true);
	// ergebnisDatum.getAttributWert(attribut).setImplausibel(false);
	// }
	//		
	// return ergebnisDatum;
	// }

	// /**
	// * Führt eine MWE durch für den Fall, dass der Sensor des
	// Nachbarfahrstreifens defekt ist,
	// * der Sensor des Ersatzfahrstreifen aber nicht defekt ist
	// *
	// * @param fahrStreifenPuffer Datenpuffer des Fahrstreifens, der
	// plausibibilisiert werden soll
	// * @param ersatzPuffer Datenpuffer des Ersatzfahrstreifens
	// * @return das plausibilisierte Datum des Fahrstreifens oder
	// <code>null</code>, wenn
	// * nicht alle Daten zur Berechnung vorhanden sind
	// */
	// @Deprecated
	// private final KZDatum plausibilisiereNachVorschrift2(
	// FSDatenPuffer fahrStreifenPuffer,
	// FSDatenPuffer ersatzPuffer){
	// KZDatum ergebnisDatum = null;
	// KZDatum zielDatum = fahrStreifenPuffer.getDatumAktuell();
	//		
	// if(zielDatum != null &&
	// !zielDatum.isBereitsWiederFreigegeben()){
	// /**
	// * Das heißt, das Zieldatum ist nicht vollständig plausibel und
	// * noch nicht interpoliert
	// */
	//			
	// KZDatum ersatzZielDatum =
	// ersatzPuffer.getDatumMitZeitStempel(zielDatum.getDatum().getDataTime());
	// if(ersatzZielDatum != null){
	// if(zielDatum.getDatum().getData().getTimeValue("T").getMillis() ==
	// //$NON-NLS-1$
	// ersatzZielDatum.getDatum().getData().getTimeValue("T").getMillis()){
	// //$NON-NLS-1$
	//
	// ergebnisDatum = new KZDatum(zielDatum.getDatum());
	//
	// for(MweAttribut attribut:MweAttribut.getInstanzen()){
	// if(ergebnisDatum.getAttributWert(attribut).isImplausibel() &&
	// !ersatzZielDatum.getAttributWert(attribut).isImplausibel() &&
	// ersatzZielDatum.getAttributWert(attribut).getWert() >= 0){
	// ergebnisDatum.setAttributWert(new
	// MweAttributWert(ersatzZielDatum.getAttributWert(attribut)));
	// ergebnisDatum.getAttributWert(attribut).setInterpoliert(true);
	// }
	// }
	// }
	// }
	// }
	//		
	// return ergebnisDatum;
	// }

	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// hier findet keine Publikation statt
	}

}
