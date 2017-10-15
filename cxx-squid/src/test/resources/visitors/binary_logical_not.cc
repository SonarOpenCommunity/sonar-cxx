bool logical_not(bool a, bool b, bool c)
{
    if (a          // +1 for `if`
        &&         // +1
        !(b && c)) // +1
    {
        return true;
    }
    return false;
}
