
This directory contains automated smoke tests for a build of the Sonar
C++ Community Plugin. This testsuite will check if the current build
(the .jar archive) does basically work (can be installed, can be
booted with, can run a basic analysis etc.) with Sonar versions you
care about.

= Preconditions =
- Download and install all Sonar version which the plugin version
  has to support
- copy the sitedef:
  $ cp sitedefs_sample.py sitedefs.py
  and adjust its content to fit your site (SONAR_HOME, SONAR_VERSIONS
  etc.)  to fit your site

- install the requests lib (using something like 'pip install
  requests' )

- optional: install colorama (using something like 'pip install
  colorama' ) to get colorized output


== Usage ==
Just run smoketests in a shell.
