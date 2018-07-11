// https://gcc.gnu.org/onlinedocs/gcc-3.0.1/cpp_3.html#SEC31

#define A0 A0
#if A0
#endif

#define B1 A1
#define A1 B1
#if A1
#endif

#define C2 B2
#define B2 C2
#define A2 B2
#if A2
#endif

#define x (4 + y)
#define y (2 * x)
#define NULL 0
#if NULL
#elif y
#endif

#if NULL
#elif x
#endif

// macros are not affected by recursive evaluation
// this is here for completeness only
#define MACRO_FUNC0( A, B ) MACRO_FUNC( A, B )
#define MACRO_FUNC( A, B ) MACRO_FUNC0( A, B )

int main() {
	MACRO_FUNC( 1, 2 );
}
