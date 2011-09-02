/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package org.docx4j.samples;


import java.util.HashMap;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;


public class PartsList extends AbstractSample {
	
	private static Logger log = Logger.getLogger(PartsList.class);						

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		

		
		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
//			 inputfilepath = System.getProperty("user.dir") 
//			+ "/sample-docs/test-docs/header-footer/header_sections_some-linked.xml";
//	 inputfilepath = System.getProperty("user.dir") + "/sample-docs/xlsx/pivot.xlsm";
		inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.xml";
	// inputfilepath = System.getProperty("user.dir") + "/sample-docs/pptx/lines.pptx";
		}
		
			
		// Open a document from the file system
		// 1. Load the Package - .docx or Flat OPC .xml
		org.docx4j.openpackaging.packages.OpcPackage opcPackage = org.docx4j.openpackaging.packages.OpcPackage.load(new java.io.File(inputfilepath));		
		
		//printContentTypes(opcPackage);
		
		// List the parts by walking the rels tree
		RelationshipsPart rp = opcPackage.getRelationshipsPart();
		StringBuilder sb = new StringBuilder();
		printInfo(rp, sb, "");
		traverseRelationships(opcPackage, rp, sb, "    ");
		
		System.out.println(sb.toString());
		
//		SaveToZipFile saver = new SaveToZipFile(opcPackage);
//		saver.save(System.getProperty("user.dir") + "/out.docx");
		
	}
	
	public static void printContentTypes(org.docx4j.openpackaging.packages.OpcPackage p) {
		
		ContentTypeManager ctm = p.getContentTypeManager();
		
		ctm.listTypes();
		
	}
	
	public static void  printInfo(Part p, StringBuilder sb, String indent) {
		
		String relationshipType = "";
		if (p.getSourceRelationship()!=null ) {
			relationshipType = p.getSourceRelationship().getType();
		}
		
		sb.append("\n" + indent + "Part " + p.getPartName() + " [" + p.getClass().getName() + "] " + relationshipType );
		
//		System.out.println("//" + p.getPartName() );
//		System.out.println("public final static String XX =");
//		System.out.println("\"" +  relationshipType +  "\";");
		
		if (p instanceof JaxbXmlPart) {
			Object o = ((JaxbXmlPart)p).getJaxbElement();
			if (o instanceof javax.xml.bind.JAXBElement) {
				sb.append(" containing JaxbElement:" + XmlUtils.JAXBElementDebug((JAXBElement)o) );
			} else {
				sb.append(" containing JaxbElement:"  + o.getClass().getName() );
			}
		}
	}
	
	/**
	 * This HashMap is intended to prevent loops.
	 */
	public static HashMap<Part, Part> handled = new HashMap<Part, Part>();
	
	public static void traverseRelationships(org.docx4j.openpackaging.packages.OpcPackage wordMLPackage, 
			RelationshipsPart rp, 
			StringBuilder sb, String indent) {
		
		// TODO: order by rel id
		
//		if (rp.getRelationships().getRelationship().size()==0) {
//			System.out.println("In rels part .. empty");
//		}
		
		for ( Relationship r : rp.getRelationships().getRelationship() ) {
			
			log.info("\nFor Relationship Id=" + r.getId() 
					+ " Source is " + rp.getSourceP().getPartName() 
					+ ", Target is " + r.getTarget() 
					+ " type " + r.getType() + "\n");
		
			if (r.getTargetMode() != null
					&& r.getTargetMode().equals("External") ) {
				
				sb.append("\n" + indent + "external resource " + r.getTarget() 
						   + " of type " + r.getType() );
				continue;				
			}
			
			Part part = rp.getPart(r);
						
			
			printInfo(part, sb, indent);
			if (handled.get(part)!=null) {
				sb.append(" [additional reference] ");
				continue;
			}
			handled.put(part, part);
			if (part.getRelationshipsPart(false)==null) {
				// sb.append(".. no rels" );						
			} else {
				traverseRelationships(wordMLPackage, part.getRelationshipsPart(false), sb, indent + "    ");
			}
					
		}
		
		
	}
	
	
}
