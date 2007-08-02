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
package de.bsvrz.dua.mwelve.mwelve.testausfall;

import stauma.dav.clientside.Data;
import stauma.dav.clientside.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.testausfall.AbstraktAusfallUeberwachung;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Das Modul Ausfall�berwachung meldet sich auf alle Parameter an und f�hrt
 * mit allen �ber die Methode aktualisiereDaten(ResultData[] arg0) �bergebenen Daten
 * eine Pr�fung durch. Die Pr�fung �berwacht, ob ein Messwert vor Ablauf des
 * daf�r vorgesehenen Intervalls �bertragen wurde. Der Ende des vorgesehenen Intervalls
 * f�r einen Messwert ergibt sich aus dem Zeitstempel seines Vorg�ngers plus etwa
 * 1.5 * T. Wird der Wert nicht innerhalb dieses Zeitfensters empfangen, so wird er
 * hier erzeugt und mit <code>nicht erfasst</code> gekennzeichnet 
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class LVEAusfallUeberwachung
extends AbstraktAusfallUeberwachung{
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void aktualisiereDaten(ResultData[] resultate) {
		if(resultate != null){
			synchronized (this) {
				for(ResultData resultat:resultate){
					if(resultat != null){
						/**
						 * Jedem Fahstreifen wird ein Zeitverzug von mindestens 1/2 * T und maximal
						 * T - 10s einger�umt
						 */
						long T = this.getTVon(resultat);
						long THalbe = T / 2;
						long TMinus10s = T - 10 * Konstante.SEKUNDE_IN_MS;
						long verzug = THalbe;
						if(TMinus10s > 0 && TMinus10s > THalbe){
							verzug = TMinus10s;
						}						
						
						this.objektWertErfassungVerzug.put(resultat.getObject(), verzug);
					}
				}
			}
		}
		super.aktualisiereDaten(resultate);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultData getAusfallDatumVon(ResultData originalResultat) {
		Data data = originalResultat.getData().createModifiableCopy();
		
		for(String attribute:DUAKonstanten.KZD_ATTRIBUTE){
			DUAUtensilien.getAttributDatum(attribute + ".Wert", data).asUnscaledValue().set(DUAKonstanten.NICHT_ERMITTELBAR); //$NON-NLS-1$
			DUAUtensilien.getAttributDatum(attribute + ".Status.Erfassung.NichtErfasst", data).asUnscaledValue().set(DUAKonstanten.JA); //$NON-NLS-1$
			DUAUtensilien.getAttributDatum(attribute + ".Status.PlFormal.WertMax", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
			DUAUtensilien.getAttributDatum(attribute + ".Status.PlFormal.WertMin", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
			DUAUtensilien.getAttributDatum(attribute + ".Status.PlLogisch.WertMaxLogisch", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
			DUAUtensilien.getAttributDatum(attribute + ".Status.PlLogisch.WertMinLogisch", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
			DUAUtensilien.getAttributDatum(attribute + ".Status.MessWertErsetzung.Implausibel", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
			DUAUtensilien.getAttributDatum(attribute + ".Status.MessWertErsetzung.Interpoliert", data).asUnscaledValue().set(DUAKonstanten.NEIN); //$NON-NLS-1$
			DUAUtensilien.getAttributDatum(attribute + ".G�te.Index", data).asScaledValue().set(1.0); //$NON-NLS-1$
			DUAUtensilien.getAttributDatum(attribute + ".G�te.Verfahren", data).asUnscaledValue().set(0); //$NON-NLS-1$
		}
		long zeitStempel = originalResultat.getDataTime() + this.getTVon(originalResultat);
		
		ResultData resultat = new ResultData(originalResultat.getObject(), 
				originalResultat.getDataDescription(), zeitStempel, data);

		return resultat;
	}
		

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long getTVon(ResultData resultat) {
		return resultat.getData().getTimeValue("T").getMillis(); //$NON-NLS-1$
	}
	
}