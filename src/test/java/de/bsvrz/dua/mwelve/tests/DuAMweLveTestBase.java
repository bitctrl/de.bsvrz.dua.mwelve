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

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.tests.DuATestBase;
import de.bsvrz.dua.tests.ProgParams;
import de.bsvrz.dua.mwelve.vew.VerwaltungMessWertErsetzungLVE;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import org.junit.After;
import org.junit.Before;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class DuAMweLveTestBase extends DuATestBase {
	protected VerwaltungMessWertErsetzungLVE _messWertErsetzungLVE;

	protected static String[] getLveArgs() {
		return new String[]{"-KonfigurationsBereichsPid=kb.duaTestFs"};
	}

	@Override
	protected String[] getConfigurationAreas() {
		return new String[]{"kb.duaTestFs"};
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		_messWertErsetzungLVE = new VerwaltungMessWertErsetzungLVE();
		_messWertErsetzungLVE.parseArguments(new ArgumentList(DuAMweLveTestBase.getLveArgs()));
		_messWertErsetzungLVE.initialize(_connection);
	}

	@After
	public void tearDown() throws Exception {
		_messWertErsetzungLVE.getVerbindung().disconnect(false, "");
		super.tearDown();
	}

	public static void publishPrognoseParamsFs(final SystemObject fs) {
		ProgParams prognoseParams = new ProgParams() {
			@Override
			public String startValueFor(final String param) {
				if(param.startsWith("q")) {
					return "490";
				}
				else {
					return "95";
				}
			}

			@Override
			public String alpha1ValueFor(final String param) {
				return "0,21";
			}

			@Override
			public String alpha2ValueFor(final String param) {
				return "0,25";
			}

			@Override
			public String beta1ValueFor(final String param) {
				return "0,22";
			}

			@Override
			public String beta2ValueFor(final String param) {
				return "0,24";
			}
		};

		publishPrognoseParam(fs.getPid(), "atg.verkehrsDatenKurzZeitTrendExtraPolationPrognoseFlinkFs", prognoseParams);
		publishPrognoseParam(fs.getPid(), "atg.verkehrsDatenKurzZeitTrendExtraPolationPrognoseNormalFs", prognoseParams);
		publishPrognoseParam(fs.getPid(), "atg.verkehrsDatenKurzZeitTrendExtraPolationPrognoseTrägeFs", prognoseParams);
	}
	
	public static void publishPrognoseParamsMq(final SystemObject mq) {
		ProgParams prognoseParams = new ProgParams() {
			@Override
			public String startValueFor(final String param) {
				if(param.startsWith("q")) {
					return "490";
				}
				else {
					return "95";
				}
			}

			@Override
			public String alpha1ValueFor(final String param) {
				return "0,21";
			}

			@Override
			public String alpha2ValueFor(final String param) {
				return "0,25";
			}

			@Override
			public String beta1ValueFor(final String param) {
				return "0,22";
			}

			@Override
			public String beta2ValueFor(final String param) {
				return "0,24";
			}
		};

		publishPrognoseParam(mq.getPid(), "atg.verkehrsDatenKurzZeitTrendExtraPolationPrognoseFlinkMq", prognoseParams);
		publishPrognoseParam(mq.getPid(), "atg.verkehrsDatenKurzZeitTrendExtraPolationPrognoseNormalMq", prognoseParams);
		publishPrognoseParam(mq.getPid(), "atg.verkehrsDatenKurzZeitTrendExtraPolationPrognoseTrägeMq", prognoseParams);
	}

	public static void publishPrognoseParam(final String pid, final String atgPid, ProgParams progParams) {
		
		String[] params = {"qKfz", "vKfz", "qLkw", "vLkw", "qPkw", "vPkw", "aLkw", "kKfz", "kLkw", "kPkw", "qB", "kB"};

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{");
		for(String param : params) {
			if(atgPid.endsWith("Mq")){
				param = Character.toUpperCase(param.charAt(0)) + param.substring(1);
			}
			stringBuilder.append(param)
					.append("Start:'")
					.append(progParams.startValueFor(param))
					.append("',")
					.append(param)
					.append(":{alpha1:'")
					.append(progParams.alpha1ValueFor(param))
					.append("',alpha2:'")
					.append(progParams.alpha2ValueFor(param))
					.append("',beta1:'")
					.append(progParams.beta1ValueFor(param))
					.append("',beta2:'")
					.append(progParams.beta2ValueFor(param))
					.append("'},");
		}
		stringBuilder.setLength(stringBuilder.length()-1);
		stringBuilder.append("}");

		fakeParamApp.publishParam(pid, atgPid, stringBuilder.toString());
	}

}
