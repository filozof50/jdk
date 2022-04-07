import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.rmi.Remote;
import java.util.HashSet;
import java.util.Set;
import java.util.function.*;

public class TestStableLambdaName {
    private static final String serializedLambdasNamesFile = "stableLambdaNames.txt";
    private static final Set<String> names = new HashSet<>();
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    private enum lambdaType {
        NOT_SERIALIZABLE_NO_ALT_METHODS_NO_ALT_INTERFACES (0),
        SERIALIZABLE_ONLY (1),
        NOT_SERIALIZABLE_HAS_ALT_INTERFACES(2),
        SERIALIZABLE_HAS_ALT_INTERFACES(3),
        NOT_SERIALIZABLE_HAS_ALT_METHODS(4),
        SERIALIZABLE_HAS_ALT_METHODS(5),
        NOT_SERIALIZABLE_HAS_ALT_METHODS_HAS_ALT_INTERFACES(6),
        SERIALIZABLE_HAS_ALT_METHODS_HAS_ALT_INTERFACES(7);

        private final int index;
        lambdaType(int i) {
            index = i;
        }
    }
    private static final String[] interfaceMethods = new String[]{"accept", "consume", "apply", "supply", "get", "test", "getAsBoolean"};
    private static final Class<?>[] interfaces = new Class<?>[]{Consumer.class, Function.class, Predicate.class, Supplier.class, BooleanSupplier.class};
    private static final MethodType[] methodTypes = new MethodType[]{MethodType.methodType(String.class, Integer.class), MethodType.methodType(Throwable.class, AssertionError.class)};
    private static MethodHandle[] methodHandles;
    private static final Class<?>[] altInterfaces = new Class<?>[]{Cloneable.class, Remote.class};
    private static final MethodType[] altMethodsMethod1 = new MethodType[]{MethodType.methodType(String.class, Number.class)};
    private static final MethodType[] altMethodsMethod2 = new MethodType[]{MethodType.methodType(Throwable.class, Error.class), MethodType.methodType(Throwable.class, Throwable.class)};
    private static Object lambda;
    private static int numOfCreatedLambdas;

    private static String method1(Number number) {
        return String.valueOf(number);
    }

    private static String method1(Integer number) { return String.valueOf(number); }

    private static Throwable method2(AssertionError error) {
        return error;
    }

    private static Throwable method2(Error error) {
        return error;
    }

    private static Throwable method2(Throwable throwable) {
        return throwable;
    }

    private static String removeHashFromLambdaName(String name) {
        return name.substring(0, name.indexOf("/0x0"));
    }

