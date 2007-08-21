/**
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.5 Messwertersetzung LVE
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
 * Wei�enfelser Stra�e 67<br>
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

import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.dua.guete.GueteException;
import de.bsvrz.dua.guete.GueteVerfahren;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.FahrStreifen;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Hier findet die eigentliche Messwertersetzung statt. Die Daten werden hier 
 * zwei Intervalle zwischengespeichert und messwertersetzt, sobald alle Daten
 * zur Messwertersetzung vorliegen (sp�testens aber zum Beginn des n�chsten
 * Intervalls) 
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class LVEPruefungUndMWE
extends AbstraktBearbeitungsKnotenAdapter{

	/**
	 * Alle Attribute, die in Vorschrift 1.1 verarbeitet werden<br>
	 * <code>qKfz</code>,<br>
	 * <code>qPkw</code>,<br>
	 * <code>vKfz</code> und<br>
	 * <code>vPkw</code> 
	 */
	private static final MweAttribut[] Q_V_PKW_KFZ = new MweAttribut[]
						                                         {MweAttribut.Q_KFZ,
															      MweAttribut.Q_PKW,
															      MweAttribut.V_KFZ,
															      MweAttribut.V_PKW}; 

	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Menge aller Fahrstreifen, die sowohl einen Nachbar- wie auch einen
	 * Ersatzfahrstreifen besitzen und somit hier plausibilisiert werden
	 */
	private Set<FahrStreifen> pruefungsFahrstreifen = new HashSet<FahrStreifen>();

	/**
	 * Mapt alle (hier relevanten) Objekte vom Typ <code>FahrStreifen</code> auf
	 * deren Puffer-Objekte. Relevant sind sowohl die Objekte, die hier direkt
	 * plausibilisiert werden, wie auch deren Nachbar- bzw. Ersatzfahrstreifen 
	 */
	private Map<FahrStreifen, FSDatenPuffer> fsAufDatenPuffer = 
													new HashMap<FahrStreifen, FSDatenPuffer>();
	
	/**
	 * Assoziiert einen Fahrstreifen <code>a</code> mit den Fahrstreifen <code>x</code>,
	 * an die er �ber die Relationen <code>a = istNachbarVon(x)</code>, <code>a = istErsatzVon(x)</code>
	 * und <code>Identitaet</code> gebunden ist
	 */
	private Map<FahrStreifen, Collection<FahrStreifen>> triggerListe = 
										new HashMap<FahrStreifen, Collection<FahrStreifen>>();
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
	throws DUAInitialisierungsException {
		
		/**
		 * Puffere alle Fahrstreifen, die sowohl einen Nachbar-
		 * als auch einen Ersatzfahrtstreifen haben (und deren Nachbar-
		 * und Ersatzfahrtstreifen)
		 */
		for(SystemObject fsObjekt:dieVerwaltung.getSystemObjekte()){
			FahrStreifen fs = FahrStreifen.getInstanz(fsObjekt);
			if(fs != null){
				if(fs.getErsatzFahrStreifen() != null && fs.getNachbarFahrStreifen() != null){
					this.pruefungsFahrstreifen.add(fs);
					this.fsAufDatenPuffer.put(fs, new FSDatenPuffer(fs));
					this.fsAufDatenPuffer.put(fs.getNachbarFahrStreifen(), new FSDatenPuffer(fs.getNachbarFahrStreifen()));
					this.fsAufDatenPuffer.put(fs.getErsatzFahrStreifen(), new FSDatenPuffer(fs.getErsatzFahrStreifen()));
				}
			}else{
				throw new DUAInitialisierungsException("Fahrstreifenkonfiguration von " + fsObjekt +  //$NON-NLS-1$
						" konnte nicht ausgelesen werden"); //$NON-NLS-1$
			}
		}
		
		/**
		 * Erstelle die Trigger-Liste mit Verweisen von einem Fahrstreifen a 
		 * auf eine Menge von Fahrstreifen x, die bei der Ankunft eines Datums
		 * f�r a unter Umst�nden alle Daten zur Berechung dieses Intervalls haben<br>
		 * Insbesondere triggert jeder Fahrstreifen sich selbst und die Fahrstreifen,
		 * f�r die er entweder Nachbar- oder Ersatzfahrstreifen ist
		 */
		for(FahrStreifen fs:this.pruefungsFahrstreifen){
			
			/**
			 * jeder Fahrstreifen triggert sich selbst
			 */
			Collection<FahrStreifen> triggerFuerFS = this.triggerListe.get(fs);
			if(triggerFuerFS == null){
				triggerFuerFS = new HashSet<FahrStreifen>();
				triggerFuerFS.add(fs);
				this.triggerListe.put(fs, triggerFuerFS);
			}else{
				triggerFuerFS.add(fs);
			}
			
			/**
			 * jeder Fahrstreifen triggert die Fahrstreifen, f�r die er Nachbar ist
			 */
			FahrStreifen nachbar = fs.getNachbarFahrStreifen();
			
			Collection<FahrStreifen> triggerFuerNachbar = this.triggerListe.get(nachbar);
			if(triggerFuerNachbar == null){
				triggerFuerNachbar = new HashSet<FahrStreifen>();
				triggerFuerNachbar.add(fs);
				this.triggerListe.put(nachbar, triggerFuerNachbar);
			}else{
				triggerFuerNachbar.add(fs);
			}
			
			/**
			 * jeder Fahrstreifen trigger die Fahrstreifen, f�r die er Ersatzfahrstreifen ist
			 */
			FahrStreifen ersatz = fs.getErsatzFahrStreifen();

			Collection<FahrStreifen> triggerFuerErsatz = this.triggerListe.get(ersatz);
			if(triggerFuerErsatz == null){
				triggerFuerErsatz = new HashSet<FahrStreifen>();
				triggerFuerErsatz.add(fs);
				this.triggerListe.put(ersatz, triggerFuerErsatz);
			}else{
				triggerFuerErsatz.add(fs);
			}
		}		
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		if(resultate != null){
			/**
			 * die weiterzuleitenden Resultate m�ssen die selbe Datenidentifikation haben wie
			 * die eingetroffenen Resultate
			 */
			List<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();
			
			for(ResultData resultat:resultate){
				if(resultat != null){
					FahrStreifen fs = FahrStreifen.getInstanz(resultat.getObject());
					if(fs != null){

						/**
						 * Aktualisiere den Datenpuffer des Fahrstreifens
						 */
						FSDatenPuffer pufferFS = this.fsAufDatenPuffer.get(fs);
						if(pufferFS != null){
							KZDatum kzDatum = new KZDatum(resultat);
							pufferFS.aktualisiereDaten(kzDatum);

							if(!this.pruefungsFahrstreifen.contains(fs) || 
									kzDatum.isDefekt() ||
									kzDatum.isVollstaendigPlausibel()){
								/**
								 * Wenn das Datum zu einem Fahrstreifen geh�rt, der hier nur
								 * gespeichert wird, weil er Ersatz- oder Nachbarfahrstreifen
								 * eines hier plausibilisierten Fahrstreifens ist (er hier aber 
								 * nicht plausibilisiert wird), oder der Datensatz keine Nutzdaten
								 * enh�lt, kann er einfach so wieder freigegeben werden
								 */
								weiterzuleitendeResultate.add(resultat);
								kzDatum.setBereitsWiederFreigegeben(true);
							}

							/**
							 * Versuche eine Berechnung an allen mit dem Fahrstreifen 
							 * assoziierten Fahrstreifen
							 */
							for(FahrStreifen triggerFS:this.triggerListe.get(fs)){
								pufferFS = this.fsAufDatenPuffer.get(triggerFS);
								ResultData plausibilisiertesDatum = plausibilisiere(pufferFS);
								if(plausibilisiertesDatum != null){
									ResultData weiterzuleitendesResultat = new ResultData(resultat.getObject(), 
											resultat.getDataDescription(), resultat.getDataTime(), plausibilisiertesDatum.getData());
									weiterzuleitendeResultate.add(weiterzuleitendesResultat);
								}					
							}
						}else{
							/**
							 * Fahrstreifen wird hier nicht betrachtet
							 */
							weiterzuleitendeResultate.add(resultat);
						}

					}else{
						LOGGER.warning("Fahrstreifen-Datum zu " + resultat.getObject() + //$NON-NLS-1$
						" konnte nicht extrahiert werden"); //$NON-NLS-1$
						weiterzuleitendeResultate.add(resultat);
					}						
				}
			}
			
			/**
			 * Ergebnisse an den n�chsten Bearbeitungsknoten weiterleiten
			 */
			if(this.knoten != null && !weiterzuleitendeResultate.isEmpty()){
				this.knoten.aktualisiereDaten(weiterzuleitendeResultate.toArray(new ResultData[0]));
			}
		}
	}
	

	/**
	 * Hier findet die MWE f�r einen Fahrstreifen in Bezug auf die aktuell im Datenpuffer
	 * enthaltenen Daten statt. Diese Methode muss gew�hrleisten, dass die eintreffenden
	 * Daten entweder sofort oder innerhalb des n�chsten Intervalls wieder freigegeben werden.
	 * 
	 * @param fahrStreifenPuffer ein Fahrstreifenpuffer
	 * @return ein messwertersetztes Datum
	 */
	private final ResultData plausibilisiere(FSDatenPuffer fahrStreifenPuffer){
		KZDatum zielDatum = null;
			
		if(fahrStreifenPuffer.habeNochNichtFreigegebenesDatum()){
			if(fahrStreifenPuffer.isIntervallAbgelaufen()){
				/**
				 * gebe hier das Datum nur frei und mache nichts anderes
				 */			
				zielDatum = fahrStreifenPuffer.befreieAbgelaufenesDatum();
			}else{
				/**
				 * das Intervall ist also nicht abgelaufen
				 */
				FahrStreifen nachbar = fahrStreifenPuffer.getFahrStreifen().getNachbarFahrStreifen();
				FahrStreifen ersatz = fahrStreifenPuffer.getFahrStreifen().getErsatzFahrStreifen();
				FSDatenPuffer nachbarPuffer = this.fsAufDatenPuffer.get(nachbar);
				FSDatenPuffer ersatzPuffer = this.fsAufDatenPuffer.get(ersatz);
				
				if(!ersatzPuffer.isAktuellDefekt() && !nachbarPuffer.isAktuellDefekt()){
					zielDatum = this.plausibilisiereNachVorschrift1(fahrStreifenPuffer, nachbarPuffer, ersatzPuffer);
				}else
				if(!ersatzPuffer.isAktuellDefekt() && nachbarPuffer.isAktuellDefekt()){
					zielDatum = this.plausibilisiereNachVorschrift2(fahrStreifenPuffer, ersatzPuffer);
				}	
			}
	
			/**
			 * wenn das Zieldatum jetzt nicht leer ist, dann ist entweder ein
			 * Intervall abgelaufen und ein altes Datum wird freigegeben oder
			 * eine der beiden Plausibilisierungsvorschriften hat zugeschlagen 
			 */
			if(zielDatum != null){
				fahrStreifenPuffer.schreibeDatenFortWennNotwendig(zielDatum);
				zielDatum.setBereitsWiederFreigegeben(true);
				fahrStreifenPuffer.ersetzeDatum(zielDatum);
			}
		}
			
		return zielDatum == null?null:zielDatum.getDatum();
	}
	
	
	/**
	 * F�hrt eine MWE durch f�r den Fall, dass der Sensor des Nachbarfahrstreifens
	 * (bei einem dreistreifigen Abschnitt ist der linke Fahrstreifen der benachbarte des
	 * mittleren Fahrstreifens) und der Sensor des Ersatzfahrstreifen nicht defekt ist
	 * 
	 * @param fahrStreifenPuffer Datenpuffer des Fahrstreifens, der plausibibilisiert werden soll
	 * @param nachbarPuffer Datenpuffer des Nachbarfahrstreifens
	 * @param ersatzPuffer Datenpuffer des Ersatzfahrstreifens
	 * @return das plausibilisierte Datum des Fahrstreifens oder <code>null</code>, wenn
	 * nicht alle Daten zur Berechnung vorhanden sind
	 */
	private final KZDatum plausibilisiereNachVorschrift1(
								FSDatenPuffer fahrStreifenPuffer,
								FSDatenPuffer nachbarPuffer,
								FSDatenPuffer ersatzPuffer){
		KZDatum ergebnisDatum = null;
		KZDatum zielDatum = fahrStreifenPuffer.getDatumAktuell();
		
		if(zielDatum != null){	
			KZDatum nachbarZielDatum = nachbarPuffer.getDatumMitZeitStempel(zielDatum.getDatum().getDataTime());
			if(nachbarZielDatum != null){
				KZDatum ersatzZielDatum = ersatzPuffer.getDatumMitZeitStempel(zielDatum.getDatum().getDataTime());	
				if(ersatzZielDatum != null){
					/**
					 * Es sind alle Daten zur Berechnung des Zielintervalls da!
					 */
					try {
						ergebnisDatum = new KZDatum(zielDatum.getDatum());
						
						/**
						 * Versuche Ersetzung von qKfz, qPkw, vKfz und vPkw 
						 */
						for(MweAttribut attribut:Q_V_PKW_KFZ){
							this.ersetzeAttributWertNachVerfahren1(attribut, ergebnisDatum,
									nachbarZielDatum, fahrStreifenPuffer, nachbarPuffer);
						}
						
						/**
						 * Ersetze qLkw
						 */
						MweAttributWert attributWert = ergebnisDatum.getAttributWert(MweAttribut.Q_LKW);
						MweAttributWert attributWertQKfz = ergebnisDatum.getAttributWert(MweAttribut.Q_KFZ);
						MweAttributWert attributWertQKfzErsatz = ersatzZielDatum.getAttributWert(MweAttribut.Q_KFZ);
						MweAttributWert attributWertQLkwErsatz = ersatzZielDatum.getAttributWert(MweAttribut.Q_LKW);
						
						if(attributWert.isImplausibel() &&
						  !attributWertQKfz.isImplausibel() &&
						   attributWertQKfz.getWert() >= 0 &&
						  !attributWertQKfzErsatz.isImplausibel() &&
						   attributWertQKfzErsatz.getWert() >= 0 &&
						  !attributWertQLkwErsatz.isImplausibel() &&
						   attributWertQLkwErsatz.getWert() >= 0 &&
						   ergebnisDatum.getDatum().getData().getTimeValue("T").getMillis() == //$NON-NLS-1$
						   ersatzZielDatum.getDatum().getData().getTimeValue("T").getMillis()){ //$NON-NLS-1$
							
							double qLkwErsatz = (double)attributWertQLkwErsatz.getWert();
							GWert gueteQLkwErsatz = attributWertQLkwErsatz.getGuete();
							double qKfzErsatz = (double)attributWertQKfzErsatz.getWert();
							GWert gueteQKfzErsatz = attributWertQKfzErsatz.getGuete();
							double qKfz = (double)attributWertQKfz.getWert();
							GWert gueteQKfz = attributWertQKfz.getGuete();
							
							/**
							 * Ersatzwert berechnen
							 */
							double neuerWert = qKfz * qLkwErsatz / qKfzErsatz;
							
							/**
							 * G�teberechnung des Wertes
							 */
							GWert neueGuete = GueteVerfahren.quotient(
													GueteVerfahren.produkt(gueteQKfz, gueteQLkwErsatz),
													gueteQKfzErsatz
												);
							
							/**
							 * Wert ver�ndern
							 */
							ergebnisDatum.getAttributWert(MweAttribut.Q_LKW).setWert((long)(neuerWert + 0.5));
							ergebnisDatum.getAttributWert(MweAttribut.Q_LKW).setGuete(neueGuete);
							ergebnisDatum.getAttributWert(MweAttribut.Q_LKW).setInterpoliert(true);
						}
						
						/**
						 * Ersetze vLkw
						 */
						MweAttributWert vLkw = ergebnisDatum.getAttributWert(MweAttribut.V_LKW);
						MweAttributWert vLkwErsatz = ersatzZielDatum.getAttributWert(MweAttribut.V_LKW);
						
						if(vLkw.isImplausibel() &&
						  !vLkwErsatz.isImplausibel() &&
						   vLkwErsatz.getWert() >= 0 &&
						   ergebnisDatum.getDatum().getData().getTimeValue("T").getMillis() == //$NON-NLS-1$
						   ersatzZielDatum.getDatum().getData().getTimeValue("T").getMillis()){ //$NON-NLS-1$
							ergebnisDatum.getAttributWert(MweAttribut.V_LKW).setWert(
									ersatzZielDatum.getAttributWert(MweAttribut.V_LKW).getWert());
							ergebnisDatum.getAttributWert(MweAttribut.V_LKW).setGuete(
									ersatzZielDatum.getAttributWert(MweAttribut.V_LKW).getGuete());
							ergebnisDatum.getAttributWert(MweAttribut.V_LKW).setInterpoliert(true);
						}					
						
					} catch (GueteException e) {
						LOGGER.error(Konstante.LEERSTRING, e);
						e.printStackTrace();
					}
					
				}
			}
		}
		
		return ergebnisDatum;
	}
	
	
	/**
	 * F�hrt eine Messwertersetzung nach Vorschrift Nr. 1 f�r ein bestimmtes
	 * Attribut innerhalb eines KZ-Datensatzes durch
	 * 
	 * @param attribut das Attribut, f�r das die MWE durchgef�hrt werden soll
	 * @param ergebnisDatum das Datum, innerhalb dem der Attributwert mit dem zu ersetzenden
	 * Datum steht
	 * @param ersetzungsDatum das Datum, durch das das zu ersetzenden Datum ersetzt werden soll 
	 * @param fahrStreifenPuffer der Datenpuffer des Fahrstreifens, der MW-ersetzt werden soll
	 * @param ersetzungsPuffer der Datenpuffer des Fahrstreifens, durch den die Ersetzung durchgef�hrt
	 * werden soll
	 * @return eine ver�nderte Version von <code>ergebnisDatum</code>, f�r die
	 * ggf. der Wert des Attributs <code>attribut</code> ersetzt und gekennzeichnet
	 * wurde
	 * @throws GueteException wird weitergeleitet
	 */
	private final KZDatum ersetzeAttributWertNachVerfahren1(MweAttribut attribut,
															KZDatum ergebnisDatum, 
															KZDatum ersetzungsDatum,
															FSDatenPuffer fahrStreifenPuffer,
															FSDatenPuffer ersetzungsPuffer)
	throws GueteException{
		MweAttributWert attributWert = ergebnisDatum.getAttributWert(attribut);
		MweAttributWert attributWertErsetzung = ersetzungsDatum.getAttributWert(attribut);
		
		if(attributWert.isImplausibel() && 
		  !attributWertErsetzung.isImplausibel() &&
		   attributWertErsetzung.getWert() >= 0){
			
			double wertErsetzung = (double)attributWertErsetzung.getWert();
			GWert gueteErsetzung = attributWertErsetzung.getGuete();
			
			double alterWertErsetzung = 1.0; 
			KZDatum altesDatumErsetzung = ersetzungsPuffer.getVorgaengerVon(ersetzungsDatum);
			GWert alteGueteErsetzung = GWert.getNichtErmittelbareGuete(GueteVerfahren.STANDARD);
			if(altesDatumErsetzung != null &&
			  !altesDatumErsetzung.getAttributWert(attribut).isImplausibel() &&
			   altesDatumErsetzung.getAttributWert(attribut).getWert() >= 0){
				alterWertErsetzung = altesDatumErsetzung.getAttributWert(attribut).getWert();
				alteGueteErsetzung = altesDatumErsetzung.getAttributWert(attribut).getGuete();
			}

			double alterWert = 1.0; 
			KZDatum altesDatum = fahrStreifenPuffer.getVorgaengerVon(ergebnisDatum);
			GWert alteGuete = GWert.getNichtErmittelbareGuete(GueteVerfahren.STANDARD);
			if(altesDatum != null &&
			  !altesDatum.getAttributWert(attribut).isImplausibel() &&
			   altesDatum.getAttributWert(attribut).getWert() >= 0){
				alterWert = altesDatum.getAttributWert(attribut).getWert();
				alteGuete = altesDatum.getAttributWert(attribut).getGuete();
			}
			
			/**
			 * ggf. Intervallanpassung
			 */
			if(attribut.isQWert() && 
			   ergebnisDatum.getDatum().getData().getTimeValue("T").getMillis() !=   //$NON-NLS-1$
				   ersetzungsDatum.getDatum().getData().getTimeValue("T").getMillis()){  //$NON-NLS-1$
				double faktor = ergebnisDatum.getDatum().getData().getTimeValue("T").getMillis() /  //$NON-NLS-1$
				ersetzungsDatum.getDatum().getData().getTimeValue("T").getMillis();  //$NON-NLS-1$
				if(alterWertErsetzung != 1.0){
					alterWertErsetzung = alterWertErsetzung * faktor;
				}
				wertErsetzung = wertErsetzung * faktor;
			}
			
			/**
			 * Wertberechnung
			 */
			double neuerWert = (alterWert * wertErsetzung) / alterWertErsetzung;
			
			/**
			 * G�teberechnung des Wertes
			 */
			GWert neueGuete = GueteVerfahren.quotient(
								GueteVerfahren.produkt(alteGuete, gueteErsetzung),
								alteGueteErsetzung
							  );
			
			/**
			 * Wert ver�ndern
			 */
			ergebnisDatum.getAttributWert(attribut).setWert((long)(neuerWert + 0.5));
			ergebnisDatum.getAttributWert(attribut).setGuete(neueGuete);
			ergebnisDatum.getAttributWert(attribut).setInterpoliert(true);
		}
		
		return ergebnisDatum;
	}
	
		
	/**
	 * F�hrt eine MWE durch f�r den Fall, dass der Sensor des Nachbarfahrstreifens defekt ist,
	 * der Sensor des Ersatzfahrstreifen aber nicht defekt ist
	 * 
	 * @param fahrStreifenPuffer Datenpuffer des Fahrstreifens, der plausibibilisiert werden soll
	 * @param ersatzPuffer Datenpuffer des Ersatzfahrstreifens
	 * @return das plausibilisierte Datum des Fahrstreifens oder <code>null</code>, wenn
	 * nicht alle Daten zur Berechnung vorhanden sind  
	 */
	private final KZDatum plausibilisiereNachVorschrift2(
									FSDatenPuffer fahrStreifenPuffer,
									FSDatenPuffer ersatzPuffer){
		KZDatum ergebnisDatum = null;
		KZDatum zielDatum = fahrStreifenPuffer.getDatumAktuell();
		
		if(zielDatum != null){	
			KZDatum ersatzZielDatum = ersatzPuffer.getDatumMitZeitStempel(zielDatum.getDatum().getDataTime());	
			if(ersatzZielDatum != null){
				if(zielDatum.getDatum().getData().getTimeValue("T").getMillis() ==  //$NON-NLS-1$
   				   ersatzZielDatum.getDatum().getData().getTimeValue("T").getMillis()){ //$NON-NLS-1$

					ergebnisDatum = new KZDatum(zielDatum.getDatum());

					for(MweAttribut attribut:MweAttribut.getInstanzen()){
						if(ergebnisDatum.getAttributWert(attribut).isImplausibel() &&
								!ersatzZielDatum.getAttributWert(attribut).isImplausibel() &&
								ersatzZielDatum.getAttributWert(attribut).getWert() >= 0){
							ergebnisDatum.getAttributWert(attribut).setWert(
									ersatzZielDatum.getAttributWert(attribut).getWert());
							ergebnisDatum.getAttributWert(attribut).setGuete(
									ersatzZielDatum.getAttributWert(attribut).getGuete());
							ergebnisDatum.getAttributWert(attribut).setInterpoliert(true);								
						}
					}									
				}
			}
		}
		
		return ergebnisDatum;
	}

	
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
