// asm_overview.cpp
// processor: x86
void main()
{
    // Naked functions must provide their own prolog...
    __asm {
        push ebp
        mov ebp, esp
        sub esp, __LOCAL_SIZE
    }

    // ... and epilog
    __asm {
        pop ebp
        ret
    }
    
    __asm push ebp;
    __asm mov  ebp, esp;
    __asm sub  esp, __LOCAL_SIZE;
}

// support workaround
#define __asm asm
void func()
{
    __asm push ebp;
}
