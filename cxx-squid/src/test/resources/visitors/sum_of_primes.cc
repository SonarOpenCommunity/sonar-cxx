// Example from https://www.sonarsource.com/docs/CognitiveComplexity.pdf version 1.2 page 9

int sumOfPrimes(int max) {
   int total = 0;
   for (int i = 1; i <= max; ++i) { // +1
OUT:
      for (int j = 2; j < i; ++j) { // +2 (nesting = 1)
         if (i % j == 0) {          // +3 (nesting = 2)
            goto OUT;               // +1
         }
      }
      total += i;
   }
   return total;
} // Cognitive Complexity 7
