auto DeduceReturnType(); // Return type to be determined.

auto Correct(int i) {
    if (i == 1)
        return i;               // return type deduced as int
    else
        return Correct(i-1)+i;  // ok to call it now
}
