/*
 * component:   "openEHR Reference Implementation"
 * description: "Class ADLSerializer"
 * keywords:    "archetype"
 *
 * author:      "Rong Chen <rong@acode.se>"
 * support:     "Acode HB <support@acode.se>"
 * copyright:   "Copyright (c) 2004,2005,2006 Acode HB, Sweden"
 * license:     "See notice at bottom of class"
 *
 * file:        "$URL: $"
 * revision:    "$LastChangedRevision: 41 $"
 * last_change: "$LastChangedDate: 2006-07-12 23:24:42 +0200 (Wed, 12 Jul 2006) $"
 */
package org.openehr.am.serialize;

import org.apache.commons.lang.*;
import org.openehr.am.archetype.*;
import org.openehr.am.archetype.assertion.*;
import org.openehr.am.archetype.constraintmodel.*;
import org.openehr.am.archetype.constraintmodel.primitive.*;
import org.openehr.am.archetype.ontology.*;
import org.openehr.am.openehrprofile.datatypes.quantity.*;
import org.openehr.am.openehrprofile.datatypes.text.*;
import org.openehr.rm.common.resource.*;
import org.openehr.rm.datatypes.quantity.*;
import org.openehr.rm.datatypes.text.*;
import org.openehr.rm.support.basic.*;
import org.openehr.rm.support.identification.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

/**
 * ADL serializer for the openEHR Java kernel
 * 
 * @author Rong Chen
 * @author Mattias Forss, Johan Hjalmarsson
 * @author Sebastian Garde
 * 
 * @version 1.0
 */
public class ADLSerializer {

	/**
	 * Create an outputter with default encoding, indent and lineSeparator
	 */
	public ADLSerializer() {
		this.encoding = UTF8;
		this.indent = "    "; // 4 white space characters
		this.lineSeparator = "\r\n";
	}

	public String getLineSeparator() {
		return lineSeparator;
	}

