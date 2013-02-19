##################################################################
# Makefile with general settings for the book "The C++ Standard Library"
# - is included by each individual Makefile
# - please send updates and suggestions to libbook@josuttis.com
##################################################################

############################
# GCC settings (general)
############################
GCCFLAGS=-g -ansi -W -Wall -Wwrite-strings -pedantic
CXX = g++
CXXFLAGS = $(GCCFLAGS)
LDFLAGS = -lm


############################
# GCC settings (special)
############################
#GCCFLAGS=-g -ansi -W -Wall -Wwrite-strings -pedantic
#GCCDIR=/local/gcc/rundir
#CXX =  $(GCCDIR)/bin/g++
#CXXFLAGS = $(GCCFLAGS)
#LIBCPPDIR = $(GCCDIR)/lib
#LDFLAGS = -L$(LIBCPPDIR) -Wl,--rpath -Wl,$(LIBCPPDIR) -lm


############################
# EDG (and my personal std headers)
############################
#CXX = /local/edg/bin/eccp --exceptions --strict
#CXXFLAGS = -Imystd -I../mystd
#LDFLAGS = -lm


##################################################################

help::
	@echo "all:    progs"

all:: progs

.SUFFIXES: .ctt .htt .cpp .hpp

.cpp.o:
	$(CXX) $(CXXFLAGS) -c $*.cpp
.o:
	$(CXX) $*.o $(LDFLAGS) -o $*

help::
	@echo 'progs:  create all in $$(CPPPROGS) and in $$(OUTPROGS)'
	@echo "        (failed progs in MAKE.LOG)"
progs::
	@cat /dev/null > MAKE.LOG
	@if test "$(CPPPROGS)" != "" ; \
	then \
	    for PROG in $(CPPPROGS)""; \
	    do \
	        echo "MAKE $$PROG"; \
	        make $$PROG || echo " + make $$PROG failed !!!" >> MAKE.LOG; \
	    done; \
	fi
	@if test "$(OUTPROGS)" != "" ; \
	then \
	    for PROG in $(OUTPROGS)""; \
	    do \
	        echo "MAKE $$PROG"; \
	        make $$PROG || echo " + make $$PROG failed !!!" >> MAKE.LOG; \
	    done; \
	fi
	@if test -s MAKE.LOG ; \
	then \
	    echo "failures:"; \
	    cat MAKE.LOG; \
	else \
	    echo "no failure"; \
	fi

help::
	@echo 'clean:  clean all generated'
clean::
	rm -rf MAKE.LOG *.o *.exe *.ii *.ti *~
	rm -rf $(CPPPROGS) $(OUTPROGS)
	@for DATEI in *.ctt; \
	do \
	    BASE=`basename $$DATEI .ctt`; \
	    if test -r $$BASE.cpp; \
	    then \
	        echo " remove $$BASE.cpp"; \
		rm $$BASE.cpp; \
	    fi; \
	    if test -x $$BASE; \
            then \
                echo " remove $$BASE"; \
                rm $$BASE; \
            fi; \
	done
	@for DATEI in *.cpp; \
	do \
	    BASE=`basename $$DATEI .cpp`; \
	    if test -x $$BASE; \
            then \
                echo " remove $$BASE"; \
                rm $$BASE; \
            fi; \
	done
	@for DATEI in *.htt; \
	do \
            BASE=`basename $$DATEI .htt`; \
            if test -r $$BASE.hpp; \
            then \
                echo " remove $$BASE.hpp"; \
                rm $$BASE.hpp; \
            fi; \
	done

