/**
 * Copyright 2016 Nabarun Mondal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.noga.njexl.lang.extension.iterators;

import com.noga.njexl.lang.extension.datastructures.XList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * This lets people write code like :
 * for ( line : file ){  }
 *
 * Created by noga on 22/02/16.
 */
public class FileIterator extends YieldedIterator {

    protected int index;

    protected final List<String> lines ;

    protected boolean eof;

    public final String location ;

    protected String line;

    protected final BufferedReader reader;

    public FileIterator(String location) throws Exception {
        index = -1 ;
        lines = new XList<>();
        eof = false ;
        if (location.startsWith("http://") ||
                location.startsWith("https://") ||
                location.startsWith("ftp://")) {
            URL url = new URL(location);
            URLConnection conn = url.openConnection();
            reader = new BufferedReader( new InputStreamReader(conn.getInputStream(), "UTF-8"));
        }else{
            location = new File(location).getAbsolutePath();
            reader = new BufferedReader( new FileReader(location) );
        }
        this.location = location ;
    }

    @Override
    public YieldedIterator inverse() {
        throw new UnsupportedOperationException("Inverse is not defined on file!");
    }

    @Override
    public void reset() {
        index = -1 ;
    }

    @Override
    public synchronized boolean hasNext() {
        line();
        return line != null ;
    }

    protected String line(){
        if ( eof ){
            return lines.get(++index);
        }
        try{
            line = reader.readLine();
            index++;
            if ( line == null ){
                eof = true ;
                reader.close();
                index = lines.size();
                return line;
            }
            lines.add(line);
            return line;
        }catch (Exception e) {
            eof = true ;
        }
        return null;
    }

    @Override
    public synchronized Object next() {
        return line ;
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof FileIterator ){
            return location.equals( ((FileIterator) obj).location );
        }
        return false ;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    protected void finalize() throws Throwable {
        reader.close();
        lines.clear();
        index = -1;
        eof = true ;
    }

    @Override
    public String toString() {
        return String.format("< %s , %d , %s >", location, index, eof );
    }
}
