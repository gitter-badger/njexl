package org.apache.commons.jexl2.extension.oop;

import org.apache.commons.jexl2.Interpreter;
import org.apache.commons.jexl2.extension.ListSet;
import org.apache.commons.jexl2.parser.ASTClassDef;
import org.apache.commons.jexl2.parser.ASTMethodDef;
import org.apache.commons.jexl2.parser.JexlNode;

import java.util.HashMap;

/**
 * Created by noga on 08/04/15.
 */
public class ScriptClass implements Invokable {

    public static final String _INIT_ = "__new__" ;

    public static final String SELF = "me" ;

    final HashMap<String,ScriptMethod> methods;

    public HashMap getMethods(){ return methods ; }

    ScriptMethod constructor;

    HashMap<String,Object> fields;

    final HashMap<String,ScriptClass> supers;

    public HashMap getSupers(){ return supers ; }

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

    @Override
    public Object execMethod(String method,Interpreter interpreter ,Object[] args) throws Exception{
        ScriptMethod methodDef = methods.get(method) ;
        if ( methodDef == null){
            throw new Exception("Method : '" + method + "' is not found in class : " + this.name );
        }
        if ( methodDef.instance ) {
            return methodDef.invoke(this, interpreter, args);
        }
        return methodDef.invoke(null, interpreter, args);
    }

    @Override
    public String type(){ return ScriptClass.class.getName() ; }

    final String name;

    public String getName(){ return name; }

    public Object get(String name) throws Exception {
        if ( fields.containsKey(name )) {
            return fields.get(name);
        }
        for ( String sup : supers.keySet() ){
            if ( supers.get(sup).fields.containsKey(name ) ){
                return supers.get(sup).fields.get(name);
            }
        }
        throw new Exception("Key : '" + name + "' is not found!");
    }

    public void set(String name,Object value){
        if ( fields.containsKey(name )) {
            fields.put(name, value);
        }
        for ( String sup : supers.keySet() ){
            if ( supers.get(sup).fields.containsKey(name ) ){
                supers.get(sup).fields.put(name,value);
                return;
            }
        }
        fields.put(name,value);
    }

    public void init(Interpreter interpreter, Object[] args) throws Exception {
        for ( String n : supers.keySet() ){
            ScriptClass scriptClass = supers.get(n);
            if ( scriptClass != null ){
                continue;
            }
            scriptClass = interpreter.resolveJexlClassName(n);
            if ( scriptClass == null ){
                throw new Exception("Superclass : '" + n + "' not found!");
            }
            scriptClass.init( interpreter, args);
            supers.put(n,scriptClass);
         }
        if ( constructor != null ){
            execMethod( _INIT_, interpreter, args) ;
        }
        // nothing really
    }

    public ScriptClass(ASTClassDef ref) {
        name = ref.jjtGetChild(0).image ;
        fields = new HashMap<>();
        methods = new HashMap<>();
        supers = new HashMap<>();
        int numChild = ref.jjtGetNumChildren();
        for ( int i = 1; i < numChild - 1; i++ ){
            String superName = ref.jjtGetChild(i).image ;
            supers.put( superName , null );
        }

        findMethods(ref.jjtGetChild(numChild-1));
        constructor = methods.get(_INIT_);
    }

}
