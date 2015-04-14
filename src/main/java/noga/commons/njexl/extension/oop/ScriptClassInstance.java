package noga.commons.njexl.extension.oop;

import noga.commons.njexl.Interpreter;
import noga.commons.njexl.extension.TypeUtility;
import noga.commons.njexl.extension.oop.ScriptClassBehaviour.*;
import noga.commons.njexl.introspection.JexlMethod;
import noga.commons.njexl.introspection.JexlPropertyGet;
import noga.commons.njexl.introspection.JexlPropertySet;
import noga.commons.njexl.introspection.UberspectImpl;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by noga on 09/04/15.
 */
public class ScriptClassInstance implements Executable, Comparable,Arithmetic, Logic  {

    final HashMap<String, Object> fields;

    public HashMap<String,Object> getFields(){return fields ;}

    final HashMap<String,Object> supers;

    public static final String _ANCESTOR_ = "__anc__" ;

    protected void addSuper(ScriptClassInstance value){
        if ( scriptClass.ns.equals(value.scriptClass.ns)) {
            //make direct calling possible ONLY if the namespace of child and parent match
            supers.put(value.scriptClass.name, value);
        }
        supers.put( value.scriptClass.ns +":" + value.scriptClass.name, value);
    }

    public HashMap<String,Object> getSupers(){ return supers ; }

    boolean hasJSuper;

    public boolean hasJSuper(){
        return hasJSuper;
    }

    Interpreter interpreter;

    final ScriptClass scriptClass;

    public ScriptClass getNClass(){ return scriptClass ;}

    public ScriptClassInstance(ScriptClass scriptClass, Interpreter interpreter){
        this.interpreter = interpreter ;
        this.scriptClass = scriptClass ;
        this.fields = new HashMap<>();
        this.supers = new HashMap<>();
        this.hasJSuper = false ;
    }

    public JexlMethod getJSuperMethod(String name, Object[] sups, Object[] args) throws Exception {

        for ( String s : supers.keySet() ){
            if ( s.contains(":") ){
                continue;
            }
            Object sup = supers.get(s);
            if ( !(sup instanceof ScriptClassInstance) ){
                JexlMethod method = interpreter.uberspect.getMethod(sup, name, args, null);
                if ( method != null ){
                    sups[0] = sup;
                    return method ;
                }
            }
        }
        return null;
    }


    public void addJSuper(String name, Object obj, Object[] args) throws Exception {
        JexlMethod ctor = interpreter.uberspect.getConstructorMethod(obj, args, null);
        // DG: If we can't find an exact match, narrow the parameters and try again
        if (ctor == null) {
            if (interpreter.arithmetic.narrowArguments(args)) {
                ctor = interpreter.uberspect.getConstructorMethod(obj, args, null);
                if (ctor == null) {
                    throw new Exception("Constructor not found!");
                }
            }
        }
        Object instance = ctor.invoke(null, args);
        supers.put(name,instance);
        supers.put(scriptClass.ns + ":" + name,instance);
    }

    /**
     * replace ancestors
     * @param sName
     * @param args
     */
    public void ancestor(Object sName,Object[]args) throws Exception {
        Object old = supers.get(sName);
        if ( old != null && ! interpreter.getContext().has(sName.toString()) ) {
            ScriptClassInstance _new_ = ((ScriptClassInstance)old).scriptClass.instance(interpreter, args);
            addSuper( _new_ ); // this should replace the old
            return;
        }
        if ( !hasJSuper ){
            return;
        }
        String name;
        if ( sName instanceof  String ){
            sName = interpreter.getContext().get((String)sName);
        }
        if ( sName instanceof Class ){
            old = ((Class) sName).getName();
            name = ((Class) sName).getSimpleName();
        }else{
            old = sName.getClass().getName();
            name = sName.getClass().getSimpleName();
        }
        // java guy... call constructor
        addJSuper(name,old,args);
    }

