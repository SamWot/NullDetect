package org.sam.home.analyzer;

import org.objectweb.asm.tree.analysis.Value;

public enum NullValue implements Value {
    NULL,
    NOTNULL,
    MAYBENULL;

    @Override
    public int getSize() {
        return 1;
    }
}
