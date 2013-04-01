package edu.unc.lib.dl.util;

import java.util.List;

import org.jdom.Element;

import edu.unc.lib.dl.acl.util.UserRole;
import edu.unc.lib.dl.xml.JDOMNamespaceUtil;

public class RDFUtil {

	public static Element mergeRDF(Element baseRDF, Element incomingRDF) {
		if (baseRDF == null)
			return incomingRDF;
		if (incomingRDF == null)
			return baseRDF;
		List<?> incomingChildren = incomingRDF.getChildren("Description", JDOMNamespaceUtil.RDF_NS);
		// If there is no rdf:Description tag in the incoming data, then there is nothing to add.
		if (incomingChildren.size() == 0)
			return baseRDF;

		Element incomingDescription = (Element) incomingChildren.get(0);
		Element newDescription = null;

		newDescription = baseRDF.getChild("Description", JDOMNamespaceUtil.RDF_NS);
		// If the previous rels-ext didn't have a description, then use the new one
		if (newDescription == null) {
			baseRDF.addContent((Element) incomingDescription.clone());
			return baseRDF;
		}

		// Clone all the child elements of the incoming rdf:Description tag
		List<?> incomingElements = incomingDescription.getChildren();
		// Add all the incoming element children to the base modified object
		for (Object incomingObject : incomingElements) {
			if (incomingObject instanceof Element)
				newDescription.addContent((Element)((Element) incomingObject).clone());
		}

		return baseRDF;
	}

	public static Element updateRDF(Element baseRDF, Element incomingRDF) {
		if (baseRDF == null)
			return incomingRDF;
		if (incomingRDF == null)
			return baseRDF;
		// If there is no rdf:Description tag in the incoming data, then there is nothing to add.
		if (incomingRDF.getChildren().size() == 0)
			return baseRDF;

		Element incomingDescription = (Element) incomingRDF.getChildren("Description", JDOMNamespaceUtil.RDF_NS).get(0);
		Element newDescription = null;

		// If the previous rels-ext didn't have a description, then use the new one
		if (baseRDF.getChildren().size() == 0) {
			baseRDF.addContent((Element) incomingDescription.clone());
			return baseRDF;
		} else {
			newDescription = (Element) baseRDF.getChildren().get(0);
		}

		// Clone all the child elements of the incoming rdf:Description tag
		@SuppressWarnings("unchecked")
		List<Element> incomingElements = (List<Element>) incomingDescription.getChildren();
		// Add all the incoming element children to the base modified object
		for (Element incomingElement : incomingElements) {
			// Remove the preexisting relations of the same type before adding the new entries
			newDescription.removeChildren(incomingElement.getName(), incomingElement.getNamespace());
			newDescription.addContent((Element) incomingElement.clone());
		}

		return baseRDF;
	}

	public static Element aclToRDF(Element element) {
		Element relsExt = new Element("RDF", JDOMNamespaceUtil.RDF_NS);
		Element description = new Element("Description", JDOMNamespaceUtil.RDF_NS);
		relsExt.addContent(description);

		String value = element.getAttributeValue("discoverable", JDOMNamespaceUtil.CDR_ACL_NS);
		if (value != null) {
			Boolean discoverable = Boolean.parseBoolean(value);
			Element relation = new Element(ContentModelHelper.CDRProperty.allowIndexing.getPredicate(),
					ContentModelHelper.CDRProperty.allowIndexing.getNamespace());
			relation.setText(discoverable ? "yes" : "no");
			description.addContent(relation);
		}

		value = element.getAttributeValue("published", JDOMNamespaceUtil.CDR_ACL_NS);
		if (value != null) {
			Boolean boolValue = Boolean.parseBoolean(value);
			Element relation = new Element(ContentModelHelper.CDRProperty.isPublished.getPredicate(),
					ContentModelHelper.CDRProperty.isPublished.getNamespace());
			relation.setText(boolValue ? "yes" : "no");
			description.addContent(relation);
		}

		value = element.getAttributeValue("inherit", JDOMNamespaceUtil.CDR_ACL_NS);
		if (value != null) {
			Boolean boolValue = Boolean.parseBoolean(value);
			Element relation = new Element(ContentModelHelper.CDRProperty.inheritPermissions.getPredicate(),
					ContentModelHelper.CDRProperty.inheritPermissions.getNamespace());
			relation.setText(boolValue.toString());
			description.addContent(relation);
		}

		value = element.getAttributeValue("embargo-until", JDOMNamespaceUtil.CDR_ACL_NS);
		if (value != null) {
			Element relation = new Element(ContentModelHelper.CDRProperty.embargoUntil.getPredicate(),
					ContentModelHelper.CDRProperty.embargoUntil.getNamespace());
			relation.setText(value);
			description.addContent(relation);
		}

		for (Object childObject : element.getChildren()) {
			Element childElement = (Element) childObject;
			if (childElement.getNamespace().equals(JDOMNamespaceUtil.CDR_ACL_NS)) {
				String group = childElement.getAttributeValue("group", JDOMNamespaceUtil.CDR_ACL_NS);
				String role = childElement.getAttributeValue("role", JDOMNamespaceUtil.CDR_ACL_NS);

				// Validate the role is real
				UserRole userRole = UserRole.getUserRole(JDOMNamespaceUtil.CDR_ROLE_NS.getURI() + role);
				if (userRole != null) {
					Element relation = new Element(userRole.getPredicate(), JDOMNamespaceUtil.CDR_ROLE_NS);
					relation.setText(group);
					description.addContent(relation);
				}
			}
		}

		return relsExt;
	}
}
