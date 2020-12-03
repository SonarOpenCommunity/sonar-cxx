/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * MapChain
 *
 * @param <K>
 * @param <V>
 *
 */
public class MapChain<K, V> {

  private final Map<K, V> enabled = new HashMap<>();
  private final Map<K, V> disabled = new HashMap<>();

  /**
   * get
   *
   * @param key
   * @return V
   */
  public V get(Object key) {
    return enabled.get(key);
  }

  /**
   * put
   *
   * @param key
   * @param value
   * @return V
   */
  public V put(K key, V value) {
    return enabled.put(key, value);
  }

  public void putAll(Map<K, V> m) {
    enabled.putAll(m);
  }

  /**
   * remove
   *
   * @param key
   * @return V
   */
  public V remove(K key) {
    return enabled.remove(key);
  }

  /**
   * clear
   */
  public void clear() {
    enabled.clear();
    disabled.clear();
  }

  /**
   * disable
   *
   * @param key
   */
  public void disable(K key) {
    move(key, enabled, disabled);
  }

  /**
   * enable
   *
   * @param key
   */
  public void enable(K key) {
    move(key, disabled, enabled);
  }

  public Map<K, V> getMap() {
    return Collections.unmodifiableMap(enabled);
  }

  private void move(K key, Map<K, V> from, Map<K, V> to) {
    V value = from.remove(key);
    if (value != null) {
      to.put(key, value);
    }
  }

}
