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

package com.analog.lyric.dimple.model.core;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.serializerdetails.Deserializer;
import com.analog.lyric.dimple.model.serializerdetails.Serializer;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;

public class xmlSerializer
{
	public static final String VERSION_ONE = "01.00.00";
	public static final String VERSION_ONE_010001 = "01.00.01";
	public static final String VERSION_TWO = "02.00.00";
	
	public static final String VERSION = VERSION_TWO;

	private boolean _dbg;
	private Deserializer _deserializer;
	private Serializer _serializer;
	
	public xmlSerializer()
	{
		_dbg = false;
		_deserializer = new Deserializer(_dbg);
		_serializer = new Serializer(_dbg);
	}
	
	public String serializeToXML(FactorGraph fg, String FgName, String targetDirectory)
	{
		return _serializer.serializeToXML(fg, FgName, targetDirectory);
	}

	public String serializeFactorTableToXML(IFactorTable ct, String ctName, String targetDirectory)
	{
		return _serializer.serializeFactorTableToXML(ct, ctName, targetDirectory);
	}

	public @Nullable IFactorTable deserializeFactorTableFromXML(String docName)
	{
		return _deserializer.deserializeFactorTableFromXML(docName);
	}
	
	public FactorGraph deserializeFromXML(String docName, @Nullable IFactorGraphFactory<?> solver)
		throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return _deserializer.deserializeFromXML(docName, solver);
	}
	
		
	static public String prettyFormat(Document document)
	{
       DOMImplementation domImplementation = document.getImplementation();
       if (domImplementation.hasFeature("LS", "3.0") && domImplementation.hasFeature("Core", "2.0"))
       {
           DOMImplementationLS domImplementationLS = (DOMImplementationLS) domImplementation.getFeature("LS", "3.0");
           LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
           DOMConfiguration domConfiguration = lsSerializer.getDomConfig();
           if (domConfiguration.canSetParameter("format-pretty-print", Boolean.TRUE))
           {
              lsSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
              LSOutput lsOutput = domImplementationLS.createLSOutput();
              lsOutput.setEncoding("UTF-8");
              StringWriter stringWriter = new StringWriter();
              lsOutput.setCharacterStream(stringWriter);
              lsSerializer.write(document, lsOutput);
             return stringWriter.toString();
          }
          else
          {
              throw new RuntimeException("DOMConfiguration 'format-pretty-print' parameter isn't settable.");
          }
      } else {
          throw new RuntimeException("DOM 3.0 LS and/or DOM 2.0 Core not supported.");
      }
	}
	public void setDbg(boolean dbg)
	{
		_dbg = dbg;
		_deserializer.setDbg(dbg);
		_serializer.setDbg(dbg);
	}
	public boolean isDbg() {
		return _dbg;
	}
	public static void DBG_printDoc(String docName)
	{
		try
        {
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder parser = factory.newDocumentBuilder();
    		Document document = parser.parse(docName);
    		Element root = document.getDocumentElement();
    			        	
    		System.out.println("==========================================================");
    		org.w3c.dom.NodeList allNodes = root.getElementsByTagName("*");
    		for(int i = 0; i < allNodes.getLength(); ++i)
    		{
    			org.w3c.dom.Node n = allNodes.item(i);
    			String s = String.format("[%s], %d", n.getNodeName(), n.getNodeType());
    			System.out.println(s);
    		}
    		System.out.println("==========================================================");
    		DBG_printNode(root, "\t");
    		System.out.println("==========================================================");
    			
        }
      catch (FactoryConfigurationError e) {
	        System.out.println("Could not locate a JAXP factory class");
	      }
	      catch (ParserConfigurationException e) {
	        System.out.println(
	          "Could not locate a JAXP DocumentBuilder class"
	        );
	      }
	      catch (DOMException e) {
	        System.out.println("exception: " + e.getMessage());
	      }
	      catch(Exception e)
	      {
	        	System.out.println("exception: " + e.getMessage());
	      }
	}
	public static void DBG_printNode(Node node, String indent)
	{
		switch (node.getNodeType())
		{
		case Node.DOCUMENT_NODE:
			System.out.println(indent + "Document node");
			NodeList nodes = node.getChildNodes();

			if (nodes != null)
			{
				for (int i = 0; i < nodes.getLength(); i++)
				{
					DBG_printNode(nodes.item(i), "");
				}
			}

			break;

		case Node.ELEMENT_NODE:

			String name = node.getNodeName();
			System.out.println(indent + name);

			NamedNodeMap attributes = node.getAttributes();

			for (int i = 0; i < attributes.getLength(); i++)
			{
				Node current = attributes.item(i);
				System.out.println(indent + " " + current.getNodeName() +
				        "=\"" + current.getNodeValue() + "\"");
			}

			NodeList children = node.getChildNodes();

			if (children != null)
			{
				for (int i = 0; i < children.getLength();
					        i++)
				{
					DBG_printNode(children.item(i),
					        indent + "  ");
				}
			}

			break;

		case Node.TEXT_NODE:
		case Node.CDATA_SECTION_NODE:
			System.out.println(indent + node.getNodeValue());

			break;

		case Node.PROCESSING_INSTRUCTION_NODE:
			System.out.println(indent + "<?" + node.getNodeName() +
			        " " + node.getNodeValue() + " ?>");

			break;

		case Node.ENTITY_REFERENCE_NODE:
			System.out.println("&" + node.getNodeName() + ";");

			break;

		case Node.DOCUMENT_TYPE_NODE:

			DocumentType docType = (DocumentType) node;
			System.out.print("<!DOCTYPE " + docType.getName());

			if (docType.getPublicId() != null)
			{
				System.out.print("PUBLIC \"" +
				        docType.getPublicId() + "\"");
			}
			else
			{
				System.out.print(" SYSTEM ");
			}

			System.out.println("\"" + docType.getSystemId() +
			        "\" >");

			break;
		}
	}
	public void trace(String format, Object... args)
	{
		if (_dbg == true)
		{
			System.out.println(String.format(format, args));
		}
	}
}
