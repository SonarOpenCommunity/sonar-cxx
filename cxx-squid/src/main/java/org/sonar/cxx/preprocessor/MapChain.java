/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.preprocessor;

import java.util.HashMap;
import java.util.Map;

public class MapChain<K,V>{
  private Map<K,V> highPrioMap = new HashMap<K,V>();
  private Map<K,V> lowPrioMap = new HashMap<K,V>();
  private Map<K,V> highPrio_disabled = new HashMap<K,V>();
  private Map<K,V> lowPrio_disabled = new HashMap<K,V>();

  public V get(Object key){
    V value = highPrioMap.get(key);
    return value != null ? value : lowPrioMap.get(key);
  }

  public V putHighPrio(K key, V value){
    return highPrioMap.put(key, value);
  }

  public V putLowPrio(K key, V value){
    return lowPrioMap.put(key, value);
  }

  public void clearLowPrio(){
    lowPrioMap.clear();
  }

  public void disable(K key){
    move(key, lowPrioMap, lowPrio_disabled);
    move(key, highPrioMap, highPrio_disabled);
  }

  public void enable(K key){
    move(key, lowPrio_disabled, lowPrioMap);
    move(key, highPrio_disabled, highPrioMap);
  }

  private void move(K key, Map<K,V> from, Map<K,V> to){
    V value = from.remove(key);
    if(value != null){
      to.put(key, value);
    }
  }
}


 //  void clear(){}
 //  boolean containsKey(Object key){ return false; }
 //  boolean containsValue(Object value)
 //          Returns true if this map maps one or more keys to the specified value.
 // Set<Map.Entry<K,V>>	entrySet()
 //          Returns a Set view of the mappings contained in this map.
 // boolean	equals(Object o)
 //          Compares the specified object with this map for equality.
 // V	get(Object key)
 //          Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
 // int	hashCode()
 //          Returns the hash code value for this map.
 // boolean	isEmpty()
 //          Returns true if this map contains no key-value mappings.
 // Set<K>	keySet()
 //          Returns a Set view of the keys contained in this map.
 // V	put(K key, V value)
 //          Associates the specified value with the specified key in this map (optional operation).
 // void	putAll(Map<? extends K,? extends V> m)
 //          Copies all of the mappings from the specified map to this map (optional operation).
 // V	remove(Object key)
 //          Removes the mapping for a key from this map if it is present (optional operation).
 // int	size()
 //          Returns the number of key-value mappings in this map.
 // Collection<V>	values()
 //          Returns a Collection view of the values contained in this map.
 //          Returns true if this map contains a mapping for the specified key.
