package com.tngtech.archunit.testutil.assertion;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.domain.JavaTypeVariable;
import org.assertj.core.api.AbstractObjectAssert;
import org.objectweb.asm.Type;

import static com.tngtech.archunit.base.Guava.toGuava;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.testutil.TestUtils.namesOf;
import static com.tngtech.archunit.testutil.assertion.JavaAnnotationAssertion.propertiesOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;

public class JavaTypeAssertion extends AbstractObjectAssert<JavaTypeAssertion, JavaType> {
    private static final Pattern ARRAY_PATTERN = Pattern.compile("(\\[+)(.*)");

    public JavaTypeAssertion(JavaType javaType) {
        super(javaType, JavaTypeAssertion.class);
    }

    public void matches(Class<?> clazz) {
        JavaClass javaClass = actualClass();

        assertThat(javaClass.getName()).as("Name of " + javaClass)
                .isEqualTo(clazz.getName());
        assertThat(javaClass.getSimpleName()).as("Simple name of " + javaClass)
                .isEqualTo(ensureArrayName(clazz.getSimpleName()));
        assertThat(javaClass.getPackage().getName()).as("Package of " + javaClass)
                .isEqualTo(getExpectedPackageName(clazz));
        assertThat(javaClass.getPackageName()).as("Package name of " + javaClass)
                .isEqualTo(getExpectedPackageName(clazz));
        assertThat(javaClass.getModifiers()).as("Modifiers of " + javaClass)
                .isEqualTo(JavaModifier.getModifiersForClass(clazz.getModifiers()));
        assertThat(propertiesOf(javaClass.getAnnotations())).as("Annotations of " + javaClass)
                .isEqualTo(propertiesOf(clazz.getAnnotations()));
    }

    public JavaTypeAssertion hasTypeParameters(String... names) {
        assertThat(namesOf(actualClass().getTypeParameters())).as("names of type parameters").containsExactly(names);
        return this;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // checked via AssertJ
    public JavaTypeVariableOfClassAssertion hasTypeParameter(String name) {
        List<JavaTypeVariable> typeVariables = actualClass().getTypeParameters();

        Optional<JavaTypeVariable> variable = FluentIterable.from(typeVariables).firstMatch(toGuava(name(name)));
        assertThat(variable).as("Type variable with name '%s'", name).isPresent();

        return new JavaTypeVariableOfClassAssertion(variable.get(), this);
    }

    public JavaTypeVariableOfClassAssertion hasOnlyTypeParameter(String name) {
        assertThat(actualClass().getTypeParameters()).as("Type parameters").hasSize(1);
        return hasTypeParameter(name);
    }

    private JavaClass actualClass() {
        assertThat(actual).isInstanceOf(JavaClass.class);
        return (JavaClass) actual;
    }

    private String ensureArrayName(String name) {
        String suffix = "";
        Matcher matcher = ARRAY_PATTERN.matcher(name);
        if (matcher.matches()) {
            name = Type.getType(matcher.group(2)).getClassName();
            suffix = Strings.repeat("[]", matcher.group(1).length());
        }
        return name + suffix;
    }

    public static String getExpectedPackageName(Class<?> clazz) {
        if (!clazz.isArray()) {
            return clazz.getPackage() != null ? clazz.getPackage().getName() : "";
        }
        return getExpectedPackageName(clazz.getComponentType());
    }
}
