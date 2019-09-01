void main()
{
    asm("movl %ecx %eax"); /* moves the contents of ecx to eax */
    __asm__("movb %bh (%eax)"); /*moves the byte from bh to the memory pointed by eax */

    __asm__ ("movl %eax, %ebx\n\t"
             "movl $56, %esi\n\t"
             "movl %ecx, $label(%edx,%ebx,$4)\n\t"
             "movb %ah, (%ebx)");
              
    // Qualifiers
    asm volatile ( "movl %ecx %eax" )
    asm __volatile__ ( "movl %ecx %eax" )
    asm inline ( "movl %ecx %eax" )
    __asm__ volatile ( "movl %ecx %eax" )
    __asm__ __inline__ ( "movl %ecx %eax" )
    __asm__ inline ( "movl %ecx %eax" )
    
    // Extended Asm
    int a=10, b;
    asm ("movl %1, %%eax; 
          movl %%eax, %0;"
          :"=r"(b)        /* output */
          :"r"(a)         /* input */
          :"%eax"         /* clobbered register */
        );          
}

// Asm Labels:

// Assembler names for data:
int foo1 asm ("myfoo1") = 2;
int foo2 __asm__ ("myfoo2") = 2;
extern const char cert_start1[] asm("_binary_firmware_pho_by_crt_start1");
extern const char cert_start2[] __asm__("_binary_firmware_pho_by_crt_start2");

// Assembler names for functions:
int func2 (int x, int y) asm ("MYFUNC1");
int func3 (int x, int y) __asm__ ("MYFUNC2");

// support workaround
#define __asm__ asm
void func()
{
    __asm__("movl %ecx %eax");
}
