/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.model.serializerdetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import org.eclipse.jdt.annotation.Nullable;

public class Deserializer
{
	protected boolean _dbg = false;

	protected xmlsFactorGraph _xFg = new xmlsFactorGraph();
	protected FactorGraph _fg;
	
	public Deserializer(boolean dbg)
	{
		_dbg = dbg;
		_fg = new FactorGraph();
	}
	
	protected void clear()
	{
		_xFg = new xmlsFactorGraph();
		_fg = new FactorGraph();
	}
	
	protected void addVariable(Node EdgeNode, NamedNodeMap Attributes)
	{
		
		String UUIDString = Attributes.getNamedItem("id").getNodeValue();
		String ExplicitName = "";
		Node NameNode = Attributes.getNamedItem("explicitName");
		if(NameNode != null)
		{
			ExplicitName = NameNode.getNodeValue();
		}
		String ModelerClass = Attributes.getNamedItem("class").getNodeValue();
		String InputString  = Attributes.getNamedItem("input").getNodeValue();
		String BeliefString = Attributes.getNamedItem("belief").getNodeValue();
		if(ModelerClass == "" ||
		   InputString == ""  ||
		   UUIDString == "")
		{
			throw new DimpleException(String.format("ERROR missing required attribute: class-[%s] input[%s] UUID[%s]"
					, ModelerClass
					, InputString
					, UUIDString));
		}
		/*
		String[] DomainStrings = DomainString.split("\\s");
		Double[] Domain = new Double[DomainStrings.length];
		for(int i = 0; i < DomainStrings.length; ++i)
		{
			Domain[i] = Double.parseDouble(DomainStrings[i]);
		}
		*/
		
		String[] InputStrings = InputString.split("\\s");
		double[] Input = new double[InputStrings.length];
		for(int i = 0; i < InputStrings.length; ++i)
		{
			Input[i] = Double.parseDouble(InputStrings[i]);
		}
		String[] BeliefStrings = BeliefString.split("\\s");
		double[] Belief = new double[BeliefStrings.length];
		for(int i = 0; i < BeliefStrings.length; ++i)
		{
			Belief[i] = Double.parseDouble(BeliefStrings[i]);
		}
		
		UUID uuid = java.util.UUID.fromString(UUIDString);
		if(_xFg._variables.containsKey(uuid))
		{
			throw new DimpleException("ERROR variable with uuid " + uuid.toString() + " already exists");
		}

		Node tmp = Attributes.getNamedItem("domain");
		DiscreteDomain dd = null;
		if (tmp != null)
		{
			//in legacy mode
			String DomainString = Attributes.getNamedItem("domain").getNodeValue();
			String[] DomainStrings = DomainString.split("\\s");
			Object[] Domain = new Object[DomainStrings.length];
			for(int i = 0; i < DomainStrings.length; ++i)
			{
				Domain[i] = Double.parseDouble(DomainStrings[i]);
			}
			dd = DiscreteDomain.create(Domain);

		}
		else
		{
			Element e = (Element)EdgeNode;
			NodeList nl2 = e.getElementsByTagName("domain");
			Node n2 = nl2.item(0);
			dd = getDomainFromElement((Element)n2);
		}

		
		Discrete v = new Discrete(dd);
		
		v.setInput(Input);
		if(ExplicitName.length() > 0)
		{
			v.setName(ExplicitName);
		}
		v.setUUID(uuid);
					
		Node boundaryNode = Attributes.getNamedItem("boundaryIdx");
		if(boundaryNode != null)
		{
			String boundaryIdxString = boundaryNode.getNodeValue();
			int boundaryIdx = Integer.parseInt(boundaryIdxString);
			_xFg._boundaryVariables.set(boundaryIdx, v);
		}
		
		_xFg._variables.put(uuid, v);
	}
	protected void addFunction(Node FunctionNode, NamedNodeMap Attributes)
	{
		xmlsFactor xF = new xmlsFactor();
		xF.setModelerClass(Attributes.getNamedItem("class").getNodeValue());
		xF.setExplicitName(Attributes.getNamedItem("explicitName").getNodeValue());
		UUID uuid = java.util.UUID.fromString(Attributes.getNamedItem("id").getNodeValue());
		xF.setUUID(uuid);
		if(_xFg._factors.containsKey(uuid))
		{
			throw new DimpleException("ERROR variable with uuid " + uuid.toString() + " already exists");
		}
		xF.setNumEdges(Integer.parseInt(Attributes.getNamedItem("numEdges").getNodeValue()));
		Node factorTableNode = Attributes.getNamedItem("comboTable");
		if(factorTableNode != null)
		{
			String factorTableString = factorTableNode.getNodeValue();
			int factorTableId = Integer.parseInt(factorTableString);
			xF.setFactorTableId(factorTableId);
		}
		_xFg._factors.put(uuid, xF);
	}
	
