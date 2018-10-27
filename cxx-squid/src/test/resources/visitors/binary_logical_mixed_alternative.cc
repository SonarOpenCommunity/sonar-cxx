// same as binary_logical_mixed.cc but with alternative logical operators 'and' and 'or'
bool logical_mixed(bool a, bool b, bool c, bool d, bool e, bool f)
{
    if (a           // +1 for `if`
        and b and c // +1
        or d or e   // +1
        and f)      // +1
    {
        return true;
    }
    return false;
}
