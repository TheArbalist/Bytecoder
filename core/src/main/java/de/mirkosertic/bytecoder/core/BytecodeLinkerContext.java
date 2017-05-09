/*
 * Copyright 2017 Mirko Sertic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mirkosertic.bytecoder.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BytecodeLinkerContext {

    private final Map<BytecodeObjectTypeRef, BytecodeLinkedClass> linkedClasses;
    private final BytecodeLoader loader;
    private final BytecodeMethodCollection methodCollection;

    public BytecodeLinkerContext(BytecodeLoader aLoader) {
        linkedClasses = new HashMap<>();
        loader = aLoader;
        methodCollection = new BytecodeMethodCollection();
    }

    public BytecodeMethodCollection getMethodCollection() {
        return methodCollection;
    }

    public BytecodeLinkedClass linkClass(BytecodeObjectTypeRef aTypeRef) {

        BytecodeLinkedClass theLinkedClass = linkedClasses.get(aTypeRef);
        if (theLinkedClass != null) {
            return theLinkedClass;
        }

        try {
            BytecodeLinkedClass theParentClass = null;
            BytecodeClass theLoadedClass = loader.loadByteCode(aTypeRef);
            BytecodeClassinfoConstant theSuperClass = theLoadedClass.getSuperClass();
            if (theSuperClass != BytecodeClassinfoConstant.OBJECT_CLASS) {
                BytecodeUtf8Constant theSuperClassName = theSuperClass.getConstant();
                theParentClass = linkClass(new BytecodeObjectTypeRef(theSuperClassName.stringValue().replace("/", ".")));
            }

            theLinkedClass = new BytecodeLinkedClass(theParentClass, this, aTypeRef, theLoadedClass);
            linkedClasses.put(aTypeRef, theLinkedClass);

            for (BytecodeInterface theInterface : theLoadedClass.getInterfaces()) {
                BytecodeUtf8Constant theSuperClassName = theInterface.getClassinfoConstant().getConstant();
                linkClass(new BytecodeObjectTypeRef(theSuperClassName.stringValue().replace("/", ".")));
            }

            return theLinkedClass;
        } catch (Exception e) {
            throw new RuntimeException("Error linking class " + aTypeRef.name(), e);
        }
    }

    public void linkClassMethod(BytecodeObjectTypeRef aTypeRef, String aMethodName, BytecodeMethodSignature aSignature) {
        linkClass(aTypeRef).linkStaticMethod(aMethodName, aSignature);
    }

    public void linkConstructorInvocation(BytecodeObjectTypeRef aTypeRef, String aMethodName, BytecodeMethodSignature aSignature) {
        linkClass(aTypeRef).linkStaticMethod(aMethodName, aSignature);
    }

    public void linkVirtualMethod(BytecodeObjectTypeRef aTypeRef, String aMethodName, BytecodeMethodSignature aSignature) {
        linkClass(aTypeRef).linkVirtualMethod(aMethodName, aSignature);
    }

    public void forEachClass(Consumer<Map.Entry<BytecodeObjectTypeRef, BytecodeLinkedClass>> aConsumer) {
        linkedClasses.entrySet().forEach(aConsumer);
    }

    private List<BytecodeLinkedClass> findLinkedClassWithParent(BytecodeLinkedClass aParent) {
        List<BytecodeLinkedClass> theResult = new ArrayList<>();
        linkedClasses.entrySet().forEach(aEntry -> {
            if (aEntry.getValue().getSuperClass() == aParent) {
                theResult.add(aEntry.getValue());
            }
        });
        return theResult;
    }

    public void propagateVirtualMethodsAndFields(BytecodeLinkedClass aClass) {

        aClass.propagateVirtualMethodsAndFields();

        List<BytecodeLinkedClass> theClasses = findLinkedClassWithParent(aClass);
        for (BytecodeLinkedClass theEntry : theClasses) {
            propagateVirtualMethodsAndFields(theEntry);
        }
    }

    public void propagateVirtualMethodsAndFields() {
        List<BytecodeLinkedClass> theClasses = findLinkedClassWithParent(null);
        for (BytecodeLinkedClass theEntry : theClasses) {
            propagateVirtualMethodsAndFields(theEntry);
        }
    }

    public void linkTypeRef(BytecodeTypeRef aTypeRef) {
        if (aTypeRef.isVoid()) {
            return;
        }
        if (aTypeRef.isPrimitive()) {
            return;
        }
        if (aTypeRef.isArray()) {
            BytecodeArrayTypeRef theArray = (BytecodeArrayTypeRef) aTypeRef;
            linkTypeRef(theArray.getType());
            return;
        }
        BytecodeObjectTypeRef theTypeRef = (BytecodeObjectTypeRef) aTypeRef;
        linkClass(theTypeRef);
    }
}