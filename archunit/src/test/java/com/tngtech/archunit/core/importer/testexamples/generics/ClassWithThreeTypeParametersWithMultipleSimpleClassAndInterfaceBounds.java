package com.tngtech.archunit.core.importer.testexamples.generics;

import java.io.Closeable;
import java.io.File;
import java.io.Serializable;

@SuppressWarnings("unused")
public class ClassWithThreeTypeParametersWithMultipleSimpleClassAndInterfaceBounds<
        A extends String & Serializable, B extends System & Runnable, C extends File & Serializable & Closeable> {
}
