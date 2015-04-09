package org.apache.commons.jexl2.extension.oop;

import org.apache.commons.jexl2.*;
import org.apache.commons.jexl2.extension.ListSet;
import org.apache.commons.jexl2.extension.SetOperations;
import org.apache.commons.jexl2.parser.ASTBlock;
import org.apache.commons.jexl2.parser.ASTMethodDef;
import org.apache.commons.jexl2.parser.JexlNode;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by noga on 08/04/15.
 */
public class ScriptMethod {

    public final String name;

    public final ASTBlock astBlock;

    public final boolean instance;

    public final HashMap<String, Object> defaultValues;

    public final ListSet<String> params;

    public ScriptMethod(ASTMethodDef def) {
        name = def.jjtGetChild(0).image;
        defaultValues = new HashMap<>();
        params = new ListSet<>();
        int numChild = def.jjtGetNumChildren();
        for (int i = 1; i < numChild - 1; i++) {
            // process params
            String argName = def.jjtGetChild(i).jjtGetChild(0).image;
            params.add(argName);
            Object value = null;
            if (def.jjtGetChild(i).jjtGetNumChildren() == 2) {
                value = def.jjtGetChild(i).jjtGetChild(1);
            }
            defaultValues.put(argName, value);
        }
        astBlock = (ASTBlock) def.jjtGetChild(numChild - 1);
        instance = ( !params.isEmpty() && ScriptClass.SELF.equals(params.get(0)));
        if ( instance ) {
            // This was always dummy...
            params.remove(0);
        }
    }

    private Object getDefault(String paramName,Interpreter interpreter) {
        if (defaultValues.containsKey(paramName)) {
            Object defValue =  defaultValues.get(paramName);
            if ( defValue instanceof JexlNode ) {
                defValue = ((JexlNode)defValue).jjtAccept(interpreter, null);
            }
            return defValue;
        }
        return null;
    }

    private HashMap<String, Object> getParamValues(Interpreter interpreter, Object[] args) throws Exception {
        HashMap<String, Object> map = new HashMap();
        boolean namedArgs = false ;
        for (int i = 0; i < args.length; i++) {
            if ( args[i] instanceof Interpreter.AnonymousParam ){
                continue;
            }
            if (args[i] instanceof Interpreter.NamedArgs) {
                Interpreter.NamedArgs na = (Interpreter.NamedArgs) args[0];
                map.put(na.name, na.value);
                namedArgs = true ;
                continue;
            }
            if ( namedArgs ) {
                // should never reach here....!
                throw new Exception("All Parameters are not using '=' ");
            }
        }

        if ( namedArgs ) {
            // now put the back stuff
            Set<String> remaining = SetOperations.set_d(defaultValues.keySet(), map.keySet());
            for (String name : remaining) {
                Object defValue = getDefault(name,interpreter);
                map.put(name, defValue);
            }
        }
        else{
            for ( int i = 0 ; i < params.size();i++ ){
                String name = params.get(i);
                if ( i < args.length ){
                    map.put(name,args[i]);
                }else{
                    Object defValue = getDefault(name,interpreter);
                    map.put(name, defValue);
                }
            }
        }
        map.put(Script.ARGS,args);
        return map;
    }

    private void addToContext(JexlContext context, HashMap<String, Object> map) {
        for (String key : map.keySet()) {
            context.set(key, map.get(key));
        }
    }

    private void removeFromContext(JexlContext context, HashMap<String, Object> map) {
        for (String key : map.keySet()) {
            context.remove(key);
        }
    }

    public Object invoke(Object me, Interpreter interpreter, Object[] args) throws Exception {
        //process params, if need be
        JexlContext context = interpreter.getContext();
        HashMap map = getParamValues(interpreter, args);
        if ( instance ){
            // do double check : to be sure
            if ( me == null ){
                throw new Exception("Instance Method called with null instance!");
            }
            map.put(ScriptClass.SELF, me);
        }
        addToContext(context, map);
        try {
            Object ret = astBlock.jjtAccept(interpreter,null);
            return ret;
        }catch (Exception e){
            if ( e instanceof JexlException.Return ){
                return ((JexlException.Return) e).getValue();
            }
        }
        finally {
            //finally remove
            removeFromContext(context, map);
        }
        return null;
    }
}
