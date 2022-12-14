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

package jmt.engine.dataAnalysis;

/**
 * This class contains parameters that are shared by all DynamicDataAnalyzer objects.
 * If other values are not set, default ones are used.
 *
 * @author Stefano Omini
 */
public class SimParameters {

	//-----------------------BATCH NUMBER AND SIZE------------------------------------//

	/**number of batches */
	protected int numBatch = 128;
	/**number of samples per batch */
	protected int batchLen = 8;

	//-----------------------end BATCH NUMBER AND SIZE--------------------------------//

	//-----------------------NULL TEST HYPOTHESIS------------------------------------//

	//to save time and resources, null test is not made for each sample, but repeated
	//every nullTestPeriod samples (testRate is equal to nullTestPeriod/maxData)
	private double nullTestRate = 0.01;

	//the quantile required for the confidence interval of nullTest
	private double nullTestAlfa = 0.005;

	//-----------------------end NULL TEST HYPOTHESIS--------------------------------//

	//max samples which can be collected for each measure
	private int maxSamples = 1000000;
	//min samples which must be collected for each measure
	private int minSamples = 0;
	//max simulated time after which simulation will be stopped
	private double maxSimulatedTime = -1;
	//max processed events after which simulation will be stopped
	private int maxProcessedEvents = -1;

	private boolean disableStatisticStop = false;

	// logging attributes: path, replacement, delimiter, execution time
	String logfilepath;
	String logreplacemode;
	String logdelimiterchar;
	String logdeciamlseparatorchar;
	String logtimestampvalue;

	public SimParameters() {

	}

	/**
	 * Gets the number of batches
	 * @return the number of batches
	 */
	public int getNumBatch() {
		return numBatch;
	}

	/**
	 * Sets the number of batches
	 * @param numBatch the number of batches
	 */
	public void setNumBatch(int numBatch) {
		this.numBatch = numBatch;
	}

	/**
	 * Gets the batch size
	 * @return the batch size
	 */
	public int getBatchLen() {
		return batchLen;
	}

	/**
	 * Sets the batch size
	 * @param batchLen the batch size
	 */
	public void setBatchLen(int batchLen) {
		this.batchLen = batchLen;
	}

	/**
	 * Gets the accuracy of Null Test
	 * @return the accuracy of Null Test
	 */
	public double getNullTestAlfa() {
		return nullTestAlfa;
	}

	/**
	 * Set the accuracy of Null Test
	 * @param nullTestAlfa the accuracy of Null Test
	 */
	public void setNullTestAlfa(double nullTestAlfa) {
		this.nullTestAlfa = nullTestAlfa;
	}

	/**
	 * Gets the test rate of Null Test
	 * @return the test rate of Null Test
	 */
	public double getNullTestRate() {
		return nullTestRate;
	}

	/**
	 * Set the test rate of Null Test
	 * @param nullTestRate the test rate of Null Test
	 */
	public void setNullTestRate(double nullTestRate) {
		this.nullTestRate = nullTestRate;
	}

	/**
	 * Gets the max number of samples which can be collected for each measure
	 * @return the max number of samples which can be collected for each measure
	 */
	public int getMaxSamples() {
		return maxSamples;
	}

	/**
	 * Sets the max number of samples which can be collected for each measure
	 * @param maxSamples the max number of samples which can be collected for each measure
	 */
	public void setMaxSamples(int maxSamples) {
		this.maxSamples = maxSamples;
	}

	/**
	 * Gets the min number of samples which must be collected for each measure
	 * @return the min number of samples which must be collected for each measure
	 */
	public int getMinSamples() {
		return minSamples;
	}

	/**
	 * Sets the min number of samples which must be collected for each measure
	 * @param minSamples the min number of samples which must be collected for each measure
	 */
	public void setMinSamples(int minSamples) {
		this.minSamples = minSamples;
	}

	/**
	 * Gets the max simulated time after which simulation will be stopped
	 * @return the max simulated time after which simulation will be stopped
	 */
	public double getMaxSimulatedTime() {
		return maxSimulatedTime;
	}

	/**
	 * Sets the max simulated time after which simulation will be stopped
	 * @param maxSimulatedTime max simulated time after which simulation will be stopped
	 */
	public void setMaxSimulatedTime(double maxSimulatedTime) {
		this.maxSimulatedTime = maxSimulatedTime;
	}

	/**
	 * Gets the max processed events after which simulation will be stopped
	 * @return the max processed events after which simulation will be stopped
	 */
	public int getMaxProcessedEvents() {
		return maxProcessedEvents;
	}

	/**
	 * Sets the max processed events after which simulation will be stopped
	 * @param maxProcessedEvents max processed events after which simulation will be stopped
	 */
	public void setMaxProcessedEvents(int maxProcessedEvents) {
		this.maxProcessedEvents = maxProcessedEvents;
	}

	/**
	 * Tells if Confidence interval (alpha and precision) is not to be used as stopping criteria 
	 * @return the disableStatisticStop
	 */
	public boolean isDisableStatisticStop() {
		return disableStatisticStop;
	}

	/**
	 * Sets if Confidence interval (alpha and precision) is not to be used as stopping criteria
	 * @param disableStatisticStop the disableStatisticStop to set
	 */
	public void setDisableStatisticStop(boolean disableStatisticStop) {
		this.disableStatisticStop = disableStatisticStop;
	}

	public void setLogPath(String logfilepath) {
		this.logfilepath = logfilepath;
	}

	public void setLogDelimiter(String logdelimiterchar) {
		this.logdelimiterchar = logdelimiterchar;
	}

	public void setLogDecimalSeparator(String logdeciamlseparatorchar) {
		this.logdeciamlseparatorchar = logdeciamlseparatorchar;
	}

	public void setLogReplaceMode(String logreplacemode) {
		this.logreplacemode = logreplacemode;
	}

	public void setTimestampValue(String logtimestamp) {
		this.logtimestampvalue = logtimestamp;
	}

	public String getLogPath() {
		return logfilepath;
	}

	public String getLogDelimiter() {
		return logdelimiterchar;
	}

	public String getLogDecimalSeparator() {
		return logdeciamlseparatorchar;
	}

	public String getLogReplaceMode() {
		return logreplacemode;
	}

	public String getTimestampValue() {
		return logtimestampvalue;
	}

}
