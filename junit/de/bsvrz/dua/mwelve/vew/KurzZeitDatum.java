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
package de.bsvrz.dua.mwelve.vew;

import java.util.Date;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.MesswertUnskaliert;

/**
 * Repraesentation eines Kurzzeitdatums fuer Testzwecke
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class KurzZeitDatum {

	/**
	 * das Originaldatum
	 */
	private ResultData original = null;
	
	/**
	 * interne Repraesentationen des Originaldatums
	 */
	private long T = -1;
	private long ArtMittelwertbildung = 1;
	private MesswertUnskaliert qKfz = new MesswertUnskaliert("qKfz"); //$NON-NLS-1$
	private MesswertUnskaliert qLkw = new MesswertUnskaliert("qLkw"); //$NON-NLS-1$
	private MesswertUnskaliert qPkw = new MesswertUnskaliert("qPkw"); //$NON-NLS-1$
	private MesswertUnskaliert vKfz = new MesswertUnskaliert("vKfz"); //$NON-NLS-1$
	private MesswertUnskaliert vLkw = new MesswertUnskaliert("vLkw"); //$NON-NLS-1$
	private MesswertUnskaliert vPkw = new MesswertUnskaliert("vPkw"); //$NON-NLS-1$
	private MesswertUnskaliert vgKfz = new MesswertUnskaliert("vgKfz"); //$NON-NLS-1$
	private MesswertUnskaliert b = new MesswertUnskaliert("b"); //$NON-NLS-1$
	private MesswertUnskaliert tNetto = new MesswertUnskaliert("tNetto"); //$NON-NLS-1$
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param resultat das <code>ResultData</code>-Objekt, aus dem sich dieses KZD speist
	 */
	public KurzZeitDatum(final ResultData resultat){
		this.original = resultat;
		this.T = resultat.getData().getTimeValue("T").getMillis(); //$NON-NLS-1$
		this.ArtMittelwertbildung = resultat.getData().getUnscaledValue("ArtMittelwertbildung").longValue(); //$NON-NLS-1$
		this.qKfz = new MesswertUnskaliert("qKfz", resultat.getData()); //$NON-NLS-1$
		this.qLkw = new MesswertUnskaliert("qLkw", resultat.getData()); //$NON-NLS-1$
		this.qPkw = new MesswertUnskaliert("qPkw", resultat.getData()); //$NON-NLS-1$
		this.vKfz = new MesswertUnskaliert("vKfz", resultat.getData()); //$NON-NLS-1$
		this.vLkw = new MesswertUnskaliert("vLkw", resultat.getData()); //$NON-NLS-1$
		this.vPkw = new MesswertUnskaliert("vPkw", resultat.getData()); //$NON-NLS-1$
		this.vgKfz = new MesswertUnskaliert("vgKfz", resultat.getData()); //$NON-NLS-1$
		this.b = new MesswertUnskaliert("b", resultat.getData()); //$NON-NLS-1$
		this.tNetto = new MesswertUnskaliert("tNetto", resultat.getData()); //$NON-NLS-1$		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		return compare((KurzZeitDatum)obj) == null;	
	}
	
	
	/**
	 * Erfragt den Unterschied zwischen diesem und dem uebergebenen Datum
	 * 
	 * @param that ein anderes KZD
	 * @return der Unterschied zwischen diesem und dem uebergebenen Datum
	 * oder <code>null</code> wenn beide Daten gleich sind
	 */
	public final String compare(KurzZeitDatum that) {
		boolean gleich = true;
		String info = Constants.EMPTY_STRING;
		
		if(!this.original.getObject().equals(that.original.getObject())){
			info += this.original.getObject() + " != " + that.original.getObject() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
			gleich = false;
		}
		if(this.original.getDataTime() != that.original.getDataTime()){
			info += DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(this.original.getDataTime())) + " != " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(that.original.getDataTime())) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
			gleich = false;
		}
		if(this.T != that.T){
			info += "T: " + this.T + " != " + that.T + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if(this.ArtMittelwertbildung != that.ArtMittelwertbildung){
			info += "ArtMittelwertbildung: " + this.ArtMittelwertbildung + " != " + that.ArtMittelwertbildung + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if(!this.qKfz.equals(that.qKfz)){
			info += "qKfz: " + this.qKfz + " != " + that.qKfz + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if(!this.qPkw.equals(that.qPkw)){
			info += "qPkw: " + this.qPkw + " != " + that.qPkw + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if(!this.qLkw.equals(that.qLkw)){
			info += "qLkw: " + this.qLkw + " != " + that.qLkw + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if(!this.vKfz.equals(that.vKfz)){
			info += "vKfz: " + this.vKfz + " != " + that.vKfz + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if(!this.vPkw.equals(that.vPkw)){
			info += "vPkw: " + this.vPkw + " != " + that.vPkw + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if(!this.vLkw.equals(that.vLkw)){
			info += "vLkw: " + this.vLkw + " != " + that.vLkw + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		
		if(!this.vgKfz.equals(that.vgKfz)){
			info += "vgKfz: " + this.vgKfz + " != " + that.vgKfz + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if(!this.b.equals(that.b)){
			info += "b: " + this.b + " != " + that.b + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if(!this.tNetto.equals(that.tNetto)){
			info += "tNetto: " + this.tNetto + " != " + that.tNetto + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		
		return gleich?null:info;
	}	
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.original.getData().toString();
	}
	
}
