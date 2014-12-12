#include<stdio.h>

int main()
{
    // this is valid c but invalid c++: 'class' is a keyword in c++ but
    // a perfectly valid variable name in c
    int class;

    int n, i = 3, count, c;

    printf("Enter the number of prime numbers required\n");
    scanf("%d",&n);

    if ( n >= 1 )
    {
        printf("First %d prime numbers are :\n",n);
        printf("2\n");
    }

    for ( count = 2 ; count <= n ;  )
    {
        for ( c = 2 ; c <= i - 1 ; c++ )
        {
            if ( i%c == 0 )
                break;
        }
        if ( c == i )
        {
            printf("%d\n",i);
            count++;
        }
        i++;
    }

    return 0;
}
