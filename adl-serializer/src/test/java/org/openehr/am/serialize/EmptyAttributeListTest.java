package org.openehr.am.serialize;

import org.openehr.am.archetype.constraintmodel.*;
import org.openehr.am.archetype.constraintmodel.CAttribute.*;
import org.openehr.rm.support.basic.*;

import java.util.*;

public class EmptyAttributeListTest extends SerializerTestBase {
	
	public void testOutputCComplexObjectWithEmptyAttributeList() throws Exception {
		Interval<Integer> occurrences = new Interval<Integer>(1, 1);
		List<CAttribute> attributes = null;
		CComplexObject ccobj = new CComplexObject("/path", "DV_TEXT", 
				occurrences, "at0001", attributes, null);
		outputter.printCComplexObject(null, ccobj, 0, out);
        verifyByFile("empty-attribute-list-test.adl");
	}
	
	public void testOutputCAttributeWithEmptyChildrenList() throws Exception {
		
		List<CObject> children = null;
		CSingleAttribute cattr = new CSingleAttribute("/path", "value", 
				Existence.REQUIRED, children);
		
		outputter.printCAttribute(null, cattr, 0, out);
		verifyByFile("empty-children-list-test.adl");
	}
}
