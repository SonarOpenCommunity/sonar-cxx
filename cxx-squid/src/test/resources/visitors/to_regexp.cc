// Example from https://www.sonarsource.com/docs/CognitiveComplexity.pdf version 1.2 page 17

std::string toRegexp(std::string antPattern, std::string directorySeparator) {
    std::string escapedDirectorySeparator = '\\' + directorySeparator;
    StringBuilder sb = new StringBuilder(antPattern.length());
    sb.append('^');
    int i = antPattern.startsWith("/") ||                   // +1
        antPattern.startsWith("\\") ? 1 : 0;                // +1
    while (i < antPattern.length()) {                       // +1
        char ch = antPattern.charAt(i);
        if (SPECIAL_CHARS.indexOf(ch) != -1) {              // +2 (nesting = 1)
            sb.append('\\').append(ch);
        } else if (ch == '*') {                             // +1
            if (i + 1 < antPattern.length()                 // +3 (nesting = 2)
                && antPattern.charAt(i + 1) == '*') {       // +1
                if (i + 2 < antPattern.length()             // +4 (nesting = 3)
                    && isSlash(antPattern.charAt(i + 2))) { // +1
                    sb.append("(?:.*")
                        .append(escapedDirectorySeparator).append("|)");
                    i += 2;
                } else {                                    // +1
                    sb.append(".*");
                    i += 1;
                }
            } else {                                        // +1
                sb.append("[^").append(escapedDirectorySeparator).append("]*?");
            }
        } else if (ch == '?') {                             // +1
            sb.append("[^").append(escapedDirectorySeparator).append("]");
        } else if (isSlash(ch)) {                           // +1
            sb.append(escapedDirectorySeparator);
        } else {                                            // +1
            sb.append(ch);
        }
        i++;
    }
    sb.append('$');
    return sb.toString();
}                                                          // total complexity = 20
