package org.apache.commons.jexl2.jvm;

import javassist.*;
import org.apache.commons.jexl2.ExpressionImpl;
import org.apache.commons.jexl2.parser.ASTMethodDef;

import java.util.HashMap;


/**
 * Created by noga on 17/03/15.
 */
public class ClassGen {

    protected ExpressionImpl expression;

    ClassPool classPool;

    CtClass ctClass;

    public ClassGen(ExpressionImpl expression){
        this.expression = expression;
    }
    protected void createVariables(HashMap<String,Object> context) throws Exception {
        String[] vars = expression.getLocalVariables();
        if ( vars != null ){
            for ( String name : vars ){
                String fieldBody = String.format( "public static Object %s = null;", name);
                CtField f = CtField.make(fieldBody, ctClass);
                ctClass.addField(f);
            }
        }
        if ( context != null ){
            for ( String name : context.keySet() ){
                String fieldBody = String.format( "public static Object %s;", name);
                CtField f = CtField.make(fieldBody, ctClass);
                ctClass.addField(f);
            }
        }
    }

    protected void createMethods() throws Exception{
        HashMap<String,ASTMethodDef> methods = expression.methods();
        for ( String name : methods.keySet() ){
            ASTMethodDef methodDef = methods.get(name);
            CodeGen codeGen = new CodeGen(this,false);
            String methodBody = codeGen.data(methodDef);
            CtMethod method = CtNewMethod.make(methodBody, ctClass);
            ctClass.addMethod(method);
        }
    }

    protected void addScriptBody() throws Exception{
        CodeGen codeGen = new CodeGen(this,true);
        String methodBody = "{" + codeGen.data(expression.script()) + "}";
        CtMethod bodyMethod = ctClass.getDeclaredMethod(DynaCallable.SCRIPT_ENTRY);
        bodyMethod.insertBefore(methodBody);
    }

    public Class createClass(HashMap<String,Object> context) throws Exception{
        classPool = ClassPool.getDefault();
        ctClass = classPool.get(DynaCallable.TemplateClass.class.getName());
        ctClass.setSuperclass(classPool.get(Object.class.getName()));
        ctClass.setName(expression.name());
        createVariables(context);
        createMethods();
        addScriptBody();
        ctClass.freeze();
        return ctClass.toClass();
    }

}
