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

import java.util.Collection;

import com.bitctrl.Constants;

import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.mwelve.mwelve.MessWertErsetzungLVE;
import de.bsvrz.dua.mwelve.plformal.PlFormMweLveStandardAspekteVersorger;
import de.bsvrz.dua.mwelve.pllovlve.PlLogMweLveStandardAspekteVersorger;
import de.bsvrz.dua.plformal.plformal.PlPruefungFormal;
import de.bsvrz.dua.plloglve.plloglve.PlPruefungLogischLVE;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.PublikationsModul;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapterMitGuete;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.DuaVerkehrsNetz;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Implementierung des Moduls Verwaltung der SWE Messwertersetzung LVE. Seine
 * Aufgabe besteht in der Auswertung der Aufrufparameter, der Anmeldung beim
 * Datenverteiler und der entsprechenden Initialisierung der Module PL-Prüfung
 * formal, PL-Prüfung logisch LVE, Messwertersetzung LVE sowie Publikation.
 * Weiter ist das Modul Verwaltung für die Anmeldung der zu prüfenden Daten
 * zuständig. Die Verwaltung gibt ein Objekt des Moduls PL-Prüfung formal als
 * Beobachterobjekt an, an das die zu überprüfenden Daten durch den
 * Aktualisierungsmechanismus weitergeleitet werden. Weiterhin stellt die
 * Verwaltung die Verkettung der Module PL-Prüfung formal, PL-Prüfung logisch
 * LVE, Messwertersetzung LVE sowie Publikation in dieser Reihenfolge her.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class VerwaltungMessWertErsetzungLVE extends
		AbstraktVerwaltungsAdapterMitGuete {

	/**
	 * Instanz des Moduls PL-Prüfung formal (1).
	 */
	private PlPruefungFormal plForm1 = null;

	/**
	 * Laut Afo 5.2 faellt die Plausibilitaetspruefung NACH der
	 * Messwertersetzung weg
	 */
	// /**
	// * Instanz des Moduls PL-Prüfung formal (2)
	// */
	// private PlPruefungFormal plForm2 = null;
	//
	// /**
	// * Instanz des Moduls PL-Prüfung logisch LVE (1)
	// */
	// private PlPruefungLogischLVE plLog2 = null;

	/**
	 * Instanz des Moduls PL-Prüfung logisch LVE (2).
	 */
	private PlPruefungLogischLVE plLog1 = null;

	/**
	 * Instanz des Moduls Messwertersetzung LVE.
	 */
	private MessWertErsetzungLVE mwe = null;

	/**
	 * Modul, das nur publiziert (die Publikation kann nicht im Modul MWE selbst
	 * erfolgen, da die Daten nach diesem Modul noch einmal formal und logisch
	 * plausibilisiert werden müssen).
	 */
	private PublikationsModul pub = null;

	/**
	 * {@inheritDoc}
	 */
	public SWETyp getSWETyp() {
		return SWETyp.SWE_MESSWERTERSETZUNG_LVE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiere() throws DUAInitialisierungsException {

		super.initialisiere();
		DuaVerkehrsNetz.initialisiere(this.verbindung);

		Collection<SystemObject> alleFsObjImKB = DUAUtensilien
				.getBasisInstanzen(this.verbindung.getDataModel().getType(
						DUAKonstanten.TYP_FAHRSTREIFEN), this.verbindung, this
						.getKonfigurationsBereiche());
		this.objekte = alleFsObjImKB.toArray(new SystemObject[0]);

		String infoStr = Constants.EMPTY_STRING;
		for (SystemObject obj : this.objekte) {
			infoStr += obj + "\n"; //$NON-NLS-1$
		}
		Debug.getLogger().config("---\nBetrachtete Objekte:\n" + infoStr + "---\n"); //$NON-NLS-1$ //$NON-NLS-2$

		this.plForm1 = new PlPruefungFormal(
				new PlFormMweLveStandardAspekteVersorger(this)
						.getStandardPubInfos());
		this.plLog1 = new PlPruefungLogischLVE(
				new PlLogMweLveStandardAspekteVersorger(this)
						.getStandardPubInfos());
		this.mwe = new MessWertErsetzungLVE();

		/**
		 * Laut Afo 5.2 faellt die Plausibilitaetspruefung NACH der
		 * Messwertersetzung weg
		 */
		// this.plForm2 = new PlPruefungFormal(
		// new
		// PlFormMweLveStandardAspekteVersorger(this).getStandardPubInfos());
		// this.plLog2 = new PlPruefungLogischLVE(
		// new PlLogMweLveStandardAspekteVersorger(this).getStandardPubInfos());
		this.pub = new PublikationsModul(new MweLveStandardAspekteVersorger(
				this).getStandardPubInfos(), ModulTyp.MESSWERTERSETZUNG_LVE);

		this.plForm1.setNaechstenBearbeitungsKnoten(this.plLog1);
		this.plForm1.setPublikation(true);
		this.plForm1.initialisiere(this);

		this.plLog1.setNaechstenBearbeitungsKnoten(this.mwe);
		this.plLog1.setPublikation(true);
		this.plLog1.initialisiere(this);

		this.mwe.setNaechstenBearbeitungsKnoten(this.pub);
		this.mwe.initialisiere(this);

		/**
		 * Laut Afo 5.2 faellt die Plausibilitaetspruefung NACH der
		 * Messwertersetzung weg
		 */
		// this.plForm2.setNaechstenBearbeitungsKnoten(this.plLog2);
		// this.plForm2.initialisiere(this);
		//
		// this.plLog2.setNaechstenBearbeitungsKnoten(this.pub);
		// this.plLog2.initialisiere(this);

		this.pub.initialisiere(this);

		/**
		 * Auf Daten anmelden und Start
		 */
		DataDescription anmeldungsBeschreibungKZD = new DataDescription(
				this.verbindung.getDataModel().getAttributeGroup(
						DUAKonstanten.ATG_KZD), this.verbindung.getDataModel()
						.getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG));

		this.verbindung.subscribeReceiver(this, this.objekte,
				anmeldungsBeschreibungKZD, ReceiveOptions.normal(),
				ReceiverRole.receiver());
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		this.plForm1.aktualisiereDaten(resultate);
	}

	/**
	 * Startet diese Applikation.
	 * 
	 * @param argumente
	 *            Argumente der Kommandozeile
	 */
	public static void main(String[] argumente) {
		StandardApplicationRunner.run(new VerwaltungMessWertErsetzungLVE(),
				argumente);
	}

	/**
	 * {@inheritDoc}.<br>
	 * 
	 * Standard-Gütefaktor für Ersetzungen (90%)<br>
	 * Wenn das Modul Messwertersetzung LVE einen Messwert ersetzt, so
	 * vermindert sich die Güte des Ausgangswertes um diesen Faktor (wenn kein
	 * anderer Wert über die Kommandozeile übergeben wurde)
	 */
	@Override
	public double getStandardGueteFaktor() {
		return 0.9;
	}

}
