module;                        // global module fragment

#include <vector>              // headers for libraries not modularized so far

export module math;            // module declartion

import <other> // importing of other modules

//<non-exported declarations>  // names with only visibiliy inside the module

export namespace math {
    //<exported declarations>  // exported names
}
