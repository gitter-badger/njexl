package org.apache.commons.jexl2.extension.oop;

import org.apache.commons.jexl2.Interpreter;
import org.apache.commons.jexl2.parser.ASTClassDef;
import org.apache.commons.jexl2.parser.ASTMethodDef;
import org.apache.commons.jexl2.parser.JexlNode;
import java.util.HashMap;
import org.apache.commons.jexl2.extension.oop.ScriptClassBehaviour.TypeAware;


/**
 * Created by noga on 08/04/15.
 */
public class ScriptClass  implements TypeAware {

    public static final String _INIT_ = "__new__";

    public static final String SELF = "me";

    final HashMap<String, ScriptMethod> methods;

    public HashMap getMethods() {
        return methods;
    }

    ScriptMethod constructor;

    final HashMap<String, ScriptClass> supers;

    public HashMap getSupers() {
        return supers;
    }

    protected void findMethods(JexlNode node) {
        if (node instanceof ASTMethodDef) {
            ScriptMethod methodDef = new ScriptMethod((ASTMethodDef) node);
            methods.put(methodDef.name, methodDef);
        } else {
            int numChild = node.jjtGetNumChildren();
            for (int i = 0; i < numChild; i++) {
                findMethods(node.jjtGetChild(i));
            }
        }
    }

    public ScriptMethod getMethod(String method) throws Exception {
        if (methods.containsKey(method)) {
            return methods.get(method);
        } else {
            ScriptMethod m1;
            ScriptMethod m2 = null;
            for (String sup : supers.keySet()) {
                m1 = supers.get(sup).getMethod(method);
                if (m1 != null) {
                    if (m2 == null) {
                        m2 = m1;
                    } else {
                        // m2 exists, we have diamond issue
                        throw new Exception("Ambiguous Method : '" + method + "' !");
                    }
                }
            }
            return m2;
        }
    }

    final String name;

    public String getName() {
        return name;
    }

    public ScriptClassInstance  instance(Interpreter interpreter, Object[] args) throws Exception {

        ScriptClassInstance instance = new ScriptClassInstance(this, interpreter);
        for (String n : supers.keySet()) {
            ScriptClass superClass = supers.get(n);
            if (superClass == null) {
                superClass = interpreter.resolveJexlClassName(n);
                if ( superClass == null ) {
                    throw new Exception("Superclass : '" + n + "' not found!");
                }
                // one time resolving of this
                supers.put(n,superClass);
            }
            ScriptClassInstance superClassInstance = superClass.instance(interpreter, args);
            instance.supers.put( superClass.name, superClassInstance);
        }
        if (constructor != null) {
            instance.execMethod(_INIT_, args);
        }
        // return
        return instance ;
    }

    public ScriptClass(ASTClassDef ref) {
        name = ref.jjtGetChild(0).image;
        methods = new HashMap<>();
        supers = new HashMap<>();
        int numChild = ref.jjtGetNumChildren();
        for (int i = 1; i < numChild - 1; i++) {
            String superName = ref.jjtGetChild(i).image;
            supers.put(superName, null);
        }

        findMethods(ref.jjtGetChild(numChild - 1));
        constructor = methods.get(_INIT_);
    }

    @Override
    public boolean isa(Object o) {
        if ( o == null ){
            return false ;
        }
        ScriptClass that;
        if ( o instanceof ScriptClass){
            that = (ScriptClass)o;
        }
        else if ( o instanceof ScriptClassInstance){
            that = ((ScriptClassInstance)o).scriptClass;
        }
        else{
            return false ;
        }

        if ( this.name.equals(that.name) ){
            return true ;
        }
        for ( String n :  supers.keySet()){
            boolean s = supers.get(n).isa(o);
            if ( s ){
                return true ;
            }
        }

        return false;
    }

    @Override
    public String toString(){
        return "nClass " + name ;
    }
}
