/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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

  private final Map<K, V> highPrioMap = new HashMap<>();
  private final Map<K, V> lowPrioMap = new HashMap<>();
  private final Map<K, V> highPrioDisabled = new HashMap<>();
  private final Map<K, V> lowPrioDisabled = new HashMap<>();
  private boolean isHighPrioEnabled;

  /**
   * get
   *
   * @param key
   * @return V
   */
  public V get(Object key) {
    V value = highPrioMap.get(key);
    return value != null ? value : lowPrioMap.get(key);
  }

  public void setHighPrio(boolean value) {
    isHighPrioEnabled = value;
  }

  /**
   * put
   *
   * @param key
   * @param value
   * @return V
   */
  public V put(K key, V value) {
    if (isHighPrioEnabled) {
      return highPrioMap.put(key, value);
    } else {
      return lowPrioMap.put(key, value);
    }
  }

  /**
   * removeLowPrio
   *
   * @param key
   * @return V
   */
  public V removeLowPrio(K key) {
    return lowPrioMap.remove(key);
  }

  /**
   * clearLowPrio
   */
  public void clearLowPrio() {
    lowPrioMap.clear();
  }

  /**
   * disable
   *
   * @param key
   */
  public void disable(K key) {
    move(key, lowPrioMap, lowPrioDisabled);
    move(key, highPrioMap, highPrioDisabled);
  }

  /**
   * enable
   *
   * @param key
   */
  public void enable(K key) {
    move(key, lowPrioDisabled, lowPrioMap);
    move(key, highPrioDisabled, highPrioMap);
  }

  private void move(K key, Map<K, V> from, Map<K, V> to) {
    V value = from.remove(key);
    if (value != null) {
      to.put(key, value);
    }
  }
}
