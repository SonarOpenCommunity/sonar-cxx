/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Container for preprocessor macros:
 * - the container stores all existing macros (put)
 * - the container allows quick search for macros (get)
 *
 * For recursively existing macros:
 * - the container allows to deactivate macros temporarily in the search (pushDisable/popDisable)
 */
public class MacroContainer<K, V> {

  private Map<K, V> values = new HashMap<>();
  private Deque<K> disabled = new ArrayDeque<>();

  /**
   * get value for key.
   *
   * @return if key exists and is not disabled return value, else null
   */
  public V get(K key) {
    V v = values.get(key);
    if ((v != null) && (disabled.isEmpty() || !disabled.contains(key))) {
      return v;
    }

    return null;
  }

  /**
   * Associates the specified value with the specified key in this container. If the container previously contained a
   * mapping for the key, the old value is replaced by the specified new value.
   */
  public V put(K key, V value) {
    return values.put(key, value);
  }

  /**
   * Copies all of the mappings from the specified container to this container. The effect of this call is equivalent to
   * that of calling put(k, v)} on this container once for each mapping in the specified other container.
   */
  public void putAll(MacroContainer<K, V> m) {
    values.putAll(m.values);
    disabled.addAll(m.disabled);
  }

  /**
   * Removes the mapping for a key from this container.
   *
   * @return the previous value associated with key, or null if there was no mapping for key}.
   */
  public V remove(K key) {
    return values.remove(key);
  }

  /**
   * clear container (values & disabled).
   */
  public void clear() {
    values.clear();
    disabled.clear();
  }

  /**
   * Disable key for search with get (put key on disabled stack).
   *
   * Hint: For performance reasons method does not verify if key is in values.
   */
  public void pushDisable(K key) {
    disabled.push(key);
  }

  /**
   * Enable last with pushDisable added key again (remove it from stack).
   *
   * Hint: pushDisable/popDisable calls must be symmetric. popDisable does not do any additional checking.
   */
  public void popDisable() {
    disabled.pop();
  }

  /**
   * Writes the MacroContainer to a file.
   *
   * @param fileName The system-dependent filename.
   * @throws java.io.IOException file cannot be created, or cannot be written for any other reason
   */
  public void writeToFile(String fileName) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(fileName);
         ObjectOutputStream oos = new ObjectOutputStream(fos)) {
      oos.writeObject(values);
      oos.writeObject(disabled);
    }
  }

  /**
   * Reads the MacroContainer from a file.
   *
   * @param fileName The system-dependent filename.
   * @throws java.io.IOException file cannot be opened, or cannot be read for any other reason
   * @throws java.lang.ClassNotFoundException class of a serialized object cannot be found
   */
  public void readFromFile(String fileName) throws IOException, ClassNotFoundException {
    try (FileInputStream fis = new FileInputStream(fileName);
         ObjectInputStream ois = new ObjectInputStream(fis)) {
      values = (Map) ois.readObject();
      disabled = (Deque) ois.readObject();
    }
  }

  /**
   * String of all items of container (including disabled).
   */
  @Override
  public String toString() {
    return values.values().stream()
      .map(Object::toString)
      .collect(Collectors.joining(", ", "[", "]"));
  }

}
