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

/**
 * Identifiers, variables and registers.
 */
public class ASTIdentifier extends JexlNode {
    private int register = -1;
    public ASTIdentifier(int id) {
        super(id);
    }

    public ASTIdentifier(Parser p, int id) {
        super(p, id);
    }
    
    void setRegister(String r) {
        if (r.charAt(0) == '#') {
            register = Integer.parseInt(r.substring(1));
        }
    }
    
    void setRegister(int r) {
        register = r;
    }
    
    public int getRegister() {
        return register;
    }

    @Override
    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
