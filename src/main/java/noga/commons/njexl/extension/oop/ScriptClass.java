package noga.commons.njexl.extension.oop;

import noga.commons.njexl.Debugger;
import noga.commons.njexl.extension.TypeUtility;
import noga.commons.njexl.parser.ASTClassDef;
import noga.commons.njexl.parser.JexlNode;
import noga.commons.njexl.Interpreter;
import noga.commons.njexl.parser.ASTMethodDef;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import noga.commons.njexl.extension.oop.ScriptClassBehaviour.TypeAware;


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

    public final String ns;

    public final String name;

    public final String hash;

    public String getName() {
        return name;
    }

    public ScriptClassInstance  instance(Interpreter interpreter, Object[] args) throws Exception {

        ScriptClassInstance instance = new ScriptClassInstance(this, interpreter);
        Set<String> superKeys = new HashSet<>(supers.keySet());
        for (String n : superKeys ) {
            ScriptClass superClass = supers.get(n);
            if (superClass == null) {
                superClass = interpreter.resolveJexlClassName(n);
                if ( superClass == null ) {
                    throw new Exception("Superclass : '" + n + "' not found!");
                }
                instance.hasJSuper = (superClass.clazz != null );
                // one time resolving of this
                addSuper(superClass.ns, superClass.name, superClass);
            }
            ScriptClassInstance superClassInstance = superClass.instance(interpreter, args);
            instance.addSuper(superClassInstance);
        }
        if (constructor != null) {
            instance.execMethod(_INIT_, args);
        }
        // return
        return instance ;
    }

    protected void addSuper(String ns, String superName, ScriptClass value){
        if ( this.ns.equals(ns)) {
            supers.put(superName, value);
        }
        supers.put( ns +":" + superName, value);
    }

    public ScriptClass(ASTClassDef ref,String ns) {
        this.ns = ns ;
        name = ref.jjtGetChild(0).image;
        methods = new HashMap<>();
        supers = new HashMap<>();
        int numChild = ref.jjtGetNumChildren();
        for (int i = 1; i < numChild - 1; i++) {
            int n = ref.jjtGetChild(i).jjtGetNumChildren();
            String supNs = this.ns;
            String superName = ref.jjtGetChild(i).jjtGetChild(0).image;
            if (n > 1) {
                supNs = superName;
                superName = ref.jjtGetChild(i).jjtGetChild(1).image;
            }
            addSuper(supNs, superName, null);
        }
        findMethods(ref.jjtGetChild(numChild - 1));
        constructor = methods.get(_INIT_);
        hash = TypeUtility.hash(Debugger.getText(ref));
        clazz = null ;
    }

    public final Class clazz;

    public ScriptClass(Object o,String ns){
        Class c ;
        if (o instanceof  Class){
            c = (Class)o ;
        }else{
            c = o.getClass();
        }
        clazz = c ;
        this.ns = ns ;
        name = c.getSimpleName();
        hash = ns;
        // we will do something about them later
        methods = new HashMap<>();
        supers = new HashMap<>();
    }

    @Override
    public boolean isa(Object o) {
        if ( o == null ){
            return false ;
        }
        ScriptClass that = null;
        if ( o instanceof ScriptClass){
            that = (ScriptClass)o;
        }
        else if ( o instanceof ScriptClassInstance){
            that = ((ScriptClassInstance)o).scriptClass;
        }
        if ( that != null){
            // match body hash
            if ( this.hash.equals(that.hash) ){
                return true ;
            }
        }
        else{
            // do we have Java Supers?
            Class clazz;
            if  ( o instanceof Class){
                clazz = (Class)o;
            }else{
                clazz = o.getClass();
            }
            if ( this.clazz != null && clazz.isAssignableFrom(this.clazz)){
                return true ;
            }
        }
        // nothing, continue upward : may end in loop
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
        return String.format("nClass %s:%s", ns, name) ;
    }
}
