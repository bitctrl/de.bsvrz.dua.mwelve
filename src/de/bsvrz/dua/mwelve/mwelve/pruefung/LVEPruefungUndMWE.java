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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.lve.FahrStreifen;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class LVEPruefungUndMWE
extends AbstraktBearbeitungsKnotenAdapter{

	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Menge aller Fahrstreifen, die sowohl einen Nachbar- wie auch einen
	 * Ersatzfahrstreifen besitzen und somit hier plausibilisiert werden
	 */
	private Set<FahrStreifen> pruefungsFahrstreifen = new HashSet<FahrStreifen>();

	/**
	 * Mapt alle (hier relevanten) Objekte vom Typ <code>FahrStreifen</code> auf
	 * deren Puffer-Objekte. Relevant sind sowohl die Objekte, die hier direkt
	 * plausibilisiert werden, wie auch deren Nachbar- bzw. Ersatzfahrstreifen 
	 */
	private Map<FahrStreifen, FSDatenPuffer> fsAufDatenPuffer = 
													new HashMap<FahrStreifen, FSDatenPuffer>(); 

	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
	throws DUAInitialisierungsException {
		
		/**
		 * Puffere alle Fahrstreifen, die sowohl einen Nachbar-
		 * als auch einen Ersatzfahrtstreifen haben (und deren Nachbar-
		 * und Ersatzfahrtstreifen)
		 */
		for(SystemObject fsObjekt:dieVerwaltung.getSystemObjekte()){
			FahrStreifen fs = FahrStreifen.getInstanz(fsObjekt);
			if(fs != null){
				if(fs.getErsatzFahrStreifen() != null && fs.getNachbarFahrStreifen() != null){
					this.pruefungsFahrstreifen.add(fs);
					this.fsAufDatenPuffer.put(fs, new FSDatenPuffer(fs));
					this.fsAufDatenPuffer.put(fs.getNachbarFahrStreifen(), new FSDatenPuffer(fs.getNachbarFahrStreifen()));
					this.fsAufDatenPuffer.put(fs.getNachbarFahrStreifen(), new FSDatenPuffer(fs.getNachbarFahrStreifen()));
				}
			}else{
				throw new DUAInitialisierungsException("Fahrstreifen " + fsObjekt +  //$NON-NLS-1$
						" konnte nicht identifiziert werden"); //$NON-NLS-1$
			}
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		if(resultate != null){
			List<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();
			synchronized (this) {
				for(ResultData resultat:resultate){
					if(resultat != null){
						FahrStreifen fs = FahrStreifen.getInstanz(resultat.getObject());
						if(fs != null){
							if(resultat.getData() != null){
								FSDatenPuffer pufferFS = this.fsAufDatenPuffer.get(fs);
								if(pufferFS != null){
									pufferFS.aktuelisiereDaten(resultat);
								}
								
								if(this.pruefungsFahrstreifen.contains(fs)){
									FSDatenPuffer pufferNachbarFS = this.fsAufDatenPuffer.get(fs.getNachbarFahrStreifen());
									FSDatenPuffer pufferErsatzFS = this.fsAufDatenPuffer.get(fs.getErsatzFahrStreifen());
									weiterzuleitendeResultate.add(
											plausibilisiere(pufferFS, pufferNachbarFS, pufferErsatzFS));
								}else{
									weiterzuleitendeResultate.add(resultat);
								}
							}else{
								weiterzuleitendeResultate.add(resultat);
							}						
						}else{
							LOGGER.warning("Fahrstreifen-Datum zu " + resultat.getObject() + //$NON-NLS-1$
									" konnte nicht extrahiert werden"); //$NON-NLS-1$
							weiterzuleitendeResultate.add(resultat);
						}						
					}
				}				
			}
			
			/**
			 * Ergebnisse an den nächsten Bearbeitungsknoten weiterleiten
			 */
			if(this.knoten != null && !weiterzuleitendeResultate.isEmpty()){
				this.knoten.aktualisiereDaten(weiterzuleitendeResultate.toArray(new ResultData[0]));
			}
		}
	}


	/**
	 * 
	 * @param fs
	 * @param fsNachbar
	 * @param fsErsatz
	 * @return
	 */
	private final ResultData plausibilisiere(FSDatenPuffer fs, FSDatenPuffer fsNachbar, FSDatenPuffer fsErsatz){
		ResultData ergebnis = null; // sowas wie fs.getAktuellesDatum()
		
		if(!fsNachbar.isAktuellDefekt() && !fsErsatz.isAktuellDefekt()){
			ergebnis = plausibilisiereNachVorschrift1(fs, fsNachbar, fsErsatz);
		}else
		if(fsNachbar.isAktuellDefekt() && !fsErsatz.isAktuellDefekt()){
			ergebnis = plausibilisiereNachVorschrift2(fs, fsErsatz);
		}
		
		return schreibeDatenFortWennNotwenig(ergebnis, fs);
	}
	
	
	/**
	 * 
	 * @param fs
	 * @param fsNachbar
	 * @param fsErsatz
	 * @return
	 */
	private final ResultData plausibilisiereNachVorschrift1(
			FSDatenPuffer fs, FSDatenPuffer fsNachbar, FSDatenPuffer fsErsatz){
		
		return null;
	}
	
	
	/**
	 * 
	 * @param fs
	 * @param fsErsatz
	 * @return
	 */
	private final ResultData plausibilisiereNachVorschrift2(
			FSDatenPuffer fs, FSDatenPuffer fsErsatz){
		return null;
	}
	
	
	/**
	 * 
	 * @param plausibilisiertesDatum
	 * @param fs
	 * @return
	 */
	private final ResultData schreibeDatenFortWennNotwenig(ResultData plausibilisiertesDatum, FSDatenPuffer fs){
		return null;
	}

	
	
	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return null;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// hier findet keine Publikation statt		
	}
	
}
