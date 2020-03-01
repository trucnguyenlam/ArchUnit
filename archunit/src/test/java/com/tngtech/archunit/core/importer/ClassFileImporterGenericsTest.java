package com.tngtech.archunit.core.importer;

import java.io.Closeable;
import java.io.File;
import java.io.Serializable;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaTypeVariable;
import com.tngtech.archunit.core.importer.testexamples.generics.ClassWithSingleTypeParameterWithMultipleSimpleClassAndInterfaceBounds;
import com.tngtech.archunit.core.importer.testexamples.generics.ClassWithSingleTypeParameterWithSimpleClassBound;
import com.tngtech.archunit.core.importer.testexamples.generics.ClassWithSingleTypeParameterWithSimpleInterfaceBound;
import com.tngtech.archunit.core.importer.testexamples.generics.ClassWithSingleTypeParameterWithoutBound;
import com.tngtech.archunit.core.importer.testexamples.generics.ClassWithThreeTypeParametersWithMultipleSimpleClassAndInterfaceBounds;
import com.tngtech.archunit.core.importer.testexamples.generics.ClassWithThreeTypeParametersWithSimpleClassBounds;
import com.tngtech.archunit.core.importer.testexamples.generics.ClassWithThreeTypeParametersWithoutBounds;
import com.tngtech.archunit.testutil.ArchConfigurationRule;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.tngtech.archunit.testutil.Assertions.assertThat;
import static com.tngtech.archunit.testutil.Assertions.assertThatType;
import static com.tngtech.archunit.testutil.Assertions.assertThatTypes;

public class ClassFileImporterGenericsTest {

    @Rule
    public final ArchConfigurationRule configurationRule = new ArchConfigurationRule().resolveAdditionalDependenciesFromClassPath(false);

    @Test
    public void imports_empty_list_of_type_parameters_for_non_generic_class() {
        JavaClass javaClass = new ClassFileImporter().importClass(getClass());

        assertThat(javaClass.getTypeParameters()).as("type parameters of non generic class").isEmpty();
    }

    @Test
    public void imports_single_generic_type_parameter_of_class() {
        JavaClass javaClass = new ClassFileImporter().importClasses(ClassWithSingleTypeParameterWithoutBound.class, Object.class)
                .get(ClassWithSingleTypeParameterWithoutBound.class);

        JavaTypeVariable typeVariable = getOnlyElement(javaClass.getTypeParameters());

        assertThat(typeVariable.getName()).as("type variable name").isEqualTo("T");
        assertThatTypes(typeVariable.getBounds()).as("type variable bounds").matchExactly(Object.class);
    }

    @Test
    public void imports_multiple_generic_type_parameters_of_class() {
        JavaClass javaClass = new ClassFileImporter().importClass(ClassWithThreeTypeParametersWithoutBounds.class);

        assertThatType(javaClass).hasTypeParameters("A", "B", "C");
    }

    @Test
    public void imports_simple_class_bound_of_type_variable() {
        JavaClasses classes = new ClassFileImporter().importClasses(ClassWithSingleTypeParameterWithSimpleClassBound.class, String.class);

        JavaClass javaClass = classes.get(ClassWithSingleTypeParameterWithSimpleClassBound.class);

        assertThatType(javaClass).hasOnlyTypeParameter("T").withBoundsMatching(String.class);
    }

    @Test
    public void imports_single_simple_class_bounds_of_multiple_type_variables() {
        JavaClasses classes = new ClassFileImporter().importClasses(ClassWithThreeTypeParametersWithSimpleClassBounds.class,
                String.class, System.class, File.class);

        JavaClass javaClass = classes.get(ClassWithThreeTypeParametersWithSimpleClassBounds.class);

        assertThatType(javaClass)
                .hasTypeParameters("A", "B", "C")
                .hasTypeParameter("A").withBoundsMatching(String.class)
                .hasTypeParameter("B").withBoundsMatching(System.class)
                .hasTypeParameter("C").withBoundsMatching(File.class);
    }

    @Test
    public void imports_simple_interface_bound_of_single_type_variable() {
        JavaClasses classes = new ClassFileImporter().importClasses(ClassWithSingleTypeParameterWithSimpleInterfaceBound.class, Serializable.class);

        JavaClass javaClass = classes.get(ClassWithSingleTypeParameterWithSimpleInterfaceBound.class);

        assertThatType(javaClass).hasOnlyTypeParameter("T").withBoundsMatching(Serializable.class);
    }

    @Test
    public void imports_multiple_simple_bounds_of_single_type_variable() {
        JavaClasses classes = new ClassFileImporter().importClasses(ClassWithSingleTypeParameterWithMultipleSimpleClassAndInterfaceBounds.class,
                String.class, Serializable.class, Runnable.class);

        JavaClass javaClass = classes.get(ClassWithSingleTypeParameterWithMultipleSimpleClassAndInterfaceBounds.class);

        assertThatType(javaClass).hasOnlyTypeParameter("T").withBoundsMatching(String.class, Serializable.class, Runnable.class);
    }

    @Test
    public void imports_multiple_simple_bounds_of_multiple_type_variables() {
        JavaClasses classes = new ClassFileImporter().importClasses(ClassWithThreeTypeParametersWithMultipleSimpleClassAndInterfaceBounds.class,
                String.class, Serializable.class, System.class, Runnable.class, File.class, Serializable.class, Closeable.class);

        JavaClass javaClass = classes.get(ClassWithThreeTypeParametersWithMultipleSimpleClassAndInterfaceBounds.class);

        assertThatType(javaClass)
                .hasTypeParameters("A", "B", "C")
                .hasTypeParameter("A").withBoundsMatching(String.class, Serializable.class)
                .hasTypeParameter("B").withBoundsMatching(System.class, Runnable.class)
                .hasTypeParameter("C").withBoundsMatching(File.class, Serializable.class, Closeable.class);
    }
}
