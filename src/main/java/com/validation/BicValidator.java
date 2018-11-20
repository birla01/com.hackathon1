
package com.validation;

import java.io.Serializable;
import java.util.regex.Pattern;
public class BicValidator implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final Pattern bicPattern = Pattern.compile("^([a-zA-Z]){4}([a-zA-Z]){2}([0-9a-zA-Z]){2}([0-9a-zA-Z]{3})?$");
    
    public boolean isValid(String bic) {
        if (bic == null || bic.isEmpty()) {
            return true;
        }
        return bicPattern.matcher(bic).matches();
    }

}
