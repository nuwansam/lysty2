package org.lysty.core;

import net.n3.nanoxml.IXMLElement;

import org.lysty.exceptions.InvalidXMLException;

public interface XMLSerializable {

	public IXMLElement getXml();

	public void loadFromXml(IXMLElement xmlElement) throws InvalidXMLException;
}
