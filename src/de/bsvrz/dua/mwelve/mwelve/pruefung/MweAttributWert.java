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

import stauma.dav.clientside.Data;
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.dua.guete.GueteException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;

/**
 * Korrespondiert mit einem Attributwert eines KZ- oder LZ-Datums
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class MweAttributWert
implements Comparable<MweAttributWert>{

	/**
	 * das Attribut
	 */
	private MweAttribut attr = null;
	
	/**
	 * der Wert dieses Attributs
	 */
	private long wert = -4;
	
	/**
	 * der Wert von <code>*.Status.Erfassung.NichtErfasst</code>
	 */
	private boolean nichtErfasst = false;

	/**
	 * der Wert von <code>*.Status.MessWertErsetzung.Implausibel</code>
	 */
	private boolean implausibel = false;

	/**
	 * der Wert von <code>*.Status.MessWertErsetzung.Interpoliert</code>
	 */
	private boolean interpoliert = false;
	
	/**
	 * Indiziert, ob dieser Attributwert verändert wurde
	 */
	private boolean veraendert = false;
	
	/**
	 * die Guete
	 */
	private GWert guete = null;
	
	/**
	 * Gibt an, zum wievielten Mal dieser Wert bereits fortgeschrieben wird
	 */
	private long fortgeschriebenZumXtenMal = 0;	
	
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param attr das Attribut
	 * @throws GueteException wird weitergereicht
	 */
	public MweAttributWert(final MweAttribut attr, final Data datenSatz)
	throws GueteException{
		if(attr == null){
			throw new NullPointerException("Attribut ist <<null>>"); //$NON-NLS-1$
		}
		if(datenSatz == null){
			throw new NullPointerException("Datensatz ist <<null>>"); //$NON-NLS-1$
		}
		this.attr = attr;
		this.nichtErfasst = datenSatz.getItem(attr.getName()).getItem("Status").getItem("Erfassung").  //$NON-NLS-1$//$NON-NLS-2$
										getUnscaledValue("NichtErfasst").intValue() == DUAKonstanten.JA; //$NON-NLS-1$
		this.implausibel = datenSatz.getItem(attr.getName()).getItem("Status").getItem("MessWertErsetzung").  //$NON-NLS-1$//$NON-NLS-2$
										getUnscaledValue("Implausibel").intValue() == DUAKonstanten.JA; //$NON-NLS-1$
		this.interpoliert = datenSatz.getItem(attr.getName()).getItem("Status").getItem("MessWertErsetzung").  //$NON-NLS-1$//$NON-NLS-2$
										getUnscaledValue("Interpoliert").intValue() == DUAKonstanten.JA; //$NON-NLS-1$
		this.guete = new GWert(datenSatz.getItem(attr.getName()).getItem("Güte")); //$NON-NLS-1$
	}
	
	
	/**
	 * Erfragt, zum wievielten mal dieser Wert schon fortgeschrieben wird
	 * 
	 * @return zum wievielten mal dieser Wert schon fortgeschrieben wird
	 */
	public final long getFortgeschriebenSeit(){
		return this.fortgeschriebenZumXtenMal;
	}
	
	
	/**
	 * Erfragt das Attribut
	 * 
	 * @return das Attribut
	 */
	public final MweAttribut getAttribut(){
		return this.attr;
	}


	/**
	 * Setzt den Wert dieses Attributs
	 * 
	 * @param wert der Wert dieses Attributs
	 */
	public final void setWert(final long wert){
		this.wert = wert;
	}

	
	/**
	 * Erfragt den Wert dieses Attributs
	 * 
	 * @return der Wert dieses Attributs
	 */
	public final long getWert(){
		return this.wert;
	}


	/**
	 * Erfragt den Wert von <code>*.Status.MessWertErsetzung.Interpoliert</code>
	 *  
	 * @return der Wert von <code>*.Status.MessWertErsetzung.Interpoliert</code>
	 */
	public final boolean isInterpoliert(){
		return this.interpoliert;
	}
	
	
	/**
	 * Setzt den Wert von <code>*.Status.MessWertErsetzung.Interpoliert</code>
	 *  
	 * @param interpoliert der Wert von <code>*.Status.MessWertErsetzung.Interpoliert</code>
	 */
	public final void setInterpoliert(boolean interpoliert){
		this.veraendert = true;
		this.interpoliert = nichtErfasst;
	}
	
	
	/**
	 * Erfragt den Wert von <code>*.Status.MessWertErsetzung.Implausibel</code>
	 *  
	 * @return der Wert von <code>*.Status.MessWertErsetzung.Implausibel</code>
	 */
	public final boolean isImplausibel(){
		return this.implausibel;
	}
	
	
	/**
	 * Setzt den Wert von <code>*.Status.MessWertErsetzung.Implausibel</code>
	 *  
	 * @param implausibel der Wert von <code>*.Status.MessWertErsetzung.Implausibel</code>
	 */
	public final void setImplausibel(boolean implausibel){
		this.veraendert = true;
		this.implausibel = nichtErfasst;
	}
	
	
	/**
	 * Erfragt den Wert von <code>*.Status.Erfassung.NichtErfasst</code>
	 *  
	 * @return der Wert von <code>*.Status.Erfassung.NichtErfasst</code>
	 */
	public final boolean isNichtErfasst(){
		return this.nichtErfasst;
	}
	
	
	/**
	 * Setzt den Wert von <code>*.Status.Erfassung.NichtErfasst</code>
	 *  
	 * @param nichtErfasst der Wert von <code>*.Status.Erfassung.NichtErfasst</code>
	 */
	public final void setNichtErfasst(boolean nichtErfasst){
		this.veraendert = true;
		this.nichtErfasst = nichtErfasst;
	}
	
	
	/**
	 * Erfragt, ob dieser Wert veraendert wurde
	 * 
	 * @return ob dieser Wert veraendert wurde
	 */
	public final boolean isVeraendert(){
		return this.veraendert;
	}


	/**
	 * {@inheritDoc}
	 */
	public int compareTo(MweAttributWert that) {
		return new Long(this.getWert()).compareTo(that.getWert());
	}
	
	
	/**
	 * Erfragt die Guete dieses Attributwertes
	 * 
	 * @return die Guete dieses Attributwertes
	 */
	public final GWert getGuete(){
		return this.guete;
	}
	
	
	/**
	 * Setzte die Guete dieses Attributwertes
	 * 
	 * @param guete die Guete dieses Attributwertes
	 */
	public final void setGuete(final GWert guete){
		this.veraendert = true;
		this.guete = guete;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ergebnis = false;
		
		if(obj != null && obj instanceof MweAttributWert){
			MweAttributWert that = (MweAttributWert)obj;
			ergebnis = this.getAttribut().equals(that.getAttribut()) &&
					   this.getWert() == that.getWert() &&
					   this.isNichtErfasst() == that.isNichtErfasst() &&
					   this.isImplausibel() == that.isImplausibel() &&
					   this.getGuete() == that.getGuete() &&
					   this.isInterpoliert() == that.isInterpoliert();
		}
		
		return ergebnis;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Attribut: " + this.attr + "\nWert: " + this.wert +   //$NON-NLS-1$ //$NON-NLS-2$
			   "\nNicht Erfasst: " + (this.nichtErfasst?"Ja":"Nein") +  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			   "\nImplausibel: " + (this.implausibel?"Ja":"Nein") +  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			   "\nInterpoliert: " + (this.interpoliert?"Ja":"Nein") +  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			   "\nGuete: " + this.guete +   //$NON-NLS-1$
			   "\nVeraendert: " + (this.veraendert?"Ja":"Nein");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
	}
	
}

