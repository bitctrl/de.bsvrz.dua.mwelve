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

import de.bsvrz.sys.funclib.bitctrl.dua.lve.FahrStreifen;

/**
 * TODO<br>
 * Analogon zu <code>ErsetzungsTabelle</code> aus der Feinspezifikation 
 * 
 * IDee: Fahrstreifen initial auf Kaputt setzen, damit hier keine NO_DATA und kein Quelle
 * mehr hineingesopeist werden muss
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class FSDatenPuffer {
	
	/**
	 * die Groesse des Ringpuffers
	 */
	private static final int PUFFER_KAPAZITAET = 3;
	
	/**
	 * der Fahrstreifen, dessen Daten hier gepuffert werden
	 */
	private FahrStreifen fs = null;
	
	/**
	 * die letzten drei emfangenen Datensätze im Ringpuffer
	 */
	private KZDatum[] ringPuffer = new KZDatum[PUFFER_KAPAZITAET];
	
	/**
	 * aktueller Index auf den Ringpuffer
	 */
	private int ringPufferIndex = 0;
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param fs der Fahrstreifen, dessen Daten hier gepuffert werden sollen
	 */	
	protected FSDatenPuffer(final FahrStreifen fs){
		if(fs == null){
			throw new NullPointerException("Als Fahrstreifen wurde <<null>> uebergeben"); //$NON-NLS-1$
		}
		this.fs = fs;
	}
	
	
	/**
	 * Erfragt den unmittelbar vor dem übergebenen Datum in diesen
	 * Puffer eingespeisten Datensatz
	 * 
	 * @param kzDatum ein Datensatz 
	 * @return das Vorgängerdatum oder <code>null</code>, wenn dieses
	 * nicht existiert
	 */
	public final KZDatum getVorgaengerVon(KZDatum kzDatum){
		KZDatum ergebnis = null;
		
		for(int i = 0; i<PUFFER_KAPAZITAET; i++){
			if(this.ringPuffer[i] != null){
				if(this.ringPuffer[i].getDatum().getDataTime() == kzDatum.getDatum().getDataTime()){
					ergebnis = this.ringPuffer[(i + PUFFER_KAPAZITAET - 1) % PUFFER_KAPAZITAET];
					break;
				}
			}
		}
		
		return ergebnis; 
	}
	
	
	/**
	 * Erfragt den Fahrstreifen, dessen Daten hier gepuffert werden
	 * 
	 * @return der Fahrstreifen, dessen Daten hier gepuffert werden
	 */
	public final FahrStreifen getFahrStreifen(){
		return this.fs;
	}
	
	
	/**
	 * Erfragt, ob in diesem Puffer Daten für mehr als ein Intervall gespeichert sind,
	 * die noch nicht wieder freigegeben wurden. Dieser Zustand indiziert, dass ein Intervall
	 * abgelaufen ist
	 * 
	 * @return ob in diesem Puffer Daten für mehr als ein Intervall gespeichert sind,
	 * die noch nicht wieder freigegeben wurden
	 */
	public final boolean isIntervallAbgelaufen(){
		int nichtWiederFreiGegebenZaehler = 0;
		
		for(KZDatum kzDatum:this.ringPuffer){
			if(kzDatum != null){
				if(!kzDatum.isBereitsWiederFreigegeben())nichtWiederFreiGegebenZaehler++;
				if(nichtWiederFreiGegebenZaehler > 1)break;
			}
		}
		
		return nichtWiederFreiGegebenZaehler > 1;
	}
	
	
	/**
	 * Erfragt, ob in diesem Puffer wenigstens ein Datum gespeichert ist, dass noch nicht
	 * wieder an ein anderes Modul weitergegeben wurde
	 * 
	 * @return  ob in diesem Puffer wenigstens ein Datum gespeichert ist, dass noch nicht
	 * wieder an ein anderes Modul weitergegeben wurde
	 */
	public final boolean habeNochNichtFreigegebenesDatum(){
		for(KZDatum kzDatum:this.ringPuffer){
			if(kzDatum != null){
				if(!kzDatum.isBereitsWiederFreigegeben())return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Befreit das in diesem Puffer stehende Datum, das zu einem bereits abgelaufenen Intervall
	 * gehören muss
	 * 
	 * @return das in diesem Puffer stehende Datum, das zu einem bereits abgelaufenen Intervall
	 * gehören muss
	 */
	public final KZDatum befreieAbgelaufenesDatum(){
		KZDatum abgelaufenesDatum = null;
		
		for(KZDatum kzDatum:this.ringPuffer){
			if(!kzDatum.isBereitsWiederFreigegeben()){
				if(abgelaufenesDatum != null){
					abgelaufenesDatum = kzDatum;
				}else{
					if(abgelaufenesDatum.getDatum().getDataTime() > kzDatum.getDatum().getDataTime()){
						abgelaufenesDatum = kzDatum;
						abgelaufenesDatum.setBereitsWiederFreigegeben(true);
						break;
					}
				}
			}
		}

		return abgelaufenesDatum;
	}
	
	
	/**
	 * Erfragt, ob dieser Fahrstreifen aktuell als <code>defekt</code> eingeschätzt
	 * wird. Er ist <code>defekt</code>, wenn das letzte Datum keine Nutzdaten enthielt,
	 * oder noch nie ein Datum angekommen ist.
	 * 
	 * @return  ob dieser Fahrstreifen aktuell als <code>defekt</code> eingeschätzt
	 * wird
	 */
	public final boolean isAktuellDefekt(){
		boolean defekt = true;
		
		KZDatum aktuellesDatum = this.getDatumAktuell();
		if(aktuellesDatum != null){
			defekt = aktuellesDatum.isDefekt();
		}
		
		return defekt;
	}
	
	
	/**
	 * Aktualisiert dieses Objekt mit einem aktuellen Datensatz für
	 * den assoziierten Fahrstreifen
	 * 
	 * @param datum ein KZD des assoziierten Fahrstreifens (muss 
	 * <code>!= null</code> sein)
	 */
	public final void aktualisiereDaten(KZDatum kzDatum){
		this.ringPuffer[ringPufferIndex] = kzDatum;
		ringPufferIndex = (ringPufferIndex + 1) % PUFFER_KAPAZITAET;
	}
	
	
	/**
	 * Erfragt das letzte in diesen Puffer eingespeiste Datum
	 * 
	 * @return das letzte in diesen Puffer eingespeiste Datum oder
	 * <code>null</code>, wenn noch nie ein Datum eingespeist wurde
	 */
	public final KZDatum getDatumAktuell(){
		return this.ringPuffer[(ringPufferIndex + PUFFER_KAPAZITAET - 1) % PUFFER_KAPAZITAET];
	}

	
	/**
	 * Erfragt das Datum innerhalb dieses Puffers, das den übergebenen Zeitstempel
	 * besitzt
	 * 
	 * @return das Datum innerhalb dieses Puffers, das den übergebenen Zeitstempel
	 * besitzt oder <code>null</code>, wenn kein Datum diesen Zeitstempel besitzt
	 */
	public final KZDatum getDatumMitZeitStempel(final long zeitStempel){
		KZDatum ergebnis = null;
		
		for(int i = 0; i<PUFFER_KAPAZITAET; i++){
			KZDatum dummy = this.ringPuffer[i];
			if(dummy != null){
				if(dummy.getDatum().getDataTime() == zeitStempel){
					ergebnis = dummy;
					break;
				}
			}
		}
		
		return ergebnis;
	}
	
	
	/**
	 * Ersetzt ein in diesem Puffer gespeichertes Datum durch ein
	 * messwertersetztes Datum
	 *  
	 * @param mweDatum das neue Datum
	 */
	public final void ersetzeDatum(KZDatum mweDatum){
		for(int i = 0; i<PUFFER_KAPAZITAET; i++){
			KZDatum dummy = this.ringPuffer[i];
			if(dummy != null){
				if(dummy.getDatum().getDataTime() == mweDatum.getDatum().getDataTime()){
					this.ringPuffer[i] = mweDatum;
					break;
				}
			}
		}
	}
}
