package ru.ifmo.ctddev.gera.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.ToolProvider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Generates default implementation for a given class.
 */
public class Implementor implements JarImpler {
    /**
     * Package where the class to implement is located.
     */
    private String pack = "";

    /**
     * Simple name of the class to implement.
     */
    private String classname;

    /**
     * Accumulates generated text.
     */
    private StringBuilder builder;

    /**
     * Generates default implementation for a given class.
     * <p>
     * Generated class full name is same as full name of the given class with <tt>Impl</tt> suffix added.
     * Has two modes:
     * <ul>
     *     <li><code>java classname filepath</code></li>
     *     <li><code>java -jar classname filename</code></li>
     * </ul>
     * First mode generates a <tt>filepath/classnameImpl.java</tt> file with implementation in specified place.
     * Second one generates a <tt>filename.jar</tt> file with compiled implementation.
     *
     * @param   args command line arguments.
     * @throws  ImplerException if <ul>
     *     <li>arguments do not match the pattern</li>
     *     <li>{@link ClassLoader} cannot find the specified class</li>
     *     <li>the implementation cannot be generated</li>
     * </ul>
     * @see ClassLoader#getSystemClassLoader()
     * @see ClassLoader#loadClass(String)
     */
    public static void main(String[] args) throws ImplerException {
        Implementor implementor = new Implementor();

        try {
            if (args.length == 3 && args[0].equals("-jar")) {
                implementor.implementJar(ClassLoader.getSystemClassLoader().loadClass(args[1]), Paths.get(args[2]));
            } else if (args.length == 2) {
                implementor.implement(ClassLoader.getSystemClassLoader().loadClass(args[0]), Paths.get(args[1]));
            } else {
                throw new ImplerException("Invalid arguments");
            }
        } catch (ClassNotFoundException e) {
            throw new ImplerException("Target class not found", e);
        }
    }