	protected void addEdge(Node n)
	{
		NamedNodeMap Attributes = n.getAttributes();
		String factorUUIDString = Attributes.getNamedItem("source").getNodeValue();
		String variableUUIDString = Attributes.getNamedItem("target").getNodeValue();
		int edgeIdx = Integer.parseInt(Attributes.getNamedItem("srcIdx").getNodeValue());
		UUID factorUUID = java.util.UUID.fromString(factorUUIDString);
		UUID variableUUID = java.util.UUID.fromString(variableUUIDString);
		xmlsFactor f = _xFg._factors.get(factorUUID);
		Variable v = _xFg._variables.get(variableUUID);
		
		if(edgeIdx >= f.getNumEdges())
		{
			throw new DimpleException("ERROR: edgeIdx, " + Integer.toString(edgeIdx) +
								" >= num edges in factor " + Integer.toString(f.getNumEdges()) +
								"  factor uuid:" + f.getUUID().toString());
		}
		ArrayList<Variable> fvariables = f.getVariables();
		if(fvariables.get(edgeIdx) != null)
		{
			throw new DimpleException("ERROR: edgeIdx, " + Integer.toString(edgeIdx) +
					" already has a variable " +
					"  factor uuid:" + f.getUUID().toString());
		}
		fvariables.set(edgeIdx, v);
	}
	
	private static DiscreteDomain getDomainFromElement(Element n)
	{
		org.w3c.dom.NodeList elements = n.getElementsByTagName("domain_element");
		
		Object [] objs = new Object[elements.getLength()];
		
		for (int j = 0; j < elements.getLength(); j++)
		{
			String domainValueString = elements.item(j).getAttributes().getNamedItem("value").getNodeValue();
			try
			{
				double val = Double.parseDouble(elements.item(j).getAttributes().getNamedItem("value").getNodeValue());
				objs[j] = val;
			}
			catch(Exception e)
			{
				objs[j] = domainValueString;
			}
		}
		return DiscreteDomain.create(objs);

	}
	
