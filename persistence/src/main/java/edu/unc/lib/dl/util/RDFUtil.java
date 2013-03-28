package edu.unc.lib.dl.util;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.abdera.model.Element;

import edu.unc.lib.dl.acl.util.UserRole;
import edu.unc.lib.dl.xml.JDOMNamespaceUtil;

public class RDFUtil {

	public static org.jdom.Element mergeRDF(org.jdom.Element baseRDF, org.jdom.Element incomingRDF) {
		if (baseRDF == null)
			return incomingRDF;
		if (incomingRDF == null)
			return baseRDF;
		// If there is no rdf:Description tag in the incoming data, then there is nothing to add.
		if (incomingRDF.getChildren().size() == 0)
			return baseRDF;

		org.jdom.Element incomingDescription = (org.jdom.Element) incomingRDF.getChildren("Description",
				JDOMNamespaceUtil.RDF_NS).get(0);
		org.jdom.Element newDescription = null;

		// If the previous rels-ext didn't have a description, then use the new one
		if (baseRDF.getChildren().size() == 0) {
			baseRDF.addContent((org.jdom.Element) incomingDescription.clone());
			return baseRDF;
		} else {
			newDescription = (org.jdom.Element) baseRDF.getChildren().get(0);
		}

		// Clone all the child elements of the incoming rdf:Description tag
		@SuppressWarnings("unchecked")
		List<org.jdom.Element> incomingElements = (List<org.jdom.Element>) incomingDescription.getChildren();
		// Add all the incoming element children to the base modified object
		for (org.jdom.Element incomingElement : incomingElements) {
			newDescription.addContent((org.jdom.Element) incomingElement.clone());
		}

		return baseRDF;
	}

	public static org.jdom.Element updateRDF(org.jdom.Element baseRDF, org.jdom.Element incomingRDF) {
		if (baseRDF == null)
			return incomingRDF;
		if (incomingRDF == null)
			return baseRDF;
		// If there is no rdf:Description tag in the incoming data, then there is nothing to add.
		if (incomingRDF.getChildren().size() == 0)
			return baseRDF;

		org.jdom.Element incomingDescription = (org.jdom.Element) incomingRDF.getChildren("Description", JDOMNamespaceUtil.RDF_NS).get(0);
		org.jdom.Element newDescription = null;

		// If the previous rels-ext didn't have a description, then use the new one
		if (baseRDF.getChildren().size() == 0) {
			baseRDF.addContent((org.jdom.Element) incomingDescription.clone());
			return baseRDF;
		} else {
			newDescription = (org.jdom.Element) baseRDF.getChildren().get(0);
		}

		// Clone all the child elements of the incoming rdf:Description tag
		@SuppressWarnings("unchecked")
		List<org.jdom.Element> incomingElements = (List<org.jdom.Element>) incomingDescription.getChildren();
		// Add all the incoming element children to the base modified object
		for (org.jdom.Element incomingElement : incomingElements) {
			// Remove the preexisting relations of the same type before adding the new entries
			newDescription.removeChildren(incomingElement.getName(), incomingElement.getNamespace());
			newDescription.addContent((org.jdom.Element) incomingElement.clone());
		}

		return baseRDF;
	}

	public static org.jdom.Element aclToRDF(Element element) {
		org.jdom.Element relsExt = new org.jdom.Element("RDF", JDOMNamespaceUtil.RDF_NS);
		org.jdom.Element description = new org.jdom.Element("Description", JDOMNamespaceUtil.RDF_NS);
		relsExt.addContent(description);

		String value = element.getAttributeValue(new QName(JDOMNamespaceUtil.CDR_ACL_NS.getURI(), "discoverable"));
		if (value != null) {
			Boolean discoverable = Boolean.parseBoolean(value);
			org.jdom.Element relation = new org.jdom.Element(ContentModelHelper.CDRProperty.allowIndexing.getPredicate(),
					ContentModelHelper.CDRProperty.allowIndexing.getNamespace());
			relation.setText(discoverable ? "yes" : "no");
			description.addContent(relation);
		}

		value = element.getAttributeValue(new QName(JDOMNamespaceUtil.CDR_ACL_NS.getURI(), "published"));
		if (value != null) {
			Boolean boolValue = Boolean.parseBoolean(value);
			org.jdom.Element relation = new org.jdom.Element(ContentModelHelper.CDRProperty.isPublished.getPredicate(),
					ContentModelHelper.CDRProperty.isPublished.getNamespace());
			relation.setText(boolValue ? "yes" : "no");
			description.addContent(relation);
		}

		value = element.getAttributeValue(new QName(JDOMNamespaceUtil.CDR_ACL_NS.getURI(), "inherit"));
		if (value != null) {
			Boolean boolValue = Boolean.parseBoolean(value);
			org.jdom.Element relation = new org.jdom.Element(
					ContentModelHelper.CDRProperty.inheritPermissions.getPredicate(),
					ContentModelHelper.CDRProperty.inheritPermissions.getNamespace());
			relation.setText(boolValue.toString());
			description.addContent(relation);
		}

		value = element.getAttributeValue(new QName(JDOMNamespaceUtil.CDR_ACL_NS.getURI(), "embargo-until"));
		if (value != null) {
			org.jdom.Element relation = new org.jdom.Element(ContentModelHelper.CDRProperty.embargoUntil.getPredicate(),
					ContentModelHelper.CDRProperty.embargoUntil.getNamespace());
			relation.setText(value);
			description.addContent(relation);
		}

		QName grantName = new QName(JDOMNamespaceUtil.CDR_ACL_NS.getURI(), "grant");
		for (Element childElement : element.getElements()) {
			if (grantName.equals(childElement.getQName())) {
				String group = childElement.getAttributeValue(new QName(JDOMNamespaceUtil.CDR_ACL_NS.getURI(), "group"));
				String role = childElement.getAttributeValue(new QName(JDOMNamespaceUtil.CDR_ACL_NS.getURI(), "role"));

				// Validate the role is real
				UserRole userRole = UserRole.getUserRole(JDOMNamespaceUtil.CDR_ROLE_NS.getURI() + role);
				if (userRole != null) {
					org.jdom.Element relation = new org.jdom.Element(userRole.getPredicate(),
							JDOMNamespaceUtil.CDR_ROLE_NS.getURI());
					relation.setText(group);
					description.addContent(relation);
				}
			}
		}

		return relsExt;
	}
}
