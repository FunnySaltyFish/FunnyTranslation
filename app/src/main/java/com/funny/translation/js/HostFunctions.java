package com.funny.translation.js;

import org.mozilla.javascript.ScriptableObject;

public class HostFunctions extends ScriptableObject {
    @Override
    public String getClassName() {
        return "HostFunctions";
    }
}