    private static void createPlainLambdas(int flags) throws Throwable {
        for (String interfaceMethod : interfaceMethods) {
            for (Class<?> interfaceClass : interfaces) {
                for (int i = 0; i < methodTypes.length; i++) {
                    lambda = LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                            methodTypes[i], methodHandles[i], methodTypes[i], flags).getTarget().invoke();
                    names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                    numOfCreatedLambdas++;
                }
            }
        }
    }

    private static Object lambdaWithOneAltInterface(String interfaceMethod, Class<?> interfaceClass, MethodType methodType, MethodHandle methodHandle, int flags, Class<?> altInterface) throws Throwable {
        int numOfAltInterfaces = 1;
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodType, methodHandle, methodType, flags, numOfAltInterfaces, altInterface).getTarget().invoke();
    }

    private static Object lambdaWithMultipleAltInterfaces(String interfaceMethod, Class<?> interfaceClass,  MethodType methodType, MethodHandle methodHandle, int flags) throws Throwable {
        int numOfAltInterfaces = 2;
        int altInterfacesIndex = 0;
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodType, methodHandle, methodType, flags, numOfAltInterfaces, altInterfaces[altInterfacesIndex++], altInterfaces[altInterfacesIndex]).getTarget().invoke();
    }

    private static void createLambdasWithAltInterfaces(int flags) throws Throwable {
        for (String interfaceMethod : interfaceMethods) {
            for (Class<?> interfaceClass : interfaces) {
                for (int i = 0; i < methodTypes.length; i++) {
                    for (Class<?> altInterface : altInterfaces) {
                        lambda = lambdaWithOneAltInterface(interfaceMethod, interfaceClass, methodTypes[i], methodHandles[i], flags, altInterface);
                        names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                        numOfCreatedLambdas++;
                    }

                    lambda = lambdaWithMultipleAltInterfaces(interfaceMethod, interfaceClass, methodTypes[i], methodHandles[i], flags);
                    names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                    numOfCreatedLambdas++;
                }
            }
        }
    }

    private static Object lambdaWithOneAltMethod(String interfaceMethod, Class<?> interfaceClass,  MethodType methodType, MethodHandle methodHandle,
                                                 int flags, MethodType altMethod) throws Throwable {
        int numOfAltMethods = 1;
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodType, methodHandle, methodType, flags, numOfAltMethods, altMethod).getTarget().invoke();
    }

    private static Object lambdaWithMultipleAltMethods(String interfaceMethod, Class<?> interfaceClass, int flags) throws Throwable {
        int numOfAltMethods = 2;
        int indexOfAltMethod = 0;
        MethodType methodTypeMethod2 = methodTypes[1];
        MethodHandle methodHandleMethod2 = methodHandles[1];
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodTypeMethod2, methodHandleMethod2, methodTypeMethod2, flags, numOfAltMethods, altMethodsMethod2[indexOfAltMethod++],
                altMethodsMethod2[indexOfAltMethod]).getTarget().invoke();
    }

    private static void createLambdasWithAltMethods(int flags) throws Throwable {
        int indexOfMethodWithOneAltMethod = 0;
        int indexOfMethodWithTwoAltMethods = 1;
        int altMethodIndex = 0;
        for (String interfaceMethod : interfaceMethods) {
            for (Class<?> interfaceClass : interfaces) {
                lambda = lambdaWithOneAltMethod(interfaceMethod, interfaceClass, methodTypes[indexOfMethodWithOneAltMethod], methodHandles[indexOfMethodWithOneAltMethod],
                        flags, altMethodsMethod1[altMethodIndex]);
                names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                numOfCreatedLambdas++;

                for (MethodType altMethod : altMethodsMethod2) {
                    lambda = lambdaWithOneAltMethod(interfaceMethod, interfaceClass, methodTypes[indexOfMethodWithTwoAltMethods], methodHandles[indexOfMethodWithTwoAltMethods],
                            flags, altMethod);
                    names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                    numOfCreatedLambdas++;
                }

                lambda = lambdaWithMultipleAltMethods(interfaceMethod, interfaceClass, flags);
            }
        }
    }

    private static Object lambdaWithOneAltInterfaceAndOneAltMethod(String interfaceMethod, Class<?> interfaceClass, MethodType methodType, MethodHandle methodHandle, int flags,
                                                                   Class<?> altInterface, MethodType altMethod) throws Throwable {
        int numOfAltInterfaces = 1;
        int numOfAltMethods = 1;
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodType, methodHandle, methodType, flags, numOfAltInterfaces, altInterface, numOfAltMethods, altMethod).getTarget().invoke();
    }

    private static Object lambdaWithOneAltInterfaceAndMultipleAltMethods(String interfaceMethod, Class<?> interfaceClass, int flags, Class<?> altInterface) throws Throwable {
        int numOfAltInterfaces = 1;
        int numOfAltMethods = 2;
        int indexOfAltMethod = 0;
        MethodType methodTypeMethod2 = methodTypes[1];
        MethodHandle methodHandleMethod2 = methodHandles[1];

        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodTypeMethod2, methodHandleMethod2, methodTypeMethod2, flags, numOfAltInterfaces, altInterface, numOfAltMethods, altMethodsMethod2[indexOfAltMethod++],
                altMethodsMethod2[indexOfAltMethod]).getTarget().invoke();
    }

    private static Object lambdaWithMultipleAltInterfaceAndMultipleAltMethods(String interfaceMethod, Class<?> interfaceClass, int flags) throws Throwable {
        int numOfAltInterfaces = 2;
        int numOfAltMethods = 2;
        int indexOfAltInterface = 0;
        int indexOfAltMethod = 0;
        MethodType methodTypeMethod2 = methodTypes[1];
        MethodHandle methodHandleMethod2 = methodHandles[1];

        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass), methodTypeMethod2, methodHandleMethod2, methodTypeMethod2,
                flags, numOfAltInterfaces, altInterfaces[indexOfAltInterface++], altInterfaces[indexOfAltInterface], numOfAltMethods, altMethodsMethod2[indexOfAltMethod++],
                altMethodsMethod2[indexOfAltMethod]).getTarget().invoke();
    }
    private static void createLambdasWithAltInterfacesAndAltMethods(int flags) throws Throwable {
        int indexOfMethodWithOneAltMethod = 0;
        int indexOfMethodWithTwoAltMethods = 1;
        int altMethodIndex = 0;
        for (String interfaceMethod : interfaceMethods) {
            for (Class<?> interfaceClass : interfaces) {
                for (Class<?> altInterface : altInterfaces) {
                    lambda = lambdaWithOneAltInterfaceAndOneAltMethod(interfaceMethod, interfaceClass, methodTypes[indexOfMethodWithOneAltMethod], methodHandles[indexOfMethodWithOneAltMethod], flags,
                            altInterface, altMethodsMethod1[altMethodIndex]);
                    names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                    numOfCreatedLambdas++;
                    lambda = lambdaWithOneAltInterfaceAndMultipleAltMethods(interfaceMethod, interfaceClass, flags, altInterface);
                    names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                    numOfCreatedLambdas++;

                    for (MethodType altMethod : altMethodsMethod2) {
                        lambda = lambdaWithOneAltInterfaceAndOneAltMethod(interfaceMethod, interfaceClass, methodTypes[indexOfMethodWithTwoAltMethods], methodHandles[indexOfMethodWithTwoAltMethods], flags,
                                altInterface, altMethod);
                        names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                        numOfCreatedLambdas++;
                    }
                }
                lambda = lambdaWithMultipleAltInterfaceAndMultipleAltMethods(interfaceMethod, interfaceClass, flags);
                names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                numOfCreatedLambdas++;
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        Set<String> savedNames = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(serializedLambdasNamesFile));
        String line;
        while ((line = br.readLine()) != null) {
            savedNames.add(line);
        }

        MethodType methodTypeForMethod1 = methodTypes[0];
        MethodType methodTypeForMethod2 = methodTypes[1];
        methodHandles = new MethodHandle[]{lookup.findStatic(TestStableLambdaName.class, "method1", methodTypeForMethod1),
                lookup.findStatic(TestStableLambdaName.class, "method2", methodTypeForMethod2)};

        numOfCreatedLambdas = 0;

        // All lambdas with flags 0
        createPlainLambdas(lambdaType.NOT_SERIALIZABLE_NO_ALT_METHODS_NO_ALT_INTERFACES.index);

        // All lambdas with flags 1
        createPlainLambdas(lambdaType.SERIALIZABLE_ONLY.index);

        // All lambdas with flags 2
        createLambdasWithAltInterfaces(lambdaType.NOT_SERIALIZABLE_HAS_ALT_INTERFACES.index);

        // All lambdas with flags 3
        createLambdasWithAltInterfaces(lambdaType.SERIALIZABLE_HAS_ALT_INTERFACES.index);

        // All lambdas with flags 4
        createLambdasWithAltMethods(lambdaType.NOT_SERIALIZABLE_HAS_ALT_METHODS.index);

        // All lambdas with flags 5
        createLambdasWithAltMethods(lambdaType.SERIALIZABLE_HAS_ALT_METHODS.index);

        // All lambdas with flags 6
        createLambdasWithAltInterfacesAndAltMethods(lambdaType.NOT_SERIALIZABLE_HAS_ALT_METHODS_HAS_ALT_INTERFACES.index);

        // All lambdas with flags 7
        createLambdasWithAltInterfacesAndAltMethods(lambdaType.SERIALIZABLE_HAS_ALT_METHODS_HAS_ALT_INTERFACES.index);

        // All the names must be unique
        assert names.size() == numOfCreatedLambdas;

        assert savedNames.size() == names.size();

        assert savedNames.containsAll(names);

        System.err.println(numOfCreatedLambdas);
    }
}
