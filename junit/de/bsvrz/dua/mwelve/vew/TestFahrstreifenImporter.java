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

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.Data;
import stauma.dav.configuration.interfaces.AttributeGroup;
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
	 * An dieser Spalte beginnen die wirklichen Daten
	 */
	protected static final int OFFSET = 1;
	
	/**
	 * T
	 */
	protected static long INTERVALL = Konstante.MINUTE_IN_MS;
	

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
		this.getNaechsteZeile();
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
	 * Erfragt die nächste Zeile innerhalb der CSV-Datei als einen
	 * Datensatz der übergebenen Attributgruppe
	 * 
	 * @param atg eine Attributgruppe (KZD oder LZD)
	 * @return ein Datensatz der übergebenen Attributgruppe mit den Daten der nächsten Zeile
	 * oder <code>null</code>, wenn der Dateizeiger am Ende ist
	 */
	public final Data getNaechstenDatensatz(final AttributeGroup atg){
		Data datensatz = DAV.createData(atg);

		if(datensatz != null){
			String[] zeile = this.getNaechsteZeile();
			if(zeile != null){
				try{
					int qKfz = Integer.parseInt(zeile[0+OFFSET]);
					int qLkw = Integer.parseInt(zeile[1+OFFSET]);
					int vPkw = Integer.parseInt(zeile[2+OFFSET]);
					int vLkw = Integer.parseInt(zeile[3+OFFSET]);
					int vgKfz = Integer.parseInt(zeile[4+OFFSET]);
					int b = Integer.parseInt(zeile[5+OFFSET]);
					long tNetto = Long.parseLong(zeile[6+OFFSET]) * 1000;
					int sKfz = Integer.parseInt(zeile[7+OFFSET]);
					int vKfz = -1;
					int qPkw = -1;
								
					datensatz.getTimeValue("T").setMillis(INTERVALL); //$NON-NLS-1$
					datensatz = setAttribut("qKfz", qKfz, datensatz); //$NON-NLS-1$
					datensatz = setAttribut("qLkw", qLkw, datensatz); //$NON-NLS-1$
					datensatz = setAttribut("vLkw", vLkw, datensatz); //$NON-NLS-1$
					datensatz = setAttribut("vPkw", vPkw, datensatz); //$NON-NLS-1$				
					
					if(!atg.getPid().equals(DUAKonstanten.ATG_LZD)) {
						datensatz.getUnscaledValue("ArtMittelwertbildung").set(1); //$NON-NLS-1$					
						datensatz = setAttribut("vKfz", vKfz, datensatz); //$NON-NLS-1$
						datensatz = setAttribut("qPkw", qPkw, datensatz); //$NON-NLS-1$
						datensatz = setAttribut("vgKfz", vgKfz, datensatz); //$NON-NLS-1$
						datensatz = setAttribut("b", b, datensatz); //$NON-NLS-1$
						datensatz = setAttribut("tNetto", tNetto, datensatz); //$NON-NLS-1$
						datensatz = setAttribut("sKfz", sKfz, datensatz); //$NON-NLS-1$
					}
				
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
	 * Setzt Attribut in Datensatz
	 * 
	 * @param attributName Name des Attributs
	 * @param wert Wert des Attributs
	 * @param datensatz der Datensatz
	 * @return der veränderte Datensatz
	 */
	private final Data setAttribut(final String attributName, long wert, Data datensatz){
		Data data = datensatz;

		if(attributName.startsWith("v") && wert >= 255) { //$NON-NLS-1$
			wert = -1;
		}
		
		DUAUtensilien.getAttributDatum(attributName + ".Wert", data).asUnscaledValue().set(wert); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.Erfassung.NichtErfasst", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.PlFormal.WertMax", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.PlFormal.WertMin", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.PlLogisch.WertMaxLogisch", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.PlLogisch.WertMinLogisch", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.MessWertErsetzung.Implausibel", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Status.MessWertErsetzung.Interpoliert", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Güte.Index", data).asScaledValue().set(1.0); //$NON-NLS-1$
		DUAUtensilien.getAttributDatum(attributName + ".Güte.Verfahren", data).asUnscaledValue().set(0); //$NON-NLS-1$
				
		return datensatz;
	}

}
