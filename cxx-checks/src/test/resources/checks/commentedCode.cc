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

/** @defgroup group1 The First Group
 *  This is the first group
 *  @{
 */

/** @brief class C1 in group 1 */
class C1 {};

/** @brief class C2 in group 1 */
class C2 {};

/** function in group 1 */
void func() {}

/** @} */ // end of group1

/**
 *  @defgroup group2 The Second Group
 *  This is the second group
 */

/** @defgroup group3 The Third Group
 *  This is the third group
 */

/** @defgroup group4 The Fourth Group
 *  @ingroup group3
 *  Group 4 is a subgroup of group 3
 */

/**
 *  @ingroup group2
 *  @brief class C3 in group 2
 */
class C3 {};

/** @ingroup group2
 *  @brief class C4 in group 2
 */
class C4 {};

/** @ingroup group3
 *  @brief class C5 in @link group3 the third group@endlink.
 */
class C5 {};

/** @ingroup group1 group2 group3 group4
 *  namespace N1 is in four groups
 *  @sa @link group1 The first group@endlink, group2, group3, group4 
 *
 *  Also see @ref mypage2
 */
namespace N1 {};

/** @file
 *  @ingroup group3
 *  @brief this file in group 3
 */

/** @defgroup group5 The Fifth Group
 *  This is the fifth group
 *  @{
 */

/** @page mypage1 This is a section in group 5
 *  Text of the first section
 */

/** @page mypage2 This is another section in group 5
 *  Text of the second section
 */

/** @} */ // end of group5

/** @addtogroup group1
 *  
 *  More documentation for the first group.
 *  @{
 */

/** another function in group 1 */
void func2() {}

/** yet another function in group 1 */
void func3() {}

/** @} */ // end of group1

/** A class. Details */
class Memgrp_Test
{
  public:
    //@{
    /** Same documentation for both members. Details */
    void func1InGroup1();
    void func2InGroup1();
    //@}

    /** Function without group. Details. */
    void ungroupedFunction();
    void func1InGroup2();
  protected:
    void func2InGroup2();
};

void Memgrp_Test::func1InGroup1() {}
void Memgrp_Test::func2InGroup1() {}

/** @name Group2
 *  Description of group 2. 
 */
///@{
/** Function 2 in group 2. Details. */
void Memgrp_Test::func2InGroup2() {}
/** Function 1 in group 2. Details. */
void Memgrp_Test::func1InGroup2() {}
///@}

/*! \file 
 *  docs for this file
 */

//!@{
//! one description for all members of this group 
//! (because DISTRIBUTE_GROUP_DOC is YES in the config file)
#define A 1
#define B 2
void glob_func();
//!@}


int main( int argc, char * argv[] )
{
    return 0;
}

