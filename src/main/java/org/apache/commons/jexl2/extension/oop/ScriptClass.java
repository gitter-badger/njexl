package org.apache.commons.jexl2.extension.oop;

import org.apache.commons.jexl2.Interpreter;
import org.apache.commons.jexl2.parser.ASTClassDef;
import org.apache.commons.jexl2.parser.ASTMethodDef;
import org.apache.commons.jexl2.parser.JexlNode;

import java.util.HashMap;

/**
 * Created by noga on 08/04/15.
 */
public class ScriptClass {

    public static final String _INIT_ = "__new__" ;

    public static final String _SUPER_ = "__super__" ;

    public static final String SELF = "me" ;

    public final HashMap<String,ScriptMethod> methods;

    public HashMap<String,Object> fields;

    protected void findMethods(JexlNode node){
        if ( node instanceof ASTMethodDef){
            ScriptMethod methodDef = new ScriptMethod((ASTMethodDef)node);
            methods.put( methodDef.name, methodDef );
        }else {
            int numChild = node.jjtGetNumChildren();
            for ( int i =0; i < numChild; i++ ){
                findMethods( node.jjtGetChild(i) );
            }
        }
    }

    public Object execMethod(String method,Interpreter interpreter ,Object[] args) throws Exception{
        ScriptMethod methodDef = methods.get(method) ;
        if ( methodDef == null){
            throw new Exception("Method : '" + method + "' is not found in class : " + this.name );
        }
        return methodDef.invoke(this,interpreter, args);
    }

    public final String name;

    public Object get(String name){
        return fields.get(name);
    }

    public void set(String name,Object value){
        fields.put(name,value);
    }

    public ScriptClass(ASTClassDef ref) {
        name = ref.jjtGetChild(0).image ;
        fields = new HashMap<>();
        methods = new HashMap<>();
        findMethods(ref);
    }
}
