/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.5 Messwertersetzung LVE
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.MesswertUnskaliert;

/**
 * Repraesentation eines Kurzzeitdatums fuer Testzwecke.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public class KurzZeitDatum {

	/**
	 * das Originaldatum.
	 */
	private ResultData original = null;

	/**
	 * interne Repraesentationen des Originaldatums.
	 */
	private long t = -1;

	/**
	 * <code>ArtMittelwertbildung</code>.
	 */
	private long artMittelwertbildung = 1;

	/**
	 * <code>qKfz</code>.
	 */
	private MesswertUnskaliert qKfz = new MesswertUnskaliert("qKfz"); //$NON-NLS-1$

	/**
	 * <code>qLkw</code>.
	 */
	private MesswertUnskaliert qLkw = new MesswertUnskaliert("qLkw"); //$NON-NLS-1$

	/**
	 * <code>qPkw</code>.
	 */
	private MesswertUnskaliert qPkw = new MesswertUnskaliert("qPkw"); //$NON-NLS-1$

	/**
	 * <code>vKfz</code>.
	 */
	private MesswertUnskaliert vKfz = new MesswertUnskaliert("vKfz"); //$NON-NLS-1$

	/**
	 * <code>vLkw</code>.
	 */
	private MesswertUnskaliert vLkw = new MesswertUnskaliert("vLkw"); //$NON-NLS-1$

	/**
	 * <code>vPkw</code>.
	 */
	private MesswertUnskaliert vPkw = new MesswertUnskaliert("vPkw"); //$NON-NLS-1$

	/**
	 * <code>vgKfz</code>.
	 */
	private MesswertUnskaliert vgKfz = new MesswertUnskaliert("vgKfz"); //$NON-NLS-1$

	/**
	 * <code>b</code>.
	 */
	private MesswertUnskaliert b = new MesswertUnskaliert("b"); //$NON-NLS-1$

	/**
	 * <code>tNetto</code>.
	 */
	private MesswertUnskaliert tNetto = new MesswertUnskaliert("tNetto"); //$NON-NLS-1$

	/**
	 * Standardkonstruktor.
	 *
	 * @param resultat
	 *            das <code>ResultData</code>-Objekt, aus dem sich dieses KZD
	 *            speist
	 */
	public KurzZeitDatum(final ResultData resultat) {
		original = resultat;
		t = resultat.getData().getTimeValue("T").getMillis(); //$NON-NLS-1$
		artMittelwertbildung = resultat.getData()
				.getUnscaledValue("ArtMittelwertbildung").longValue(); //$NON-NLS-1$
		qKfz = new MesswertUnskaliert("qKfz", resultat.getData()); //$NON-NLS-1$
		qLkw = new MesswertUnskaliert("qLkw", resultat.getData()); //$NON-NLS-1$
		qPkw = new MesswertUnskaliert("qPkw", resultat.getData()); //$NON-NLS-1$
		vKfz = new MesswertUnskaliert("vKfz", resultat.getData()); //$NON-NLS-1$
		vLkw = new MesswertUnskaliert("vLkw", resultat.getData()); //$NON-NLS-1$
		vPkw = new MesswertUnskaliert("vPkw", resultat.getData()); //$NON-NLS-1$
		vgKfz = new MesswertUnskaliert("vgKfz", resultat.getData()); //$NON-NLS-1$
		b = new MesswertUnskaliert("b", resultat.getData()); //$NON-NLS-1$
		tNetto = new MesswertUnskaliert("tNetto", resultat.getData()); //$NON-NLS-1$
	}

	@Override
	public boolean equals(final Object obj) {
		return compare((KurzZeitDatum) obj) == null;
	}

	/**
	 * Erfragt den Unterschied zwischen diesem und dem uebergebenen Datum.
	 *
	 * @param that
	 *            ein anderes KZD
	 * @return der Unterschied zwischen diesem und dem uebergebenen Datum oder
	 *         <code>null</code> wenn beide Daten gleich sind
	 */
	public final String compare(final KurzZeitDatum that) {
		boolean gleich = true;
		String info = Constants.EMPTY_STRING;

		if (!original.getObject().equals(that.original.getObject())) {
			info += original.getObject() + " != " + that.original.getObject() //$NON-NLS-1$
					+ "\n"; //$NON-NLS-1$
			gleich = false;
		}
		if (original.getDataTime() != that.original.getDataTime()) {
			final SimpleDateFormat dateFormat = new SimpleDateFormat(
					DUAKonstanten.ZEIT_FORMAT_GENAU_STR);
			info += dateFormat.format(new Date(original.getDataTime())) + " != " //$NON-NLS-1$
					+ dateFormat.format(new Date(that.original.getDataTime()))
					+ "\n"; //$NON-NLS-1$
			gleich = false;
		}
		if (t != that.t) {
			info += "T: " + t + " != " + that.t + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if (artMittelwertbildung != that.artMittelwertbildung) {
			info += "ArtMittelwertbildung: " + artMittelwertbildung + " != " //$NON-NLS-1$ //$NON-NLS-2$
					+ that.artMittelwertbildung + "\n"; //$NON-NLS-1$
			gleich = false;
		}
		if (!qKfz.equals(that.qKfz)) {
			info += "qKfz: " + qKfz + " != " + that.qKfz + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if (!qPkw.equals(that.qPkw)) {
			info += "qPkw: " + qPkw + " != " + that.qPkw + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if (!qLkw.equals(that.qLkw)) {
			info += "qLkw: " + qLkw + " != " + that.qLkw + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if (!vKfz.equals(that.vKfz)) {
			info += "vKfz: " + vKfz + " != " + that.vKfz + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if (!vPkw.equals(that.vPkw)) {
			info += "vPkw: " + vPkw + " != " + that.vPkw + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if (!vLkw.equals(that.vLkw)) {
			info += "vLkw: " + vLkw + " != " + that.vLkw + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}

		if (!vgKfz.equals(that.vgKfz)) {
			info += "vgKfz: " + vgKfz + " != " + that.vgKfz + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if (!b.equals(that.b)) {
			info += "b: " + b + " != " + that.b + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}
		if (!tNetto.equals(that.tNetto)) {
			info += "tNetto: " + tNetto + " != " + that.tNetto + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			gleich = false;
		}

		return gleich ? null : info;
	}

	@Override
	public String toString() {
		return original.getData().toString();
	}

}
