package jmt.engine.jwat.input;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import jmt.engine.jwat.MatrixObservations;
import jmt.engine.jwat.Observation;
import jmt.framework.data.MacroReplacer;
import jmt.gui.jwat.JWATConstants;

import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class AllInputLoader extends InputLoader implements JWATConstants {

	public AllInputLoader(Parameter param, String fileName, VariableMapping[] map, ProgressShow prg) throws FileNotFoundException {
		super(param, fileName, map, prg);
	}

	@Override
	public Object construct() {
		int i;
		boolean[] sel = param.getVarSelected();
		FileWriter w = null;
		PatternMatcherInput input = new PatternMatcherInput("");
		Perl5Compiler myComp = new Perl5Compiler();
		Perl5Matcher myMatch = new Perl5Matcher();
		String line = "";
		String[] regExp = param.getRegularExp();
		String[] separator = param.getSeparator();
		double[] lineValue = new double[param.getNumVarSelected()];
		String[] lineToken = new String[param.getNumVar()];
		String parseToken = null;
		MatrixObservations m;

		//Initialize the input log file.
		try {
			w = new FileWriter(MacroReplacer.replace(LOG_FILE_NAME));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			//Init the progress show
			initShow(param.getOptions()[0] + 1);
			countObs = 0;
			totalRaw = 0;
			//Read the first line
			line = reader.readLine();
			while (line != null) {
				//Check if user has pressed the cancel button
				if (isCanceled()) {
					try {
						w.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					valori = null;
					msg = INPUT_MSG_ABORT;
					return null;
				}
				input.setInput(line);
				totalRaw++;
				countObs++;
				//Update the progress show
				if (totalRaw % getStep() == 0) {
					updateInfos(totalRaw, "<HTML># observations read: " + totalRaw + "<p># errors: " + (totalRaw - countObs) + "</HTML>", false);
				}
				//Read strings and check format
				for (i = 0; i < param.getNumVar(); i++) {
					//Read tokens with separators (if defined) regardless of what's inside
					if (separator[i] != null) {
						parseToken = separator[i];
					} else {
						parseToken = regExp[i];
					}

					if (myMatch.contains(input, myComp.compile(parseToken))) {
						if (sel[i]) {
							//Get the token
							lineToken[i] = myMatch.getMatch().toString();
							if (separator[i] != null) {
								// If the token has separators remove them
								lineToken[i] = lineToken[i].substring(1, lineToken[i].length() - 1);
								//Take what I need from what's left
								if (myMatch.contains(lineToken[i], myComp.compile(regExp[i]))) {
									lineToken[i] = myMatch.getMatch().toString();
								} else {
									//Wrong row, token dose not contains required data
									countObs--;
									try {
										w.write("Error in row " + totalRaw + " : Element " + i + " is wrong\n");
									} catch (IOException e) {
										e.printStackTrace();
									}
									break;
								}
							}
						}
					} else {
						//Wrong raw, element not found
						countObs--;
						try {
							w.write("Error in row " + totalRaw + " : Line does not match format (element " + i + " not found)\n");
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					}
				}
				int j = 0;
				//Correct raw
				if (i == param.getNumVar()) {
					if (input.endOfInput()) {
						//Parse the string value in their correct formats
						for (i = 0; i < param.getNumVar(); i++) {
							if (sel[i]) {
								if (map[i] == null) {
									lineValue[j++] = Double.parseDouble(lineToken[i]);
								} else {
									lineValue[j++] = map[i].addNewValue(lineToken[i]);
								}
							}
						}
						valori.add(new Observation(lineValue, countObs));
					} else {
						//Worong row, the line was not processed completely
						countObs--;
						try {
							w.write("Error in row " + totalRaw + " : Too many fields\n");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				line = reader.readLine();
			}
			try {
				w.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//Wrong format, no element loaded
			if (valori.size() == 0) {
				closeView();
				valori = null;
				msg = INPUT_MSG_ABORT_WRONG_FORMAT;
				return null;
			}
			updateInfos(totalRaw, "Calculating Statistics...", false);
			m = new MatrixObservations(valori.toArray(new Observation[valori.size()]), param.getSelName(), param.getSelType(), map);
			updateInfos(totalRaw + 1, "Done", true);
			return m;

		} catch (Exception e) {
			closeView();
			try {
				w.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			valori = null;
			msg = INPUT_MSG_FAIL;
			return null;
		} catch (OutOfMemoryError err) {
			closeView();
			try {
				w.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			valori = null;
			msg = "Out of memory. Try with more memory";
			return null;
		}
	}
}
