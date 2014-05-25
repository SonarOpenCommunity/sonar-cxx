#define MYMACRO log(a,b,c)

#define TRUE 1
#define FALSE 0
#define SWITCH(x) (x!=0)
#define AUTO

#define     NULL 0
class myData {
public:
    long mydata = 0;
};

#define _TRUE 1
#define _FALSE 0
#define _FOO

#define __TRUE 1
#define __FALSE 0
#define FOO__BAR

//These are ok:
#define _test
#define TRUE_TEST
/*#define TRUE this is not an error...*/


// #define _APS_NEXT_COMMAND_VALUE         32771
// #define _APS_NEXT_CONTROL_VALUE         8000
// #define _APS_NEXT_SYMED_VALUE           8000
// #define _APS_NEXT_RESOURCE_VALUE        8000

// #define _ATL_CSTRING_EXPLICIT_CONSTRUCTORS      // some CString constructors will be explicit

int main()
{
}