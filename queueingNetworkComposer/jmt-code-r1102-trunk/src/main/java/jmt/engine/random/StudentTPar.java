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

/**
 * 
 * This is the parameter that should be passed to the StudentT
 * distribution.
 * 
 * <br><br>Copyright (c) 2003
 * <br>Politecnico di Milano - dipartimento di Elettronica e Informazione
 * @author Fabrizio Frontera - ffrontera@yahoo.it
 * 
 */
public class StudentTPar extends AbstractParameter implements Parameter {

	private double freedom;

	/**
	 * It creates a new student T parameter with the provided degrees of freedom.
	 *
	 * @param freedom double containing the degrees of freedom of the new student T
	 * distribution. It is defined as double but must be an integer greater than zero.
	 * @throws IncorrectDistributionParameterException if the degrees of freedom is
	 * not an integer greater than zero.
	 *
	 */
	public StudentTPar(double freedom) throws IncorrectDistributionParameterException {
		if (freedom < 1 || Math.floor(freedom) != freedom) {
			throw new IncorrectDistributionParameterException("freedom must be an integer gtz");
		} else {
			this.freedom = freedom;
		}
	}

	public StudentTPar(Double wfreedom) throws IncorrectDistributionParameterException {
		this(wfreedom.doubleValue());
	}

	/**
	 * It verifies if the parameter is correct. For the student T distribution,
	 * the value of the degrees of freedom must be an integer greater than zero.
	 *
	 * @return boolean indicating if the parameter is correct or not.
	 *
	 */
	@Override
	public boolean check() {
		return !(freedom < 1 || Math.floor(freedom) != freedom);
	}

	/**
	 * It returns the degrees of freedom of the student T distribution.
	 *
	 * @return double with the degrees of freedom of the student T distribution.
	 *
	 */
	public double getFreedom() {
		return freedom;
	}

	/**
	 * It allows the user to change the degrees of freedom of the student T
	 * distribution and verifies if it is an integer greater than zero.
	 *
	 * @param freedom double indicating the degrees of freedom.
	 * @throws IncorrectDistributionParameterException if the degrees of freedom is
	 * not an integer greater than zero.
	 *
	 */
	public void setFreedom(double freedom) throws IncorrectDistributionParameterException {
		this.freedom = freedom;
		if (!check()) {
			throw new IncorrectDistributionParameterException("freedom must be an integer gtz");
		}
	}

} // end StudentTPar
