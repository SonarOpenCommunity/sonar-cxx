/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2025 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package com.sonar.cxx.sslr.impl.typed;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.CheckForNull;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

class InterceptorTest {

  public static class Target extends BaseTarget {

    private final Object p;

    public Target(Object p) {
      this.p = p;
    }

    public Object m() {
      return "m()";
    }

    public Object overloaded() {
      return "overloaded()";
    }

    public Object overloaded(Object p) {
      return "overloaded(" + p + ")";
    }

    @Override
    public Object overridden() {
      return "Target.overridden()";
    }

    private Object privateMethod() {
      return "privateMethod()";
    }

    Object packageLocalMethod() {
      return "packageLocalMethod()";
    }
  }

  public static class BaseTarget {

    @SuppressWarnings("unused")
    public Object overridden() {
      return "BaseTarget.overridden()";
    }

    public Object base() {
      return "base()";
    }
  }

  private boolean intercept = false;
  private final ArrayList<Method> interceptedMethods = new ArrayList<>();
  private final MethodInterceptor methodInterceptor = method -> {
    interceptedMethods.add(method);
    return intercept;
  };
  private final Target interceptedTarget = (Target) Interceptor.create(
    Target.class,
    new Class[]{Object.class},
    new Object[]{"arg"},
    methodInterceptor
  );

  @Test
  void shouldInvokeConstructor() {
    assertThat(interceptedTarget.p).isEqualTo("arg");
  }

  @Test
  void shouldIntercept() {
    assertThat(interceptedTarget.m()).isEqualTo("m()");
    assertThat(interceptedMethods).hasSize(1);

    intercept = true;
    assertThat(interceptedTarget.m()).isNull();
    assertThat(interceptedMethods).hasSize(2);
  }

  @Test
  void shouldInterceptOverloadedMethods() {
    assertThat(interceptedTarget.overloaded()).isEqualTo("overloaded()");
    assertThat(interceptedMethods).hasSize(1);

    assertThat(interceptedTarget.overloaded("arg")).isEqualTo("overloaded(arg)");
    assertThat(interceptedMethods).hasSize(2);
  }

  @Test
  void shouldInterceptOverriddenMethods() {
    assertThat(interceptedTarget.overridden()).isEqualTo("Target.overridden()");
    assertThat(interceptedMethods).hasSize(1);
  }

  @Test
  void shouldInterceptBaseMethods() {
    assertThat(interceptedTarget.base()).isEqualTo("base()");
    assertThat(interceptedMethods).hasSize(1);
  }

  /**
   * Can not intercept non-public methods, but should not fail in their presence, because SonarTSQL uses private helper
   * methods.
   */
  @Test
  void canNotInterceptNonPublicMethods() {
    assertThat(interceptedTarget.privateMethod()).isEqualTo("privateMethod()");
    assertThat(interceptedTarget.packageLocalMethod()).isEqualTo("packageLocalMethod()");
    assertThat(interceptedMethods).isEmpty();

    assertThat(Arrays.stream(interceptedTarget.getClass().getDeclaredMethods())
      .map(Method::getName)
      .sorted()
      .toList())
      .isEqualTo(Arrays.asList("base", "m", "overloaded", "overloaded", "overridden"));
  }

  @Test
  void requiresClassToBePublic() {
    var thrown = catchThrowableOfType(IllegalAccessError.class,
      () -> Interceptor.create(NonPublicClass.class, new Class<?>[]{}, new Object[]{}, methodInterceptor)
    );
    assertThat(thrown)
      // Note that details of the message are different between JDK versions
      .hasMessageStartingWith(
        "class GeneratedBySSLR cannot access its superclass com.sonar.cxx.sslr.impl.typed.InterceptorTest$NonPublicClass");
  }

  private static class NonPublicClass {
  }

  /**
   * @see #can_not_intercept_non_public_methods()
   */
  @Test
  void requiresFinalMethodsToBeNonPublic() {
    var thrown = catchThrowableOfType(IncompatibleClassChangeError.class,
      () -> Interceptor.create(PublicFinalMethod.class, new Class[]{}, new Object[]{}, methodInterceptor)
    );
    assertThat(thrown)
      // Note that details of the message are different between JDK versions
      .hasMessageStartingWith("class GeneratedBySSLR overrides final method");
  }

  public static class PublicFinalMethod {

    @SuppressWarnings("unused")
    @CheckForNull
    public final Object m() {
      return null;
    }
  }

  @Test
  void requiresNonPrimitiveReturnTypes() {
    var thrown = catchThrowableOfType(UnsupportedOperationException.class,
      () -> Interceptor.create(PrimitiveReturnType.class, new Class[]{}, new Object[]{}, methodInterceptor)
    );
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  public static class PrimitiveReturnType {

    @SuppressWarnings("unused")
    public void m() {
    }
  }

  @Test
  void shouldUseClassLoaderOfInterceptedClass() throws Exception {
    var cv = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    cv.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Target", null, "java/lang/Object", null);
    var mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    mv.visitInsn(Opcodes.RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "m", "()Ljava/lang/String;", null, null);
    mv.visitLdcInsn("m()");
    mv.visitInsn(Opcodes.ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    var classBytes = cv.toByteArray();

    Class<?> cls = new ClassLoader() {
      public Class<?> defineClass() {
        return defineClass("Target", classBytes, 0, classBytes.length);
      }
    }.defineClass();

    var interceptedTarget = Interceptor.create(cls, new Class[]{}, new Object[]{}, methodInterceptor);
    assertThat(interceptedTarget.getClass().getMethod("m").invoke(interceptedTarget)).isEqualTo("m()");
    assertThat(interceptedMethods).hasSize(1);
  }

}
