package iotSys.broker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.data.category.CategoryDataset;
//import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.opencsv.CSVWriter;

public class ReadSimulationResults {
	
	public static ArrayList<PerformanceIndex> performanceIndices = new ArrayList<PerformanceIndex>();
//	final String INPUT_FILE_NAME = "QueuingNetwork_realisticScenario_deterministic.jsimg";
//	final String OUTPUT_FILE_NAME = "RealisticScenario_Deterministic.csv";
	
	final String PATH = "/home/houssamhh/phd/mdw22/experiments/setup1/mediumload/maxmin/";
	final String INPUT_FILE_NAME = "iotsystem_testing";
	final String INPUT_FILE_PATH = PATH + INPUT_FILE_NAME + ".jsimg";
	final String OUTPUT_FILE_NAME = INPUT_FILE_NAME + ".csv";
	final String OUTPUT_FILE_PATH = PATH + OUTPUT_FILE_NAME;

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		ReadSimulationResults reader = new ReadSimulationResults();
		reader.readFromXML();
		reader.writeToCsv();
//		reader.visualizeResults();
	}
	
	public void readFromXML() throws ParserConfigurationException, SAXException, IOException {
		File file = new File(INPUT_FILE_PATH);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
		NodeList nodeList = doc.getElementsByTagName("results");
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			System.out.println("\nNode name: " + node.getNodeName());
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) node;
				System.out.println("Reference Class: "  + e.getAttribute("elapsedTime"));
				NodeList childNodes = e.getChildNodes();
				System.out.println("Number of child nodes: " + childNodes.getLength());
				for (int j = 0; i < childNodes.getLength(); j++) {
					Node measure = childNodes.item(j);
					if (measure != null && measure.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) measure;
						String name = element.getAttribute("name");
						String referenceClass = element.getAttribute("referenceClass");
						Double value = Double.parseDouble(element.getAttribute("finalValue"));
						PerformanceIndex index = new PerformanceIndex (name, referenceClass, value);
						performanceIndices.add(index);
					}
					else if (measure == null)
						break;
				}
			}
		}
	}
	
	public void writeToCsv() {
		System.out.println("Starting....");
        File file = new File(OUTPUT_FILE_PATH);
        try {
            FileWriter output = new FileWriter(file);
            CSVWriter write = new CSVWriter(output);

            // Header column value
            String[] header = { "name", "referenceClass", "Value"};
            write.writeNext(header);
            // Value
            for (PerformanceIndex pi : performanceIndices) {
                String[] data = { pi.name, pi.referenceClass, pi.value.toString()};
                write.writeNext(data);
            }
            write.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        System.out.println("End.");
	}
	
}