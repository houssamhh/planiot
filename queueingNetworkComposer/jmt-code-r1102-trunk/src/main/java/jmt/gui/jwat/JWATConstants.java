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

package jmt.gui.jwat;

import java.io.File;
import java.text.DecimalFormat;

public interface JWATConstants {

	// Position of JWAT directories 
	public static final int BUTTONSIZE = 25;

	//Variable Type constants
	public static final short NUMERIC = 0;
	public static final short STRING = 1;
	public static final short DATE = 2;

	public static final String HTML_FONT_SMALL = "<font size=\"2\">";
	public static final String HTML_FONT_TIT_END_NO_CAPO = "</b></font>";

	public static final DecimalFormat defaultFormat = new DecimalFormat("000.000E0");

	public static final int KMEANS = 0;
	public static final int FUZZYK = 1;

	//Loading constants
	public static final String LOG_FILE_NAME = "${jmt.work.dir}/LoadingError.log";
	public static final String EXAMPLE_PATH = "examples" + File.separator + "jwat" + File.separator;
	public static final String INPUT_MSG_ABORT = "Loading aborted by user";
	public static final String INPUT_MSG_ABORT_WRONG_FORMAT = "Wrong format, no data match the given pattern";
	public static final String INPUT_MSG_FAIL = "Fatal error in loading data!!";

	public static final int WORKLOAD_INPUT_PANEL = 1;
	public static final int WORKLOAD_BIVARIATE_PANEL = 2;
	public static final int WORKLOAD_CLUSTERING_PANEL = 3;
	public static final int WORKLOAD_INFOCLUSTERING_PANEL = 4;

	public static final int TRAFFIC_INPUT_PANEL = 1;
	public static final int TRAFFIC_EPOCH_PANEL = 2;
	public static final int TRAFFIC_TEXTUAL_PANEL = 3;
	public static final int TRAFFIC_GRAPH_PANEL = 4;
	public static final int TRAFFIC_GRAPHARRIVAL_PANEL = 5;

	public static final int FITTING_INPUT_PANEL = 1;
	public static final int FITTING_PARETO_PANEL = 2;
	public static final int FITTING_EXPO_PANEL = 3;

}
