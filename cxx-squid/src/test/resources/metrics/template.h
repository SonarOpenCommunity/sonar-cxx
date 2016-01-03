/**
 testClass
*/
template<T>
class testClass
{
public:
	void publicMethod();
	
	int publicAttr;
};

/*!
 * \brief issue #736
 */
template <typename T>
using intrinsic_type = typename intrinsic_traits<T>::intrinsic_type;

template <typename T>
using inline_intrinsic_type = typename intrinsic_traits<T>::intrinsic_type; ///< issue #736
