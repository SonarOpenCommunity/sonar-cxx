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
/**
 * Provides a basic framework to sequentially read any kind of character stream in order to feed a generic OUTPUT.
 *
 * This framework can used for instance in order to :
 * <ul>
 *   <li>Create a lexer in charge to generate a list of tokens from a character stream</li>
 *   <li>Create a source code syntax highligther in charge to decorate a source code with HTML tags</li>
 *   <li>Create a javadoc generator</li>
 *   <li>...</li>
 * </ul>
 *
 * The entry point of this framework is the {@link org.sonar.cxx.sslr.channel.ChannelDispatcher} class.
 * This class must be initialized with a {@link org.sonar.cxx.sslr.channel.CodeReader} and a list of {@link org.sonar.cxx.sslr.channel.Channel}.
 *
 * The {@link org.sonar.cxx.sslr.channel.CodeReader} encapsulates any character stream in order to provide all mechanisms to Channels
 * in order to look ahead and look behind the current reading cursor position.
 *
 * A {@link org.sonar.cxx.sslr.channel.Channel} is in charge to consume the character stream through the CodeReader in order to feed
 * the OUTPUT.
 *
 * @since 1.20
 */
package org.sonar.cxx.sslr.channel;

