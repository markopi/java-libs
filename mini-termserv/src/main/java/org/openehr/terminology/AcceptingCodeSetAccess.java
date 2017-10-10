package org.openehr.terminology;

import org.openehr.rm.datatypes.text.*;
import org.openehr.rm.support.terminology.*;

import java.util.*;

/**
 * @author markopi
 */
public enum AcceptingCodeSetAccess implements CodeSetAccess {
    INSTANCE;

    @Override
    public String id() {
        return getClass().getSimpleName();
    }

    @Override
    public Set<CodePhrase> allCodes() {
        return Collections.emptySet();
    }

    @Override
    public boolean hasCode(CodePhrase code) {
        return true;
    }

    @Override
    public boolean hasLang(CodePhrase lang) {
        return true;
    }
}