	private static xmlsFactorTable deserializeFactorTableFromXML(Element factorTableElement)
	{
		org.w3c.dom.NodeList lSize = factorTableElement.getElementsByTagName("size");
		org.w3c.dom.NodeList lEntry = factorTableElement.getElementsByTagName("entry");
		org.w3c.dom.NodeList lValue = factorTableElement.getElementsByTagName("value");
		org.w3c.dom.NodeList lDomain = factorTableElement.getElementsByTagName("domain");
		
		if(lSize.getLength() != 1)
		{
			throw new DimpleException(String.format("ERROR expected 1 size element, found %d", lSize.getLength()));
		}
					
		NamedNodeMap sizeAttributes = lSize.item(0).getAttributes();
		int rows 	= Integer.parseInt(sizeAttributes.getNamedItem("rows").getNodeValue());
		int columns = Integer.parseInt(sizeAttributes.getNamedItem("columns").getNodeValue());
		
		if(lEntry.getLength() != rows * columns)
		{
			throw new DimpleException(String.format("ERROR expected expected %d indices elements, found %d"
					, rows * columns
					, lEntry.getLength()));
		}
		if(lValue.getLength() != rows)
		{
			throw new DimpleException(String.format("ERROR expected expected %d values elements, found %d"
					, rows
					, lValue.getLength()));
		}

		int[][] indices = new int[rows][columns];
		double[] weights = new double[rows];
		
		for(int i = 0; i < lEntry.getLength(); ++i)
		{
			Node n = lEntry.item(i);
			NamedNodeMap attributes = n.getAttributes();
    		int row 	= Integer.parseInt(attributes.getNamedItem("row").getNodeValue());
    		int column	= Integer.parseInt(attributes.getNamedItem("column").getNodeValue());
    		int value 	= Integer.parseInt(attributes.getNamedItem("value").getNodeValue());
    		
    		indices[row][column] = value;
		}
		
   		for(int i = 0; i < lValue.getLength(); ++i)
		{
			Node n = lValue.item(i);
			NamedNodeMap attributes = n.getAttributes();
    		int row 	= Integer.parseInt(attributes.getNamedItem("row").getNodeValue());
    		double value= Double.parseDouble(attributes.getNamedItem("value").getNodeValue());
    		
    		weights[row] = value;
		}
   		
   		NamedNodeMap factorTableAttributes = factorTableElement.getAttributes();
   		String functionName = "";
   		Node node = factorTableAttributes.getNamedItem("function");
   		if(node != null)
   		{
   			functionName = node.getNodeValue();
   		}
   		int id = 1;
   		node = factorTableAttributes.getNamedItem("id");
   		if(node != null)
   		{
   			id = Integer.parseInt(node.getNodeValue());
   		}
   		
   		DiscreteDomain [] domains = new DiscreteDomain[lDomain.getLength()];
   		
   		for (int i = 0; i < lDomain.getLength(); i++)
   		{
   			
   			Element n = (Element)lDomain.item(i);
   			domains[i] = getDomainFromElement(n);
   		}
   		
   		if (domains.length > 0)
   		{
   			JointDomainIndexer indexer = JointDomainIndexer.create(domains);

   			int[] jointIndices = new int[rows];
   			for (int row = 0; row < rows; ++row)
   			{
   				jointIndices[row] = indexer.jointIndexFromIndices(indices[row]);
   			}

   			return new xmlsFactorTable(functionName, id, jointIndices, weights,JointDomainIndexer.create(domains));
   		}
   		else
   		{
   			return new xmlsFactorTable(functionName, id, indices, weights);
   		}
	}
	protected void deserializeFromXMLToMemory(String docName) throws ParserConfigurationException, SAXException, IOException
	{
		//init
		clear();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document document = parser.parse(docName);
		Element DocElement = document.getDocumentElement();
		
		//graph
		org.w3c.dom.NodeList graphNodes = DocElement.getElementsByTagName("graph");
		if(graphNodes.getLength() != 1)
		{
			throw new DimpleException("ERROR: only 1 graph node now supported");
		}
		Node graphNode = graphNodes.item(0);
		NamedNodeMap graphAttributes = graphNode.getAttributes();
		String graphUUIDString = graphAttributes.getNamedItem("id").getNodeValue();
		_xFg._uuid = java.util.UUID.fromString(graphUUIDString);
		Node nameNode = graphAttributes.getNamedItem("explicitName");
		if(nameNode != null)
		{
			_xFg._explicitName = nameNode.getNodeValue();
		}
		Node boundaryNodes = graphAttributes.getNamedItem("numBoundaryVariables");
		int numBoundaryVariables = Integer.parseInt(boundaryNodes.getNodeValue());
		_xFg._boundaryVariables.ensureCapacity(numBoundaryVariables);
		for(int i = 0; i < numBoundaryVariables; ++i)
		{
			_xFg._boundaryVariables.add(null);
		}
		Node solverClass = graphAttributes.getNamedItem("solverClass");
		if(solverClass != null)
		{
			_xFg._solverClass = solverClass.getNodeValue();
		}
		
		//nodes
		org.w3c.dom.NodeList nodes = DocElement.getElementsByTagName("node");
		
		
		for(int i = 0; i < nodes.getLength(); ++i)
		{
			Node n = nodes.item(i);
			NamedNodeMap attributes = n.getAttributes();
			String type = attributes.getNamedItem("type").getNodeValue();
			if(type.compareTo("Function") == 0)
			{
    			addFunction(n, attributes);
			}
			else if(type.compareTo("Variable") == 0)
			{
    			addVariable(n, attributes);
			}
			else
			{
				throw new DimpleException(String.format("Unknown node type [%s]", type));
			}
		}
		trace(String.format("%d variables:", _xFg._variables.size()));
		if(_dbg)
		{
			int i = 0;
			for(Variable v :  _xFg._variables.values())
			{
				trace(String.format("\t%d: {%s}", i, v.toString()));
				i++;
			}
		}
		trace(String.format("%d factors:", _xFg._factors.size()));

		//combo tables
		org.w3c.dom.NodeList factorTableElements = DocElement.getElementsByTagName("ComboTable");
		for(int i = 0; i < factorTableElements.getLength(); ++i)
		{
  			Node ctNode = factorTableElements.item(i);
  			xmlsFactorTable xct = deserializeFactorTableFromXML((Element) ctNode);
  			if(_xFg._factorTables.containsKey(xct._ephemeralId))
  			{
  				throw new DimpleException("ERROR: combo table [" + xct._ephemeralId + "] has already been deserialized");
  			}
  			_xFg._factorTables.put(xct._ephemeralId, xct);
  		}
		int id = 0;
		for(xmlsFactorTable xct : _xFg._factorTables.values())
		{
			_xFg._ephemeralToSequentialId.put(xct._ephemeralId, id);
			id++;
		}
		
		//edges
		org.w3c.dom.NodeList edges = DocElement.getElementsByTagName("edge");
		
		for(int i = 0; i < edges.getLength(); ++i)
		{
			addEdge(edges.item(i));
		}
	}
	
