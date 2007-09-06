/**
 * Segment 4 Daten¸bernahme und Aufbereitung (DUA), SWE 4.5 Messwertersetzung LVE
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
 * Weiﬂenfelser Straﬂe 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */
package de.bsvrz.dua.mwelve.vew;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.DuaVerkehrsNetz;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.FahrStreifen;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.MessQuerschnitt;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.MessQuerschnittAllgemein;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.MessQuerschnittVirtuell;
import de.bsvrz.sys.funclib.bitctrl.dua.test.DAVTest;
import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Test des Verwaltungsmoduls
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class VerwaltungMessWertErsetzungLVETest 
implements ClientSenderInterface{

	/**
	 * Intervalll‰nge in s
	 */
	private static final long T = 2;
	
	/**
	 * Wurzelverzeichnis mit den Testdaten
	 */
	private static final String ROOT = "C:\\Dokumente und Einstellungen\\Thierfelder\\" + //$NON-NLS-1$
			"workspace3.3\\de.bsvrz.dua.mwelve\\extra\\testDatenBitCtrl\\"; //$NON-NLS-1$
	
	/**
	 * Verbindungsdaten
	 */
	public static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083", //$NON-NLS-1$ 
			"-benutzer=Tester", //$NON-NLS-1$
			"-authentifizierung=c:\\passwd1", //$NON-NLS-1$
			"-debugLevelStdErrText=INFO", //$NON-NLS-1$
			"-debugLevelFileText=INFO" }; //$NON-NLS-1$
	
	/**
	 * Verbindung zum Datenverteiler
	 */
	private ClientDavInterface dav = null;
	
	
	/**
	 * Verbindung zum Datenverteiler herstellen und laden des
	 * Verkehrsnetzes
	 * 
	 * @throws Exception wird weitergereicht
	 */
	@Before
	public void setUp()
	throws Exception{
		dav = DAVTest.getDav(CON_DATA);		
	}
	
	
	/**
	 * Testet, ob die innerhalb der Testkonfiguration beschriebenen Verweise
	 * von Fahrstreifen auf ihre Nachbar- bzw. Ersatzfahrstreifen richtig ausgelesen
	 * werden.
	 */
	@Test
	public void testVerkehrsNetzLaden()
	throws Exception{
		
		DuaVerkehrsNetz.initialisiere(dav);
		
		/**
		 * Hole alle Objekte von virtuellen Fahrstreifen
		 */
		Collection<SystemObject> fsMenge = new HashSet<SystemObject>();
		
		SystemObject fs1 = dav.getDataModel().getObject("AAA.Test.fs.kzd.1"); //$NON-NLS-1$
		SystemObject fs2 = dav.getDataModel().getObject("AAA.Test.fs.kzd.2"); //$NON-NLS-1$
		SystemObject fs3 = dav.getDataModel().getObject("AAA.Test.fs.kzd.3"); //$NON-NLS-1$
		
		SystemObject fs2_1 = dav.getDataModel().getObject("AAA.Test.fs.kzd.2.1"); //$NON-NLS-1$
		SystemObject fs2_2 = dav.getDataModel().getObject("AAA.Test.fs.kzd.2.2"); //$NON-NLS-1$
		
		SystemObject fs3_1 = dav.getDataModel().getObject("AAA.Test.fs.kzd.3.1"); //$NON-NLS-1$
		SystemObject fs3_2 = dav.getDataModel().getObject("AAA.Test.fs.kzd.3.2"); //$NON-NLS-1$
		
		SystemObject fs4_1 = dav.getDataModel().getObject("AAA.Test.fs.kzd.4.1"); //$NON-NLS-1$

		SystemObject fs6_1 = dav.getDataModel().getObject("AAA.Test.fs.kzd.6.1"); //$NON-NLS-1$
		SystemObject fs6_2 = dav.getDataModel().getObject("AAA.Test.fs.kzd.6.2"); //$NON-NLS-1$
		
		SystemObject fs7_1 = dav.getDataModel().getObject("AAA.Test.fs.kzd.7.1"); //$NON-NLS-1$

		SystemObject fs8_1 = dav.getDataModel().getObject("AAA.Test.fs.kzd.8.1"); //$NON-NLS-1$
		
		fsMenge.add(fs1);
		fsMenge.add(fs2);
		fsMenge.add(fs3);
		
		fsMenge.add(fs2_1);
		fsMenge.add(fs2_2);

		fsMenge.add(fs3_1);
		fsMenge.add(fs3_2);

		fsMenge.add(fs4_1);

		fsMenge.add(fs6_1);
		fsMenge.add(fs6_2);
		
		fsMenge.add(fs7_1);
		
		fsMenge.add(fs8_1);

		
		/**
		 * Hole alle Objekte von Messquerschnitten
		 */
		Collection<SystemObject> mqMenge = new HashSet<SystemObject>();

		SystemObject mq1 = dav.getDataModel().getObject("AAA.Test.mq.1"); //$NON-NLS-1$
		SystemObject mq2 = dav.getDataModel().getObject("AAA.Test.mq.2"); //$NON-NLS-1$
		SystemObject mq3 = dav.getDataModel().getObject("AAA.Test.mq.3"); //$NON-NLS-1$
		SystemObject mq4 = dav.getDataModel().getObject("AAA.Test.mq.4"); //$NON-NLS-1$
		SystemObject mq6 = dav.getDataModel().getObject("AAA.Test.mq.6"); //$NON-NLS-1$
		SystemObject mq7 = dav.getDataModel().getObject("AAA.Test.mq.7"); //$NON-NLS-1$
		SystemObject mq8 = dav.getDataModel().getObject("AAA.Test.mq.8"); //$NON-NLS-1$
		
		mqMenge.add(mq1);
		mqMenge.add(mq2);
		mqMenge.add(mq3);
		mqMenge.add(mq4);
		mqMenge.add(mq6);
		mqMenge.add(mq7);
		mqMenge.add(mq8);

		/**
		 * Hole alle Objekte von virtuellen Messquerschnitten
		 */
		Collection<SystemObject> mqMengeVirtuell = new HashSet<SystemObject>();
		SystemObject mqv5 = dav.getDataModel().getObject("AAA.Test.mqv.5"); //$NON-NLS-1$
		mqMengeVirtuell.add(mqv5);
		
		/**
		 * Hole alle Objekte von allgemeinen Messquerschnitten
		 */
		Collection<SystemObject> mqMengeAllg = new HashSet<SystemObject>();
		mqMengeAllg.addAll(mqMenge);
		mqMengeAllg.addAll(mqMengeVirtuell);
		
		/**
		 * Test ob alle Objekte ermittelt wurden (und keine doppelt)
		 */
		Assert.assertEquals(MessQuerschnittAllgemein.getAlleInstanzen().size(), mqMengeAllg.size());
		for(SystemObject obj:mqMengeAllg){
			Assert.assertEquals(obj, MessQuerschnittAllgemein.getInstanz(obj).getSystemObject());
		}

		Assert.assertEquals(MessQuerschnitt.getInstanzen().size(), mqMenge.size());
		for(SystemObject obj:mqMenge){
			Assert.assertEquals(obj, MessQuerschnitt.getInstanz(obj).getSystemObject());
		}

		Assert.assertEquals(MessQuerschnittVirtuell.getInstanzen().size(), mqMengeVirtuell.size());
		for(SystemObject obj:mqMengeVirtuell){
			Assert.assertEquals(obj, MessQuerschnittVirtuell.getInstanz(obj).getSystemObject());
		}
	
		/**
		 * Test der ermittelten Ersatzfahrstreifen
		 */
		Assert.assertEquals("fs3 = ErsatzFS(fs1)", FahrStreifen.getInstanz(fs3).getSystemObject(), FahrStreifen.getInstanz(fs1).getErsatzFahrStreifen().getSystemObject()); //$NON-NLS-1$
		Assert.assertEquals("fs1 = ErsatzFS(fs2)", FahrStreifen.getInstanz(fs1).getSystemObject(), FahrStreifen.getInstanz(fs2).getErsatzFahrStreifen().getSystemObject()); //$NON-NLS-1$
		Assert.assertEquals("fs2 = ErsatzFS(fs3)", FahrStreifen.getInstanz(fs2).getSystemObject(), FahrStreifen.getInstanz(fs3).getErsatzFahrStreifen().getSystemObject()); //$NON-NLS-1$
		
		
		Assert.assertEquals("null = ErsatzFS(fs2_1)", FahrStreifen.getInstanz(fs2_1).getErsatzFahrStreifen(), null); //$NON-NLS-1$
		Assert.assertEquals("fs2_1 = ErsatzFS(fs2_2)", FahrStreifen.getInstanz(fs2_2).getErsatzFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs2_1).getSystemObject()); //$NON-NLS-1$
				
		Assert.assertEquals("fs2_1 = ErsatzFS(fs3_1)", FahrStreifen.getInstanz(fs3_1).getErsatzFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs2_1).getSystemObject()); //$NON-NLS-1$
		Assert.assertEquals("fs3_1 = ErsatzFS(fs3_2)", FahrStreifen.getInstanz(fs3_2).getErsatzFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs3_1).getSystemObject()); //$NON-NLS-1$
		
		Assert.assertEquals("null = ErsatzFS(fs4_1)", FahrStreifen.getInstanz(fs4_1).getErsatzFahrStreifen(), null); //$NON-NLS-1$
		
		Assert.assertEquals("fs4_1 = ErsatzFS(fs6_1)", FahrStreifen.getInstanz(fs6_1).getErsatzFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs4_1).getSystemObject()); //$NON-NLS-1$
		
		Assert.assertEquals("fs2_1 = ErsatzFS(fs7_1)", FahrStreifen.getInstanz(fs7_1).getErsatzFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs2_1).getSystemObject()); //$NON-NLS-1$
		
		Assert.assertEquals("null = ErsatzFS(fs8_1)", FahrStreifen.getInstanz(fs8_1).getErsatzFahrStreifen(), null); //$NON-NLS-1$
		
		/**
		 * Test der ermittelten Nachbarfahrstreifen 
		 */		
		Assert.assertEquals(FahrStreifen.getInstanz(fs1).getNachbarFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs2).getSystemObject());		
		Assert.assertEquals(FahrStreifen.getInstanz(fs2).getNachbarFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs3).getSystemObject());
		Assert.assertEquals(FahrStreifen.getInstanz(fs3).getNachbarFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs2).getSystemObject());
		
		Assert.assertEquals(FahrStreifen.getInstanz(fs2_1).getNachbarFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs2_2).getSystemObject());
		Assert.assertEquals(FahrStreifen.getInstanz(fs2_2).getNachbarFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs2_1).getSystemObject());
		
		Assert.assertEquals(FahrStreifen.getInstanz(fs3_1).getNachbarFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs3_2).getSystemObject());
		Assert.assertEquals(FahrStreifen.getInstanz(fs3_2).getNachbarFahrStreifen().getSystemObject(), FahrStreifen.getInstanz(fs3_1).getSystemObject());
		
		Assert.assertEquals(FahrStreifen.getInstanz(fs4_1).getNachbarFahrStreifen(), null);
		Assert.assertEquals(FahrStreifen.getInstanz(fs6_1).getNachbarFahrStreifen(), null);
		Assert.assertEquals(FahrStreifen.getInstanz(fs7_1).getNachbarFahrStreifen(), null);
		Assert.assertEquals(FahrStreifen.getInstanz(fs8_1).getNachbarFahrStreifen(), null);
	}
	
	
	/**
	 * Testet, ob die Daten ordnungsgem‰ﬂ durch die SWE MWE wieder publiziert werden
	 */
	@Test
	public void testMWEDatenfluss()
	throws Exception{
		
		SystemObject fs1 = dav.getDataModel().getObject("AAA.Test.fs.kzd.1"); //$NON-NLS-1$
		SystemObject fs3 = dav.getDataModel().getObject("AAA.Test.fs.kzd.3"); //$NON-NLS-1$
		
		AttributeGroup atg = dav.getDataModel().getAttributeGroup(DUAKonstanten.ATG_KZD);
		Aspect asp = dav.getDataModel().getAspect(DUAKonstanten.ASP_EXTERNE_ERFASSUNG);
		DataDescription dd = new DataDescription(atg, asp, (short)0);
		
		dav.subscribeSender(this, new SystemObject[]{fs1, fs3}, dd, SenderRole.source());
		Pause.warte(Konstante.SEKUNDE_IN_MS);
		
		TestFahrstreifenImporter importer = new TestFahrstreifenImporter(dav, ROOT + "Fahrstreifen1.csv"); //$NON-NLS-1$
		TestFahrstreifenImporter importer3 = new TestFahrstreifenImporter(dav, ROOT + "Fahrstreifen3.csv"); //$NON-NLS-1$
		TestFahrstreifenImporter.setT(Konstante.SEKUNDE_IN_MS * T);
		
		GregorianCalendar start = new GregorianCalendar();
		start.setTimeInMillis(System.currentTimeMillis());
		start.set(Calendar.MILLISECOND, 0);
		long sekundenJetzt = start.get(Calendar.SECOND);
		sekundenJetzt = ((long)sekundenJetzt / T) * T;
		start.set(Calendar.SECOND, (int)sekundenJetzt);
		start.add(Calendar.SECOND, (int)T);
		
		long startDaten = start.getTimeInMillis();
		long startSenden = start.getTimeInMillis() + Konstante.SEKUNDE_IN_MS * T + Konstante.SEKUNDE_IN_MS/2 * T;
		
		while(true){
			Data data1 = importer.getNaechstenDatensatz(atg);
			Data data3 = importer3.getNaechstenDatensatz(atg);
			DAVTest.warteBis(startSenden);
			
			ResultData resultat = new ResultData(fs1, dd, startDaten, data1);
			ResultData resultat3 = new ResultData(fs3, dd, startDaten, data3);
			dav.sendData(resultat);
			dav.sendData(resultat3);
			startDaten += Konstante.SEKUNDE_IN_MS * T;
			startSenden += Konstante.SEKUNDE_IN_MS * T;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		// TODO Auto-generated method stub
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		return false;
	}

}
