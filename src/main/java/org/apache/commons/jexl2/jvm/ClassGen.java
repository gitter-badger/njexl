package org.apache.commons.jexl2.jvm;

import javassist.*;
import org.apache.commons.jexl2.ExpressionImpl;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.parser.ASTBlock;
import org.apache.commons.jexl2.parser.ASTImportStatement;
import org.apache.commons.jexl2.parser.ASTMethodDef;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by noga on 17/03/15.
 */
public class ClassGen {

    public static final Pattern METHOD_WITH_NAMESPACE =
            Pattern.compile("(?<func>[a-z_A-Z][a-z_A-Z0-9]*\\:[a-z_A-Z][a-z_A-Z0-9]*[ ]*\\()");

    protected ExpressionImpl expression;

    ClassPool classPool;

    CtClass ctClass;

    HashMap<String,Object> imports;

    protected String replaceNamespace(String body){
        Matcher matcher = METHOD_WITH_NAMESPACE.matcher(body);
        while ( matcher.find() ){
            String func = matcher.group("func");
            String[] arr = func.split(":");
            String nameSpace = arr[0];
            String name = arr[1];

            if ( imports.containsKey(nameSpace)){
                Object actClass = imports.get(nameSpace);
                if ( actClass instanceof Class ) {
                    func = ((Class)actClass).getName() + "." + name;
                }else{
                    func = actClass.toString() + "." + name;
                }
                body = matcher.replaceFirst(func);
                matcher = METHOD_WITH_NAMESPACE.matcher(body);
            }
        }
        return body;
    }

    public ClassGen(ExpressionImpl expression){
        this.expression = expression;
    }

    HashSet<String> variables;

    public void createVariable(String name) throws Exception{
        if ( variables.contains(name)){
            return;
        }
        String fieldBody = String.format( "public static Object %s;", name);
        CtField f = CtField.make(fieldBody, ctClass);
        ctClass.addField(f);
        variables.add(name);
    }

    protected void createVariables(HashMap<String,Object> context) throws Exception {
        if ( context != null ){
            for ( String name : context.keySet() ){
                createVariable(name);
            }
        }
    }


    protected void createMethods() throws Exception{
        HashMap<String,ASTMethodDef> methods = expression.methods();
        for ( String name : methods.keySet() ){
            ASTMethodDef methodDef = methods.get(name);
            CodeGen codeGen = new CodeGen(this,false);
            String methodBody = codeGen.data(methodDef);
            methodBody = replaceNamespace(methodBody);
            CtMethod method = CtNewMethod.make(methodBody, ctClass);
            ctClass.addMethod(method);
        }
    }

    protected void addScriptBody() throws Exception{
        CodeGen codeGen = new CodeGen(this,true);
        String methodBody = "{" + codeGen.data(expression.script()) + "}";
        methodBody = replaceNamespace(methodBody);
        CtMethod bodyMethod = ctClass.getDeclaredMethod(DynaCallable.SCRIPT_ENTRY);
        bodyMethod.insertBefore(methodBody);
    }

    protected boolean isJexlFile(String path){
        if ( path.contains("/") || path.startsWith("_") || path.endsWith(".jexl")){
            return true;
        }
        return false;
    }

    protected Class importFile(String path, String as) throws Exception{
        JexlEngine engine = new JexlEngine();
        ExpressionImpl impl = (ExpressionImpl)engine.importScriptForJVM(path, as);
        Class c = impl.myClass(null);
        return c;
    }

    protected void createImports() throws Exception {
        imports = new HashMap<>();
        for ( String ns : expression.imports().keySet() ){
            Object c = null;
            ASTImportStatement importStatement = expression.imports().get(ns);
            String objectToImport = importStatement.jjtGetChild(0).image ;
            if ( isJexlFile(objectToImport)){
                c = importFile(objectToImport,ns);
            }else{
                //class import - just alias
                c = objectToImport ;
            }
            imports.put(ns,c);
        }
    }

    public String createAnonymousMethod(ASTBlock block){
        CodeGen codeGen = new CodeGen(this,false);
        String body = codeGen.data(block);

        String methodName = expression.name() + "__" + Long.toString( System.nanoTime() );
        String methodBody = "public static Object " + methodName + "(Object _item_)" ;
        body = body.replaceAll("\\$_", "_item_");
        methodBody+= body ;
        try {
            CtMethod method = CtNewMethod.make(methodBody, ctClass);
            ctClass.addMethod(method);
        }catch (Exception e){
            System.err.println(e);
        }
        return methodName;
    }

    public Class createClass(HashMap<String,Object> context) throws Exception{
        classPool = ClassPool.getDefault();
        ctClass = classPool.get(DynaCallable.TemplateClass.class.getName());
        ctClass.setSuperclass(classPool.get(Object.class.getName()));
        ctClass.setName(expression.name());
        variables = new HashSet<>();
        // create imports
        createImports();
        // create variables
        createVariables(context);
        // create methods
        createMethods();
        // add the main script
        addScriptBody();
        // ready the class
        ctClass.freeze();
        // finally : return the class
        return ctClass.toClass();
    }

}
