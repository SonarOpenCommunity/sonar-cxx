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
package org.sonar.cxx.channels;

import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import org.sonar.channel.Channel;
import org.sonar.channel.CodeReader;
import org.sonar.cxx.api.CxxTokenType;

public class PreprocessorChannel extends Channel<Lexer> {
  private static final char EOF = (char) -1;

  @Override
  public boolean consume(CodeReader code, Lexer output) {
    int line = code.getLinePosition();
    int column = code.getColumnPosition();

    char ch = code.charAt(0);
    if ((ch != '#')) {
      return false;
    }

    String tokenValue = read(code);
    CxxTokenType tokenType = classify(tokenValue);

    output.addToken(Token.builder()
        .setLine(line)
        .setColumn(column)
        .setURI(output.getURI())
        .setValueAndOriginalValue(tokenValue)
        .setType(tokenType)
        .build());
    
    return true;
  }

  private String read(CodeReader code) {
    StringBuilder sb = new StringBuilder();
    char ch;
    
    while (true) {
      ch = (char) code.pop();
      if (isNewline(ch) || ch == EOF) {
        break;
      }
      if (ch == '\\' && isNewline((char) code.peek())) {
        // the newline is escaped: we have a the multi line preprocessor directive
        // consume both the backslash and the newline, insert a space instead
        consumeNewline(code);
        sb.append(' ');
        ch = (char) code.pop();
      }
      sb.append(ch);
    }
    return sb.toString();
  }


  private static void consumeNewline(CodeReader code) {
    if ((code.charAt(0) == '\r') && (code.charAt(1) == '\n')) {
      // \r\n
      code.pop();
      code.pop();
    } else {
      // \r or \n
      code.pop();
    }
  }

  private static boolean isNewline(char ch) {
    return (ch == '\n') || (ch == '\r');
  }

  private CxxTokenType classify(String tokenValue){
    CxxTokenType type = CxxTokenType.PREPROCESSOR;
    if(tokenValue.length() >= 4){
      String prefix = tokenValue.substring(1, 4);
      if(prefix.equals("def")){
        type = CxxTokenType.PREPROCESSOR_DEFINE;
      } else if(prefix.equals("inc")){
        type = CxxTokenType.PREPROCESSOR_INCLUDE;
      } else if(prefix.equals("ifd")){
        type = CxxTokenType.PREPROCESSOR_IFDEF;
      } else if(prefix.equals("if ") || prefix.equals("if(")){
        type = CxxTokenType.PREPROCESSOR_IF;
      } else if(prefix.equals("ifn")){
        type = CxxTokenType.PREPROCESSOR_IFNDEF;
      } else if(prefix.equals("els")){
        type = CxxTokenType.PREPROCESSOR_ELSE;
      } else if(prefix.equals("end")){
        type = CxxTokenType.PREPROCESSOR_ENDIF;
      }
    }
    
    return type;
  }
}
