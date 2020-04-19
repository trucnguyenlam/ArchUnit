/*
 * Copyright 2014-2020 TNG Technology Consulting GmbH
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
package com.tngtech.archunit.core.importer;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.tngtech.archunit.PublicAPI;
import com.tngtech.archunit.core.InitialConfiguration;

import static com.tngtech.archunit.PublicAPI.Usage.ACCESS;

/**
 * Represents a set of {@link Location locations} of Java class files. Also offers methods to derive concrete locations (i.e. URIs) from
 * higher level concepts like packages or the classpath.
 */
public final class Locations {
    private static final InitialConfiguration<LocationResolver> locationResolver = new InitialConfiguration<>();

    static {
        ImportPlugin.Loader.loadForCurrentPlatform().plugInLocationResolver(locationResolver);
    }

    private Locations() {
    }

    /**
     * Directly converts the passed URLs to {@link Location locations}. URLs can be of class files
     * as well as directories. They can also be JAR URLs of class files
     * (e.g. <code>jar:file:///some.jar!/some/Example.class</code>) or folders within JAR files.
     *
     * @param urls URLs to directly convert to {@link Location locations}
     * @return {@link Location Locations} representing the passed URLs
     */
    @PublicAPI(usage = ACCESS)
    public static Set<Location> of(Iterable<URL> urls) {
        ImmutableSet.Builder<Location> result = ImmutableSet.builder();
        for (URL url : urls) {
            result.add(Location.of(url));
        }
        return result.build();
    }

    /**
     * All {@link Location locations} in the classpath that match the supplied package.
     *
     * @param pkg the package to look for within the classpath
     * @return {@link Location Locations} of all paths that match the supplied package
     */
    @PublicAPI(usage = ACCESS)
    public static Set<Location> ofPackage(String pkg) {
        ImmutableSet.Builder<Location> result = ImmutableSet.builder();
        for (Location location : getLocationsOf(asResourceName(pkg))) {
            result.add(location);
        }
        return result.build();
    }

    /**
     * Set of {@link Location locations} where the class file of the supplied class can be found.<br>
     * Note that this is really a set, since the same (or in bad cases a different version of the same) class
     * might be found within the classpath several times.
     *
     * @param clazz A {@link Class} to import
     * @return {@link Location Locations} of the respective class file within the classpath
     */
    @PublicAPI(usage = ACCESS)
    public static Set<Location> ofClass(Class<?> clazz) {
        return getLocationsOf(asResourceName(clazz.getName()) + ".class");
    }

    /**
     * @return All classes that can be found within the classpath. Note that ArchUnit does not distinguish between
     * the classpath and the modulepath, thus for Java &gt;= 9 all locations of class files from the
     * modulepath with be returned as well.
     */
    @PublicAPI(usage = ACCESS)
    public static Set<Location> inClassPath() {
        ImmutableSet.Builder<Location> result = ImmutableSet.builder();
        for (URL url : locationResolver.get().resolveClassPath()) {
            result.add(Location.of(url));
        }
        return result.build();
    }

    private static String asResourceName(String qualifiedName) {
        return qualifiedName.replace('.', '/');
    }

    private static Set<Location> getLocationsOf(String resourceName) {
        UrlSource classpath = locationResolver.get().resolveClassPath();
        NormalizedResourceName normalizedResourceName = NormalizedResourceName.from(resourceName);
        return ImmutableSet.copyOf(getResourceLocations(normalizedResourceName, classpath));
    }

    /**
     * We used to call {@link ClassLoader#getResources(String)} for the given resource name here,
     * but this had a number of disadvantages. One was that e.g. loading the package via
     * <pre><code>importPackage("java.io") -> getResources("/java/io")</code></pre>
     * did not behave correctly if archives were missing the respective folder entry.
     * In other words, even if there is a {@code java/io/File.class} within the JAR,
     * if the folder entry {@code java/io/} is missing, we will not detect that the package
     * {@code java.io} contains this class.
     * Unfortunately to optimize for space it seems to be too common to leave out folder
     * entries from a JAR file to ignore this as a corner case.<br>
     * Another problem that occurred was in combination with Android, since Gradle seemed to sometimes
     * create JARs with package entries (Java libraries) and sometimes without (Android libraries).
     * Then the test would start to behave inconsistently between running from Android Studio
     * (where the classpath would contain file entries and thus all package resources were found)
     * and via Gradle (where the classpath would contain JAR entries and for Android libraries
     * the package entries were missing, even though they were present for Java libraries).
     * Altogether we decided to drop using the {@link ClassLoader} and use
     * the former workaround we had for cases where {@link ClassLoader#getResources(String)}
     * would not return a result as the default way, since it seems to behave consistently
     * for all the cases.
     */
    private static Collection<Location> getResourceLocations(NormalizedResourceName resourceName, Iterable<URL> classpath) {
        Set<Location> result = new HashSet<>();
        for (Location location : Locations.of(classpath)) {
            if (containsEntryWithPrefix(location, resourceName)) {
                result.add(location.append(resourceName.toString()));
            }
        }
        return result;
    }

    private static boolean containsEntryWithPrefix(Location location, NormalizedResourceName searchedJarEntryPrefix) {
        for (NormalizedResourceName name : location.iterateEntries()) {
            if (name.startsWith(searchedJarEntryPrefix)) {
                return true;
            }
        }
        return false;
    }
}
