/*
 * Copyright (c) 2017, 2024 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.inject.cdi.se;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javax.ws.rs.core.GenericType;

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.inject.cdi.se.bean.BeanHelper;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.process.internal.RequestScoped;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests that {@link DisposableSupplier} is properly processed by {@link BeanHelper}.
 *
 * @author Petr Bouda
 */
@Vetoed
public class DisposableSupplierTest {

    private static final Type DISPOSABLE_SUPPLIER_TYPE =
            new GenericType<DisposableSupplier<String>>() {}.getType();
    private static final Type PROXIABLE_DISPOSABLE_SUPPLIER_TYPE =
            new GenericType<DisposableSupplier<ProxiableHolder>>() {}.getType();
    private static final Type SUPPLIER_TYPE = new GenericType<Supplier<String>>() {}.getType();

    private InjectionManager injectionManager;

    @BeforeEach
    public void setup() {
        injectionManager = BindingTestHelper.createInjectionManager();
    }

    @AfterEach
    public void teardown() {
        injectionManager.shutdown();
    }

    @Test
    public void testBindSingletonClassDisposableSupplier() {
        BindingTestHelper.bind(
                injectionManager, binder ->  binder.bindFactory(DisposableSupplierImpl.class, Singleton.class).to(String.class));

        Object supplier = injectionManager.getInstance(SUPPLIER_TYPE);
        Object disposableSupplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertNotNull(supplier);
        assertNotNull(disposableSupplier);
        assertSame(supplier, disposableSupplier);
    }

