/*
 * Copyright (C) 2005 Acode HB, Sweden.
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/gpl.txt
 *
 */

package se.acode.openehr.parser;

import org.openehr.am.archetype.Archetype;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * Test cases tests archetype validation after parsing.
 *
 * @author Rong Chen
 * @version 1.0
 */
public class ArchetypeValidatorTest extends ADLParserTestBase {

    public ArchetypeValidatorTest(String test) {
        super(test);
    }

    public void setUp() throws Exception {
        ADLParser parser = new ADLParser(new File(dir,
                "adl-test-car.use_node.draft.adl"));
        archetype = parser.parse();
        validator = new ArchetypeValidator(archetype);
    }

    /**
     * Tests validation logic for internal node reference.
     *
     * @throws Exception
     */
    public void testCheckInternalReferences() throws Exception {
        Map<String, String> expected = new HashMap<String, String>();

        // wrong target path
        expected.put("/[at0000]/wheels[at0005]/parts",
                "/[at0000]/engine[at0001]/parts[at0002]/");

        // wrong type
        expected.put("/[at0000]/wheels[at0006]/parts",
                "/[at0000]/wheels[at0001]/parts[at0002]/");

        assertEquals(expected, validator.checkInternalReferences());
    }

    private Archetype archetype;
    private ArchetypeValidator validator;
}