#include <stdio.h>
#include <string>

#if DEBUG
#endif

class Program
{
    // This line is fine, but the following is not
    //int Id = 0;

    /*
    The following line is bad

    void Test() { int dirId = Id++; }

    No violation on the following line, because there is at most one violation per comment
    MyMethod();
    */

    /// <summary>
    /// Create task.
    /// </summary>
    /// <example>
    /// <code>
    /// Task* TaskFactory::create_task(const std::string& type, map<string, string> vMap){...}
    /// </code>
    /// </example>
    /// <param name="task">The task.</param>

    /** This is some doxygen documentation
     * @{
     * - List item 1;
     * - List item 2;
     * @}
     * @code
     * void myFunction() { return 0; }
     * @endcode
     */

    /*! Alternative doxygen syntax
     * void myFunction() { return 0; }
     */

    //! This is as well: void myFunction() { return 0; }

    void foo()
    {
    }
};

int main( int argc, char * argv[] )
{
    return 0;
}




