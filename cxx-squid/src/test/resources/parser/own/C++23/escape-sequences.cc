// simple escape sequences
auto s1  = '\'';
auto s2  = '\"';
auto s3  = '\?';
auto s4  = '\\';
auto s5  = '\a';
auto s6  = '\b';
auto s7  = '\f';
auto s8  = '\n';
auto s9  = '\r';
auto s10 = '\t';
auto s11 = '\v';

// numeric escape sequences
auto n1 = '\123';
auto n2 = '\o{12345}';
auto n3 = '\xABCDEF';
auto n4 = '\x{ABFF}';

// universal character names
auto u1 = '\u1234';
auto u2 = '\u{112233FF}';
auto u3 = '\U12345678';
auto u4 = '\N{NULL}';