	class FactorFunctionHolder
	{
		@Nullable String name;
		@Nullable int [][] indices;
		int id;
		@Nullable double [] probs;
		@Nullable TableFactorFunction factorFunc;
	}
	
	public FactorGraph deserializeFromXML(String docName, @Nullable IFactorGraphFactory<?> solver)
		throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, InstantiationException,
		IllegalAccessException
	{
		deserializeFromXMLToMemory(docName);
		
		Variable[] boundaryVariables = new Variable[_xFg._boundaryVariables.size()];
		for(int i = 0; i < _xFg._boundaryVariables.size(); ++i)
		{
			boundaryVariables[i] = _xFg._boundaryVariables.get(i);
			if(boundaryVariables[i] == null)
			{
				throw new DimpleException("ERROR missing boundary variable at idx " + Integer.toString(i));
			}
		}
		_fg = new FactorGraph(boundaryVariables);
		if(_xFg._explicitName.length() > 0)
		{
			_fg.setName(_xFg._explicitName);
		}
		_fg.setUUID(_xFg._uuid);
		if(solver != null)
		{
			_fg.setSolverFactory(solver);
		}
		else if(_xFg._solverClass.length() > 0)
		{
			@SuppressWarnings("all")
			Class cl = Class.forName(_xFg._solverClass);
			_fg.setSolverFactory((IFactorGraphFactory<?>)cl.newInstance());
		}
		
		
		HashMap<Object, TableFactorFunction> factorFunctions = new HashMap<Object, TableFactorFunction>();
		
		for(xmlsFactor xF : _xFg._factors.values())
		{
			//HACK. Fix to use actual qualified class name and only append if qualification
			//missing
			
			String prefix = "com.lyricsemi.dimple.factorfunctions.";
			String lastPart = xF.getModelerClass();
			String qualifiedHack = prefix + lastPart;
			
			//.... what if matlab name is different??? use combo table?
			Variable[] arguments = new Variable[xF.getNumEdges()];
			for(int i = 0; i < arguments.length; ++i)
			{
				arguments[i] = xF.getVariables().get(i);
				if(arguments[i] == null)
				{
					throw new DimpleException("ERROR variable at idx " + Integer.toString(i) + " of factor " + xF.getUUID().toString() + " is null");
				}
			}
			
			@SuppressWarnings("all")
			Class factorFunctionClass  = null;
			try
			{
				factorFunctionClass  = Class.forName(qualifiedHack);
			}
			catch(Exception e){}
		    
		    Factor f = null;
		    //If we have that class, build with that class.
		    if (factorFunctionClass != null)
		    {
				f = _fg.addFactor((FactorFunction)factorFunctionClass.newInstance(),
			 			arguments);
		    	
		    }
		    //If it doesn't exist, but it has a combo table,
		    //build with that combo table
		    else if (xF.isDiscrete())
		    {
		    	
		    	xmlsFactorTable xct = _xFg._factorTables.get(xF.getFactorTableId());
		    	
		    	TableFactorFunction ff = null;
		    	if (factorFunctions.containsKey(xct))
		    		ff = factorFunctions.get(xct);
		    	else
		    	{
		    		DiscreteDomain [] domains = new DiscreteDomain[arguments.length];
		    		for (int i = 0; i < domains.length; i++)
		    			domains[i] = (DiscreteDomain)arguments[i].getDomain();
		    		IFactorTable ft = xct.makeTable(domains);
		    		ff = new TableFactorFunction(xct._functionName, ft);
		    		factorFunctions.put(xct,ff);
		    	}
		    	
		    	f = _fg.addFactor(ff, arguments);
		    }
		    
		    if (f != null)
		    {
		    	if (xF.getExplicitName().length() > 0)
		    	{
		    		f.setName(xF.getExplicitName());
		    	}
		    	f.setUUID(xF.getUUID());
		    }
		}
		return _fg;
	}
	
