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
package com.sonar.cxx.sslr.api.typed;

import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A container object which may or may not contain a non-null value. * Loosely modeled after {@link java.util.Optional}.
 *
 * @param <T> type of the Optional value
 *
 * @since 1.21
 */
public abstract class Optional<T> {

  /**
   * Returns an Absent instance.
   *
   * @param <T> the type of the value
   * @return returns an Absent instance
   */
  @SuppressWarnings("unchecked")
  public static <T> Optional<T> absent() {
    return (Optional<T>) Absent.INSTANCE;
  }

  /**
   * Returns an Optional describing the given non-null value.
   *
   * @param <T> the type of the value
   * @param reference the value to describe, which must be non-null
   * @return an Optional with the value present
   */
  public static <T> Optional<T> of(@Nonnull T reference) {
    return new Present<>(Objects.requireNonNull(reference));
  }

  /**
   * If a value is present, returns true, otherwise false.
   *
   * @return true if a value is present, otherwise false
   */
  public abstract boolean isPresent();

  /**
   * If a value is present, returns the value, otherwise throws IllegalStateException.
   *
   * @return the non-null value described by this Optional
   */
  public abstract T get();

  /**
   * If a value is present, returns an Optional describing the value, otherwise returns an Optional with the default
   * value.
   *
   * @param defaultValue produces an Optional to be returned
   * @return returns an Optional describing the value of this Optional, if a value is present, otherwise an Optional
   * with the default value
   */
  public abstract T or(T defaultValue);

  /**
   * If a value is present, returns an Optional describing the value, otherwise returns null.
   *
   * @return if a value is present, returns an Optional describing the value, otherwise returns null.
   */
  @CheckForNull
  public abstract T orNull();

  private static class Present<T> extends Optional<T> {

    private final T reference;

    public Present(T reference) {
      this.reference = reference;
    }

    @Override
    public boolean isPresent() {
      return true;
    }

    @Override
    public T get() {
      return reference;
    }

    @Override
    public T or(@Nonnull Object defaultValue) {
      Objects.requireNonNull(defaultValue, "use orNull() instead of or(null)");
      return reference;
    }

    @CheckForNull
    @Override
    public T orNull() {
      return reference;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (obj != null && getClass() == obj.getClass()) {
        var other = (Present) obj;
        return reference.equals(other.reference);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return 0x598df91c + reference.hashCode();
    }

    @Override
    public String toString() {
      return "Optional.of(" + reference + ")";
    }
  }

  private static class Absent extends Optional<Object> {

    private static final Absent INSTANCE = new Absent();

    @Override
    public boolean isPresent() {
      return false;
    }

    @Override
    public Object get() {
      throw new IllegalStateException("value is absent");
    }

    @Override
    public Object or(@Nonnull Object defaultValue) {
      return Objects.requireNonNull(defaultValue, "use orNull() instead of or(null)");
    }

    @CheckForNull
    @Override
    public Object orNull() {
      return null;
    }

    @Override
    public boolean equals(@Nullable Object object) {
      return object == this;
    }

    @Override
    public int hashCode() {
      return 0x598df91c;
    }

    @Override
    public String toString() {
      return "Optional.absent()";
    }
  }

}
