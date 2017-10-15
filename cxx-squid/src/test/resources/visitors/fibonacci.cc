int fibonacci(int n)  {
    if (n == 0) {                 // +1
        return 0;
    }
    else if (n == 1) {            // +1
        return 1;
    }
    else {                        // +1
        return fibonacci(n - 1) + // +1
            fibonacci(n - 2);     // +1
    }
}
