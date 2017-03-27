//------------------------------------
bits unixtime1(bits ld, bits ex)
{
    ld &= 0xFF;
    ld -= 51;
    if (ex < 1855547904U) ld--;
    ex -= 1855548004U;
    return ex / 100 + 42949673U * ld - ld / 25;
}

bits unixtime2(bits ld, bits ex)
{
    if (ld && ex)
    {
        ld &= 0xFF;
        ld -= 51;
        if (ex < 1855547904U) ld--;
        ex -= 1855548004U;
        return ex / 100 + 42949673U * ld - ld / 25;
    }
    return 0;
}

//------------------------------------
int acorntime(bits *ex, bits *ld, time_t utime)
{
    unsigned timlo;      /* 3 lower bytes of acorn file-time plus carry byte */
    unsigned timhi;      /* 2 high bytes of acorn file-time */

    timlo = ((unsigned)utime & 0x00ffffffU) * 100 + 0x00996a00U;
    timhi = ((unsigned)utime >> 24);
    timhi = timhi * 100 + 0x0000336eU + (timlo >> 24);
    if (timhi & 0xffff0000U)
        return 1;        /* calculation overflow, do not change time */

                         /* insert the five time bytes into loadaddr and execaddr variables */
    *ex = (timlo & 0x00ffffffU) | ((timhi & 0x000000ffU) << 24);
    *ld = (*ld & 0xffffff00U) | ((timhi >> 8) & 0x000000ffU);

    return 0;            /* subject to future extension to signal overflow */
}

//------------------------------------
int object_exists1(char *fn)
{
    int ob;
    if (xosfile_read_stamped_no_path(fn, &ob, 0, 0, 0, 0, 0)) return 0;
    switch (ob)
    {
    case osfile_IS_FILE:return 1;
    case osfile_IS_DIR:return 1;
    case osfile_IS_IMAGE:return 1;
    }
    return 0;
}

int object_exists2(char *fn)
{
    int ob;
    if (xosfile_read_stamped_no_path(fn, &ob, 1, 1, 1, 1, 1)) return 1;
    switch (ob)
    {
    case osfile_IS_FILE:return 2;
    case osfile_IS_DIR:return 2;
    case osfile_IS_IMAGE:return 2;
    }
    return 0;
}

//------------------------------------
char* tostring1(int value)
{
    switch (value)
    {
    case 0: return "zero";
    case 1: return "one";
    }
    return "undefined";
}

char* tostring2(int value)
{
    switch (value)
    {
    case 2: return "two";
    case 3: return "three";
    }
    return "undefined";
}

// CPD should ignore declarations
class A {
public:
   virtual void a();
   virtual void b();
   virtual void c();
};

class B : public A {
public:
   virtual void a();
   virtual void b();
   virtual void c();
};
