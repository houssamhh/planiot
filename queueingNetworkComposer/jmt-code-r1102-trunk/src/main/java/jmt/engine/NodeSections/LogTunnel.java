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

package jmt.engine.NodeSections;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jmt.common.exception.NetException;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NodeSection;
import jmt.engine.log.CSVLogger;
import jmt.engine.log.JSimLoggerFactory;
import jmt.engine.log.LoggerParameters;

/**
 * <p>Title: LogTunnel Extension</p>
 * <p>Description: This class extends the "tunnel" service-section
 * by adding logging (information about forward messages only).
 * Every message sent from the input section is forwarded to the
 * output section and every message sent the from output section
 * is backward to the input section.</p>
 *
 * @author Michael Fercu
 *         Date: 12-lug-2008
 *         Time: 14.04.02
 *         
 * @author Marco Bertoli
 * 			Date: 12-dec-2009
 * 
 * 		Nearly rewrote everything to fix bugs with log files kept open and with log lines deleted when 
 *      multiple loggers were writing on the same file.
 *      Now instead of using Log4j it relies on a fast, custom, thread-safe implementation.
 *
 */
public class LogTunnel extends ServiceTunnel {

	public static final String COLUMN_LOGGERNAME = "LOGGERNAME";
	public static final String COLUMN_TIMESTAMP = "TIMESTAMP";
	public static final String COLUMN_JOBID = "JOB_ID";
	public static final String COLUMN_CLASSID = "CLASS_ID";
	public static final String COLUMN_INTERARRIVAL_SAMECLASS = "INTERARRIVAL_SAMECLASS";
	public static final String COLUMN_INTERARRIVAL_ANYCLASS = "INTERARRIVAL_ANYCLASS";
	public static final String COLUMN_START_TIME = "SIMUL_START_TIME";

	public static final String[] COLUMNS = {COLUMN_LOGGERNAME, COLUMN_TIMESTAMP, COLUMN_JOBID, COLUMN_CLASSID, 
			COLUMN_INTERARRIVAL_SAMECLASS, COLUMN_INTERARRIVAL_ANYCLASS, COLUMN_START_TIME};

	private String chDelimiter;
	private String decimalSeparator;
	private int intReplacePolicy;
	private String strTimestampValue;
	private LoggerParameters lp;

	private CSVLogger logger;
	private Map<String, String> defaultValues;

	/**
	 *  Creates a new instance of LogTunnel; called by simulator engine.
	 */
	public LogTunnel(String argFN, String argFP, Boolean argET, Boolean argBLN, Boolean argBTS, Boolean argBJID, Boolean argBJC, Boolean argBTSC,
			Boolean argBTAC, Integer numClasses) {
		/* Create an object to hold the Logger's parameters, with parameters from XMLReader */
		lp = new LoggerParameters(argFN, argFP, argET, argBLN, argBTS, argBJID, argBJC, argBTSC, argBTAC);
	}

	/**
	 * Checks if the file is writable.
	 */
	private void initLoggerParameters() {
		// Get global values (from the XML file) for the path, auto-replace, and delimiter 
		lp.path = getOwnerNode().getSimParameters().getLogPath();
		intReplacePolicy = new Integer(getOwnerNode().getSimParameters().getLogReplaceMode()).intValue();
		chDelimiter = getOwnerNode().getSimParameters().getLogDelimiter();
		decimalSeparator = getOwnerNode().getSimParameters().getLogDecimalSeparator();
		strTimestampValue = getOwnerNode().getSimParameters().getTimestampValue();
	}

	@Override
	protected int process(NetMessage message) throws NetException {
		try {
			switch (message.getEvent()) {

			case NetEvent.EVENT_START:
				break;

			case NetEvent.EVENT_STOP:
				if (logger != null) {
					logger.close();
				}
				break;

			case NetEvent.EVENT_JOB:
				Job job = message.getJob();
				// Build logger and default values if needed
				if (logger == null) {
					initLoggerParameters();
					logger = JSimLoggerFactory.getCSVLogger(
							new File(lp.path, lp.name), 
							COLUMNS, 
							intReplacePolicy == LoggerParameters.LOGGER_AR_APPEND, 
							chDelimiter, 
							decimalSeparator
							);
					defaultValues = new HashMap<String, String>();
					if (lp.boolExecTimestamp.booleanValue()) {
						defaultValues.put(COLUMN_START_TIME, strTimestampValue);
					}
					if (lp.boolLoggername.booleanValue()) {
						defaultValues.put(COLUMN_LOGGERNAME, message.getSource().getName());
					}
				}
				// Fills the values for the log column
				Map<String, Object> values = new HashMap<String, Object>();
				if (lp.boolTimeStamp.booleanValue()) {
					values.put(COLUMN_TIMESTAMP, message.getTime());
				}

				if (lp.boolJobID.booleanValue()) {
					values.put(COLUMN_JOBID, job.getId());
				}

				if (lp.boolJobClass.booleanValue()) {
					values.put(COLUMN_CLASSID, job.getJobClass().getName());
				}

				if (lp.boolTimeSameClass.booleanValue()) {
					double interarrivalSame = message.getTime() - jobsList.getLastJobOutTimePerClass(job.getJobClass());
					values.put(COLUMN_INTERARRIVAL_SAMECLASS, interarrivalSame);
				}

				if (lp.boolTimeAnyClass.booleanValue()) {
					double interarrivalAny = message.getTime() - jobsList.getLastJobOutTime();
					values.put(COLUMN_INTERARRIVAL_ANYCLASS, interarrivalAny);
				}
				// Finally logs the line
				logger.log(values, defaultValues);
				// Do not break here

			default:
				if (message.getSourceSection() == NodeSection.INPUT) {
					sendForward(message.getEvent(), message.getData(), 0.0);
				}
				if (message.getSourceSection() == NodeSection.OUTPUT) {
					sendBackward(message.getEvent(), message.getData(), 0.0);
				}
				break;
			}

			return MSG_PROCESSED;
		} catch (IOException ex) {
			throw new NetException("Error while writing to the log file: " + ex.getMessage());
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (logger != null) {
			logger.close();
			logger = null;
		}
	}

}