	public ADLSerializer setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
		return this;
	}

	public String getIndent() {
		return indent;
	}

	public ADLSerializer setIndent(String indent) {
		this.indent = indent;
		return this;
	}

	/**
	 * Output given archetype as string in ADL format
	 * 
	 * @param archetype
	 * @return a string in ADL format
	 * @throws IOException
	 */
	public String output(Archetype archetype) throws IOException {
		StringWriter writer = new StringWriter();
		output(archetype, writer);
		return writer.toString();
	}

		/**
	 * Output  archetype DEFINITION as string in ADL format
	 * 
	 * @param archetype
	 * @return a string in ADL format
	 * @throws IOException
	 */
	public String outputDefinitionOnly(Archetype archetype) throws IOException {
		StringWriter writer = new StringWriter();

		printDefinition(archetype, archetype.getDefinition(), writer);
				
		return writer.toString();
	}
	
	/**
	 * Output given archetype to outputStream
	 * 
	 * @param archetype
	 * @param out
	 * @throws IOException
	 */
	public void output(Archetype archetype, OutputStream out)
			throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new BufferedOutputStream(out), encoding));
		output(archetype, writer);
	}

	/**
	 * Output given archetype to writer
	 * 
	 * @param archetype
	 * @param out
	 * @throws IOException
	 */
	public void output(Archetype archetype, Writer out) throws IOException {
		printHeader(archetype.getAdlVersion(), archetype.getArchetypeId(), 
				archetype.getParentArchetypeId(), archetype.getUid(), archetype.getConcept(), out);
		newline(out);
		
		printLanguage(archetype, out);
		newline(out);
		
		if(archetype.getDescription() != null) {
			printDescription(archetype.getDescription(), out);
			newline(out);	
		}

		printDefinition(archetype, archetype.getDefinition(), out);
		newline(out);
		
		printOntology(archetype.getOntology(), out);
		out.flush();
		out.close();
	}
	
	protected void printHeader(String adlVersion,
			ArchetypeID id, ArchetypeID parentId, ObjectID uid,
			String conceptCode, Writer out) throws IOException {

		out.write("archetype");
        if(StringUtils.isNotEmpty(adlVersion) || (uid != null && StringUtils.isNotEmpty(uid.toString()))) {
            out.write(" (");  
        }

		if(StringUtils.isNotEmpty(adlVersion)) {
			out.write("adl_version=");
			out.write(adlVersion);
		}
		if(uid != null && StringUtils.isNotEmpty(uid.toString())) {
			if (StringUtils.isNotEmpty(adlVersion)) {
				out.write("; ");
			}
			out.write("uid=");
            out.write(uid.toString());
        }
	    if(StringUtils.isNotEmpty(adlVersion) || (uid!=null &&StringUtils.isNotEmpty(uid.toString()))) {
	        out.write(")");  
	    }
		newline(out);
		indent(1, out);
		out.write(id.toString());
		newline(out);

		if (parentId != null) {
			out.write("specialize");
			newline(out);
			indent(1, out);
			out.write(parentId.toString());
			newline(out);
		}

		newline(out);
		out.write("concept");
		newline(out);
		indent(1, out);
		out.write("[" + conceptCode + "]");
		newline(out);
	}
	
	protected void printLanguage(AuthoredResource authored,
			Writer out) throws IOException {

		out.write("language");
		newline(out);
		indent(1, out);
		out.write("original_language = <");
		out.write("[");
		out.write(authored.getOriginalLanguage().getTerminologyId().getValue());
		out.write("::");
		out.write(authored.getOriginalLanguage().getCodeString());
		out.write("]");
		out.write(">");
		newline(out);
		if(authored.getTranslations() != null) {
			indent(1, out);
			out.write("translations = <");
			newline(out);
			Map<String, TranslationDetails> translations = 
				authored.getTranslations();
			for(String lang : translations.keySet()) {
				TranslationDetails td = translations.get(lang);
				
				indent(2, out);
				out.write("[");
				out.write(quoteString(lang));
				out.write("] = <");
				newline(out);
				
				indent(3, out);
				out.write("language = <");
				out.write("[");
				out.write(td.getLanguage().getTerminologyId().getValue());
				out.write("::");
				out.write(td.getLanguage().getCodeString());
				out.write("]");
				out.write(">");
				newline(out);
				
				indent(3, out);
				out.write("author = <");
				newline(out);				
				printMap(td.getAuthor(), out, 4);				
				indent(3, out);
				out.write(">");
				newline(out);
				
				if(td.getAccreditation() != null) {
					indent(3, out);
					out.write("accreditation = <");
					out.write(quoteString(td.getAccreditation()));
					out.write(">");
					newline(out);
				}
				
				if(td.getOtherDetails() != null && !td.getOtherDetails().isEmpty()) {
					indent(3, out);
					out.write("other_details = <");
					newline(out);				
					printMap(td.getOtherDetails(), out, 4);				
					indent(3, out);
					out.write(">");
					newline(out);
				}
				
				indent(2, out);
				out.write(">");				
				newline(out);
			}
			indent(1, out);
			out.write(">");
			newline(out);
		}
	}

	protected void printMapAttribute(String attribute, Map<String,String> map, Writer out, int indent)
			throws IOException {
		if(map == null || map.size() == 0) {
			return;
		}
		indent(indent, out);
		out.write(attribute);
		out.write(" = <");
		printMap(map, out, indent+1);
		out.write(">");
		newline(out);
	}

	protected void printMap(Map<String,String> map, Writer out, int indent)
			throws IOException {
		if(map == null || map.size() == 0) {
			return;
		}
		for(String key : map.keySet()) {
			indent(indent, out);
			out.write("[");
			out.write(quoteString(key));
			out.write("] = <");
			out.write(quoteString(map.get(key)));
			out.write(">");
			newline(out);
		}
	}

	protected void printDescription(ResourceDescription description, Writer out)
			throws IOException {

		if (description == null) {
			return;
		}

		out.write("description");
		newline(out);

		indent(1, out);
		out.write("original_author = <");
		newline(out);
		Map<String, String> map = description.getOriginalAuthor();
		for (String key : map.keySet()) {
			indent(2, out);
			out.write("[" + quoteString(key) + "] = <" + quoteString(map.get(key)) + ">");
			newline(out);
		}
		indent(1, out);
		out.write(">");
		newline(out);

		indent(1, out);
		out.write("lifecycle_state = <");
		out.write(quoteString(description.getLifecycleState()));
		out.write(">");
		newline(out);

		printNonEmptyStringList("other_contributors", description.getOtherContributors(), 1, out);

		printNonEmptyString("resource_package_uri", description.getResourcePackageUri(), 1, out);
		
		indent(1, out);
		out.write("details = <");
		newline(out);
		for (ResourceDescriptionItem item : description.getDetails().values()) {
			printDescriptionItem(item, 2, out);
		}
		indent(1, out);
		out.write(">");
		newline(out);

		printNonEmptyStringMap("other_details", description.getOtherDetails(), 1, out);
	}

	protected void printDescriptionItem(ResourceDescriptionItem item,
			int indent, Writer out) throws IOException {
		indent(indent, out);
		out.write("[");
		out.write(quoteString(item.getLanguage().getCodeString()));
		out.write("] = <");
		newline(out);

		indent(indent + 1, out);
		out.write("language = <");
		out.write("[");
		out.write(item.getLanguage().getTerminologyId().getValue());
		out.write("::");
		out.write(item.getLanguage().getCodeString());
		out.write("]>");
		newline(out);
		
		printNonEmptyString("purpose", item.getPurpose(), indent + 1, out);
		printNonEmptyStringList("keywords", item.getKeywords(), indent + 1,
				out);
		printNonEmptyString("copyright", item.getCopyright(), indent + 1, out);
		printNonEmptyString("use", item.getUse(), indent + 1, out);
		printNonEmptyString("misuse", item.getMisuse(), indent + 1, out);
		printNonEmptyStringMap("original_resource_uri", item.getOriginalResourceUri(), indent + 1, out);

		// other details not printed
		printNonEmptyStringMap("other_details", item.getOtherDetails(), indent+1, out);

		indent(indent, out);
		out.write(">");
		newline(out);
	}

	private void printNonEmptyString(String label, String value, int indent,
			Writer out) throws IOException {

		if (StringUtils.isEmpty(value)) {
			return;
		}
		indent(indent, out);
		out.write(label);
		out.write(" = <");
		out.write(quoteString(value));
		out.write(">");
		newline(out);
	}

	private void printNonEmptyStringList(String label, List<String> list,
			int indent, Writer out) throws IOException {

		if (list == null || list.isEmpty()) {
			return;
		}
		indent(indent, out);
		out.write(label);
		out.write(" = <");
		for (int i = 0, j = list.size(); i < j; i++) {
			out.write(quoteString(list.get(i)));
			if (i != j - 1) {
				out.write(",");
			}
		}
		if (list.size()==1) {
			out.write(", ...");
		}
		out.write(">");
		newline(out);
	}

	private void printNonEmptyStringMap(String label, Map<String, String> map,
			int indent, Writer out) throws IOException {
		if (map == null || map.isEmpty()) {
			return;
		}

		indent(indent, out);
		out.write(label);
		out.write(" = <");

		for (String key : map.keySet()) {
			newline(out);
			indent(indent+1, out);
			out.write("[" + quoteString(key) + "] = <" + quoteString(map.get(key)) + ">");
		}
		newline(out);
		indent(indent, out);
		out.write(">");
		newline(out);
	}

	protected void printDefinition(Archetype archetype, CComplexObject definition, Writer out)
			throws IOException {

		out.write("definition");
		newline(out);

		printCComplexObject(archetype, definition, 1, out);
	}

	protected void printCComplexObject(Archetype archetype, CComplexObject ccobj, int indent,
			Writer out) throws IOException {
		
		// TODO skip c_obj with [0,0] occurrences
		Interval<Integer> occurrences = ccobj.getOccurrences();
		if(occurrences != null 
				&& (Integer.valueOf(0).equals(occurrences.getLower()))
				&& (Integer.valueOf(0).equals(occurrences.getUpper()))) {
			return;		
		}
						

		// print rmTypeName and nodeId
		indent(indent, out);
		out.write(ccobj.getRmTypeName());
		if (StringUtils.isNotEmpty(ccobj.getNodeId())) {
			out.write("[" + ccobj.getNodeId() + "]");
		}

		printOccurrences(ccobj.getOccurrences(), out);

		out.write(" matches {");

		// print all attributes
		if (!ccobj.isAnyAllowed()) {
			printLabelComment(archetype, ccobj.getNodeId(), out);
			for (CAttribute cattribute : ccobj.getAttributes()) {
				printCAttribute(archetype, cattribute, indent + 1, out);
			}
			newline(out);
			indent(indent, out);
			out.write("}");
		} else {
			out.write("*}");
			printLabelComment(archetype, ccobj.getNodeId(), out);
		}
		newline(out);
	}

	private void printLabelComment(Archetype archetype, String nodeId, Writer out) throws IOException {
		if (archetype==null || nodeId == null) return;
		if (archetype.getOriginalLanguage() == null) return;
		if (archetype.getOntology() == null) return;
		if (archetype.getOntology().getTermDefinitionsList()==null) return;
		for (OntologyDefinitions ontologyDefinitions : archetype.getOntology().getTermDefinitionsList()) {
			if (!Objects.equals(ontologyDefinitions.getLanguage(), archetype.getOriginalLanguage().getCodeString())) continue;
			if (ontologyDefinitions.getDefinitions()==null) continue;

			for (ArchetypeTerm at : ontologyDefinitions.getDefinitions()) {
				if (!nodeId.equals(at.getCode()) || at.getText()==null || at.getText().isEmpty()) continue;
				String label = at.getText().replace("\\s+", " ");
				out.write("    -- ");
				out.write(label);
				return;
			}
		}

	}

	protected void printOccurrences(Interval<Integer> occurrences, Writer out)
			throws IOException {

		Interval<Integer> defaultOccurrences = new Interval<Integer>(1, 1);
		if(occurrences == null || defaultOccurrences.equals(occurrences)) {
			return;
		}
		if (occurrences != null) {
			out.write(" occurrences matches {");
			if (occurrences.getLower() == null) {
				out.write("*");
			} else {
				out.write(Integer.toString(occurrences.getLower()));
			}
			out.write("..");
			if (occurrences.getUpper() == null) {
				out.write("*");
			} else {
				out.write(Integer.toString(occurrences.getUpper()));
			}
			out.write("}");
		}
	}

	protected void printArchetypeInternalRef(ArchetypeInternalRef ref,
			int indent, Writer out) throws IOException {
		indent(indent, out);
		out.write("use_node ");
		out.write(ref.getRmTypeName());
		printOccurrences(ref.getOccurrences(), out);
		out.write(" ");		
		out.write(ref.getTargetPath());
		newline(out);
	}

	protected void printArchetypeSlot(Archetype archetype, ArchetypeSlot slot, int indent, Writer out)
			throws IOException {

		indent(indent, out);
		out.write("allow_archetype ");
		out.write(slot.getRmTypeName());
		if (StringUtils.isNotEmpty(slot.getNodeId())) {
			out.write("[" + slot.getNodeId() + "]");
		}

		printOccurrences(slot.getOccurrences(), out);
		out.write(" matches {");

		if (slot.isAnyAllowed()) {
			out.write("*}");
			printLabelComment(archetype, slot.getNodeId(), out);
		} else {
			printLabelComment(archetype, slot.getNodeId(), out);
			if (slot.getIncludes() != null) {
				printAssertions(slot.getIncludes(), "include", indent, out);
			}
			if (slot.getExcludes() != null) {
				printAssertions(slot.getExcludes(), "exclude", indent, out);
			}
			newline(out);
			indent(indent, out);
			out.write("}");			
		}
		newline(out);
	}
	
	private void printAssertions(Set<Assertion> assertions, String purpose,
			int indent, Writer out)	throws IOException {

		if(assertions == null) {
			return;
		}
		
		newline(out);
		indent(indent + 1, out);
		out.write(purpose);
		
		for (Assertion assertion : assertions) {
			
			if(assertion.getStringExpression() == null) {
				continue;
			}
			
			newline(out);
			indent(indent + 2, out);
			
			// FIXME: The string expression is null when an archetype is parsed, but after the archetype is recreated in the archetype 
			// editor, the string expression exists. Please provide a valid string expression from the parser since it's _much_ easier to 
			// maintain this line of code instead of adding hundreds of lines just to output some expressions, operators etc.
			// Opening an archetype directly in the ADL format view will show the output of the parsed archetype in this way:
			//
			// include
			//     null
			out.write(assertion.getStringExpression());
		}
	}

	protected void printCAttribute(Archetype archetype, CAttribute cattribute, int indent, Writer out)
			throws IOException {
		newline(out);
		indent(indent, out);
		out.write(cattribute.getRmAttributeName());
		if (!CAttribute.Existence.REQUIRED.equals(cattribute.getExistence())) {
			out.write(" ");
		}
		printExistence(cattribute.getExistence(), out);
		if (cattribute instanceof CMultipleAttribute) {
			CMultipleAttribute cma = (CMultipleAttribute) cattribute;
			if(cma.getCardinality() != null) {
				out.write(" ");
			    printCardinality(cma.getCardinality(), out);
			}
		}
		List<CObject> children = cattribute.getChildren();
		out.write(" matches {");
		if(children == null || children.size() == 0) {
			out.write("*");
		} else if (children.size() != 1
				|| !(children.get(0) instanceof CPrimitiveObject)) {
			newline(out);
			for (CObject cobject : cattribute.getChildren()) {
				printCObject(archetype, cobject, indent + 1, out);
			}
			indent(indent, out);
		} else {
			CObject child = children.get(0);
			printCPrimitiveObject((CPrimitiveObject) child, out);
		}
		out.write("}");
	}

	protected void printExistence(CAttribute.Existence existence, Writer out)
			throws IOException {
		if (existence==null) return;
		if (CAttribute.Existence.REQUIRED.equals(existence)) {
			return;
		}
		out.write("existence matches ");
		if (CAttribute.Existence.OPTIONAL.equals(existence)) {
			out.write("{0..1}");
		} else {
			out.write("{0}");
		}
	}

	protected void printCObject(Archetype archetype, CObject cobj, int indent, Writer out)
			throws IOException {

		// print specialised types
		if (cobj instanceof CDomainType) {
			printCDomainType(archetype, (CDomainType) cobj, indent, out);
		} else if (cobj instanceof CPrimitiveObject) {
			printCPrimitiveObject((CPrimitiveObject) cobj, out);
		} else if (cobj instanceof CComplexObject) {
			printCComplexObject(archetype, (CComplexObject) cobj, indent, out);
		} else if (cobj instanceof ArchetypeInternalRef) {
			printArchetypeInternalRef((ArchetypeInternalRef) cobj, indent, out);
		} else if (cobj instanceof ArchetypeSlot) {
			printArchetypeSlot(archetype, (ArchetypeSlot) cobj, indent, out);
		} else if (cobj instanceof ConstraintRef) {
			printConstraintRef((ConstraintRef) cobj, indent, out);
		}
	}
	
	protected void printConstraintRef(ConstraintRef ref, int indent, Writer out) throws IOException {
		indent(indent, out);
		out.write("[");
		out.write(ref.getReference());
		out.write("]");
		newline(out);
	}

	protected void printCardinality(Cardinality cardinality, Writer out)
			throws IOException {
		out.write("cardinality matches {");
		Interval<Integer> interval = cardinality.getInterval();
		if (interval != null) {
			if (interval.isLowerUnbounded()) {
				out.write("*");
			} else {
				out.write(interval.getLower().toString());
			}
			out.write("..");
			if (interval.isUpperUnbounded()) {
				out.write("*");
			} else {
				out.write(interval.getUpper().toString());
			}
		} else {
			out.write("*");
		}
		out.write("; ");
		if (cardinality.isOrdered()) {
			out.write("ordered");
		} else {
			out.write("unordered");
		}
		if (cardinality.isUnique()) {
			out.write("; unique");
		}
		out.write("}");
	}

	protected void printCDomainType(Archetype archetype, CDomainType cdomain, int indent, Writer out)
			throws IOException {
		if (cdomain instanceof CDvOrdinal) {
			printCDvOrdinal((CDvOrdinal) cdomain, indent, out);
		} else if (cdomain instanceof CDvQuantity) {
			printCDvQuantity((CDvQuantity) cdomain, indent, out);
		}  
		 else if (cdomain instanceof CCodePhrase) {
		    printCCodePhrase(archetype, (CCodePhrase) cdomain, indent, out);
		}
		// unknow CDomainType
	}

	protected void printCCodePhrase(Archetype archetype, CCodePhrase ccoded, int indent, Writer out)
			throws IOException {

		indent(indent, out);
		
		if(ccoded.isAnyAllowed()) {
			out.write("C_CODE_PHRASE <");
			newline(out);
			indent(indent, out);
			out.write(">");
			newline(out);
			return;
		}

		if (ccoded.getTerminologyId() != null) {
			out.write("[" + ccoded.getTerminologyId().getValue() + "::");
		}

		if (ccoded.getCodeList() != null) {
			if (ccoded.getCodeList().size() > 1) {
				newline(out);

				for (int i = 0, j = ccoded.getCodeList().size(); i < j; i++) {
					if (j > 1) {
						indent(indent, out);
					}
					out.write(ccoded.getCodeList().get(i));
					if (i != j - 1) {
						out.write(",");
						printLabelComment(archetype, ccoded.getCodeList().get(i), out);
					} else {
						if(ccoded.hasAssumedValue()) {
							out.write(";");
							printLabelComment(archetype, ccoded.getCodeList().get(i), out);
							newline(out);
							indent(indent, out);
							out.write(ccoded.getAssumedValue().getCodeString());
							out.write("]");
						} else {
							out.write("]");
							printLabelComment(archetype, ccoded.getCodeList().get(i), out);
						}
					}
					newline(out);
				}
			} else {
				out.write(ccoded.getCodeList().get(0));
				if(ccoded.hasAssumedValue()) {
					out.write(";" + ccoded.getAssumedValue().getCodeString());
				}
				out.write("]");
				printLabelComment(archetype, ccoded.getCodeList().get(0), out);
				newline(out);
			} 
		} else {
			out.write("]");
			newline(out);
		}
	}

	protected void printCDvOrdinal(CDvOrdinal cordinal, int indent, Writer out)
			throws IOException {

		// if the list is null, the CDvOrdinal is not further constrained
		// (other than that it is a CDvOrdinal)
		if (cordinal.isAnyAllowed()) {
			indent(indent, out);
			out.write("C_DV_ORDINAL <");
			newline(out);
			indent(indent, out);
			out.write(">");
			newline(out);
		}
		else {
			for (Iterator<Ordinal> it = cordinal.getList().iterator(); 
			it.hasNext();) {
				Ordinal ordinal = it.next();			
				indent(indent, out);
				printOrdinal(ordinal, out);
				if (it.hasNext()) {
					out.write(",");
				} else if(cordinal.hasAssumedValue()) {
					out.write(";");
				}
				newline(out);			
			}
			if(cordinal.hasAssumedValue()) {
				printOrdinal(cordinal.getAssumedValue(), out);
				newline(out);

			}
		}	
	}

	protected void printOrdinal(Ordinal ordinal, Writer out) 
			throws IOException {
		CodePhrase symbol = ordinal.getSymbol();
		out.write(Integer.toString(ordinal.getValue()));
		out.write("|[");
		out.write(symbol.getTerminologyId().getValue());
		out.write("::");
		out.write(symbol.getCodeString());
		out.write("]");		
	}

	protected void printCDvQuantity(CDvQuantity cquantity, int indent,
			Writer out) throws IOException {
		indent(indent, out);
		out.write("C_DV_QUANTITY <");
		newline(out);
		indent(indent + 1, out);
		CodePhrase property = cquantity.getProperty();
		if (property != null) {
			out.write("property = <[");
			out.write(property.getTerminologyId().getValue());
			out.write("::");
			out.write(property.getCodeString());
			out.write("]>");
		}
		List<CDvQuantityItem> list = cquantity.getList();
		if (list != null) {
			newline(out);			
			indent(indent + 1, out);
			out.write("list = <");
			newline(out);
			int index = 1;
			for (CDvQuantityItem item : list) {
				indent(indent + 2, out);
				out.write("[");
				out.write(quoteString(Integer.toString(index)));
				out.write("] = <");
				newline(out);
				indent(indent + 3, out);
				out.write("units = <");
				out.write(quoteString(item.getUnits()));
				out.write(">");
				newline(out);
				Interval<Double> value = item.getMagnitude();
				if (value != null) {
					indent(indent + 3, out);
					out.write("magnitude = <");
					printInterval(value, out);
					out.write(">");
					newline(out);
				}
				
				Interval<Integer> precision = item.getPrecision();
				if (precision != null) {
					indent(indent + 3, out);
					out.write("precision = <");
					printInterval(precision, out);
					out.write(">");
					newline(out);
				}
				index++;
				indent(indent + 2, out);
				out.write(">");
				newline(out);
			}
			indent(indent + 1, out);
			out.write(">");
			newline(out);
		}
		
		
		if(cquantity.getAssumedValue() != null) {
			newline(out);			
			indent(indent + 1, out);
			out.write("assumed_value = <");
			newline(out);
			printDvQuantity(cquantity.getAssumedValue(), indent + 1, out);
			indent(indent + 1, out);
			out.write(">");
			newline(out);
		}		
		
		indent(indent, out);
		out.write(">");
		newline(out);
	}
	
	protected void printDvQuantity(DvQuantity quantity, int indent, Writer out) 
			throws IOException {
		
		indent(indent + 1, out);
		printUnits(quantity.getUnits(), out);
		newline(out);
		
		if(quantity.getMagnitude() != null) {
			indent(indent + 1, out);
			out.write("magnitude = <");
			out.write(quantity.getMagnitude().toString());
			out.write(">");
			newline(out);
		}
		indent(indent + 1, out);
		out.write("precision = <");
		out.write(Integer.toString(quantity.getPrecision()));
		out.write(">");
		newline(out);
	}
	
	protected void printUnits(String units, Writer out) throws IOException {
		out.write("units = <");
		out.write(quoteString(units));
		out.write(">");
	}

	protected void printOntology(ArchetypeOntology ontology, Writer out)
			throws IOException {

		out.write("ontology");
		newline(out);

		printNonEmptyStringList("terminologies_available", ontology.getTerminologies(), 1, out);

		// *** Term definition section *** (ADL 1.4 spec 8.6.3)
		indent(1, out);
		out.write("term_definitions = <");
		newline(out);
		List<OntologyDefinitions> termDefinitionsList = ontology.getTermDefinitionsList();
		printDefinitionList(out, termDefinitionsList);
		indent(1, out);
		out.write(">");
		newline(out);

		// *** Constraint definition section *** (ADL 1.4 spec 8.6.4)
		List<OntologyDefinitions> constraintDefinitionsList = ontology.getConstraintDefinitionsList();
		if (constraintDefinitionsList != null) {
			indent(1, out);
			out.write("constraint_definitions = <");
			newline(out);
			printDefinitionList(out, constraintDefinitionsList);
			indent(1, out);
			out.write(">");
			newline(out);
		}

		// *** Term binding section *** (ADL 1.4 spec 8.6.5)
		if (ontology.getTermBindingList() != null) {
			indent(1, out);
			out.write("term_binding = <");
			newline(out);
			for (int i = 0; i < ontology.getTermBindingList().size(); i++) {
				OntologyBinding bind = ontology.getTermBindingList().get(i);
				indent(2, out);
				out.write("[");
				out.write(quoteString(bind.getTerminology()));
				out.write("] = <");
				newline(out);
				indent(3, out);
				out.write("items = <");
				newline(out);

				for (int j = 0; j < ontology.getTermBindingList().get(i)
						.getBindingList().size(); j++) {
					TermBindingItem item = (TermBindingItem) ontology
							.getTermBindingList().get(i).getBindingList()
							.get(j);
					indent(4, out);
					out.write("[");
					out.write(quoteString(item.getCode()));
					out.write("] = <");
					out.write(item.getTerms().get(0));

					if (item.getTerms().size() > 1) {
						for (int k = 1; k < item.getTerms().size(); k++) {
                            out.write("," + item.getTerms().get(k));
                        }
					}

					out.write(">");
					newline(out);
				}
				for (int l = 3; l > 1; l--) {
					indent(l, out);
					out.write(">");
					newline(out);
				}
			}
			indent(1, out);
			out.write(">");
			newline(out);
		}

		// *** Constraint binding section *** (ADL 1.4 spec 8.6.6)
		if (ontology.getConstraintBindingList() != null) {
			indent(1, out);
			out.write("constraint_binding = <");
			newline(out);
			for (int i = 0; i < ontology.getConstraintBindingList().size(); i++) {
				OntologyBinding bind = ontology.getConstraintBindingList().get(
						i);
				indent(2, out);
				out.write("[");
				out.write(quoteString(bind.getTerminology()));
				out.write("] = <");
				newline(out);
				indent(3, out);
				out.write("items = <");
				newline(out);

				for (int j = 0; j < ontology.getConstraintBindingList().get(i)
						.getBindingList().size(); j++) {
					QueryBindingItem item = (QueryBindingItem) ontology
							.getConstraintBindingList().get(i).getBindingList()
							.get(j);
					indent(4, out);
					out.write("[");
					out.write(quoteString(item.getCode()));
					out.write("] = <");
					out.write(item.getQuery().getUrl());
					out.write(">");
					newline(out);
				}
				for (int l = 3; l > 1; l--) {
					indent(l, out);
					out.write(">");
					newline(out);
				}
			}
			indent(1, out);
			out.write(">");
			newline(out);
		}
	}

    private String quoteString(String value) {
		String escaped = value.replace("\\", "\\\\")
				.replace("\"", "\\\"");

        return "\"" + escaped + "\"";
    }

	private void printDefinitionList(Writer out,
			List<OntologyDefinitions> termDefinitionsList) throws IOException {
		for (OntologyDefinitions defs : termDefinitionsList) {
			indent(2, out);
			out.write("[");
			out.write(quoteString(defs.getLanguage()));
			out.write("] = <");
			newline(out);
			indent(3, out);
			out.write("items = <");
			newline(out);
			for (ArchetypeTerm term : defs.getDefinitions()) {
				indent(4, out);
				out.write("[");
				out.write(quoteString(term.getCode()));
				out.write("] = <");
				for (Map.Entry<String, String> entry : term.getItems().entrySet()) {
					newline(out);
					indent(5, out);
					out.write(entry.getKey());
					out.write(" = <");
					out.write(quoteString(entry.getValue()));
					out.write(">");
				}
				newline(out);
				indent(4, out);
				out.write(">");
				newline(out);
			}
			for (int i = 3; i > 1; i--) {
				indent(i, out);
				out.write(">");
				newline(out);
			}
		}
	}

	protected void printCPrimitiveObject(CPrimitiveObject cpo, Writer out)
			throws IOException {

		CPrimitive cp = cpo.getItem();
		if (cp instanceof CBoolean) {
			printCBoolean((CBoolean) cp, out);
		} else if (cp instanceof CDate) {
			printCDate((CDate) cp, out);
		} else if (cp instanceof CDateTime) {
			printCDateTime((CDateTime) cp, out);
		} else if (cp instanceof CTime) {
			printCTime((CTime) cp, out);
		} else if (cp instanceof CDuration) {
			printCDuration((CDuration) cp, out);
		} else if (cp instanceof CInteger) {
			printCInteger((CInteger) cp, out);
		} else if (cp instanceof CReal) {
			printCReal((CReal) cp, out);
		} else if (cp instanceof CString) {
			printCString((CString) cp, out);
		}
		// unknow CPrimitive type
	}

	protected void printCBoolean(CBoolean cboolean, Writer out)
			throws IOException {
		if (cboolean.isTrueValid()) {
			out.write("true");
			if (cboolean.isFalseValid()) {
				out.write(", false");
			}
		} else {
			out.write("false");
		}
		if(cboolean.hasAssumedValue()) {
			out.write("; ");
			if(cboolean.assumedValue().booleanValue()) {
				out.write("true");
			} else {
				out.write("false");
			}
 		}
	}

	protected void printCDate(CDate cdate, Writer out) throws IOException {
		if (cdate.getPattern() != null) {
			out.write(cdate.getPattern());
		} else if (cdate.getList() != null) {
			out.write(cdate.getList().get(0).toString());
		} else {
			printInterval(cdate.getInterval(), out);
		}
		if(cdate.hasAssumedValue()) {
			out.write("; ");
			out.write(cdate.assumedValue().toString());
		}
	}

	protected void printCDateTime(CDateTime cdatetime, Writer out)
			throws IOException {
		if (cdatetime.getPattern() != null) {
			out.write(cdatetime.getPattern());
		} else if (cdatetime.getList() != null) {
			out.write(cdatetime.getList().get(0).toString());
		} else {
			printInterval(cdatetime.getInterval(), out);
		}
		if(cdatetime.hasAssumedValue()) {
			out.write("; ");
			out.write(cdatetime.assumedValue().toString());
		}
	}

	protected void printCTime(CTime ctime, Writer out) throws IOException {
		if (ctime.getPattern() != null) {
			out.write(ctime.getPattern());
		} else if (ctime.getList() != null) {
			out.write(ctime.getList().get(0).toString());
		} else {
			printInterval(ctime.getInterval(), out);
		}
		if(ctime.hasAssumedValue()) {
			out.write("; ");
			out.write(ctime.assumedValue().toString());
		}
	}

	protected void printCDuration(CDuration cduration, Writer out)
			throws IOException {
		if (cduration.getValue() != null) {
			out.write(cduration.getValue().toString());
		} else {
			if(cduration.getPattern() != null) {
				out.write(cduration.getPattern());
			}
			if (cduration.getInterval()!=null) {
				if (cduration.getPattern()!=null) out.write("/");
				printInterval(cduration.getInterval(), out);
			}
		}
		if(cduration.assumedValue() != null) {
			out.write("; ");
			out.write(cduration.assumedValue().toString());
		}
	}

	protected void printCInteger(CInteger cinteger, Writer out)
			throws IOException {
		if (cinteger.getList() != null) {
			printList(cinteger.getList(), out);
		} else {
			printInterval(cinteger.getInterval(), out);
		}
		if(cinteger.assumedValue() != null) {
			out.write("; ");
			out.write(cinteger.assumedValue().toString());
		}
	}

	protected void printCReal(CReal creal, Writer out) throws IOException {
		if (creal.getList() != null) {
			printList(creal.getList(), out);
		} else {
			printInterval(creal.getInterval(), out);
		}
		if(creal.assumedValue() != null) {
			out.write("; ");
			out.write(creal.assumedValue().toString());
		}
	}

	protected void printCString(CString cstring, Writer out) throws IOException {
		if (cstring.getPattern() != null) {
			out.write("/" + cstring.getPattern() + "/");
		} else if(cstring.getList() != null){
			printList(cstring.getList(), out, true);
		} else if(cstring.defaultValue() != null) {
			out.write(quoteString(cstring.defaultValue()));
		}
		if(cstring.hasAssumedValue()) {
			out.write("; ");
			out.write(quoteString(ObjectUtils.toString(cstring.assumedValue(), "")));
		}
	}

	protected void printList(List list, Writer out) throws IOException {
		printList(list, out, false);
	}

	protected void printList(List list, Writer out, boolean string)
			throws IOException {
		for (int i = 0, j = list.size(); i < j; i++) {
			if (i != 0) {
				out.write(",");
			}
			String item = list.get(i).toString();
			if (string) {
				out.write(quoteString(item));
			} else {
			    out.write(item);
            }
		}
	}

	protected void printInterval(Interval interval, Writer out)
			throws IOException {
		out.write("|");
		if (interval.getLower() != null && interval.getUpper() != null) {
			if(interval.getLower().equals(interval.getUpper())
					&& interval.isLowerIncluded() 
					&& interval.isUpperIncluded()) {
				out.write(interval.getLower().toString());
			} else {
				if (!interval.isLowerIncluded()) out.write(">");
				out.write(interval.getLower().toString());
				out.write("..");
				if (!interval.isUpperIncluded()) out.write("<");
				out.write(interval.getUpper().toString());
			}
		} else if (interval.getLower() == null) {
			out.write("<");
			if (interval.isUpperIncluded()) {
				out.write("=");
			}
			out.write(interval.getUpper().toString());
		} else {
			out.write(">");
			if (interval.isLowerIncluded()) {
				out.write("=");
			}
			out.write(interval.getLower().toString());
		}
		out.write("|");
	}

	private void newline(Writer out) throws IOException {
		out.write(lineSeparator);
	}

	private void indent(int level, Writer out) throws IOException {
		for (int i = 0; i < level; i++) {
			out.write(indent);
		}
	}

	/* charset encodings */
	public static final Charset UTF8 = Charset.forName("UTF-8");
	public static final Charset LATIN1 = Charset.forName("ISO-8859-1");

	/* fields */
	private Charset encoding;
	private String lineSeparator;
	private String indent;
}
/*
 * ***** BEGIN LICENSE BLOCK ***** Version: MPL 1.1/GPL 2.0/LGPL 2.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the 'License'); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is ADLSerializer.java
 * 
 * The Initial Developer of the Original Code is Rong Chen. Portions created by
 * the Initial Developer are Copyright (C) 2004-2007 the Initial Developer. All
 * Rights Reserved.
 * 
 * Contributor(s): Mattias Forss, Johan Hjalmarsson, Erik Sundvall, 
 *                 Sebastian Garde
 * 
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * ***** END LICENSE BLOCK *****
 */