	public @Nullable IFactorTable deserializeFactorTableFromXML(String docName)
	{
		IFactorTable ct = null;
		try
        {
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder parser = factory.newDocumentBuilder();
    		Document document = parser.parse(docName);
    		Element DocElement = document.getDocumentElement();

    		xmlsFactorTable xct = deserializeFactorTableFromXML(DocElement);
    		ct = xct.makeTable();
        }
		catch (FactoryConfigurationError e)
		{
	        System.out.println("Could not locate a JAXP factory class");
	    }
	    catch (ParserConfigurationException e)
	    {
	        System.out.println(
	          "Could not locate a JAXP DocumentBuilder class"
	        );
	     }
	     catch (DOMException e)
	     {
	        System.out.println("ERROR in deserialize to XML: " + e.toString());
	     }
	     catch(Exception e)
	     {
	        System.out.println("ERROR in deserialize to XML : " + e.toString());
	     }
	  return ct;
	}
	
	static public String getDocVersion(String docName) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document document = parser.parse(docName);
		Element DocElement = document.getDocumentElement();
		
		org.w3c.dom.NodeList versions = DocElement.getElementsByTagName("version");
		if(versions.getLength() != 1)
		{
			throw new DimpleException("ERROR: more than 1 version found");
		}
		Node verNode = versions.item(0);
		NamedNodeMap attributes = verNode.getAttributes();
		String version = attributes.getNamedItem("version").getNodeValue();
		return version;
	}
	
	public void trace(String format, Object... args)
	{
		if (_dbg == true)
		{
			System.out.println(String.format(format, args));
		}
	}
	public void setDbg(boolean dbg)
	{
		_dbg = dbg;
	}
}
