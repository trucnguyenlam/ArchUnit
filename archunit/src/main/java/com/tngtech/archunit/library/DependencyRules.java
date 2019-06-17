/*
 * Copyright 2014-2020 TNG Technology Consulting GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tngtech.archunit.library;

import com.tngtech.archunit.PublicAPI;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.PublicAPI.Usage.ACCESS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@PublicAPI(usage = ACCESS)
public final class DependencyRules {
    private DependencyRules() {
    }

    @PublicAPI(usage = ACCESS)
    public static final ArchRule NO_CLASSES_SHOULD_ACCESS_CLASSES_THAT_RESIDE_IN_AN_UPPER_PACKAGE =
            noClasses().should(accessClassesThatResideInAnUpperPackage());

    @PublicAPI(usage = ACCESS)
    public static ArchCondition<JavaClass> accessClassesThatResideInAnUpperPackage() {
        return new AccessClassesThatResideInAnUpperPackageCondition();
    }

    private static class AccessClassesThatResideInAnUpperPackageCondition extends ArchCondition<JavaClass> {
        AccessClassesThatResideInAnUpperPackageCondition() {
            super("access classes that reside in an upper package");
        }

        @Override
        public void check(final JavaClass clazz, final ConditionEvents events) {
            for (JavaAccess<?> access : clazz.getAccessesFromSelf()) {
                boolean callToSuperPackage = isCallToSuperPackage(access.getOriginOwner(), access.getTargetOwner());
                events.add(new SimpleConditionEvent(access, callToSuperPackage, access.getDescription()));
            }
        }

        private boolean isCallToSuperPackage(JavaClass origin, JavaClass target) {
            String originPackageName = getOutermostEnclosingClass(origin).getPackageName();
            String targetSubPackagePrefix = getOutermostEnclosingClass(target).getPackageName() + ".";
            return originPackageName.startsWith(targetSubPackagePrefix);
        }

        private JavaClass getOutermostEnclosingClass(JavaClass javaClass) {
            while (javaClass.getEnclosingClass().isPresent()) {
                javaClass = javaClass.getEnclosingClass().get();
            }
            return javaClass;
        }
    }
}
