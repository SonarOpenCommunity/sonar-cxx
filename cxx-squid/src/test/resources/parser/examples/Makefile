include Makefile.h

all::

CODE_DIRS = util stl cont iter fo algo string num io i18n memory
all::
	@for DIR in $(CODE_DIRS); \
        do \
            (cd $$DIR; make all) \
        done
clean::
	@for DIR in $(CODE_DIRS); \
        do \
            (cd $$DIR; make clean) \
        done