    @Override
    public Object execMethod(String method, Object[] args)  {
        try {
            if ( method.equals(_ANCESTOR_)){
                Object arg0 = args[0];
                args = TypeUtility.shiftArrayLeft(args,1);
                ancestor(arg0,args);
                return null;
            }
            ScriptMethod methodDef = scriptClass.getMethod(method);
            if (methodDef == null ) {
                if ( hasJSuper ) {
                    Object[] sups = new Object[1];
                    JexlMethod jexlMethod = getJSuperMethod(method, sups, args);
                    if (jexlMethod != null) {
                        // call jexl method
                        return jexlMethod.invoke(sups[0], args);
                    }
                }
                throw new Exception("Method : '" + method + "' is not found in class : " + this.scriptClass.name);
            }
            if (methodDef.instance) {
                return methodDef.invoke(this, interpreter, args);
            }
            return methodDef.invoke(null, interpreter, args);
        }catch (Exception e){
            throw new Error(e);
        }
    }

    public Object get(String name) throws Exception {

        if (fields.containsKey(name)) {
            return fields.get(name);
        }
        for (String sup : supers.keySet()) {
            Object s = supers.get(sup);
            if (s instanceof  ScriptClassInstance ) {
                ScriptClassInstance supI = (ScriptClassInstance)s;
                if ( supI.fields.containsKey(name)){
                    return supI.fields.get(name);
                }
            }else{
                // try from this
                Field field = ((UberspectImpl)interpreter.uberspect).getField(s, name, null);
                if (field != null) {
                    JexlPropertyGet fg = new UberspectImpl.FieldPropertyGet(field);
                    return fg.invoke(s);
                }
            }
        }
        throw new Exception("Key : '" + name + "' is not found!");
    }

    public void set(String name, Object value) throws Exception {
        if (fields.containsKey(name)) {
            fields.put(name, value);
            return;
        }
        for (String sup : supers.keySet()) {
            Object s = supers.get(sup);
            if (s instanceof  ScriptClassInstance ) {
                ScriptClassInstance supI = (ScriptClassInstance)s;
                if ( supI.fields.containsKey(name)){
                    supI.fields.put(name,value);
                    return;
                }
            }else{
                Field field = ((UberspectImpl)interpreter.uberspect).getField(s, name, null);
                if (field != null) {
                    JexlPropertySet fs = new UberspectImpl.FieldPropertySet(field);
                    fs.invoke(s,value);
                    return;
                }
            }
        }
        fields.put(name, value);
    }

    @Override
    public String toString() {
        try {
            return execMethod(ScriptClassBehaviour.STR, new Object[]{}).toString();
        } catch (Throwable e) {
        }
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        try {
            return TypeUtility.castBoolean(execMethod(ScriptClassBehaviour.EQ, new Object[]{o}), false);
        } catch (Throwable e) {

        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        try {
            return TypeUtility.castInteger(execMethod(ScriptClassBehaviour.HC, new Object[]{}));
        } catch (Throwable e) {
        }
        return super.hashCode();
    }

    @Override
    public int compareTo(Object o) {
        return TypeUtility.castInteger(execMethod(ScriptClassBehaviour.CMP, new Object[]{o}));
    }

    @Override
    public Object add(Object o) {
        return execMethod(Arithmetic.ADD, new Object[]{o});
    }

    @Override
    public Object neg()  {
        return execMethod(Arithmetic.NEG, new Object[]{});
    }

    @Override
    public Object sub(Object o)  {
        return execMethod(Arithmetic.SUB, new Object[]{o});
    }

    @Override
    public Object mul(Object o)  {
        return execMethod(Arithmetic.MUL, new Object[]{o});
    }

    @Override
    public Object div(Object o)  {
        return execMethod(Arithmetic.DIV, new Object[]{o});
    }

    @Override
    public Object exp(Object o)  {
        return execMethod(Arithmetic.EXP, new Object[]{o});
    }

    @Override
    public Object and(Object o)  {
        return execMethod(Logic.AND, new Object[]{o});
    }

    @Override
    public Object complement()  {
        return execMethod(Logic.COMPLEMENT, new Object[]{});
    }

    @Override
    public Object or(Object o) {
        return execMethod(Logic.OR, new Object[]{o});
    }

    @Override
    public Object xor(Object o)  {
        return execMethod(Logic.XOR, new Object[]{o});
    }
}
