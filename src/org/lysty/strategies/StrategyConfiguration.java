package org.lysty.strategies;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;

import org.lysty.core.XMLSerializable;
import org.lysty.exceptions.InvalidXMLException;

public class StrategyConfiguration implements XMLSerializable {

	private static final String XML_ELEM_STRATEGY_CONFIG = "strategy_config";
	private static final String XML_ELEM_ATTRIBS = "attribs";
	private static final String XML_ELEM_ATTRIB = "attrib";
	private static final String XML_ATTRIB_KEY = "key";
	private static final String XML_ATTRIB_VALUE = "value";
	private Map<String, String> attributes;

	public StrategyConfiguration() {
		attributes = new HashMap<String, String>();
	}

	public StrategyConfiguration(IXMLElement xml) throws InvalidXMLException {
		attributes = new HashMap<String, String>();
		loadFromXml(xml);
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	/**
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	@Override
	public IXMLElement getXml() {
		IXMLElement head = new XMLElement();
		head.setName(XML_ELEM_STRATEGY_CONFIG);
		IXMLElement attribs = new XMLElement();
		attribs.setName(XML_ELEM_ATTRIBS);
		IXMLElement attrib;
		Iterator<Entry<String, String>> it = attributes.entrySet().iterator();
		Entry<String, String> entry;
		while (it.hasNext()) {
			entry = it.next();
			attrib = new XMLElement();
			attrib.setName(XML_ELEM_ATTRIB);
			attrib.setAttribute(XML_ATTRIB_KEY, entry.getKey());
			attrib.setAttribute(XML_ATTRIB_VALUE, entry.getValue());
			attribs.addChild(attrib);
		}
		head.addChild(attribs);
		return head;
	}

	@Override
	public void loadFromXml(IXMLElement xmlElement) throws InvalidXMLException {
		try {
			IXMLElement attribsXml = (IXMLElement) xmlElement.getChildren()
					.get(0);
			Vector<IXMLElement> attribs = attribsXml.getChildren();
			for (IXMLElement attrib : attribs) {
				attributes.put(attrib.getAttribute(XML_ATTRIB_KEY, null),
						attrib.getAttribute(XML_ATTRIB_VALUE, null));
			}
		} catch (Exception e) {
			throw new InvalidXMLException();
		}
	}

}
