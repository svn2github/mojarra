/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package beanvalidation.constraints;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Valid a credit card number using the standard LUHN check. Implementation borrowed from Mojarra's
 * CreditCardValidator.
 */
public class CreditCardConstraintValidator
      implements ConstraintValidator<CreditCard, String> {

    private Pattern basicSyntaxPattern;

    public void initialize(CreditCard parameters) {
        basicSyntaxPattern = Pattern.compile("^[0-9\\ \\-]*$");
    }

    public boolean isValid(String value, ConstraintValidatorContext ctxt) {
        if (value == null || value.length() == 0) {
            return true;
        }

        if (!basicSyntaxPattern.matcher(value).matches()) {
            return false;
        }

        return luhnCheck(stripNonDigits(value));
    }

    private String stripNonDigits(String s) {
        return s.replaceAll(" ", "").replaceAll("-", "");
    }


    private boolean luhnCheck(String number) {
        int sum = 0;

        boolean timestwo = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (timestwo) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            timestwo = !timestwo;
        }
        return sum % 10 == 0;
    }

}
