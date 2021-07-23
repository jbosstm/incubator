/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana.lra.checker;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Utility class used for loading classes to be then used
 * for creating synthetic bean archive for Weld
 */
final class ClassAndFilesProcessing {
    private final Log mavenLog;

    ClassAndFilesProcessing(Log mavenLog) {
        this.mavenLog = mavenLog;
    }

    /**
     * Says if file is in the zip format.
     *
     * @param file  file to check if it's zip
     * @return true if zip format, false otherwise
     */
    static boolean isZipFile(File file) {
        try (final ZipFile ignored = new ZipFile(file)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Traversing jar file and loading all '.class' files by provided classloader.
     *
     * @param pathToJar  where jar file resides
     * @param classLoader  class loader used for loading classes
     * @return  list of loaded classes
     */
    List<Class<?>> loadFromJar(final File pathToJar, final ClassLoader classLoader) throws MojoFailureException {
        try (ZipFile zipFile = new ZipFile(pathToJar)) {
            Stream<String> stream = zipFile.stream()
                .filter(zipEntry -> !zipEntry.isDirectory())
                .map(ZipEntry::getName);
            return processStream(stream, classLoader, pathToJar);
        } catch (IOException ioe) {
            throw new MojoFailureException("Can't read from jar file '" + pathToJar + "'", ioe);
        }
    }

    /**
     * Traversing directory and loading all '.class' files by provided classloader.
     *
     * @param pathToDir  path of directory
     * @param classLoader  class loader used for loading classes
     * @return  list of loaded classes
     */
    List<Class<?>> loadFromDir(final File pathToDir, final ClassLoader classLoader) throws MojoFailureException {
        final Path pathToDirAsPath = Paths.get(pathToDir.getPath());
        try (Stream<Path> paths = Files.walk(pathToDirAsPath)) {
            Stream<String> stream = paths
                .filter(Files::isRegularFile)
                .map(pathToDirAsPath::relativize)
                .map(path -> path.toFile().getPath());
            return processStream(stream, classLoader, pathToDir);
        } catch (IOException ioe) {
            throw new MojoFailureException("Can't read from directory '" + pathToDir + "'", ioe);
        }
    }

    private List<Class<?>> processStream(Stream<? extends String> stream, ClassLoader classLoader, File streamedFile) {
        return stream
            .filter(fileName -> fileName.endsWith(".class"))
            .map(fileName -> fileName.replace(File.separator, "."))
            .map(fileName -> fileName.substring(0, fileName.length() - ".class".length()))
            .map(className -> loadClass(className, streamedFile, classLoader))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Class<?> loadClass(String className, File fileLoadingFrom, ClassLoader classLoader) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            mavenLog.debug("Error on loading class by URLClassLoader from: "
                + Arrays.asList(((URLClassLoader) classLoader).getURLs()), e);
            String errMsg = "Can't load class '" + className + "' from '" + fileLoadingFrom.getPath() + "'";
            mavenLog.error(errMsg);
            return null;
        }
    }
}
