

// BOOST_PP_STRINGIZE(boost/mpl/aux_/preprocessed/AUX778076_PREPROCESSED_HEADER)'
// ERROR: Undefined functionlike macro 'BOOST_PP_ARRAY_SIZE' assuming 0

//#define _MSC_VER 1900
#define _MSC_VER 1600

#define BOOST_COMPILER_CONFIG "boost/config/compiler/visualc.hpp"

#define BOOST_MPL_PREPROCESSED_HEADER apply_wrap.hpp
#define BOOST_MPL_CFG_COMPILER_DIR plain


# /* BOOST_PP_CONFIG_FLAGS */
#
# define BOOST_PP_CONFIG_STRICT() 0x0001
# define BOOST_PP_CONFIG_IDEAL() 0x0002
#
# define BOOST_PP_CONFIG_MSVC() 0x0004
# define BOOST_PP_CONFIG_MWCC() 0x0008
# define BOOST_PP_CONFIG_BCC() 0x0010
# define BOOST_PP_CONFIG_EDG() 0x0020
# define BOOST_PP_CONFIG_DMC() 0x0040
#
# ifndef BOOST_PP_CONFIG_FLAGS
#    if defined(__GCCXML__)
#        define BOOST_PP_CONFIG_FLAGS() (BOOST_PP_CONFIG_STRICT())
#    elif defined(__WAVE__)
#        define BOOST_PP_CONFIG_FLAGS() (BOOST_PP_CONFIG_STRICT())
#    elif defined(__MWERKS__) && __MWERKS__ >= 0x3200
#        define BOOST_PP_CONFIG_FLAGS() (BOOST_PP_CONFIG_STRICT())
#    elif defined(__EDG__) || defined(__EDG_VERSION__)
#        if defined(_MSC_VER) && (defined(__INTELLISENSE__) || __EDG_VERSION__ >= 308)
#            define BOOST_PP_CONFIG_FLAGS() (BOOST_PP_CONFIG_MSVC())
#        else
#            define BOOST_PP_CONFIG_FLAGS() (BOOST_PP_CONFIG_EDG() | BOOST_PP_CONFIG_STRICT())
#        endif
#    elif defined(__MWERKS__)
#        define BOOST_PP_CONFIG_FLAGS() (BOOST_PP_CONFIG_MWCC())
#    elif defined(__DMC__)
#        define BOOST_PP_CONFIG_FLAGS() (BOOST_PP_CONFIG_DMC())
#    elif defined(__BORLANDC__) && __BORLANDC__ >= 0x581
#        define BOOST_PP_CONFIG_FLAGS() (BOOST_PP_CONFIG_STRICT())
#    elif defined(__BORLANDC__) || defined(__IBMC__) || defined(__IBMCPP__) || defined(__SUNPRO_CC)
#        define BOOST_PP_CONFIG_FLAGS() (BOOST_PP_CONFIG_BCC())
#    elif defined(_MSC_VER) && !defined(__clang__)
#        define BOOST_PP_CONFIG_FLAGS() (BOOST_PP_CONFIG_MSVC())
#    else
#        define BOOST_PP_CONFIG_FLAGS() (BOOST_PP_CONFIG_STRICT())
#    endif
# endif
#
# /* BOOST_PP_CONFIG_EXTENDED_LINE_INFO */
#
# ifndef BOOST_PP_CONFIG_EXTENDED_LINE_INFO
#    define BOOST_PP_CONFIG_EXTENDED_LINE_INFO 0
# endif
#
# /* BOOST_PP_CONFIG_ERRORS */
#
# ifndef BOOST_PP_CONFIG_ERRORS
#    ifdef NDEBUG
#        define BOOST_PP_CONFIG_ERRORS 0
#    else
#        define BOOST_PP_CONFIG_ERRORS 1
#    endif
# endif
#
# /* BOOST_PP_VARIADICS */
#
# define BOOST_PP_VARIADICS_MSVC 0
# if !defined BOOST_PP_VARIADICS
#    /* variadic support explicitly disabled for all untested compilers */
#    if defined __GCCXML__ || defined __CUDACC__ || defined __PATHSCALE__ || defined __DMC__ || defined __CODEGEARC__ || defined __BORLANDC__ || defined __MWERKS__ || ( defined __SUNPRO_CC && __SUNPRO_CC < 0x5130 ) || defined __HP_aCC && !defined __EDG__ || defined __MRC__ || defined __SC__ || defined __IBMCPP__ || defined __PGI
#        define BOOST_PP_VARIADICS 0
#    /* VC++ (C/C++) */
#    elif defined _MSC_VER && _MSC_VER >= 1400 && (!defined __EDG__ || defined(__INTELLISENSE__)) && !defined __clang__
#        define BOOST_PP_VARIADICS 1
#        undef BOOST_PP_VARIADICS_MSVC
#        define BOOST_PP_VARIADICS_MSVC 1
#    /* Wave (C/C++), GCC (C++) */
#    elif defined __WAVE__ && __WAVE_HAS_VARIADICS__ || defined __GNUC__ && defined __GXX_EXPERIMENTAL_CXX0X__ && __GXX_EXPERIMENTAL_CXX0X__
#        define BOOST_PP_VARIADICS 1
#    /* EDG-based (C/C++), GCC (C), and unknown (C/C++) */
#    elif !defined __cplusplus && __STDC_VERSION__ >= 199901L || __cplusplus >= 201103L
#        define BOOST_PP_VARIADICS 1
#    else
#        define BOOST_PP_VARIADICS 0
#    endif
# elif !BOOST_PP_VARIADICS + 1 < 2
#    undef BOOST_PP_VARIADICS
#    define BOOST_PP_VARIADICS 1
#    if defined _MSC_VER && _MSC_VER >= 1400 && (defined(__INTELLISENSE__) || !(defined __EDG__ || defined __GCCXML__ || defined __CUDACC__ || defined __PATHSCALE__ || defined __clang__ || defined __DMC__ || defined __CODEGEARC__ || defined __BORLANDC__ || defined __MWERKS__ || defined __SUNPRO_CC || defined __HP_aCC || defined __MRC__ || defined __SC__ || defined __IBMCPP__ || defined __PGI))
#        undef BOOST_PP_VARIADICS_MSVC
#        define BOOST_PP_VARIADICS_MSVC 1
#    endif
# else
#    undef BOOST_PP_VARIADICS
#    define BOOST_PP_VARIADICS 0
# endif
#