    /**
     * Produces a <tt>.java</tt> file with implementation for a given class or interface.
     * <p>
     * Generated class will have the same name as the given type token, but with suffix <tt>Impl</tt>.
     * </p>
     *
     * @param   token type token to implement
     * @param   root  path to the resulting file
     * @throws  ImplerException if <ul>
     *     <li>the directories representing package which the type token belongs to cannot be created</li>
     *     <li>the implementation cannot be generated or printed</li>
     * </ul>
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        checkToken(token);

        classname = token.getSimpleName() + "Impl";
        if (token.getPackage() != null) {
            pack = token.getPackage().getName();
        }

        try {
            Files.deleteIfExists(root.resolve(pack.replace('.', File.separatorChar)).resolve(classname + ".java"));
            Files.createDirectories(root.resolve(pack.replace('.', File.separatorChar)));
        } catch (IOException e) {
            throw new ImplerException("Could not create package " + pack, e);
        }

        try (Writer pw = new Converter(new PrintWriter(new OutputStreamWriter(Files.newOutputStream(
                root.resolve(pack.replace('.', File.separatorChar)).resolve(classname + ".java")
        ), StandardCharsets.UTF_8)))) {
            builder = new StringBuilder();
            printImplementation(token);
            pw.write(builder.toString());
        } catch (IOException e) {
            throw new ImplerException(
                    "Error occurred while working with file " + classname + ".java " + e.getMessage(), e);
        }
    }

    /**
     * Produces a <tt>.jar</tt> file with compiled implementation for a given class ot interface.
     *
     * @param   token   a type token to implement
     * @param   jarFile a path to the resulting file
     * @throws  ImplerException if <ul>
     *     <li>generated <tt>.java</tt> fails to compile</li>
     *     <li>implementation cannot be generated or written to <tt>.jar</tt></li>
     * </ul>
     * @see     ToolProvider#getSystemJavaCompiler()
     * @see     javax.tools.JavaCompiler#run(InputStream, OutputStream, OutputStream, String...)
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        implement(token, Paths.get("."));

        String resFile = pack.replace('.', File.separatorChar) + File.separatorChar + classname;
        Path classPath = Paths.get(resFile + ".class");

        if (ToolProvider.getSystemJavaCompiler().run(
                null, null, null, resFile + ".java", "-encoding", "WINDOWS-1251") != 0) {
            throw new ImplerException("Could not compile class " + classname);
        }

        (new Manifest()).getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        try (
                InputStream inputStream = Files.newInputStream(classPath);
                JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile))) {

            jarOutputStream.putNextEntry(new JarEntry(classPath.toString()));

            int len;
            byte block[] = new byte[1024];
            while ((len = inputStream.read(block, 0, 1024)) >= 0) {
                jarOutputStream.write(block, 0, len);
            }

            jarOutputStream.closeEntry();
        } catch (IOException e) {
            throw new ImplerException("Error while writing jar", e);
        }
    }

    /**
     * Checks whether the specified class or interface can be extended or implemented.
     *
     * @param   token the class to check
     * @throws  ImplerException if the specified class or interface cannot be extended or implemented
     */
    private void checkToken(Class<?> token) throws ImplerException {
        String msg = "You are not allowed to extend ";
        
        if (token.isPrimitive()) {
            throw new ImplerException(msg + "primitives");
        }
        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException(msg + "final classes");
        }
        if (token == Enum.class) {
            throw new ImplerException(msg + "enums");
        }
    }

    /**
     * Prints the implementation for the specified class or interface.
     *
     * @param   token type token of class or interface to implement
     * @throws  ImplerException if the implementation cannot be generated
     */
    private void printImplementation(Class<?> token) throws ImplerException {
        if (!pack.equals("")) {
            print("package " + pack + ";\n\n");
        }

        print("public class " + classname + " " +
                (token.isInterface() ? "implements " : "extends ") +
                token.getSimpleName() + " {\n");

        if (!token.isInterface()) {
            printCtors(token);
        }
        printMethods(token);
        print("}\n");
    }

    /**
     * Obtains and prints constructors for the specified class.
     *
     * @param   token type token of class or interface to implement
     * @throws  ImplerException if the type token refers to the class and
     *                          has no <code>public</code> or <code>protected</code> constructors
     * @see     Implementor#print(Constructor)
     */
    private void printCtors(Class<?> token) throws ImplerException {
        boolean found = false;
        for (Constructor<?> ctor : token.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(ctor.getModifiers())) {
                found = true;
                print(ctor);
            }
        }

        if (!found) {
            throw new ImplerException("No constructors found");
        }
    }

    /**
     * Obtains and prints methods for the specified class or interface.
     * Works only with <code>public</code> and <code>private</code> constructors.
     *
     * @param   token type token of class or interface to implement
     * @see     Implementor#print(Method)
     */
    private void printMethods(Class<?> token) {
        for (Method m : token.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                print(m);
            }
        }

        Class<?> temp = token;
        while (temp != null) {
            for (Method m : temp.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (Modifier.isProtected(mod) && Modifier.isAbstract(mod)) {
                    print(m);
                }
            }
            temp = temp.getSuperclass();
        }
    }

    /**
     * Prints a constructor.
     * Prints a default constructor implementation which calls <code>super</code>.
     *
     * @param   ctor a constructor to print
     */
    private void print(Constructor ctor) {
        printTitle(ctor);
        print("{\n\t\tsuper(");
        printArr(ctor.getParameters(), Parameter::getName);
        print(");\n\t}\n");
    }


    /**
     * Prints a method.
     * Prints an implementation which returns default value based on return type.
     *
     * @param   method a method to print
     */
    private void print(Method method) {
        printTitle(method);
        print("{\n\t\treturn" + defaultValue(method.getReturnType()) + ";\n\t}\n");
    }

    /**
     * Generates default value for a class.
     * Defaults are the following:
     * <ul>
     *     <li>for a <code>boolean</code> it is <code>false</code></li>
     *     <li>for numeric primitives it is <code>0</code></li>
     *     <li>for other classes it is <code>null</code></li>
     * </ul>
     *
     * @param   token type token of class to get the default value of
     * @return  a {@link String} representing default value of the type token
     */
    private String defaultValue(Class<?> token) {
        if (token.equals(boolean.class)) {
            return " false";
        }
        if (token.equals(void.class)) {
            return "";
        }
        if (token.isPrimitive()) {
            return " 0";
        }
        return " null";
    }

    /**
     * Prints title for an {@link Executable} object.
     * Prints {@link Annotation annotations}, return type (if specified executable is not a constructor),
     * {@link Modifier modifiers}, name and {@link Parameter parameters} named by default as <code>argN</code>,
     * where <code>N</code> is the number of parameter in the list.
     *
     * @param   exec an executable to print the title for
     */
    private void printTitle(Executable exec) {
        for (Annotation annotation : exec.getDeclaredAnnotations()) {
            print("\t" + annotation + "\n");
        }

        print("\t" + Modifier.toString(exec.getModifiers() &
                ~(Modifier.ABSTRACT | Modifier.INTERFACE | Modifier.TRANSIENT)) + " ");

        if (exec instanceof Method) {
            if (((Method) exec).getGenericReturnType().getTypeName().equals("T")) {
                print(" <T> ");
            }
            print(((Method) exec).getGenericReturnType().getTypeName() + " " + exec.getName());
        } else if (exec instanceof Constructor) {
            print(classname);
        }

        print("(");

        Type[] types = exec.getGenericParameterTypes();
        for (int i = 0; i < types.length; i++) {
            print(types[i].getTypeName() + " arg" + i);
            if (i < types.length - 1) {
                print(", ");
            }
        }

        print(") ");

        Type[] exceptions = exec.getGenericExceptionTypes();
        if (exceptions.length > 0) {
            print("throws ");
            printArr(exceptions, Type::getTypeName);
            print(" ");
        }
    }

    /**
     * Prints an array, separating its elements by a comma.
     *
     * @param arr     an array of elements to print
     * @param printer a {@link Consumer} which specifies how the object should be printed
     */
    private <T> void printArr(T[] arr, Function<T, String> printer) {
        for (int i = 0; i < arr.length; i++) {
            print(printer.apply(arr[i]));
            if (i < arr.length - 1) {
                print(", ");
            }
        }
    }

    /**
     * Prints an object by calling a <code>toString</code> method.
     *
     * @param o an object to print
     */
    private void print(Object o) {
        builder.append(o.toString());
    }

    /**
     * Converts unicode symbols to an appropriate format.
     */
    private class Converter extends FilterWriter {
        protected Converter(Writer writer) {
            super(writer);
        }

        @Override
        public void write(int i) throws IOException {
            out.write(String.format("\\u%04X", i));
        }

        @Override
        public void write(char[] chars, int i, int i1) throws IOException {
            for (int j = i; j < i1; j++) {
                write(chars[j]);
            }
        }

        @Override
        public void write(String s, int i, int i1) throws IOException {
            write(s.toCharArray(), i, i1);
        }
    }
}
