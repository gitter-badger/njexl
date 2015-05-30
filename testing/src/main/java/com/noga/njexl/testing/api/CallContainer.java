/**
 * Copyright 2015 Nabarun Mondal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.noga.njexl.testing.api;

import com.noga.njexl.testing.dataprovider.DataSourceTable;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.regex.Pattern;


@XStreamAlias("R")
public class CallContainer implements Serializable, Cloneable {

    public static final String TEST_ID = "TEST_ID" ;

    public static final String TEST_DESCRIPTION = "TEST_DESCRIPTION" ;

    public static final String EXPECTED_EXCEPTION = "EXPECTED_EXCEPTION" ;

    public static final String TEST_DISABLED = "TEST_DISABLED" ;


    @XStreamOmitField
    public String globals ;

    @XStreamOmitField
    public String pre = "" ;

    @XStreamOmitField
    public String post = "" ;

    @XStreamOmitField
    public Object service;

    @XStreamOmitField
    public DataSourceTable dataTable;

    @XStreamAlias("i")
    public int rowId;

    @XStreamOmitField
    public Method method;

    @XStreamImplicit(itemFieldName = "p")
    public Object[] parameters;

    @XStreamAlias("r")
    public Object result;

    @XStreamAlias("t")
    public long timing;

    @XStreamOmitField
    public Throwable error;

    @XStreamAlias("th")
    public String exceptionMessage;

    @XStreamAlias("s")
    public boolean validationResult;

    public String column(String columnName){
        return dataTable.columnValue(columnName,rowId);
    }

    public String[] values(){
        return dataTable.row(rowId);
    }

    @XStreamAlias("ee")
    public Pattern exceptionPattern = Pattern.compile("");


    public boolean disabled(){
        try {
            String val = column(TEST_DISABLED);
            if (val == null) {
                return false;
            }
            return Boolean.valueOf(val);
        }catch (Exception e){
            return false;
        }
    }

    public void setExceptionPattern(String ee){
        if ( ee == null ) {
            ee = column(EXPECTED_EXCEPTION);
        }
        if ( ee == null ){
            return;
        }
        exceptionPattern = Pattern.compile(ee);
    }

    public void setExceptionMessage() {
        if (error != null) {
            exceptionMessage = "" ;
            exceptionMessage = String.format("%s ; Cause : ",error.getMessage());
            if(error.getCause() != null)
            {
                exceptionMessage += String.format("%s",error.getCause().getMessage());
            }
        }
    }

    public boolean isExceptionValid(){
        if ( exceptionPattern.pattern().isEmpty()){
            if ( error == null ){
                return true;
            }
            return false;

        }
        if ( error == null ){
            return false ;
        }
        String className = error.getClass().getName();
        if ( exceptionPattern.matcher(className).matches()){
            return true;
        }
        String message = error.getMessage();
        if ( message != null ){
            if ( exceptionPattern.matcher(message).matches()){
                return true;
            }
        }

        return false;
    }


    public String testId(){
        if ( dataTable == null ){
            return Integer.toString(rowId) ;
        }
        String t = dataTable.columnValue( TEST_ID, rowId);
        return (t!=null) ? t : Integer.toString(rowId) ;
    }

    public String uniqueId(){
        return testId() + "_" + Long.toString( Thread.currentThread().getId() ) ;
    }
    public String description(){
        String t = dataTable.columnValue( TEST_DESCRIPTION, rowId);
        return (t!=null) ? t : Integer.toString(rowId) ;
    }

    @Override
    public String toString(){
        return String.format("%s@%s : %s", uniqueId(), dataTable != null ? dataTable.name() : "(null)", validationResult );
    }

    public  String toXml(){
        XStream xStream = new XStream();
        xStream.alias("R", CallContainer.class);
        xStream.autodetectAnnotations(true);
        return xStream.toXML(this);
    }

    /**
     * <pre>
     *     Creates a cloned copy of one object.
     *     Needed in case of multi threaded environment
     * </pre>
     * @param inputObject the object whose copy is required.
     * @return  clone copy
     */
    public static synchronized CallContainer clone(CallContainer inputObject) {
        try {
            CallContainer clonedObject = (CallContainer) inputObject.clone();
            return clonedObject;
        }catch (Exception e){

        }
        return inputObject ;
    }

    public void call(){
        try{
            timing = System.nanoTime();
            result = method.invoke(service,parameters);
            timing = System.nanoTime() - timing ;
        }catch (Throwable t){
            timing = -1;
            error = t.getCause() ;
            if ( error == null ){
                error = t;
            }
            result = null;
        }
    }
}
