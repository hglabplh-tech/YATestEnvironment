/*
 * Copyright (c) 2026 Harald Glab-Plhak
 */

package java.io.github.hglabplh_tech.reflect.clojure.api.utils;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.PersistentArrayMap;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static io.github.hglabplh_tech.reflect.clojure.api.utils.ClojFunctionalUtils.*;

/**
 * This is a utility class for getting reflection information about switch
 * statements and switch expressions in compiled Java classes.
 * <p>
 * The Java reflection API does not expose method bodies. This utility reads the
 * class file behind the reflected class and looks for the bytecode instructions
 * used by both classic switch statements and modern switch expressions:
 * {@code tableswitch} and {@code lookupswitch}.
 * </p>
 *
 * @author Harald Glab-Plhak (Harald G.P. IT-Consulting / Projéct Support)
 */
public class SwitchUtil {

    /**
     * Private constructor for all methods are static.
     */
    private SwitchUtil() {

    }

    /**
     * Get the switch reflection specification of a class.
     * The returned map is intended for the pseudo AST and contains the methods
     * that include switch bytecode, the number of switch instructions, and the
     * observed switch opcode names.
     *
     * @param theClass the class which is analyzed
     * @return the switch reflection specification as Clojure map
     */
    public static @Nonnull IPersistentMap getSwitchSpec(@Nonnull Class<?> theClass) {
        IPersistentMap switchMethods = getSwitchMethods(theClass);
        return PersistentArrayMap.EMPTY
                .assocEx(retrieveKeywordForJavaID("switchMethods", ObjType.NONE),
                        switchMethods)
                .assocEx(retrieveKeywordForJavaID("switchMethodCount", ObjType.NONE),
                        switchMethods.count());
    }

