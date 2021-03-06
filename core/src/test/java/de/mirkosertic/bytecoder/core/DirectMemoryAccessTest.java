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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.mirkosertic.bytecoder.classlib.Address;
import de.mirkosertic.bytecoder.classlib.MemoryManager;
import de.mirkosertic.bytecoder.unittest.BytecoderUnitTestRunner;
import de.mirkosertic.bytecoder.unittest.WASMOnly;

@RunWith(BytecoderUnitTestRunner.class)
@WASMOnly
public class DirectMemoryAccessTest {

    @Test
    public void testReadAndWrite() {
        MemoryManager.initWithSize(100);
        Address theAddress = new Address(20);
        Address.setIntValue(theAddress, 5, 10);
        int theStored = Address.getIntValue(theAddress, 5);
        Assert.assertEquals(10, theStored, 0);
    }
}
