package org.lysty.core;

import org.lysty.exceptions.InvalidXMLException;

import net.n3.nanoxml.IXMLElement;

public interface XMLSerializable {

	public IXMLElement getXml();

	public void loadFromXml(IXMLElement xmlElement) throws InvalidXMLException;
}