# /* BOOST_PP_STRINGIZE */
#
# if BOOST_PP_CONFIG_FLAGS() & BOOST_PP_CONFIG_MSVC()
#    define BOOST_PP_STRINGIZE(text) BOOST_PP_STRINGIZE_A((text))
#    define BOOST_PP_STRINGIZE_A(arg) BOOST_PP_STRINGIZE_I arg
# elif BOOST_PP_CONFIG_FLAGS() & BOOST_PP_CONFIG_MWCC()
#    define BOOST_PP_STRINGIZE(text) BOOST_PP_STRINGIZE_OO((text))
#    define BOOST_PP_STRINGIZE_OO(par) BOOST_PP_STRINGIZE_I ## par
# else
#    define BOOST_PP_STRINGIZE(text) BOOST_PP_STRINGIZE_I(text)
# endif
#
#
# define BOOST_PP_STRINGIZE_I(text) #text

#if !defined(BOOST_NEEDS_TOKEN_PASTING_OP_FOR_TOKENS_JUXTAPOSING)
#   define AUX778076_PREPROCESSED_HEADER BOOST_MPL_CFG_COMPILER_DIR/BOOST_MPL_PREPROCESSED_HEADER /**/
#else
#   define AUX778076_PREPROCESSED_HEADER BOOST_PP_CAT(BOOST_MPL_CFG_COMPILER_DIR,/)##BOOST_MPL_PREPROCESSED_HEADER /**/
#endif

#if BOOST_WORKAROUND(__IBMCPP__, BOOST_TESTED_AT(700))
#   define AUX778076_INCLUDE_STRING BOOST_PP_STRINGIZE(boost/mpl/aux_/preprocessed/AUX778076_PREPROCESSED_HEADER)
#   include AUX778076_INCLUDE_STRING
#   undef AUX778076_INCLUDE_STRING
#else
#   include BOOST_PP_STRINGIZE(boost/mpl/aux_/preprocessed/AUX778076_PREPROCESSED_HEADER)
#   include BOOST_PP_STRINGIZE(parser/preprocessor/AUX778076_PREPROCESSED_HEADER)
#endif

#   undef AUX778076_PREPROCESSED_HEADER

#undef BOOST_MPL_PREPROCESSED_HEADER
