/*
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.mwelve.tests.
 * 
 * de.bsvrz.dua.mwelve.tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.mwelve.tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.mwelve.tests.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.mwelve.tests;

import com.google.common.collect.ImmutableList;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.tests.DuaLayout;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestDuAMweLve extends DuAMweLveTestBase {

	private DataDescription _ddIn;
	private DataDescription _ddOut;
	private SystemObject[] _testFsA;
	private SystemObject[] _testFsB;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		// Fahrstreifen mit Ersatz
		_testFsA = new SystemObject[]{_dataModel.getObject("fs.mq.1.hfs"), _dataModel.getObject("fs.mq.2.hfs")};
		
		// Fahrstreifen ohen Ersatz
		_testFsB = new SystemObject[]{_dataModel.getObject("fs.mq.1b.hfs")};

		publishParams(_testFsA);
		publishParams(_testFsB);

		AttributeGroup atg = _dataModel.getAttributeGroup("atg.verkehrsDatenKurzZeitIntervall");
		Aspect aspInput = _dataModel.getAspect("asp.externeErfassung");
		Aspect aspOutput = _dataModel.getAspect("asp.messWertErsetzung");
		_ddIn = new DataDescription(atg, aspInput);
		_ddOut = new DataDescription(atg, aspOutput);

		Thread.sleep(1000);
	}

	protected void publishParams(final SystemObject[] testFsA) {
		for(SystemObject obj : testFsA) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallMessWertErsetzung",
			                          "{" +
					                          "MaxErsetzungsDauer:'5 Minuten'," +
					                          "MaxWiederholungsZeit:'2 Minuten'," +
					                          "MaxWiederholungAnzahl:'2'" +
					                          "}"
			);

			publishPrognoseParamsFs(obj);

		}
	}

	@Test
	public void testDua2022WithReplacement() throws Exception {
		startTestCase("DUA20.22.Ersatz.csv", _testFsA, Arrays.copyOfRange(_testFsA, 0, 1), _ddIn, _ddOut, new DuaLayout(){
			@Override
			public Collection<String> getIgnored() {
				return ImmutableList.of("T");
			}
		});
	}

	@Test
	public void testDua2022NoReplacement() throws Exception {
		startTestCase("DUA20.22.OhneErsatz.csv", _testFsB, _testFsB, _ddIn, _ddOut, new DuaLayout(){
			@Override
			public Collection<String> getIgnored() {
				return ImmutableList.of("T");
			}
		});
	}

	@Override
	public void sendData(final ResultData... resultDatas) throws SendSubscriptionNotConfirmed {
		_messWertErsetzungLVE.update(resultDatas);
	}
}
