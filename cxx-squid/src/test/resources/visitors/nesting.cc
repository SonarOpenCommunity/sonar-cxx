int func3(int a, int b) {
    if( a ) {                   // +1
        if( b ) {               // +2 (nesting = 1)
            return a+b ? 1 : 2; // +3 (nesting = 2)
        } else {                // +1
            return a+b ? 3 : 4; // +3 (nesting = 2)
        }
    } else {                    // +1
        if( b ) {               // +2 (nesting = 1)
            return a+b ? 5 : 6; // +3 (nesting = 2)
        } else {                // +1
            return a+b ? 7 : 8; // +3 (nesting = 2)
        }
    }
}
