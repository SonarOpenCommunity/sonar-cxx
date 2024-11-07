/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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
import java.util.Arrays;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

@SuppressWarnings("java:S1192")
public final class Interceptor {

  private Interceptor() {
  }

  public static Object create(
    Class<?> superClass,
    Class<?>[] constructorParameterTypes,
    Object[] constructorArguments,
    MethodInterceptor interceptor
  ) {
    var cv = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    String className = "GeneratedBySSLR";
    String superClassName = Type.getInternalName(superClass);
    cv.visit(
      Opcodes.V1_8,
      Opcodes.ACC_PUBLIC,
      className,
      null,
      superClassName,
      null);

    cv.visitField(
      Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
      "methodInterceptor",
      Type.getDescriptor(MethodInterceptor.class),
      null,
      null);

    cv.visitField(
      Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
      "methods",
      Type.getDescriptor(Method[].class),
      null,
      null);

    String constructorDescriptor = Type.getMethodDescriptor(
      Type.getType(void.class),
      Arrays.stream(constructorParameterTypes)
        .map(Type::getType)
        .toArray(Type[]::new));
    MethodVisitor mv = cv.visitMethod(
      Opcodes.ACC_PUBLIC,
      "<init>",
      constructorDescriptor,
      null,
      null);
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    for (int i = 0; i < constructorParameterTypes.length; i++) {
      mv.visitVarInsn(Type.getType(constructorParameterTypes[i]).getOpcode(Opcodes.ILOAD), 1 + i);
    }
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
      superClassName,
      "<init>",
      constructorDescriptor,
      false);
    mv.visitInsn(Opcodes.RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    var methods = superClass.getMethods();
    for (int methodId = 0; methodId < methods.length; methodId++) {
      var method = methods[methodId];
      if (Object.class.equals(method.getDeclaringClass())) {
        continue;
      }
      if (method.getReturnType().isPrimitive()) {
        throw new UnsupportedOperationException();
      }
      mv = cv.visitMethod(
        Opcodes.ACC_PUBLIC,
        method.getName(),
        Type.getMethodDescriptor(method),
        null,
        null);

      mv.visitFieldInsn(Opcodes.GETSTATIC,
        className,
        "methodInterceptor",
        Type.getDescriptor(MethodInterceptor.class));

      mv.visitFieldInsn(Opcodes.GETSTATIC,
        className,
        "methods",
        Type.getDescriptor(Method[].class));
      mv.visitLdcInsn(methodId);
      mv.visitInsn(Opcodes.AALOAD);

      mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
        Type.getInternalName(MethodInterceptor.class),
        "intercept",
        Type.getMethodDescriptor(
          Type.getType(boolean.class),
          Type.getType(Method.class)),
        true);
      var label = new Label();
      mv.visitJumpInsn(Opcodes.IFEQ, label);
      mv.visitInsn(Opcodes.ACONST_NULL);
      mv.visitInsn(Opcodes.ARETURN);
      mv.visitLabel(label);
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      Class<?>[] parameterTypes = method.getParameterTypes();
      for (int i = 0; i < method.getParameterCount(); i++) {
        mv.visitVarInsn(Type.getType(parameterTypes[i]).getOpcode(Opcodes.ILOAD), 1 + i);
      }
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
        superClassName,
        method.getName(),
        Type.getMethodDescriptor(method),
        false);
      mv.visitInsn(Opcodes.ARETURN);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }

    var classBytes = cv.toByteArray();

    Class<?> cls = new ClassLoader(superClass.getClassLoader()) {
      public Class<?> defineClass() {
        return defineClass(className, classBytes, 0, classBytes.length);
      }
    }.defineClass();
    Object instance;
    try {
      instance = cls
        .getConstructor(constructorParameterTypes)
        .newInstance(constructorArguments);
      cls.getField("methods")
        .set(instance, methods);
      cls.getField("methodInterceptor")
        .set(instance, interceptor);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
    return instance;
  }

}
