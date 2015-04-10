package noga.commons.njexl.extension.oop;

import noga.commons.njexl.Interpreter;
import noga.commons.njexl.extension.TypeUtility;
import noga.commons.njexl.extension.oop.ScriptClassBehaviour.*;

import java.util.HashMap;

/**
 * Created by noga on 09/04/15.
 */
public class ScriptClassInstance implements Executable, Comparable,Arithmetic, Logic  {

    final HashMap<String, Object> fields;

    public HashMap<String,Object> getFields(){return fields ;}

    final HashMap<String,ScriptClassInstance> supers;

    public HashMap<String,ScriptClassInstance> getSupers(){ return supers ; }

    Interpreter interpreter;

    final ScriptClass scriptClass;

    public ScriptClass getNClass(){ return scriptClass ;}

    public ScriptClassInstance(ScriptClass scriptClass, Interpreter interpreter){
        this.interpreter = interpreter ;
        this.scriptClass = scriptClass ;
        this.fields = new HashMap<>();
        this.supers = new HashMap<>();

    }

    @Override
    public void setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter ;
    }

    @Override
    public Object execMethod(String method, Object[] args)  {
        try {
            ScriptMethod methodDef = scriptClass.getMethod(method);
            if (methodDef == null) {
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
            if (supers.get(sup).fields.containsKey(name)) {
                return supers.get(sup).fields.get(name);
            }
        }
        throw new Exception("Key : '" + name + "' is not found!");
    }

    public void set(String name, Object value) {
        if (fields.containsKey(name)) {
            fields.put(name, value);
            return;
        }
        for (String sup : supers.keySet()) {
            if (supers.get(sup).fields.containsKey(name)) {
                supers.get(sup).fields.put(name, value);
                return;
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
