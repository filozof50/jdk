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
    private static final Set<String> names = new HashSet<>();
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    // 0 - not serializable, no altMethods no altInterfaces
    // 1 - serializable only
    // 2 - not serializable, has altInterfaces
    // 4 - not serializable, has altMethods
    // 3 - serializable, has altInterfaces
    // 5 - serializable, has altMethods
    // 6 - not serializable, has altMethods has altInterfaces
    // 7 - serializable, has altMethods has altInterfaces
    private static final int[] flags = new int[]{0, 1, 2, 4, 3, 5, 6, 7};
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

    private static String method1(Integer number) {
        return String.valueOf(number);
    }

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

    private static void createPlainLambdas(int flagsIndex) throws Throwable {
        for (String interfaceMethod : interfaceMethods) {
            for (Class<?> interfaceClass : interfaces) {
                for (int i = 0; i < methodTypes.length; i++) {
                    lambda = LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                            methodTypes[i], methodHandles[i], methodTypes[i], flags[flagsIndex]).getTarget().invoke();
                    names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                    numOfCreatedLambdas++;
                }
            }
        }
    }

    private static Object lambdaWithOneAltInterface(String interfaceMethod, Class<?> interfaceClass, int index, int flagsIndex, Class<?> altInterface) throws Throwable {
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodTypes[index], methodHandles[index], methodTypes[index], flags[flagsIndex], 1, altInterface).getTarget().invoke();
    }

    private static Object lambdaWithMultipleAltInterfaces(String interfaceMethod, Class<?> interfaceClass, int index, int flagsIndex) throws Throwable {
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodTypes[index], methodHandles[index], methodTypes[index], flags[flagsIndex], 2, altInterfaces[0], altInterfaces[1]).getTarget().invoke();
    }

    private static void createLambdasWithAltInterfaces(int flagsIndex) throws Throwable {
        for (String interfaceMethod : interfaceMethods) {
            for (Class<?> interfaceClass : interfaces) {
                for (int i = 0; i < methodTypes.length; i++) {
                    for (Class<?> altInterface : altInterfaces) {
                        lambda = lambdaWithOneAltInterface(interfaceMethod, interfaceClass, i, flagsIndex, altInterface);
                        names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                        numOfCreatedLambdas++;
                    }

                    lambda = lambdaWithMultipleAltInterfaces(interfaceMethod, interfaceClass, i, flagsIndex);
                    names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                    numOfCreatedLambdas++;
                }
            }
        }
    }

    private static Object lambdaWithOneAltMethod(String interfaceMethod, Class<?> interfaceClass, int index, int flagsIndex, MethodType altMethod) throws Throwable {
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodTypes[index], methodHandles[index], methodTypes[index], flags[flagsIndex], 1, altMethod).getTarget().invoke();
    }

    private static Object lambdaWithMultipleAltMethods(String interfaceMethod, Class<?> interfaceClass, int flagsIndex) throws Throwable {
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodTypes[1], methodHandles[1], methodTypes[1], flags[flagsIndex], 2, altMethodsMethod2[0], altMethodsMethod2[1]).getTarget().invoke();
    }

    private static void createLambdasWithAltMethods(int flagsIndex) throws Throwable {
        for (String interfaceMethod : interfaceMethods) {
            for (Class<?> interfaceClass : interfaces) {
                lambda = lambdaWithOneAltMethod(interfaceMethod, interfaceClass, 0, flagsIndex, altMethodsMethod1[0]);
                names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                numOfCreatedLambdas++;

                for (MethodType altMethod : altMethodsMethod2) {
                    lambda = lambdaWithOneAltMethod(interfaceMethod, interfaceClass, 1, flagsIndex, altMethod);
                    names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                    numOfCreatedLambdas++;
                }

                lambda = lambdaWithMultipleAltMethods(interfaceMethod, interfaceClass, flagsIndex);
            }
        }
    }

    private static Object lambdaWithOneAltInterfaceAndOneAltMethod(String interfaceMethod, Class<?> interfaceClass, int index, int flagsIndex, Class<?> altInterface, MethodType altMethod) throws Throwable {
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodTypes[index], methodHandles[index], methodTypes[index], flags[flagsIndex], 1, altInterface, 1, altMethod).getTarget().invoke();
    }

    private static Object lambdaWithOneAltInterfaceAndMultipleAltMethods(String interfaceMethod, Class<?> interfaceClass, int flagsIndex, Class<?> altInterface) throws Throwable {
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass),
                methodTypes[1], methodHandles[1], methodTypes[1], flags[flagsIndex], 1, altInterface, 2, altMethodsMethod2[0], altMethodsMethod2[1]).getTarget().invoke();
    }

    private static Object lambdaWithMultipleAltInterfaceAndMultipleAltMethods(String interfaceMethod, Class<?> interfaceClass, int flagsIndex) throws Throwable {
        return LambdaMetafactory.altMetafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass), methodTypes[1], methodHandles[1], methodTypes[1],
                flags[flagsIndex], 2, altInterfaces[0], altInterfaces[1], 2, altMethodsMethod2[0], altMethodsMethod2[1]).getTarget().invoke();
    }
    private static void createLambdasWithAltInterfacesAndAltMethods(int flagsIndex) throws Throwable {
        for (String interfaceMethod : interfaceMethods) {
            for (Class<?> interfaceClass : interfaces) {
                for (Class<?> altInterface : altInterfaces) {
                    lambda = lambdaWithOneAltInterfaceAndOneAltMethod(interfaceMethod, interfaceClass, 0, flagsIndex, altInterface, altMethodsMethod1[0]);
                    names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                    numOfCreatedLambdas++;
                    lambda = lambdaWithOneAltInterfaceAndMultipleAltMethods(interfaceMethod, interfaceClass, flagsIndex, altInterface);
                    names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                    numOfCreatedLambdas++;

                    for (MethodType altMethod : altMethodsMethod2) {
                        lambda = lambdaWithOneAltInterfaceAndOneAltMethod(interfaceMethod, interfaceClass, 1, flagsIndex, altInterface, altMethod);
                        names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                        numOfCreatedLambdas++;
                    }
                }
                lambda = lambdaWithMultipleAltInterfaceAndMultipleAltMethods(interfaceMethod, interfaceClass, flagsIndex);
                names.add(removeHashFromLambdaName(lambda.getClass().getName()));
                numOfCreatedLambdas++;
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        Set<String> savedNames = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader("stableLambdaNames.txt"));
        for(String line; (line = br.readLine()) != null; ) {
            savedNames.add(line);
        }

        methodHandles = new MethodHandle[]{lookup.findStatic(TestStableLambdaName.class, "method1", methodTypes[0]),
                lookup.findStatic(TestStableLambdaName.class, "method2", methodTypes[1])};

        numOfCreatedLambdas = 0;

        // All lambdas with flags 0
        createPlainLambdas(0);

        // All lambdas with flags 1
        createPlainLambdas(1);

        // All lambdas with flags 2
        createLambdasWithAltInterfaces(2);

        // All lambdas with flags 3
        createLambdasWithAltInterfaces(4);

        // All lambdas with flags 4
        createLambdasWithAltMethods(3);

        // All lambdas with flags 5
        createLambdasWithAltMethods(5);

        // All lambdas with flags 6
        createLambdasWithAltInterfacesAndAltMethods(6);

        // All lambdas with flags 7
        createLambdasWithAltInterfacesAndAltMethods(7);

        // All the names must be unique
        assert names.size() == numOfCreatedLambdas;

        assert savedNames.size() == names.size();

        assert savedNames.containsAll(names);
    }
}
