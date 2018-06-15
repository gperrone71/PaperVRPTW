package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

import objects.*;


public class XMLTester {
	
	private class ClassTest {
		private int index;
		private ArrayList<Node> nodeList = new ArrayList<Node>();
		
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public ArrayList<Node> getNodeList() {
			return nodeList;
		}
		public void setNodeList(ArrayList<Node> nodeList) {
			this.nodeList = nodeList;
		}
		
		
	}

	private ArrayList<ClassTest> data = new ArrayList<ClassTest>();
	
	private void generateData() {

		ClassTest tmp = new ClassTest();
		// generate first object
		tmp.setIndex(0);
		for (int i = 0; i < 10; i++) {
			Node tmpNode = new Node();
			tmpNode.setId(i);
			tmpNode.generateRndPosition(100, 100);
			tmp.getNodeList().add(tmpNode);
		}
		data.add(tmp);

		ClassTest tmp1 = new ClassTest();
		tmp1.setIndex(1);
		tmp1.getNodeList().clear();
		for (int i = 0; i < 20; i++) {
			Node tmpNode = new Node();	
			tmpNode.setId(i);
			tmpNode.generateRndPosition(100, 100);
			tmp1.getNodeList().add(tmpNode);
		}
		data.add(tmp1);
	}
	
	private void XMLCreator() {
        XStream xstream = new XStream();
        String strXML = xstream.toXML(data);
        
        System.out.println(strXML);
		
        List<String> tmp = new ArrayList<String>();
        tmp.add(strXML);
        
		try {
			Files.write(Paths.get("XMLtest.xml"), tmp, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("unchecked")
	private void XMLReader() {
		XStream xstream = new XStream();
		String funz = "";
        List<String> tmp = new ArrayList<String>();
		
		try {
			tmp = Files.readAllLines(Paths.get("XMLtest.xml"), StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String str : tmp)
			funz += str;
	
		data.clear();
		
		PerroUtils.print("READ");
		
		data = (ArrayList<ClassTest>)xstream.fromXML(funz);
		
		for (ClassTest dataTmp : data) {
			PerroUtils.print("Item #" + dataTmp.getIndex());
			for (Node tmpNd : dataTmp.getNodeList())
				PerroUtils.print("Node " + tmpNd.getId() + " X: " + tmpNd.getLatitude() + " Y: " + tmpNd.getLongitude());
		}
			
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		XMLTester unz = new XMLTester();
		unz.generateData();
		unz.XMLCreator();
		unz.XMLReader();
		
	}

}
