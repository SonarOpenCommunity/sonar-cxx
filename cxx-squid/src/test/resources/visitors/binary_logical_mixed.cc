bool logical_mixed(bool a, bool b, bool c, bool d, bool e, bool f)
{
    if (a         // +1 for `if`
        && b && c // +1
        || d || e // +1
        && f)     // +1
    {
        return true;
    }
    return false;
}
