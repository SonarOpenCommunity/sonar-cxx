// Example from https://www.sonarsource.com/docs/CognitiveComplexity.pdf version 1.2 page 9

std::string getWords(int number) {
   switch (number) { // +1
      case 1:
         return "one";
      case 2:
         return "a couple";
      case 3:
         return "a few";
      default:
         return "lots";
    }
} // Cognitive Complexity 1
