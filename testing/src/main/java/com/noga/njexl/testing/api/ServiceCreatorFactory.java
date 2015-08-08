package com.noga.njexl.testing.api;

import java.lang.reflect.Constructor;

import com.noga.njexl.lang.JexlContext;
import com.noga.njexl.lang.JexlEngine;
import com.noga.njexl.lang.Main;
import com.noga.njexl.lang.Script;
import com.noga.njexl.lang.internal.logging.Log;
import com.noga.njexl.lang.internal.logging.LogFactory;
import com.noga.njexl.testing.api.Annotations.*;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A factory to create service creators
 */
public final class ServiceCreatorFactory {

    public static final Log LOG = LogFactory.getLog( ServiceCreatorFactory.class );

    /**
     * A template service creator
     */
    public static abstract class ServiceCreator{

        /**
         * Tries to create an instance of a class
         * @param clazz the class
         * @param init a @{NApiServiceInit} metadata
         * @return one instance if possible
         * @throws Exception in case fails
         */
        protected abstract Object instantiate(Class clazz, NApiServiceInit init) throws Exception;

        protected Constructor constructor ;

        protected NApiServiceInit init ;

        public ServiceCreator(){
            constructor = null;
            init = null;
        }

        protected void findInit(Class clazz){
            Constructor[] constructors = clazz.getConstructors();
            for ( int i = 0 ; i < constructors.length ; i++ ){
                init = Annotations.NApiServiceInit(constructors[i]);
                if ( init != null ){
                    constructor = constructors[i];
                    break;
                }
            }
        }

        /**
         * Given a class, creates instance of the class
         * @param clazz the class
         * @return one instance of the class, if possible
         * @throws Exception if fails to do so
         */
        public Object create(Class clazz) throws Exception {
            findInit(clazz);
            if ( constructor == null ) throw new Exception("No Constructor with Init Property!");
            Object o = instantiate( clazz, init);
            return o;
        }
    }

    /**
     * A simple class instance creator - args converted from string
     */
    public static class SimpleCreator extends ServiceCreator{

        public SimpleCreator(){}

        @Override
        protected Object instantiate(Class clazz, NApiServiceInit init) throws Exception {
            return createInstance( clazz , init.args() );
        }
    }

    /**
     * Create class instance using the spring context
     */
    public static class SpringContextCreator extends ServiceCreator{

        protected AutowireCapableBeanFactory beanFactory ;

        public SpringContextCreator(String[] args) {
            try {
                ApplicationContext ctx = new ClassPathXmlApplicationContext(args,true);
                beanFactory = ctx.getAutowireCapableBeanFactory();
                LOG.info(String.format("Bean factory is : %s", beanFactory));
            }catch (Exception e){
                LOG.error(" No application context found! Thus, I can not use spring context!", e);
            }
        }

        @Override
        protected Object instantiate(Class clazz, NApiServiceInit init) throws Exception {
            Object object = null;
            try {
                object = beanFactory.getBean(init.bean());
            }catch (Exception e){
                LOG.info(String.format("No bean  [%s] , creating one fully, pray to Spring God!", clazz));
                object = beanFactory.createBean(clazz);
            }
            LOG.info(String.format("Object is [%s]", object));
            return object ;
        }
    }

    /**
     * Converts the arguments string array into object array
     * using nJexl - thus every string gets executed as script
     * @param args the string array
     * @return an object array
     */
    public static Object[] params(String[] args){
        JexlContext context = Main.getContext() ;
        JexlEngine engine = Main.getJexl(context);
        Object[] params = new Object[args.length ];
        for ( int i = 0 ; i < args.length ; i++ ){
            Script s = engine.createScript( args[i]);
            params[i] = s.execute(context);
        }
        return params;
    }

    /**
     * Create instance of a class
     * @param clazz the class
     * @param args as strings, nJexl will be used to covert each one to object
     * @return one instance
     * @throws Exception if fails
     */
    public static Object createInstance(Class clazz, String[] args) throws Exception {
        if ( args.length == 0 ){ return clazz.newInstance() ; }
        Constructor[] constructors = clazz.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            int c = constructors[i].getParameterCount();
            if (c == args.length) {
                Object[] params = params( args);
                Object o = constructors[i].newInstance( params );
                return o;
            }
        }
        return null;
    }

    /**
     * Gets a service creator from metadata
     * @param nApiServiceCreator the metadata
     * @return the service creator
     */
    public static ServiceCreator creator(NApiServiceCreator nApiServiceCreator){
        Class type = nApiServiceCreator.type() ;
        ServiceCreator sc = null;
        try {
            sc = (ServiceCreator)createInstance( type, nApiServiceCreator.args() );
        }catch (Exception e){
            LOG.error( String.format("Error at creating Service Creator!"), e );
        }
        return sc ;
    }
}
