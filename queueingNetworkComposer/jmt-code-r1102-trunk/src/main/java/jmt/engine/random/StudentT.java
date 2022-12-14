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

package jmt.engine.random;

import jmt.common.exception.IncorrectDistributionParameterException;
import jmt.engine.math.Probability;
import jmt.engine.math.Sfun;

/**
 * 
 * This is the StudentT distribution (see the constructor description
 * for its pdf definition).
 * 
 * <br><br>Copyright (c) 2003
 * <br>Politecnico di Milano - dipartimento di Elettronica e Informazione
 * @author Fabrizio Frontera - ffrontera@yahoo.it
 * @author Modified by Stefano Omini, 7/5/2004
 * @author Modified by Bertoli Marco, 8/9/2005
 * 
 */
public class StudentT extends AbstractDistribution implements Distribution {

	/**
	 * This is the constructor. It creates a new student T distribution which
	 * is defined from pdf:
	 * <pre>             G((f+1)/2)                    1
	 * pdf(x)= -------------------- *  -------------------------
	 *          (sqrt(pi*f)*G(f/2)      (1+((x^2)/f))^((f+1)/2)</pre>
	 * where G(a) is the gamma (also called "Eulero") function.
	 * f is called degrees of "freedom".
	 */
	public StudentT() {
	}

	/**
	 * This method is used to obtain from the distribution its probability distribution
	 * function evaluated where required by the user.
	 *
	 * @param x double indicating where to evaluate the pdf.
	 * @param p parameter of the student T distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the probability distribution function evaluated in x.
	 */
	public double pdf(double x, Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			double freedom = ((StudentTPar) p).getFreedom();
			double value = Sfun.logGamma((freedom + 1.0) / 2.0) - Sfun.logGamma(freedom / 2.0);
			double TERM = Math.exp(value) / Math.sqrt(Math.PI * freedom);
			return TERM * Math.pow((1.0 + x * x / freedom), -(freedom + 1.0) * 0.5);
		} else {
			throw new IncorrectDistributionParameterException("Remember: freedom must be an integer gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution its cumulative distribution
	 * function evaluated where required by the user.
	 *
	 * @param x double indicating where to evaluate the cdf.
	 * @param p parameter of the student T distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the cumulative distribution function evaluated in x.
	 */
	public double cdf(double x, Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			double freedom = ((StudentTPar) p).getFreedom();
			return Probability.studentT(freedom, x);
		} else {
			throw new IncorrectDistributionParameterException("Remember: freedom must be an integer gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution the value of its own
	 * theoretic mean.
	 *
	 * @param p parameter of the student T distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the theoretic mean of the distribution.
	 */
	public double theorMean(Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			return 0.0;
		} else {
			throw new IncorrectDistributionParameterException("Remember: freedom must be an integer gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution the value of its own
	 * theoretic variance.
	 *
	 * @param p parameter of the student T distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the theoretic variance of the distribution.
	 */
	public double theorVariance(Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			double freedom = ((StudentTPar) p).getFreedom();
			if (freedom < 2.0) {
				return 0.0;
			} else {
				return freedom / (freedom - 2.0);
			}
		} else {
			throw new IncorrectDistributionParameterException("Remember: freedom must be an integer gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution the next number distributed
	 * according to the distribution parameter.
	 *
	 * @param p parameter of the student T distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the next random number of this distribution.
	 */
	public double nextRand(Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			double freedom = ((StudentTPar) p).getFreedom();
			double u, v, w;
			do {
				u = 2.0 * engine.raw() - 1.0;
				v = 2.0 * engine.raw() - 1.0;
				w = u * u + v * v;
			} while (w > 1.0);
			// If generated number is in the past, reruns this method
			double ret = (u * Math.sqrt(freedom * (Math.exp(-2.0 / freedom * Math.log(w)) - 1.0) / w));
			return (ret > 0.0) ? ret : nextRand(p);
		} else {
			throw new IncorrectDistributionParameterException("Remember: freedom must be an integer gtz");
		}
	}

} // end StudentT
