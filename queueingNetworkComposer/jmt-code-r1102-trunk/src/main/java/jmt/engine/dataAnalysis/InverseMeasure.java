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
 * It may be convenient to use an inverse measure to save computational time:
 * In fact, in some cases, it is easier to analyze a measure by passing samples which are
 * equals to 1/measure, instead of doing one division for each sample
 * (this would make simulation much slower).
 * At the end the correct value is passed, since the "get" methods are overridden.
 */

public class InverseMeasure extends jmt.engine.dataAnalysis.Measure {

	public InverseMeasure(String Name, double alfa, double precision, int maxData, boolean Verbose) {
		super(Name, alfa, precision, maxData, Verbose, null);
	}

	/** Gets a measure value, calculated as the inverse of measure mean.
	 * @return measure value.
	 */
	@Override
	public double getMeanValue() {
		if (receivedNoSamples()) {
			//no samples have been collected -> throughput is zero
			return 0.0;
		}
		if (hasBeenAborted() && (analyzer.getMean() == 0)) {
			//measure computation was aborted before noSampleTest could be done
			return 0.0;
		} else if (analyzer.getMean() > 0) {
			return 1.0 / analyzer.getMean();
		} else {
			return 0.0;
		}
	}

	/** Gets measure value: if the confidence requirements have not been
	 * reached, it is returned the value estimated up to that moment.
	 * @return measure value.
	 */
	@Override
	public double getEstimatedMeanValue() {
		if (analyzer.estimatedMean() > 0) {
			return 1 / analyzer.estimatedMean();
		} else {
			return 0.0;
		}
	}

	/** Gets lower limit of the inverse measure.
	 * @return Lower limit.
	 */
	@Override
	public double getLowerLimit() {
		if (receivedNoSamples()) {
			//no samples have been collected -> throughput is zero
			return 0.0;
		} else {
			return 1 / (analyzer.getMean() + analyzer.getConfInt());
		}
	}
	
	/** Gets upper limit of the inverse measure.
	 * @return Upper limit.
	 */
	@Override
	public double getUpperLimit() {
		if (receivedNoSamples()) {
			//no samples have been collected -> throughput is zero
			return 0.0;
		} else {
			return 1 / (analyzer.getMean() - analyzer.getConfInt());
		}
	}

	/** Gets last interval average value of the inverse measure.
	 * @return Last interval average value.
	 */
	@Override
	public double getLastIntervalAvgValue() {
		if (receivedNoSamples()) {
			//no samples have been collected -> throughput is zero
			return 0.0;
		} else {
			return 1 / (analyzer.getLastIntervalAvg());
		}
	}

}
