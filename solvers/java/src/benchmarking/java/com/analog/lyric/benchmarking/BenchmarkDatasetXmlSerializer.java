/*******************************************************************************
 *   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.benchmarking;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BenchmarkDatasetXmlSerializer
{
	public void serialize(Writer writer, BenchmarkDataset round)
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try
		{
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element roundElement = doc.createElement("object");
			doc.appendChild(roundElement);
			addTextChild(roundElement, "date", round.getProperties()
					.getProperty("create.date"));
			addTextChild(roundElement, "label", round.getProperties()
					.getProperty("label"));
			addTextChild(roundElement, "configuration", round.getProperties()
					.getProperty("configuration"));
			addProperties(roundElement, round.getProperties(), "label",
					"create.date", "configuration");

			Element benchmarksElement = addChild(roundElement, "benchmarkruns");
			benchmarksElement.setAttribute("type", "list");
			for (BenchmarkRun benchmarkRun : round.getBenchmarkRuns())
			{
				Element runElement = addChild(benchmarksElement, "object");
				addTextChild(runElement, "label", benchmarkRun.getProperties()
						.getProperty("label"));
				addTextChild(runElement, "iterations", benchmarkRun
						.getProperties().getProperty("iterations"));
				addTextChild(runElement, "warmupIterations", benchmarkRun
						.getProperties().getProperty("warmupIterations"));
				addProperties(runElement, benchmarkRun.getProperties(),
						"label", "warmupIterations", "iterations");
				Element gcSamplesElement = addChild(runElement, "gcSamples");
				gcSamplesElement.setAttribute("type", "list");

				Element memoryUsageSamplesElement = addChild(runElement,
						"memorySamples");
				memoryUsageSamplesElement.setAttribute("type", "list");

				Element timeSamplesElement = addChild(runElement, "timeSamples");
				timeSamplesElement.setAttribute("type", "list");

				for (BenchmarkRunIteration iteration : benchmarkRun
						.getIterations())
				{
					Properties iterationProperties = iteration.getProperties();
					if (iterationProperties
							.containsKey("gc.collection.count.post"))
					{
						Element s = addChild(gcSamplesElement, "object");
						addTextChild(s, "label", "pre");
						addTextChild(
								s,
								"count",
								String.valueOf(iterationProperties
										.getProperty("gc.collection.count.pre")))
								.setAttribute("type", "integer");
						long nanoseconds = (long) 1e6
								* Long.parseLong(iterationProperties
										.getProperty("gc.collection.time.milli.pre"));
						addTextChild(s, "nanoseconds",
								String.valueOf(nanoseconds));

						s = addChild(gcSamplesElement, "object");
						addTextChild(s, "label", "post");
						addTextChild(
								s,
								"count",
								String.valueOf(iterationProperties
										.getProperty("gc.collection.count.post")))
								.setAttribute("type", "integer");
						nanoseconds = (long) 1e6
								* Long.parseLong(iterationProperties
										.getProperty("gc.collection.time.milli.post"));
						addTextChild(s, "nanoseconds",
								String.valueOf(nanoseconds));
					}
					if (iterationProperties.containsKey("nanoseconds.post"))
					{
						Element s = addChild(timeSamplesElement, "object");
						addTextChild(s, "label", "pre");
						long nanoseconds = Long.parseLong(iterationProperties
								.getProperty("nanoseconds.pre"));
						addTextChild(s, "nanoseconds",
								String.valueOf(nanoseconds));

						s = addChild(timeSamplesElement, "object");
						addTextChild(s, "label", "post");
						nanoseconds = Long.parseLong(iterationProperties
								.getProperty("nanoseconds.post"));
						addTextChild(s, "nanoseconds",
								String.valueOf(nanoseconds));
					}
					for (Entry<Object, Object> entry : iteration
							.getProperties().entrySet())
					{
						String key = (String) entry.getKey();
						if (key.equals("heap.allocation"))
						{
							String value = (String) entry.getValue();
							Element s = addChild(memoryUsageSamplesElement,
									"object");
							addTextChild(s, "label", "memoryAllocation");
							addTextChild(s, "bytes", value);
						}
					}
				}
			}
			XmlWriter.Write(writer, doc);
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}
	}

	private static void addKVP(Node parent, String key, String value)
	{
		Document doc = parent.getOwnerDocument();
		Element e = doc.createElement("object");
		addTextChild(e, "key", key);
		addTextChild(e, "value", value);
		parent.appendChild(e);
	}

	private static void addProperties(Node parent, Properties properties,
			String... except)
	{
		ArrayList<String> keys = new ArrayList<String>();
		for (Object key : properties.keySet())
		{
			keys.add((String) key);
		}
		keys.removeAll(Arrays.asList(except));
		Collections.sort(keys);
		Element propertiesElement = addChild(parent, "properties");
		propertiesElement.setAttribute("type", "list");
		for (String key : keys)
		{
			String value = properties.getProperty(key);
			addKVP(propertiesElement, key, value);
		}
	}

	private static Element addChild(Node parent, String kind)
	{
		Document doc = parent.getOwnerDocument();
		Element e = doc.createElement(kind);
		parent.appendChild(e);
		return e;
	}

	private static Element addTextChild(Node parent, String kind, String text)
	{
		Document doc = parent.getOwnerDocument();
		Element e = doc.createElement(kind);
		Node t = doc.createTextNode(text);
		e.appendChild(t);
		parent.appendChild(e);
		return e;
	}
}
