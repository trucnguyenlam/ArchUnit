package com.tngtech.archunit.exampletest;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.example.layers.ClassViolatingCodingRules;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.tngtech.archunit.library.DependencyRules.NO_CLASSES_SHOULD_ACCESS_CLASSES_THAT_RESIDE_IN_AN_UPPER_PACKAGE;

@Category(Example.class)
public class DependencyRulesTest {

    private final JavaClasses classes = new ClassFileImporter().importPackagesOf(ClassViolatingCodingRules.class);

    @Test
    public void no_accesses_to_upper_package() {
        NO_CLASSES_SHOULD_ACCESS_CLASSES_THAT_RESIDE_IN_AN_UPPER_PACKAGE
                .check(classes);
    }
}
