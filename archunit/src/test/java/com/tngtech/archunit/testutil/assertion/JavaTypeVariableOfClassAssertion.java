package com.tngtech.archunit.testutil.assertion;

import com.tngtech.archunit.core.domain.JavaTypeVariable;
import org.assertj.core.api.AbstractObjectAssert;

import static com.tngtech.archunit.testutil.Assertions.assertThatTypeVariable;

public class JavaTypeVariableOfClassAssertion extends AbstractObjectAssert<JavaTypeVariableOfClassAssertion, JavaTypeVariable> {
    private final JavaTypeAssertion backlink;

    JavaTypeVariableOfClassAssertion(JavaTypeVariable actual, JavaTypeAssertion backlink) {
        super(actual, JavaTypeVariableOfClassAssertion.class);
        this.backlink = backlink;
    }

    public JavaTypeAssertion withBoundsMatching(Class<?>... bounds) {
        assertThatTypeVariable(actual).hasBoundsMatching(bounds);
        return backlink;
    }
}