    /**
     * Get all methods that contain switch bytecode instructions.
     * This includes methods compiled from switch expressions, because the
     * expression form is also represented as switch bytecode.
     *
     * @param theClass the class which is analyzed
     * @return a map with method indexes and switch method specifications
     */
    public static @Nonnull IPersistentMap getSwitchMethods(@Nonnull Class<?> theClass) {
        String resourceName = "/" + theClass.getName().replace('.', '/') + ".class";
        try (InputStream classStream = theClass.getResourceAsStream(resourceName)) {
            if (classStream == null) {
                return PersistentArrayMap.EMPTY;
            }
            return readSwitchMethods(theClass, new DataInputStream(classStream));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Read a class file and collect switch methods from it.
     * The method parses only the parts of the class-file format that are needed
     * to reach method {@code Code} attributes.
     *
     * @param theClass    the class which is analyzed
     * @param classStream the class file stream
     * @return a map with method indexes and switch method specifications
     * @throws IOException if the class file cannot be read
     */
    private static @Nonnull IPersistentMap readSwitchMethods(
            @Nonnull Class<?> theClass,
            @Nonnull DataInputStream classStream) throws IOException {
        int magic = classStream.readInt();
        if (magic != 0xCAFEBABE) {
            return PersistentArrayMap.EMPTY;
        }
        classStream.readUnsignedShort();
        classStream.readUnsignedShort();
        Object[] constantPool = readConstantPool(classStream);
        classStream.readUnsignedShort();
        classStream.readUnsignedShort();
        classStream.readUnsignedShort();
        skipInterfaces(classStream);
        skipMembers(classStream);
        return readMethods(theClass, classStream, constantPool);
    }

    /**
     * Read the constant pool of a class file.
     *
     * @param classStream the class file stream
     * @return the constant pool entries needed by this utility
     * @throws IOException if the class file cannot be read
     */
    private static @Nonnull Object[] readConstantPool(
            @Nonnull DataInputStream classStream) throws IOException {
        int constantPoolCount = classStream.readUnsignedShort();
        Object[] constantPool = new Object[constantPoolCount];
        for (int index = 1; index < constantPoolCount; index++) {
            int tag = classStream.readUnsignedByte();
            switch (tag) {
                case 1 -> constantPool[index] = classStream.readUTF();
                case 3, 4 -> classStream.skipBytes(4);
                case 5, 6 -> {
                    classStream.skipBytes(8);
                    index++;
                }
                case 7, 8, 16, 19, 20 -> classStream.skipBytes(2);
                case 9, 10, 11, 12, 18 -> classStream.skipBytes(4);
                case 15 -> classStream.skipBytes(3);
                default -> throw new IOException("unknown constant pool tag: " + tag);
            }
        }
        return constantPool;
    }

    /**
     * Skip the interfaces of a class file.
     *
     * @param classStream the class file stream
     * @throws IOException if the class file cannot be read
     */
    private static void skipInterfaces(
            @Nonnull DataInputStream classStream) throws IOException {
        int interfacesCount = classStream.readUnsignedShort();
        classStream.skipBytes(interfacesCount * 2);
    }

    /**
     * Skip fields or other members with the same class-file member layout.
     *
     * @param classStream the class file stream
     * @throws IOException if the class file cannot be read
     */
    private static void skipMembers(
            @Nonnull DataInputStream classStream) throws IOException {
        int memberCount = classStream.readUnsignedShort();
        for (int index = 0; index < memberCount; index++) {
            classStream.readUnsignedShort();
            classStream.readUnsignedShort();
            classStream.readUnsignedShort();
            skipAttributes(classStream);
        }
    }

    /**
     * Read the methods and collect those containing switch instructions.
     * When a reflected method with the same name is available, generic parameter
     * and return types are added to the pseudo AST fragment.
     *
     * @param theClass     the class which is analyzed
     * @param classStream  the class file stream
     * @param constantPool the class file constant pool
     * @return a map with switch method specifications
     * @throws IOException if the class file cannot be read
     */
    private static @Nonnull IPersistentMap readMethods(
            @Nonnull Class<?> theClass,
            @Nonnull DataInputStream classStream,
            @Nonnull Object[] constantPool) throws IOException {
        int methodCount = classStream.readUnsignedShort();
        IPersistentMap result = PersistentArrayMap.EMPTY;
        Map<String, Method> reflectedMethods = getReflectedMethodMap(theClass);
        int switchIndex = 0;
        for (int index = 0; index < methodCount; index++) {
            classStream.readUnsignedShort();
            String methodName = getUtf8(constantPool, classStream.readUnsignedShort());
            String descriptor = getUtf8(constantPool, classStream.readUnsignedShort());
            SwitchInstructionInfo switchInfo =
                    readSwitchInstructions(classStream, constantPool);
            if (switchInfo.switchCount > 0) {
                Method method = reflectedMethods.get(methodName);
                IPersistentMap methodMap = PersistentArrayMap.EMPTY
                        .assocEx(retrieveKeywordForJavaID("methodName", ObjType.NONE),
                                methodName)
                        .assocEx(retrieveKeywordForJavaID("descriptor", ObjType.NONE),
                                descriptor)
                        .assocEx(retrieveKeywordForJavaID("switchCount", ObjType.NONE),
                                switchInfo.switchCount)
                        .assocEx(retrieveKeywordForJavaID("switchOpcodes", ObjType.NONE),
                                getArrayAsLazyVector(toStringArray(switchInfo.opcodes)));
                if (method != null) {
                    methodMap = methodMap
                            .assocEx(retrieveKeywordForJavaID("returnType", ObjType.NONE),
                                    method.getGenericReturnType())
                            .assocEx(retrieveKeywordForJavaID("paramTypes", ObjType.NONE),
                                    getArrayAsLazyVector(method.getGenericParameterTypes()));
                }
                result = result.assocEx(switchIndex, methodMap);
                switchIndex++;
            }
        }
        return result;
    }

    /**
     * Read a method's attributes and collect switch instructions from Code.
     *
     * @param classStream  the class file stream
     * @param constantPool the class file constant pool
     * @return switch instruction information for the method
     * @throws IOException if the class file cannot be read
     */
    private static @Nonnull SwitchInstructionInfo readSwitchInstructions(
            @Nonnull DataInputStream classStream,
            @Nonnull Object[] constantPool) throws IOException {
        int attributeCount = classStream.readUnsignedShort();
        SwitchInstructionInfo result = new SwitchInstructionInfo();
        for (int index = 0; index < attributeCount; index++) {
            String attributeName = getUtf8(constantPool, classStream.readUnsignedShort());
            int attributeLength = classStream.readInt();
            if ("Code".equals(attributeName)) {
                classStream.readUnsignedShort();
                classStream.readUnsignedShort();
                int codeLength = classStream.readInt();
                byte[] code = new byte[codeLength];
                classStream.readFully(code);
                result = result.merge(analyzeCode(code));
                int exceptionTableLength = classStream.readUnsignedShort();
                classStream.skipBytes(exceptionTableLength * 8);
                skipAttributes(classStream);
            } else {
                classStream.skipBytes(attributeLength);
            }
        }
        return result;
    }

    /**
     * Analyze bytecode and count switch instructions.
     * This intentionally records only instruction presence and opcode kind, not
     * source-level case labels, because labels are not reliably recoverable from
     * plain reflection data.
     *
     * @param code the method bytecode
     * @return switch instruction information
     */
    private static @Nonnull SwitchInstructionInfo analyzeCode(@Nonnull byte[] code) {
        SwitchInstructionInfo result = new SwitchInstructionInfo();
        for (byte byteCode : code) {
            int opcode = byteCode & 0xFF;
            if (opcode == 0xAA) {
                result.add("tableswitch");
            } else if (opcode == 0xAB) {
                result.add("lookupswitch");
            }
        }
        return result;
    }

    /**
     * Skip class-file attributes.
     *
     * @param classStream the class file stream
     * @throws IOException if the class file cannot be read
     */
    private static void skipAttributes(
            @Nonnull DataInputStream classStream) throws IOException {
        int attributeCount = classStream.readUnsignedShort();
        for (int index = 0; index < attributeCount; index++) {
            classStream.readUnsignedShort();
            int attributeLength = classStream.readInt();
            classStream.skipBytes(attributeLength);
        }
    }

    /**
     * Get a UTF-8 value from the class-file constant pool.
     *
     * @param constantPool the class file constant pool
     * @param index        the constant pool index
     * @return the string value
     */
    private static String getUtf8(@Nonnull Object[] constantPool, int index) {
        return (String) constantPool[index];
    }

    /**
     * Get methods by name for adding reflected signatures to switch specs.
     *
     * @param theClass the class which is analyzed
     * @return a map with method names and reflected methods
     */
    private static @Nonnull Map<String, Method> getReflectedMethodMap(
            @Nonnull Class<?> theClass) {
        Map<String, Method> result = new HashMap<>();
        for (Method method : theClass.getDeclaredMethods()) {
            result.putIfAbsent(method.getName(), method);
        }
        return result;
    }

    /**
     * Transform a Clojure vector with strings to a Java string array.
     *
     * @param vector the vector to transform
     * @return the Java string array
     */
    private static String[] toStringArray(@Nonnull IPersistentVector vector) {
        String[] result = new String[vector.length()];
        for (int index = 0; index < vector.length(); index++) {
            result[index] = (String) vector.nth(index);
        }
        return result;
    }

    /**
     * This class contains switch instruction information for a method.
     */
    private static class SwitchInstructionInfo {
        private int switchCount = 0;
        private IPersistentVector opcodes = getArrayAsLazyVector(new String[]{});

        /**
         * Add one switch opcode to this switch instruction information.
         *
         * @param opcodeName the switch opcode name
         */
        private void add(@Nonnull String opcodeName) {
            switchCount++;
            opcodes = opcodes.cons(opcodeName);
        }

        /**
         * Merge other switch instruction information into this instance.
         *
         * @param other the other switch instruction information
         * @return this merged switch instruction information
         */
        private @Nonnull SwitchInstructionInfo merge(
                @Nonnull SwitchInstructionInfo other) {
            for (int index = 0; index < other.opcodes.length(); index++) {
                add((String) other.opcodes.nth(index));
            }
            return this;
        }
    }
}
