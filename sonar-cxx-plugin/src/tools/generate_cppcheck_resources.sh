#!/bin/sh
cppcheck --errorlist | python cppcheck_createrules.py rules > cppcheck.xml
cppcheck --errorlist | python cppcheck_createrules.py profile > cppcheck-profile.xml
