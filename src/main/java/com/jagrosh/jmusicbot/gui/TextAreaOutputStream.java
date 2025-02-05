/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.gui;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.swing.JTextArea;
/**
 *
 * @author Lawrence Dol
 */
public class TextAreaOutputStream extends OutputStream {

    // *************************************************************************************************
    // INSTANCE MEMBERS
    // *************************************************************************************************
    
        private byte[]                          oneByte;                                                    // array for write(int val);
        private Appender                        appender;                                                   // most recent action
        
        public TextAreaOutputStream(JTextArea textArea, int maxLines) {
            if (maxLines < 1) {
                throw new IllegalArgumentException("Maximum lines must be positive. Provided: " + maxLines);
            }
            this.appender = new Appender(textArea, maxLines);
        }
        
        public TextAreaOutputStream(JTextArea textArea) {
            this(textArea, 1000);
        }
        
        /** Clears the text area. */
        public synchronized void clear() {
            appender.clear();
        }
        
        @Override
        public synchronized void write(int value) {
            oneByte[0] = (byte) value;
            write(oneByte, 0, 1);
        }
        
        @Override
        public synchronized void write(byte[] buffer, int offset, int length) {
            if (appender != null) {
                String text = new String(buffer, offset, length, StandardCharsets.UTF_8);
                appender.append(text);
            }
        }
        
        @Override
        public synchronized void close() {
            appender.stop();
        }
        
        @Override
        public void flush() {
            // No operation needed for this implementation
        }
    } 