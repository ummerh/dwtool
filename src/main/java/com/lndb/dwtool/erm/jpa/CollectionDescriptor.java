package com.lndb.dwtool.erm.jpa;

public class CollectionDescriptor extends ReferenceDescriptor {
    private String elementClassReference;

    public CollectionDescriptor() {
    }

    /**
     * @return the elementClassReference
     */
    public String getElementClassReference() {
	return elementClassReference;
    }

    /**
     * @param elementClassReference
     *            the elementClassReference to set
     */
    public void setElementClassReference(String elementClassReference) {
	this.elementClassReference = elementClassReference;
    }
}
