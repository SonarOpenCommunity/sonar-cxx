struct Unresolved;

int knownField = 42;

void useIt() {
    Unresolved u;
    u.knownField = 1;
}
