/*
* Copyright 2015 Nabarun Mondal
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

/**
 * @deprecated Only for use in maintaining binary compatibility - should not actually be used - will be removed in 3.0
 */
@Deprecated
public final class ASTFloatLiteral extends JexlNode implements JexlNode.Literal<Float> {
    /** The type literal value. */
    Float literal = null;

    public ASTFloatLiteral(int id) {
        super(id);
    }

    public ASTFloatLiteral(Parser p, int id) {
        super(p, id);
    }
    
    /**
     * Gets the literal value.
     * @return the float literal
     */
    public Float getLiteral() {
        return literal;
    }
    
    /** {@inheritDoc} */
    @Override
    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
