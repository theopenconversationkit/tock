/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Initializes the classes whose names are read on the standard input, and prints the ones that need a class missing
 * from the classpath (a NoClassDefFoundError, a lambda whose signature names a missing class...).
 */
public class LoadClasses {
    public static void main(String[] args) throws Exception {
        int failures = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String name;
        while ((name = reader.readLine()) != null) {
            try {
                Class.forName(name, true, LoadClasses.class.getClassLoader());
            } catch (LinkageError e) {
                Throwable cause = e;
                while (cause.getCause() != null) cause = cause.getCause();
                System.out.println("MISSING " + name + " -> " + cause);
                failures++;
            } catch (Throwable ignored) {
                // an initialization that needs the injector, a property...: not a missing class
            }
        }
        System.exit(failures == 0 ? 0 : 1);
    }
}
