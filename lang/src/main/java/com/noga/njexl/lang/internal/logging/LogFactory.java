/**
 * Copyright 2015 Nabarun Mondal
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

package com.noga.njexl.lang.internal.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by noga on 12/05/15.
 */
public final class LogFactory {

    public static Log getLog( Class c){
        return new LogImpl(c);
    }

    public static class LogImpl implements Log{

        protected Logger logger;

        public LogImpl(Class c){
            logger = Logger.getLogger(c.getName());
        }

        @Override
        public void debug(Object message) {
            logger.log(Level.INFO , String.format("%s",message));
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        public boolean isFatalEnabled() {
            return true;
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public boolean isTraceEnabled() {
            return true;
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public void trace(Object message) {
            logger.log(Level.FINE, String.format("%s",message));
        }

        @Override
        public void trace(Object message, Throwable t) {
            logger.log(Level.FINE, String.format("%s",message),t);
        }

        @Override
        public void debug(Object message, Throwable t) {
            logger.log(Level.CONFIG, String.format("%s",message),t);

        }

        @Override
        public void info(Object message) {
            logger.log(Level.INFO, String.format("%s",message));
        }

        @Override
        public void info(Object message, Throwable t) {
            logger.log(Level.INFO, String.format("%s",message),t);
        }

        @Override
        public void warn(Object message) {
            logger.log(Level.WARNING, String.format("%s",message));
        }

        @Override
        public void warn(Object message, Throwable t) {
            logger.log(Level.WARNING, String.format("%s",message),t);
        }

        @Override
        public void error(Object message) {
            logger.log(Level.SEVERE, String.format("%s",message));
        }

        @Override
        public void error(Object message, Throwable t) {
            logger.log(Level.SEVERE, String.format("%s",message));
        }

        @Override
        public void fatal(Object message) {
            error(message);
        }

        @Override
        public void fatal(Object message, Throwable t) {
            error(message,t);
        }
    }
}
