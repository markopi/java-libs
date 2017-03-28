package org.openehr.am.serialize;

import org.openehr.am.archetype.*;
import se.acode.openehr.parser.*;

import java.io.*;

public class RoundTripTest extends SerializerTestBase {

	public void testRoundTripOnSimpleEvaluation() throws Exception {
		adlFile = "openEHR-EHR-EVALUATION.test_concept.v1.adl";
		ADLParser parser = new ADLParser(loadFromClasspath(adlFile));
		Archetype archetype = parser.parse();

		outputter.output(archetype, out);

		parser.ReInit(new StringReader(out.toString()));

		Archetype roundTripedArchetype = parser.parse();

		assertEquals("adlVersion diff", archetype.getAdlVersion(),
				roundTripedArchetype.getAdlVersion());
		assertEquals("archetypeId diff", archetype.getArchetypeId(),
				roundTripedArchetype.getArchetypeId());
		assertEquals("concept diff", archetype.getConcept(),
				roundTripedArchetype.getConcept());
		assertEquals("definition diff", archetype.getDefinition(),
				roundTripedArchetype.getDefinition());

		// TODO skipped problematic description comparison
		// assertEquals("description diff", archetype.getDescription(),
		// roundTripedArchetype.getDescription());
		
		assertEquals("ontology diff", archetype.getOntology(),
				roundTripedArchetype.getOntology());
		assertEquals("original language diff", archetype.getOriginalLanguage(),
				roundTripedArchetype.getOriginalLanguage());

	}

	public void testRoundtrip() throws Exception {
		roundtrip("openEHR-EHR-OBSERVATION.height.v1.adl");
	}

	private InputStream loadFromClasspath(String adl) throws Exception {
		return this.getClass().getClassLoader().getResourceAsStream(adl);
	}

	private Archetype parse14FromClasspath(String classpath) throws Exception {
		ADLParser parser = new ADLParser(loadFromClasspath(classpath));
		return parser.parse();
	}

	private static Archetype parse14FromString(String inputAdl) throws Exception {
		ADLParser parser14 = new ADLParser(inputAdl);
		return parser14.parse();
	}


	private Archetype roundtrip(String classpath) throws Exception {
		return roundtrip(parse14FromClasspath(classpath));
	}
	private Archetype roundtrip(Archetype r) throws Exception {
		ADLSerializer sr = new ADLSerializer();
		String ser = sr.output(r);
		return parse14FromString(ser);
	}

}
