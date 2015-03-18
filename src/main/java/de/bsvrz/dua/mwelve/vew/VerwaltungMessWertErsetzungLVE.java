/*
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.5 Messwertersetzung LVE
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
 * Wei�enfelser Stra�e 67<br>
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
import de.bsvrz.sys.funclib.bitctrl.daf.BetriebsmeldungIdKonverter;
import de.bsvrz.sys.funclib.bitctrl.daf.DefaultBetriebsMeldungsIdKonverter;
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

/**
 * Implementierung des Moduls Verwaltung der SWE Messwertersetzung LVE. Seine
 * Aufgabe besteht in der Auswertung der Aufrufparameter, der Anmeldung beim
 * Datenverteiler und der entsprechenden Initialisierung der Module PL-Pr�fung
 * formal, PL-Pr�fung logisch LVE, Messwertersetzung LVE sowie Publikation.
 * Weiter ist das Modul Verwaltung f�r die Anmeldung der zu pr�fenden Daten
 * zust�ndig. Die Verwaltung gibt ein Objekt des Moduls PL-Pr�fung formal als
 * Beobachterobjekt an, an das die zu �berpr�fenden Daten durch den
 * Aktualisierungsmechanismus weitergeleitet werden. Weiterhin stellt die
 * Verwaltung die Verkettung der Module PL-Pr�fung formal, PL-Pr�fung logisch
 * LVE, Messwertersetzung LVE sowie Publikation in dieser Reihenfolge her.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 * @version $Id: VerwaltungMessWertErsetzungLVE.java 53825 2015-03-18 09:36:42Z
 *          peuker $
 */
public class VerwaltungMessWertErsetzungLVE extends
		AbstraktVerwaltungsAdapterMitGuete {

	private static final Debug LOGGER = Debug.getLogger();

	/**
	 * Der statische Umsetzer fuer die Betriebsmeldungs-Id.
	 */
	private static final BetriebsmeldungIdKonverter KONVERTER = new DefaultBetriebsMeldungsIdKonverter();

	/**
	 * Instanz des Moduls PL-Pr�fung formal (1).
	 */
	private PlPruefungFormal plForm1;

	/**
	 * Laut Afo 5.2 faellt die Plausibilitaetspruefung NACH der
	 * Messwertersetzung weg
	 */
	// /**
	// * Instanz des Moduls PL-Pr�fung formal (2)
	// */
	// private PlPruefungFormal plForm2 = null;
	//
	// /**
	// * Instanz des Moduls PL-Pr�fung logisch LVE (1)
	// */
	// private PlPruefungLogischLVE plLog2 = null;

	/**
	 * Instanz des Moduls PL-Pr�fung logisch LVE (2).
	 */
	private PlPruefungLogischLVE plLog1 = null;

	/**
	 * Instanz des Moduls Messwertersetzung LVE.
	 */
	private MessWertErsetzungLVE mwe = null;

	/**
	 * Modul, das nur publiziert (die Publikation kann nicht im Modul MWE selbst
	 * erfolgen, da die Daten nach diesem Modul noch einmal formal und logisch
	 * plausibilisiert werden m�ssen).
	 */
	private PublikationsModul pub = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SWETyp getSWETyp() {
		return SWETyp.SWE_MESSWERTERSETZUNG_LVE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialisiere() throws DUAInitialisierungsException {

		super.initialisiere();
		DuaVerkehrsNetz.initialisiere(verbindung);
		MessageSender.getInstance()
				.setApplicationLabel("Messwertersetzung LVE");

		final Collection<SystemObject> alleFsObjImKB = DUAUtensilien
				.getBasisInstanzen(
						verbindung.getDataModel().getType(
								DUAKonstanten.TYP_FAHRSTREIFEN), verbindung,
								getKonfigurationsBereiche());
		objekte = alleFsObjImKB.toArray(new SystemObject[0]);

		String infoStr = Constants.EMPTY_STRING;
		for (final SystemObject obj : objekte) {
			infoStr += obj + "\n"; //$NON-NLS-1$
		}
		LOGGER.config("---\nBetrachtete Objekte:\n" + infoStr + "---\n"); //$NON-NLS-1$ //$NON-NLS-2$

		plForm1 = new PlPruefungFormal(
				new PlFormMweLveStandardAspekteVersorger(this)
						.getStandardPubInfos());
		plLog1 = new PlPruefungLogischLVE(
				new PlLogMweLveStandardAspekteVersorger(this)
						.getStandardPubInfos());
		mwe = new MessWertErsetzungLVE();

		/**
		 * Laut Afo 5.2 faellt die Plausibilitaetspruefung NACH der
		 * Messwertersetzung weg
		 */
		// this.plForm2 = new PlPruefungFormal(
		// new
		// PlFormMweLveStandardAspekteVersorger(this).getStandardPubInfos());
		// this.plLog2 = new PlPruefungLogischLVE(
		// new PlLogMweLveStandardAspekteVersorger(this).getStandardPubInfos());
		pub = new PublikationsModul(
				new MweLveStandardAspekteVersorger(this).getStandardPubInfos(),
				ModulTyp.MESSWERTERSETZUNG_LVE);

		plForm1.setNaechstenBearbeitungsKnoten(plLog1);
		plForm1.setPublikation(true);
		plForm1.initialisiere(this);

		plLog1.setNaechstenBearbeitungsKnoten(mwe);
		plLog1.setPublikation(true);
		plLog1.initialisiere(this);

		mwe.setNaechstenBearbeitungsKnoten(pub);
		mwe.initialisiere(this);

		/**
		 * Laut Afo 5.2 faellt die Plausibilitaetspruefung NACH der
		 * Messwertersetzung weg
		 */
		// this.plForm2.setNaechstenBearbeitungsKnoten(this.plLog2);
		// this.plForm2.initialisiere(this);
		//
		// this.plLog2.setNaechstenBearbeitungsKnoten(this.pub);
		// this.plLog2.initialisiere(this);

		pub.initialisiere(this);

		/**
		 * Auf Daten anmelden und Start
		 */
		final DataDescription anmeldungsBeschreibungKZD = new DataDescription(
				verbindung.getDataModel().getAttributeGroup(
						DUAKonstanten.ATG_KZD), verbindung.getDataModel()
						.getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG));

		verbindung.subscribeReceiver(this, objekte, anmeldungsBeschreibungKZD,
				ReceiveOptions.normal(), ReceiverRole.receiver());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(final ResultData[] resultate) {
		plForm1.aktualisiereDaten(resultate);
	}

	/**
	 * Startet diese Applikation.
	 *
	 * @param argumente
	 *            Argumente der Kommandozeile
	 */
	public static void main(final String[] argumente) {
		StandardApplicationRunner.run(new VerwaltungMessWertErsetzungLVE(),
				argumente);
	}

	/**
	 * {@inheritDoc}.<br>
	 *
	 * Standard-G�tefaktor f�r Ersetzungen (90%)<br>
	 * Wenn das Modul Messwertersetzung LVE einen Messwert ersetzt, so
	 * vermindert sich die G�te des Ausgangswertes um diesen Faktor (wenn kein
	 * anderer Wert �ber die Kommandozeile �bergeben wurde)
	 */
	@Override
	public double getStandardGueteFaktor() {
		return 0.9;
	}

	/**
	 * Erfragt den statischen Umsetzer fuer die Betriebsmeldungs-Id.
	 *
	 * @return der statischen Umsetzer fuer die Betriebsmeldungs-Id.
	 */
	public static final BetriebsmeldungIdKonverter getStaticBmvIdKonverter() {
		return KONVERTER;
	}

}
