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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.dua.guete.GueteException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.GanzZahl;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.FahrStreifen;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

import static de.bsvrz.dua.guete.GueteVerfahren.STANDARD;
import static de.bsvrz.dua.guete.GueteVerfahren.produkt;

/**
 * Hier findet die eigentliche Messwertersetzung statt. Die Daten werden hier
 * zwei Intervalle zwischengespeichert und messwertersetzt, sobald alle Daten
 * zur Messwertersetzung vorliegen (spätestens aber zum Beginn des nächsten
 * Intervalls)
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class LVEPruefungUndMWE extends AbstraktBearbeitungsKnotenAdapter {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * statische Verbindung zum Datenverteiler.
	 */
	public static ClientDavInterface sDav = null;

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

	private static final Debug _debug = LOGGER;
	
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
		if( sDav != null) {
			LOGGER.error("SWE wurde bereits initialisiert");
		}
		sDav = dieVerwaltung.getVerbindung();

		/**
		 * Puffere alle Fahrstreifen. Für Fahrstreifen mit Ersatzfahrstreifen, wird dieser zusätzlich angemeldet.
		 */
		for(SystemObject fsObjekt : dieVerwaltung.getSystemObjekte()) {
			FahrStreifen fs = FahrStreifen.getInstanz(fsObjekt);
			if(fs != null) {
				this.pruefungsFahrstreifen.add(fs);
				this.fsAufDatenPuffer.put(fs, new FSDatenPuffer(fs));
				if(fs.getErsatzFahrStreifen() != null) {
					this.fsAufDatenPuffer.put(
							fs.getErsatzFahrStreifen(),
							new FSDatenPuffer(fs.getErsatzFahrStreifen())
					);
				}
			}
			else {
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
						LOGGER
								.warning("Fahrstreifen zu Datensatz konnte nicht identifiziert werden: " + resultat.getObject());
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
				FahrStreifen ersatz = fahrStreifenPuffer.getFahrStreifen()
						.getErsatzFahrStreifen();
				if(ersatz != null) {
					FSDatenPuffer ersatzPuffer = this.fsAufDatenPuffer.get(ersatz);

					if(!ersatzPuffer.isAktuellDefekt()) {
						zielDatum = this.plausibilisiereNachVorschrift1(
								fahrStreifenPuffer, ersatzPuffer);
					}
					else {
						zielDatum = fahrStreifenPuffer
								.befreieAeltestesAbgelaufenesDatum();
					}
				}
				else {
					zielDatum = fahrStreifenPuffer
							.befreieAeltestesAbgelaufenesDatum();
				}
			}

			/**
			 * wenn das Zieldatum jetzt nicht leer ist, dann ist entweder ein
			 * Intervall abgelaufen und ein altes Datum wird freigegeben oder
			 * eine der beiden Plausibilisierungsvorschriften hat zugeschlagen
			 */
			if(zielDatum == null) {
				return null;
			}
			fahrStreifenPuffer.schreibeDatenFortWennNotwendig(zielDatum);
			SystemObject fsObj = fahrStreifenPuffer.getFahrStreifen().getSystemObject();
			if(MweParameter.isParameterValide(fsObj)) {
				MweParameter parameter = MweParameter.getParameter(fsObj);
				long ersetzungsDauer = zielDatum.getErsetzungsDauer();
				long maxErsetzungsDauer = parameter.getMaxErsetzungsDauer();

				if(ersetzungsDauer > maxErsetzungsDauer) {
					// Maximale Ersetzungsdauer überschritten, Originaldatum weiterverschicken
					zielDatum = zielDatum.getOriginalDatum();
				}
			}
			else {
				// Kein Parameter vorhanden, Originaldatum weiterverschicken
				zielDatum = zielDatum.getOriginalDatum();
			}
			zielDatum.setBereitsWiederFreigegeben(true);
			fahrStreifenPuffer.ersetzeDatum(zielDatum);
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

		final GanzZahl sweGueteWert = GanzZahl.getGueteIndex();
		sweGueteWert.setSkaliertenWert(0.95);
		final GWert sweGuete = new GWert(sweGueteWert, STANDARD, false);
		
		if (zielDatum != null) {
			KZDatum ersatzZielDatum = null;
			if(ersatzPuffer != null && zielDatum.getDatum() != null) {
				ersatzZielDatum = ersatzPuffer
					.getDatumMitZeitStempel(zielDatum.getDatum().getDataTime());
			}
			if (ersatzZielDatum != null) {
				/**
				 * Es sind alle Daten zur Berechnung des Zielintervalls da!
				 */
				ergebnisDatum = new KZDatum(zielDatum.getDatum());
				
				if(ersatzZielDatum.isVollstaendigPlausibelUndNichtInterpoliert()
						&& ersatzZielDatum.getT() == ergebnisDatum.getT()) {
					
					for(MweAttribut attribut : MweAttribut.getInstanzen()) {
						MweAttributWert attributWert = ersatzZielDatum.getAttributWert(attribut);
						if(!ersatzZielDatum.istBereitsGueteReduziert()) {
							try {
								attributWert.setGuete(produkt(attributWert.getGuete(), sweGuete));
							}
							catch(GueteException e) {
								_debug.warning("Kann Güte nicht reduzieren", e);
							}
						}
						attributWert.setInterpoliert(true);
						ergebnisDatum.setAttributWert(attributWert);
					}
					ergebnisDatum.setErsetzungsDauer(fahrStreifenPuffer.getVorgaengerVon(zielDatum).getErsetzungsDauer() + ergebnisDatum.getT());
				}
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

	public ModulTyp getModulTyp() {
		return null;
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// hier findet keine Publikation statt
	}

}
