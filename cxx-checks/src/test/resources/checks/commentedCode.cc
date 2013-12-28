#include <stdio.h>
#include <tchar.h>

class Program
{
    // This line is fine, but the following is not
    //int Id = 0;

    /*
    The following line is bad

    void Test() { int dirId = this.Id++; }

    No violation on the following line, because there is at most one violation per comment
    MyMethod();
    */

    int _tmain(int argc, _TCHAR* argv[])
    {
        return 0;
    }

    /// <summary>
    /// Handles the task exception.
    /// </summary>
    /// <example>
    /// <code>
    /// Task.Factory.StartNew (() => { throw null; }).IgnoreExceptions();
    /// </code>
    /// </example>
    /// <param name="task">The task.</param>
    void foo()
    {
    }
};

#if DEBUG
#endif
