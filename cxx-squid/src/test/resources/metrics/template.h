/**
 testClass
*/
template<typename T>
class testClass
{
public:
	void publicMethod();

	int publicAttr;

    // Issue #928 (should be marked undocumented)
    using type = void;
};

template<typename Second>
struct second_struct {
    /*!
     * \brief Issue #928 (should be marked documented)
     */
    using alpha = float;

    /*!
     * \brief Issue #928 (should be marked documented)
     */
    using beta = double;
};

/*!
 * \brief issue #736
 */
template <typename T>
using intrinsic_type = typename intrinsic_traits<T>::intrinsic_type;

template <typename T>
using inline_intrinsic_type = typename intrinsic_traits<T>::intrinsic_type; ///< issue #736

/**
 * @brief cascaded templateDeclaration doc (issue #1000)
 */
template <class U>
template <class T>
void TestClass::functionTest();

/**
 * @brief issue #1025
 */
template <class U>
class TestClass2 {
public:

   /**
    * @brief DOCUMENTATION FUNCTION
    */
   template <class T>
   void functionTest2();
};

/**
 * @brief DOCUMENTATION
 */
template <class U>
template <class T>
void TestClass2::functionTest2()
{
}

/**
 * @brief issue #1067
 */
struct A {
   template<class T> class B;
};
