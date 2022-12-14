/**
 * Copyright (C) 2016, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package jmt.engine.jwat.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import jmt.engine.jwat.JwatSession;
import jmt.engine.jwat.ProgressStatusListener;
import jmt.engine.jwat.workloadAnalysis.utils.FormatFileReader;
import jmt.gui.jwat.JWATConstants;

public class Loader implements JWATConstants {

	public static int calcNumOfObs(String filePath) throws FileNotFoundException, IOException {
		int numObs = 0;
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		while (reader.readLine() != null) {
			numObs++;
		}
		reader.close();
		return numObs;
	}

	public static void readData(String filePath, Parameter param, ProgressShow prShow, ProgressStatusListener pStatusList) {
		InputLoader loads = null;
		VariableMapping[] map;

		//Initializing the object necessary to load data.
		map = new VariableMapping[param.getNumVar()];

		int[] varType = param.getVarType();

		//Creo mappings
		for (int i = 0; i < map.length; i++) {
			switch (varType[i]) {
			case Parameter.NUMBER:
				map[i] = null;
				break;
			case Parameter.DATE:
				map[i] = new DataMapping();
				break;
			case Parameter.STRING:
				map[i] = new StringMapping();
				break;
			}
		}
		try {
			switch (param.getSampleMethod()) {
			case Parameter.ALL_INPUT:
				loads = new AllInputLoader(param, filePath, map, prShow);
				break;
			case Parameter.INTERVAL_INPUT:
				loads = new IntervalInputLoader(param, filePath, map, prShow);
				break;
			case Parameter.RANDOM_INPUT:
				loads = new RndInputLoader(param, filePath, map, prShow);
				break;
			}
		} catch (FileNotFoundException e) {
			pStatusList.statusEvent(new EventFinishAbort("Loading aborted. File not found."));
		}
		loads.addStatusListener(pStatusList);
		loads.start();
	}

	public static Parameter loadParameter(File demoPath, String demoName) throws FileNotFoundException, IOException {
		FormatFileReader form = new FormatFileReader(new File(demoPath, demoName + "Format.jwatformat").getAbsolutePath());

		boolean[] varSelected = new boolean[form.getNumVars()];
		String[] varName = new String[form.getNumVars()];
		String[] regularExp = new String[form.getNumVars()];
		String[] tokenExp = new String[form.getNumVars()];
		int[] varType = new int[form.getNumVars()];

		int options[] = new int[] { Loader.calcNumOfObs(new File(demoPath, demoName + "Data.jwat").getAbsolutePath()) };

		for (int i = 0; i < form.getNumVars(); i++) {
			varSelected[i] = true;
			varType[i] = form.getType();
			varName[i] = form.getName();
			regularExp[i] = form.getRegExpr();
			tokenExp[i] = form.getDelimiters();
			if (tokenExp[i].length() == 0) {
				tokenExp[i] = null;
			}
			form.next();
		}

		return new Parameter(varSelected, varType, regularExp, tokenExp, varName, Parameter.ALL_INPUT, null, options, null, null);
	}

	/**
	 * This function reads a specific session from an XML file
	 * 
	 * @param filePath
	 */
	public static void loadSession(String fileName, ProgressShow prShow, ProgressStatusListener pStatusList, JwatSession session) {
		try {
			SessionLoader sLoader = new SessionLoader(fileName, prShow);
			sLoader.addStatusListener(pStatusList);
			sLoader.start();
		} catch (IOException e) {
			pStatusList.statusEvent(new EventFinishAbort("Loading session failed."));
		}
	}

}
