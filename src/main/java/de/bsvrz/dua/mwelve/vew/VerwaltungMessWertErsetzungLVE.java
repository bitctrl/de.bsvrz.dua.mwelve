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

package de.bsvrz.dua.mwelve.vew;

import de.bsvrz.dav.daf.main.ClientDavInterface;
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
import de.bsvrz.sys.funclib.operatingMessage.MessageSender;

import java.util.Collection;

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
 */
public class VerwaltungMessWertErsetzungLVE extends
		AbstraktVerwaltungsAdapterMitGuete {

	/**
	 * Instanz des Moduls PL-Prüfung formal (1).
	 */
	private PlPruefungFormal plForm1 = null;

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

	public SWETyp getSWETyp() {
		return SWETyp.SWE_MESSWERTERSETZUNG_LVE;
	}
	
	@Override
	public void initialize(ClientDavInterface dieVerbindung) throws Exception {
		MessageSender.getInstance().setApplicationLabel("Messwertersetzung LVE");
		super.initialize(dieVerbindung);
	}

	@Override
	protected void initialisiere() throws DUAInitialisierungsException {

		super.initialisiere();
		DuaVerkehrsNetz.initialisiere(this.verbindung);

		Collection<SystemObject> alleFsObjImKB = DUAUtensilien
				.getBasisInstanzen(
						this.verbindung.getDataModel().getType(
								DUAKonstanten.TYP_FAHRSTREIFEN),
						this.verbindung, this.getKonfigurationsBereiche());
		this.objekte = alleFsObjImKB.toArray(new SystemObject[0]);

		String infoStr = "";
		for (SystemObject obj : this.objekte) {
			infoStr += obj + "\n"; //$NON-NLS-1$
		}
		Debug.getLogger().config(
				"---\nBetrachtete Objekte:\n" + infoStr + "---\n"); //$NON-NLS-1$ //$NON-NLS-2$

		this.plForm1 = new PlPruefungFormal(
				new PlFormMweLveStandardAspekteVersorger(this)
						.getStandardPubInfos());
		this.plLog1 = new PlPruefungLogischLVE(
				new PlLogMweLveStandardAspekteVersorger(this)
						.getStandardPubInfos());
		this.mwe = new MessWertErsetzungLVE();
		
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
	
	@Override
	public double getStandardGueteFaktor() {
		return 0.9;
	}
}
