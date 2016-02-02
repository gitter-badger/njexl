/*
* Copyright 2016 Nabarun Mondal
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package com.noga.njexl.lang.parser;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ASTNumberLiteral extends JexlNode implements JexlNode.Literal<Number> {
    /** The type literal value. */
    Number literal = null;
    /** The expected class. */
    Class<?> clazz = null;

    public ASTNumberLiteral(int id) {
        super(id);
    }

    public ASTNumberLiteral(Parser p, int id) {
        super(p, id);
    }

    /**
     * Gets the literal value.
     * @return the number literal
     */
    public Number getLiteral() {
        return literal;
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean isConstant(boolean literal) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public Class<?> getLiteralClass() {
        return clazz;
    }

    public boolean isInteger() {
        return Integer.class.equals(clazz);
    }

    /**
     * Sets this node as a natural literal.
     * Originally from OGNL.
     * @param s the natural as string
     */
    public void setNatural(String s) {
        Number result;
        Class<?> rclass;
        // determine the base
        final int base;
        if (s.charAt(0) == '0') {
            if ((s.length() > 1 && (s.charAt(1) == 'x' || s.charAt(1) == 'X'))) {
                base = 16;
                s = s.substring(2); // Trim the 0x off the front
            } else {
                base = 8;
            }
        } else {
            base = 10;
        }
        final int last = s.length() - 1;
        switch (s.charAt(last)) {
            case 'l':
            case 'L': {
                rclass = Long.class;
                result = Long.valueOf(s.substring(0, last), base);
                break;
            }
            case 'h':
            case 'H': {
                rclass = BigInteger.class;
                result = new BigInteger(s.substring(0, last), base);
                break;
            }
            default: {
                rclass = Integer.class;
                try {
                    result = Integer.valueOf(s, base);
                } catch (NumberFormatException take2) {
                    try {
                        result = Long.valueOf(s, base);
                    } catch (NumberFormatException take3) {
                        result = new BigInteger(s, base);
                    }
                }
            }
        }
        literal = result;
        clazz = rclass;
    }

    /**
     * Sets this node as a real literal.
     * Originally from OGNL.
     * @param s the real as string
     */
    public void setReal(String s) {
        Number result;
        Class<?> rclass;
        int last = s.length() - 1;
        switch (s.charAt(last)) {
            case 'b':
            case 'B': {
                result = new BigDecimal(s.substring(0, last));
                rclass = BigDecimal.class;
                break;
            }
            case 'd':
            case 'D': {
                rclass = Double.class;
                result = Double.valueOf(s);
                break;
            }
            case 'f':
            case 'F':
                result = Float.valueOf(s);
                rclass = Float.class ;
                break;
            default: {
                Float f = Float.valueOf(s);
                Double d = Double.valueOf(s);
                BigDecimal bd = new BigDecimal(s);
                result = bd;
                rclass = BigDecimal.class ;
                s = bd.toPlainString();
                last = s.length() - 1;
                int dotInx = s.indexOf('.');
                int sig = 0 ;
                if ( dotInx >= 0 ){
                    //it has dot. find no after dots?
                    sig = last - dotInx ;
                }
                String formatString = String.format("%%.%df", sig);
                String sf = String.format(formatString,f);
                String sd = String.format(formatString,d);
                if ( s.equals(sf)){
                    result = f;
                    rclass = Float.class ;
                    break;
                }
                if ( s.equals(sd)){
                    result = d;
                    rclass = Double.class ;
                    break;
                }
            }
        }
        literal = result;
        clazz = rclass;
    }
}
