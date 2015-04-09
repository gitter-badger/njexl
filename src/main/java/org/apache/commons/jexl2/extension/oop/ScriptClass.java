package org.apache.commons.jexl2.extension.oop;

import org.apache.commons.jexl2.Interpreter;
import org.apache.commons.jexl2.extension.TypeUtility;
import org.apache.commons.jexl2.parser.ASTClassDef;
import org.apache.commons.jexl2.parser.ASTMethodDef;
import org.apache.commons.jexl2.parser.JexlNode;
import org.apache.commons.jexl2.extension.oop.ScriptClassBehaviour.* ;

import java.util.HashMap;

/**
 * Created by noga on 08/04/15.
 */
public class ScriptClass implements Executable,ObjectComparable {

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

    public ScriptMethod getMethod(String method) throws Exception{
        if ( methods.containsKey(method)){
            return methods.get(method);
        }
        else {
            ScriptMethod m1;
            ScriptMethod m2 = null;
            for (String sup : supers.keySet()) {
                m1 = supers.get(sup).getMethod(method);
                if ( m1 != null ){
                    if ( m2 == null ) {
                        m2 = m1;
                    }else{
                        // m2 exists, we have diamond issue
                        throw new Exception("Ambiguous Method : '" + method + "' !");
                    }
                }
            }
            return m2;
        }
    }

    Interpreter interpreter;

    @Override
    public void setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter ;
    }

    @Override
    public Object execMethod(String method,Object[] args) throws Exception{
        ScriptMethod methodDef = getMethod(method) ;
        if ( methodDef == null){
            throw new Exception("Method : '" + method + "' is not found in class : " + this.name );
        }
        if ( methodDef.instance ) {
            return methodDef.invoke(this, interpreter, args);
        }
        return methodDef.invoke(null, interpreter, args);
    }

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

    public void init( Object[] args) throws Exception {
        for ( String n : supers.keySet() ){
            ScriptClass scriptClass = supers.get(n);
            if ( scriptClass != null ){
                continue;
            }
            scriptClass = interpreter.resolveJexlClassName(n);
            if ( scriptClass == null ){
                throw new Exception("Superclass : '" + n + "' not found!");
            }
            scriptClass.interpreter = this.interpreter ;
            scriptClass.init(args);
            supers.put(n,scriptClass);
         }
        if ( constructor != null ){
            execMethod( _INIT_, args) ;
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

    @Override
    public String toString(){
        if (  methods.containsKey(ScriptClassBehaviour.STR ) ) {
            try {
                return execMethod(ScriptClassBehaviour.STR, new Object[]{} ).toString();
            } catch (Exception e) {

            }
        }
        return super.toString();
    }

    @Override
    public boolean equals(Object o){
        if (  methods.containsKey(ScriptClassBehaviour.EQ ) ) {
            try {
                return TypeUtility.castBoolean( execMethod(ScriptClassBehaviour.EQ, new Object[]{ o } ), false );
            } catch (Exception e) {

            }
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        try {
            if (methods.containsKey(ScriptClassBehaviour.HC)) {
                return TypeUtility.castInteger(execMethod(ScriptClassBehaviour.EQ, new Object[]{}));
            }
        }catch (Exception e){}
        return super.hashCode();
    }

    @Override
    public int compare(Object o) throws Exception {
        if (  methods.containsKey(ObjectComparable.COMPARE ) ) {
            return TypeUtility.castInteger( execMethod(ScriptClassBehaviour.EQ, new Object[]{ o } ));
        }
        throw new Exception( "Compare Function is not defined!" );
    }
}
