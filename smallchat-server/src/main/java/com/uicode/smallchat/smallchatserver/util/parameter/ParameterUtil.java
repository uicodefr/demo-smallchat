package com.uicode.smallchat.smallchatserver.util.parameter;

import java.util.Optional;

public class ParameterUtil {

    private ParameterUtil() {
    }

    public static Long getLong(Optional<String> parameterValue, Long defaultValue) {
        if (parameterValue.isPresent()) {
            return Long.valueOf(parameterValue.get());
        } else {
            return defaultValue;
        }
    }

}
