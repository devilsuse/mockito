/*
 * Copyright (c) 2020 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.util.reflection;

import net.bytebuddy.ClassFileVersion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.plugins.MemberAccessor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(Parameterized.class)
public class MemberAccessorTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[] {new ReflectionMemberAccessor()});
        if (ClassFileVersion.ofThisVm().isAtLeast(ClassFileVersion.JAVA_V9)) {
            data.add(new Object[] {new ModuleMemberAccessor()});
        }
        return data;
    }

    private final MemberAccessor accessor;

    public MemberAccessorTest(MemberAccessor accessor) {
        this.accessor = accessor;
    }

    @Test
    public void test_read_field() throws Exception {
        assertThat(accessor.get(Sample.class.getDeclaredField("test"), new Sample("foo")))
                .isEqualTo("foo");
    }

    @Test
    public void test_write_field() throws Exception {
        Sample sample = new Sample("foo");
        accessor.set(Sample.class.getDeclaredField("test"), sample, "bar");
        assertThat(sample.test).isEqualTo("bar");
    }

    @Test
    public void test_invoke() throws Exception {
        assertThat(
                        accessor.invoke(
                                Sample.class.getDeclaredMethod("test", String.class),
                                new Sample(null),
                                "foo"))
                .isEqualTo("foo");
    }

    @Test
    public void test_invoke_invocation_error() {
        assertThatThrownBy(
                        () ->
                                accessor.invoke(
                                        Sample.class.getDeclaredMethod("test", String.class),
                                        new Sample(null),
                                        "exception"))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    public void test_new_instance() throws Exception {
        assertThat(accessor.newInstance(Sample.class.getDeclaredConstructor(String.class), "foo"))
                .isInstanceOf(Sample.class);
    }

    @Test
    public void test_new_instance_invocation_error() {
        assertThatThrownBy(
                        () ->
                                accessor.newInstance(
                                        Sample.class.getDeclaredConstructor(String.class),
                                        "exception"))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(RuntimeException.class);
    }

    private static class Sample {

        private String test;

        public Sample(String test) {
            if ("exception".equals(test)) {
                throw new RuntimeException();
            }
            this.test = test;
        }

        private String test(String value) {
            if ("exception".equals(value)) {
                throw new RuntimeException();
            }
            return value;
        }
    }
}