    @Test
    public void testBindPerLookupClassDisposableSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class).to(String.class));

        Object supplier = injectionManager.getInstance(SUPPLIER_TYPE);
        Object disposableSupplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertNotNull(supplier);
        assertNotNull(disposableSupplier);
        assertNotSame(supplier, disposableSupplier);
    }

    @Test
    public void testBindInstanceDisposableSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new DisposableSupplierImpl()).to(String.class));

        Object supplier = injectionManager.getInstance(SUPPLIER_TYPE);
        Object disposableSupplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertNotNull(supplier);
        assertNotNull(disposableSupplier);
        assertSame(supplier, disposableSupplier);
    }

    @Test
    public void testNotBindClassDisposableSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(SupplierGreeting.class).to(String.class));
        assertNull(injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE));
    }

    @Test
    public void testNotBindInstanceDisposableSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new SupplierGreeting()).to(String.class));
        assertNull(injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE));
    }

    @Test
    public void testOnlyIncrementSingletonSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance1).get());
        Object instance2 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("2", ((DisposableSupplier<?>) instance2).get());
        Object instance3 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("3", ((DisposableSupplier<?>) instance3).get());
    }

    @Test
    public void testOnlyIncrementInstanceSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new DisposableSupplierImpl())
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance1).get());
        Object instance2 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("2", ((DisposableSupplier<?>) instance2).get());
        Object instance3 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("3", ((DisposableSupplier<?>) instance3).get());
    }

    @Test
    public void testOnlyIncrementPerLookupSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class)
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance1).get());
        Object instance2 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance2).get());
        Object instance3 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        assertEquals("1", ((DisposableSupplier<?>) instance3).get());
    }

    @Test
    public void testOnlyIncrementSingletonInstances() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(String.class);
        assertEquals("1", instance1);
        Object instance2 = injectionManager.getInstance(String.class);
        assertEquals("2", instance2);
        Object instance3 = injectionManager.getInstance(String.class);
        assertEquals("3", instance3);
    }

    @Test
    public void testOnlyIncrementInstanceInstance() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(new DisposableSupplierImpl())
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(String.class);
        assertEquals("1", instance1);
        Object instance2 = injectionManager.getInstance(String.class);
        assertEquals("2", instance2);
        Object instance3 = injectionManager.getInstance(String.class);
        assertEquals("3", instance3);
    }

    @Test
    public void testOnlyIncrementPerLookupInstance() {
        BindingTestHelper
                .bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class)
                        .to(String.class));

        Object instance1 = injectionManager.getInstance(String.class);
        assertEquals("1", instance1);
        Object instance2 = injectionManager.getInstance(String.class);
        assertEquals("1", instance2);
        Object instance3 = injectionManager.getInstance(String.class);
        assertEquals("1", instance3);
    }

    @Test
    public void testDisposeSingletonSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
                        .to(String.class));

        // 1-1
        DisposableSupplier<String> supplier1 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String instance1 = supplier1.get();
        // 2-2
        DisposableSupplier<String> supplier2 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String instance2 = supplier2.get();
        // 3-3
        DisposableSupplier<String> supplier3 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        supplier3.get();
        // 2-2
        supplier1.dispose(instance1);
        // 1-1
        supplier2.dispose(instance2);
        // 2-2
        Supplier<String> supplier4 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String result = supplier4.get();
        assertEquals("2", result);
    }

    @Test
    public void testDisposePerLookupSupplier() {
        BindingTestHelper.bind(injectionManager, binder -> binder.bindFactory(DisposableSupplierImpl.class)
                .to(String.class));

        // 1
        DisposableSupplier<String> supplier1 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String instance1 = supplier1.get();
        // 1
        DisposableSupplier<String> supplier2 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String instance2 = supplier2.get();
        // 1
        DisposableSupplier<String> supplier3 =
                injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        supplier3.get();
        // 0
        supplier1.dispose(instance1);
        // 0
        supplier2.dispose(instance2);
        // 1
        Supplier<String> supplier4 = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
        String result = supplier4.get();
        assertEquals("1", result);
    }

    @Test
    public void testDisposeSingletonSupplierRequestScopedInstance() {
        BindingTestHelper.bind(injectionManager, binder -> {
                    binder.bindFactory(ProxiableDisposableSupplierImpl.class, Singleton.class)
                            .to(ProxiableHolder.class)
                            .in(RequestScoped.class);
                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<ProxiableHolder>> atomicSupplier = new AtomicReference<>();
        request.runInScope(() -> {
            // Save Singleton Supplier for later check that the instance was disposed.
            Supplier<ProxiableHolder> supplier = injectionManager.getInstance(PROXIABLE_DISPOSABLE_SUPPLIER_TYPE);
            atomicSupplier.set(supplier);

            // All instances should be the same because they are request scoped.
            ProxiableHolder instance1 = injectionManager.getInstance(ProxiableHolder.class);
            assertEquals("1", instance1.getValue());
            ProxiableHolder instance2 = injectionManager.getInstance(ProxiableHolder.class);
            assertEquals("1", instance2.getValue());
        });

        Supplier<ProxiableHolder> cleanedSupplier = atomicSupplier.get();
        // Next should be 1-1
        assertEquals("1", cleanedSupplier.get().getValue());
    }

    /**
     * Tests that object created in request scope is disposing at the time of ending the scope.
     */
    @Test
    public void testDisposePerLookupSupplierRequestScopedInstance() {
        BindingTestHelper.bind(injectionManager, binder -> {
                    binder.bindFactory(ProxiableDisposableSupplierImpl.class)
                            .to(ProxiableHolder.class)
                            .in(RequestScoped.class);
                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<ProxiableHolder>> atomicSupplier = new AtomicReference<>();
        request.runInScope(() -> {
            // Save Singleton Supplier for later check that the instance was disposed.
            Supplier<ProxiableHolder> supplier = injectionManager.getInstance(PROXIABLE_DISPOSABLE_SUPPLIER_TYPE);
            atomicSupplier.set(supplier);

            // All instances should be the same because they are request scoped.
            ProxiableHolder instance1 = injectionManager.getInstance(ProxiableHolder.class);
            assertEquals("1", instance1.getValue());
            ProxiableHolder instance2 = injectionManager.getInstance(ProxiableHolder.class);
            assertEquals("1", instance2.getValue());
        });

        Supplier<ProxiableHolder> cleanedSupplier = atomicSupplier.get();
        // Next should be 1
        assertEquals("1", cleanedSupplier.get().getValue());
    }

    /**
     * Tests that inherited request scoped is also cleaned by disposing the objects.
     */
    @Test
    public void testDisposeSingletonSupplierMultiRequestScoped() {
        BindingTestHelper.bind(injectionManager, binder -> {
                    binder.bindFactory(ProxiableDisposableSupplierImpl.class)
                            .to(ProxiableHolder.class)
                            .in(RequestScoped.class);
                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<ProxiableHolder>> firstSupplier = new AtomicReference<>();
        AtomicReference<Supplier<ProxiableHolder>> secondSupplier = new AtomicReference<>();

        request.runInScope(() -> {
            Supplier<ProxiableHolder> supplier1 = injectionManager.getInstance(PROXIABLE_DISPOSABLE_SUPPLIER_TYPE);
            firstSupplier.set(supplier1);

            ProxiableHolder instance1 = injectionManager.getInstance(ProxiableHolder.class);
            assertEquals("1", instance1.getValue());

            request.runInScope(() -> {
                // Save Singleton Supplier for later check that the instance was disposed.
                Supplier<ProxiableHolder> supplier2 = injectionManager.getInstance(PROXIABLE_DISPOSABLE_SUPPLIER_TYPE);
                secondSupplier.set(supplier2);

                ProxiableHolder instance2 = injectionManager.getInstance(ProxiableHolder.class);
                // 1-2 because the same static class is used in inherited runInScope
                assertEquals("1", instance2.getValue());
            });
        });

        Supplier<ProxiableHolder> cleanedSupplier1 = firstSupplier.get();
        Supplier<ProxiableHolder> cleanedSupplier2 = secondSupplier.get();
        // Next should be 1-1
        assertEquals("1", cleanedSupplier1.get().getValue());
        // 1-2 because the same static class is used but the instance is cleaned.
        assertEquals("1", cleanedSupplier2.get().getValue());
    }

    /**
     * PerLookup fields are not disposed therefore they should never be used as a DisposedSupplier because the field stay in
     * {@link org.glassfish.jersey.inject.cdi.se.bean.SupplierClassBean} forever.
     */
    @Test
    public void testDisposeComposedObjectWithPerLookupFields() {
        BindingTestHelper.bind(injectionManager, binder -> {
                    binder.bindFactory(DisposableSupplierImpl.class, Singleton.class)
                            .to(String.class);

                    binder.bindAsContract(ComposedObject.class)
                            .in(RequestScoped.class);
                });

        RequestScope request = injectionManager.getInstance(RequestScope.class);
        AtomicReference<Supplier<String>> atomicSupplier = new AtomicReference<>();
        request.runInScope(() -> {
            // Save Singleton Supplier for later check that the instance was disposed.
            Supplier<String> supplier = injectionManager.getInstance(DISPOSABLE_SUPPLIER_TYPE);
            atomicSupplier.set(supplier);

            // All instances should be the same because they are request scoped.
            ComposedObject instance = injectionManager.getInstance(ComposedObject.class);
            Set<String> set1 = new HashSet<String>() {{
                add("1");
                add("2");
                add("3");
            }};
            Set<String> set2 = new HashSet<String>() {{
                add(instance.getFirst().toString());
                add(instance.getSecond().toString());
                add(instance.getThird().toString());
            }};
            assertEquals(set1, set2);
        });

        Supplier<String> cleanedSupplier = atomicSupplier.get();
        // Next should be 1 - all instances are disposed and decremented back
        assertEquals("1", cleanedSupplier.get());
    }

    @Vetoed
    private static class ComposedObject {

        @Inject
        String first;

        @Inject
        String second;

        @Inject
        String third;

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }

        public String getThird() {
            return third;
        }
    }

    @Vetoed
    private static class DisposableSupplierImpl implements DisposableSupplier<String> {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public String get() {
            // Create a new string - don't share the instances in the string pool.
            return new String(counter.incrementAndGet() + "");
        }

        @Override
        public void dispose(final String instance) {
            counter.decrementAndGet();
        }
    }

    @Vetoed
    private static class ProxiableDisposableSupplierImpl implements DisposableSupplier<ProxiableHolder> {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public ProxiableHolder get() {
            // Create a new string - don't share the instances in the string pool.
            return new ProxiableHolder(counter.incrementAndGet() + "");
        }

        @Override
        public void dispose(ProxiableHolder instance) {
            counter.decrementAndGet();
        }
    }

    @Vetoed
    private static class ProxiableHolder {
        private String value;

        public ProxiableHolder(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
