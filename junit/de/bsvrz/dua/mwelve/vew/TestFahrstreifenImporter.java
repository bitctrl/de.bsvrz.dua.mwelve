/** 
 * Segment 4 Datenübernahme und Aufbereitung (DUA)
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

package de.bsvrz.dua.mwelve.vew;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.test.CSVImporter;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;


/**
 * Liest die Ausgangsdaten eines Fahrstreifens ein
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class TestFahrstreifenImporter
extends CSVImporter{
	
	/**
	 * Verbindung zum Datenverteiler
	 */
	protected static ClientDavInterface DAV = null;
	
	/**
	 * T
	 */
	protected static long INTERVALL = Konstante.MINUTE_IN_MS;
	
	/**
	 * die Zeile, die als letztes eingelesen wurde
	 */
	private String[] aktuelleZeile = null;
	

	/**
	 * Standardkonstruktor
	 * 
	 * @param dav Datenverteier-Verbindung
	 * @param csvQuelle Quelle der Daten (CSV-Datei)
	 * @throws Exception falls dieses Objekt nicht vollständig initialisiert werden konnte
	 */
	public TestFahrstreifenImporter(final ClientDavInterface dav, 
								    final String csvQuelle)
	throws Exception{
		super(csvQuelle);
		if(DAV == null){
			DAV = dav;
		}
		
		/**
		 * Tabellenkopf überspringen
		 */
		this.next();
	}
	
	
	/**
	 * Setzt Datenintervall
	 * 
	 * @param t Datenintervall
	 */
	public static final void setT(final long t){
		INTERVALL = t;
	}
	
	
	/**
	 * Springt zur naechsten Zeile innerhalb der CSV-Datei
	 * 
	 * @return ob weiter aus der Datei gelesen werden kann
	 */
	public final boolean next(){
		this.aktuelleZeile = this.getNaechsteZeile();
		if(this.aktuelleZeile == null)return false;
		return true;
	}
	
	
	/**
	 * Erfragt, ob der Dateizeiger am Ende steht
	 * 
	 * @return ob der Dateizeiger am Ende steht
	 */
	public final boolean isEnde(){
		return this.aktuelleZeile == null;
	}
	
	
	/**
	 * Erfragt einen KZ-Datensatz aus der aktuellen Zeile der importierten
	 * Tabelle fuer den Fahrstreifen mit dem uebergebenen Index
	 * 
	 * @param index der Index des Fahrstreifens, dessen Daten gelesen werden
	 * sollen (1 fuer Fahrstreifen 1, 2 fuer Fahrstreifen 2 und 3 fuer Fahrstreifen 3)
	 * @return einen KZ-Datensatz aus der aktuellen Zeile der importierten
	 * Tabelle fuer den Fahrstreifen mit dem uebergebenen Index
	 */
	public final Data getDatensatz(int index){
		Data datensatz = DAV.createData(DAV.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD));

		if(datensatz != null){
			
			if(this.aktuelleZeile != null){
				try{
					datensatz.getTimeValue("T").setMillis(INTERVALL); //$NON-NLS-1$
					datensatz.getUnscaledValue("ArtMittelwertbildung").set(1); //$NON-NLS-1$
					modifyAttribut("sKfz", DUAKonstanten.NICHT_ERMITTELBAR, Konstante.LEERSTRING, datensatz); //$NON-NLS-1$
					
					long qKfz = Integer.parseInt(this.aktuelleZeile[0 + (index) * 2]);
					String qKfzStatus = this.aktuelleZeile[1 + 0 + (index) * 2];
					modifyAttribut("qKfz", qKfz, qKfzStatus, datensatz); //$NON-NLS-1$
					
					long qPkw = Integer.parseInt(this.aktuelleZeile[6 + (index) * 2]);
					String qPkwStatus = this.aktuelleZeile[1 + 6 + (index) * 2];
					modifyAttribut("qPkw", qPkw, qPkwStatus, datensatz); //$NON-NLS-1$
					
					long qLkw = Integer.parseInt(this.aktuelleZeile[12 + (index) * 2]);
					String qLkwStatus = this.aktuelleZeile[1 + 12 + (index) * 2];
					modifyAttribut("qLkw", qLkw, qLkwStatus, datensatz); //$NON-NLS-1$
					
					long vKfz = Integer.parseInt(this.aktuelleZeile[18 + (index) * 2]);
					String vKfzStatus = this.aktuelleZeile[1 + 18 + (index) * 2];
					modifyAttribut("vKfz", vKfz, vKfzStatus, datensatz); //$NON-NLS-1$
					
					long vPkw = Integer.parseInt(this.aktuelleZeile[24 + (index) * 2]);
					String vPkwStatus = this.aktuelleZeile[1 + 24 + (index) * 2];
					modifyAttribut("vPkw", vPkw, vPkwStatus, datensatz); //$NON-NLS-1$
					
					long vLkw = Integer.parseInt(this.aktuelleZeile[30 + (index) * 2]);
					String vLkwStatus = this.aktuelleZeile[1 + 30 + (index) * 2];
					modifyAttribut("vLkw", vLkw, vLkwStatus, datensatz); //$NON-NLS-1$
					
					long vgKfz = Integer.parseInt(this.aktuelleZeile[36 + (index) * 2]);
					String vgKfzStatus = this.aktuelleZeile[1 + 36 + (index) * 2];
					modifyAttribut("vgKfz", vgKfz, vgKfzStatus, datensatz); //$NON-NLS-1$
					
					long b = Integer.parseInt(this.aktuelleZeile[42 + (index) * 2]);
					String bStatus = this.aktuelleZeile[1 + 42 + (index) * 2];
					modifyAttribut("b", b, bStatus, datensatz); //$NON-NLS-1$
					
					long tNetto = Integer.parseInt(this.aktuelleZeile[48 + (index) * 2]);
					String tStatus = this.aktuelleZeile[1 + 48 + (index) * 2];
					modifyAttribut("tNetto", tNetto, tStatus, datensatz); //$NON-NLS-1$
																	
				}catch(ArrayIndexOutOfBoundsException ex){
					datensatz = null;
				}
			}else{
				datensatz = null;
			}
		}
		
		return datensatz;
	}
	
		
	/**
	 * Modifiziert ein Attribut in einem Datensatz
	 * 
	 * @param attributName Name des Attributs
	 * @param wert Wert des Attributs
	 * @param datensatz der Datensatz
	 * @return der veränderte Datensatz
	 */
	private final void modifyAttribut(final String attributName,
									  final long wert,
									  final String status,
									  Data datensatz){		
		int nErf = DUAKonstanten.NEIN;
		int wMax = DUAKonstanten.NEIN;
		int wMin = DUAKonstanten.NEIN;
		int wMaL = DUAKonstanten.NEIN;
		int wMiL = DUAKonstanten.NEIN;
		int impl = DUAKonstanten.NEIN;
		int intp = DUAKonstanten.NEIN;
		double guete = 1.0;
		
		int errCode = 0;
		
		if(status != null) {
			String[] splitStatus = status.trim().split(" "); //$NON-NLS-1$
			
			for(int i = 0; i<splitStatus.length;i++) {
				if(splitStatus[i].equalsIgnoreCase("Fehl")){ //$NON-NLS-1$
					errCode = errCode-2;
				}
				
				if(splitStatus[i].equalsIgnoreCase("nErm")){ //$NON-NLS-1$
					errCode = errCode-1;
				}
				
				if(splitStatus[i].equalsIgnoreCase("Impl")){ //$NON-NLS-1$
					impl = DUAKonstanten.JA;
				}
				
				if(splitStatus[i].equalsIgnoreCase("Intp")){ //$NON-NLS-1$
					intp = DUAKonstanten.JA;				
				}

				if(splitStatus[i].equalsIgnoreCase("nErf")){ //$NON-NLS-1$
					nErf = DUAKonstanten.JA;
				}

				if(splitStatus[i].equalsIgnoreCase("wMaL")){ //$NON-NLS-1$
					wMaL = DUAKonstanten.JA;
				}
				
				if(splitStatus[i].equalsIgnoreCase("wMax")){ //$NON-NLS-1$
					wMax = DUAKonstanten.JA;
				}

				if(splitStatus[i].equalsIgnoreCase("wMiL")){ //$NON-NLS-1$
					wMiL = DUAKonstanten.JA;
				}

				if(splitStatus[i].equalsIgnoreCase("wMin")){ //$NON-NLS-1$
					wMin = DUAKonstanten.JA;
				}
				
				try {
					guete = Float.parseFloat(splitStatus[i].replace(",", ".")); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (Exception e) {
					// kein float Wert
				}
			}
		}
			
		DUAUtensilien.getAttributDatum(attributName + ".Wert", datensatz).asUnscaledValue().set(errCode < 0?errCode:wert); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.Erfassung.NichtErfasst", datensatz).asUnscaledValue().set(nErf); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.PlFormal.WertMax", datensatz).asUnscaledValue().set(wMax); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.PlFormal.WertMin", datensatz).asUnscaledValue().set(wMin); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.PlLogisch.WertMaxLogisch", datensatz).asUnscaledValue().set(wMaL); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.PlLogisch.WertMinLogisch", datensatz).asUnscaledValue().set(wMiL); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.MessWertErsetzung.Implausibel", datensatz).asUnscaledValue().set(impl); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.MessWertErsetzung.Interpoliert", datensatz).asUnscaledValue().set(intp); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Güte.Index", datensatz).asScaledValue().set(guete); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Güte.Verfahren", datensatz).asUnscaledValue().set(0); //$NON-NLS-1$
	}

}
