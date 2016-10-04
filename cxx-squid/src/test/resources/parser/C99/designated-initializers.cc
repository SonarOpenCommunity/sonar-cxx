//C-COMPATIBILITY: C99 designated initializers
struct Point { int x, y; };
Point p = { .y = 45, .x = 72 };
int a[6] = { [4] = 29, [2] = 15 };
int widths[200] = { [0 ... 9] = 1, [10 ... 99] = 2, [100] = 3 };

// In the following example, the designator is .any_member and the designated initializer is .any_member = 13:
union { /* … */ } caw = { .any_member = 13 };

// The following example shows how the second and third members b and c of structure variable klm are initialized with designated initializers:
struct xyz {
       int a;
       int b;
       int c;
    } klm = { .a = 99, .c = 100 };

// In the following example, the third and second elements of the one-dimensional array aa are initialized to 3 and 6, respectively:
int aa[4] = { [2] = 3, [1] = 6 };

// The following example initializes the first four and last four elements, while omitting the middle four:
static short grid[3] [4] = { [0][0]=8, [0][1]=6,
                             [0][2]=4, [0][3]=1,
                             [2][0]=9, [2][1]=3,
                             [2][2]=1, [2][3]=1 };
                             
// Designated initializers can be combined with regular initializers, as in the following example:
int a[10] = { 2, 4, [8]=9, 10 };

// In the following example, a single designator is used to "allocate" space from both ends of an array:
int a[MAX] = { 1, 3, 5, 7, 9, [MAX-5] = 8, 6, 4, 2, 0 };

// You can also use designators to represent members of nested structures. For example:
struct a {
   struct b {
      int c;
      int d;
   } e;
   float f;
} g = { .e.c = 3 };
