package com.tngtech.archunit.core.importer.testexamples.generics;

import java.io.Serializable;

@SuppressWarnings("unused")
public class ClassWithSingleTypeParameterWithSimpleInterfaceBound<T extends Serializable> {
}
