# -*- coding: utf-8 -*-
# SonarQube C++ Community Plugin (cxx plugin)
# Copyright (C) 2010-2021 SonarOpenCommunity
# http://github.com/SonarOpenCommunity/sonar-cxx
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program; if not, write to the Free Software Foundation,
# Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

"""
Script to support the creation of a PEG grammar.
[https://en.wikipedia.org/wiki/Parsing_expression_grammar]

The fundamental difference between context-free grammars and parsing expression grammars is that
the PEG's choice operator is ordered. If the first alternative succeeds, the second alternative
is ignored. The consequence is that if a CFG is transliterated directly to a PEG, any ambiguity
in the former is resolved by deterministically picking one parse tree from the possible parses.

By carefully choosing the order in which the grammar alternatives are specified, a programmer
has a great deal of control over which parse tree is selected. Additional syntax sugar like
'next', 'nextNot' can help.

This script helps to filter the grammar by dependencies and usage or to check if a sequence in
a rule hides another sequence (and the order has to be checked).
"""
import sys
import logging
import argparse
import re
import itertools
from collections import deque

class GrammarParser:
    """
    C++ Grammar Parser.
    """

    keywords = {
        "alignas", "alignof", "asm", "auto", "bool", "break", "case", "catch", "char", "char8_t",
        "char16_t", "char32_t", "class", "const", "concept", "consteval", "constexpr", "constinit",
        "const_cast", "continue", "co_await", "co_return", "co_yield", "decltype", "default",
        "delete", "do", "double", "dynamic_cast", "else", "enum", "explicit", "extern", "false",
        "float", "for", "friend", "goto", "if", "inline", "int", "long", "mutable", "namespace",
        "new", "noexcept","nullptr", "operator", "private", "protected", "public", "register",
        "reinterpret_cast", "return", "requires", "short", "signed", "sizeof", "static",
        "static_assert", "static_cast", "struct", "switch", "template", "this", "thread_local",
        "throw", "true", "try", "typedef", "typename", "union", "unsigned", "using", "virtual",
        "void", "volatile", "wchar_t", "while", "and", "and_eq", "bitand", "bitor", "compl", "not",
        "not_eq", "or", "or_eq", "xor", "xor_eq", "typeid"
    }

    def __init__(self):
        self.lineno = 0
        self.rules = {} # map: rulename : sequences

    def __parse_sequence(self, ifile, rulename):
        if rulename in  self.rules:
            logging.warning("rule '%s' in line %i does already exist, ignored",
                            rulename, self.lineno)
            return
        for line in ifile:
            self.lineno +=1
            expressions = line.strip().split()
            if not expressions:
                return
            self.rules.setdefault(rulename, list()).append(expressions)

    def parse_file(self, filename):
        """
        Parse a grammar file and extract all rules.

        grammar:
            rule_1:
                sequence_1 [expression_1 ... expression_N]
                ...
                sequence_N
            ...
            rule_N:
                ...
        """
        logging.info("*** Parsing Grammar ***")
        logging.info("")
        logging.info("parsing grammar file '%s'...", filename)
        re_rulename = re.compile('[ \t]*([a-z-]+)[:][\n]')
        with open(filename, 'r', encoding='utf-8') as ifile:
            for line in ifile:
                self.lineno +=1
                match = re_rulename.match(line)
                if match:
                    self.__parse_sequence(ifile, match.group(1))
        logging.info("parsing finished")
        logging.info("")

    def __dependencies(self, rulename, expressions):
        if rulename in self.rules:
            expressions[rulename] = 1
            for sequence in self.rules[rulename]:
                for expression in sequence:
                    expression = re.sub('opt$', '', expression)
                    if (expression in self.rules) and (expression not in expressions):
                        expressions[expression] = 0
            for expression in list(expressions.keys()):
                if expressions[expression] == 0:
                    expressions.update(self.__dependencies(expression, expressions))
        return expressions

    def root(self, rulename):
        """
        Define root rule, search all dependant rules and remove all other rules.
        """
        expressions = {} # use map instead of set to keep order
        expressions = self.__dependencies(rulename, expressions)
        rules = {}
        for expression in expressions:
            if expression in self.rules:
                rules[expression] = self.rules[expression]
        self.rules = rules

    @staticmethod
    def __rule_use_expression(search, sequences):
        for sequence in sequences:
            for expression in sequence:
                if search == expression:
                    return True
        return False

    def use(self, search):
        """
        Search all rules using an expression and remove all other rules.
        """
        rules = {}
        if search in self.rules:
            rules[search] = self.rules[search]
        for rulename, sequences in self.rules.items():
            if self.__rule_use_expression(search, sequences):
                rules[rulename] = sequences
        self.rules = rules

    @staticmethod
    def __variants(original_sequence):
        expression_variants = []
        for expression in original_sequence:
            value = re.sub('opt$', '', expression)
            if value != expression:
                expression_variants.append([value, None]) # optional expression
            else:
                expression_variants.append([value])
        sequence_variants = list(itertools.product(*expression_variants))
        # remove None
        for i, sequence in enumerate(sequence_variants):
            sequence_variants[i] = [expression for expression in sequence if expression is not None]
        return sequence_variants

    def resolve_optionals(self):
        """
        Replace rules with optional tokens with explicit rules.
        """
        for rulename, sequences in self.rules.items():
            sequences_without_opts = []
            for i, sequence in enumerate(sequences, 1):
                sequence.append('[[{}.{}]]'.format(rulename, i)) # add original rulename.sequence
                sequences_without_opts.extend(self.__variants(sequence))
            self.rules[rulename] = sequences_without_opts

    def __expand_rule(self, already_replaced, original_sequence):
        # collect sub-rules
        subsequences = []
        replaced = set()
        for expression in original_sequence:
            if (expression not in already_replaced) and (expression in self.rules):
                subsequences.append(self.rules[expression])
                replaced.add(expression)
            else:
                subsequences.append([[expression]])
        # combinations
        sequences_product = list(itertools.product(*subsequences))
        # flatten to lists again
        expanded_sequence = []
        for sequence in sequences_product:
            sequence = [expression for sublist in list(sequence) for expression in sublist]
            expanded_sequence.append(sequence)
        return replaced, expanded_sequence

    @staticmethod
    def __sort_attachments(sequences):
        """
        Move all [[xxx]] to the end.
        """
        for i, sequence in enumerate(sequences):
            expressions = []
            attachments = []
            for expression in sequence:
                if expression.startswith('[['):
                    attachments.append(expression)
                else:
                    expressions.append(expression)
            attachments.reverse()
            expressions.extend(attachments)
            sequences[i] = expressions
        return sequences

    def flatten_rules(self, max_sequences):
        """
        Tokens of rules are replaced recursively.
        """
        for rulename, sequences in self.rules.items():
            already_replaced = { rulename }
            while True:
                expanded_sequences = []
                sequences_replaced = set()
                for sequence in sequences:
                    replaced, expanded = self.__expand_rule(already_replaced, sequence)
                    expanded_sequences.extend(expanded)
                    sequences_replaced.update(replaced)
                if (len(expanded_sequences) < max_sequences) and (expanded_sequences != sequences):
                    sequences = expanded_sequences
                    already_replaced.update(sequences_replaced)
                    continue
                self.rules[rulename] = self.__sort_attachments(expanded_sequences)
                break
            break # flatten only first one

    def __verify_rule(self, rulename):
        if rulename not in self.rules:
            return
        for i, first_sequence in enumerate(self.rules[rulename], 1): # current sequence
            current = []
            for expression in first_sequence: # remove attachments
                if expression.startswith('[['):
                    break
                current.append(expression)
            if not current:
                continue # empty sequence
            num_expressions = len(current)
            # serach duplicate in following sequences
            for j, following_sequence in enumerate(self.rules[rulename][i:], i+1):
                if current == following_sequence[:num_expressions]:
                    logging.error("conflict in rule '%s'\n   %i:%s\n   %i:%s",
                                  rulename, i, str(first_sequence), j, str(following_sequence))
                    return # show only first conflict to avoid too many duplicates

    def verify_rules(self):
        """
        Verify sequence order in rules.

        PEG's choice operator is ordered. If the first alternative succeeds,
        the second alternative is ignored.

        Do verification in reverse order to get error messages bottom up.
        """
        logging.info("*** Verify Rule(s) ***")
        logging.info("")
        for rulename in reversed(list(self.rules.keys())):
            self.__verify_rule(rulename)
        logging.info("verification done")

    def __log_branch(self, fmt, branch, sequence):
        s =' '.join(sequence) + ' < ' + ' < '.join(reversed(branch))
        logging.info(fmt, s)

    def __match_sequences(self, branch, sequences, tokens):
        count_matches = 0
        for sequence in sequences:
            if self.__match_sequence(branch, sequence, tokens):
                count_matches += 1
        return count_matches > 0

    def __match_sequence(self, branch, sequence, tokens):
        for expression in sequence:
            if expression.startswith('[['):
                break; # end of sequence
            if not tokens:
                self.__log_branch(" partly reduced: %s", branch, sequence)
                return False # partly
            token = tokens[-1]
            if token != expression:
                if expression in self.rules:
                    branch.append(expression)
                    if not self.__match_sequences(branch, self.rules[expression], tokens):
                        branch.pop()
                        return False
                    branch.pop()
                    continue
                else:
                   self.__log_branch(" rejected: %s", branch, sequence)
                   return False # error
            else:
                self.__log_branch(" |- shift: %s", branch, ["'"+token+"'"])
                tokens.pop() # shift
                continue
        self.__log_branch(" |- reduced: %s", branch, sequence)
        return True # reduce

    def match(self, input):
        """
        Searches for rules that match given input tokens.
        """
        tokens = input.split()
        tokens.reverse()
        branch = deque()
        for rulename, sequences in self.rules.items():
            logging.info("Match: '%s', Root: [[%s]]", input, rulename)
            logging.info("")
            for sequence in sequences:
                logging.info(" * sequence: %s", ' '.join(sequence))
                branch.append(rulename)
                if self.__match_sequence(branch, sequence, tokens.copy()):
                    logging.info(" accept")
                branch.pop()
                logging.info("")
            break # search only in root (first) rule

    def __keyword_upper(self, expression):
        keyword = re.sub('opt$', '', expression)
        if keyword not in self.keywords:
            return expression
        if keyword == expression:
            return expression.upper()
        return keyword.upper() + 'opt'

    def print_grammar(self):
        """
        Print grammar of object.
        """
        logging.info("*** Grammar ***")
        logging.info("")
        for rulename, sequences in self.rules.items():
            logging.info("%s:", rulename)
            for i, sequence in enumerate(sequences, 1):
                tokens = "  {}".format(i)
                for expression in sequence:
                    tokens += ' ' + self.__keyword_upper(expression)
                logging.info("%s", tokens)
            logging.info("")


def main(_argv):
    """
    Main: Handling command line parameters.
    """
    logging.basicConfig(format='%(levelname)s: %(message)s', level=logging.INFO)

    parser = argparse.ArgumentParser(
            description="""Script to support the creation of a PEG grammar.""",
            usage='%(prog)s [options]',
            formatter_class=argparse.RawTextHelpFormatter)

    parser.add_argument('-l', '--log-level', dest='log_level', action='store', default='INFO',
                        choices=['DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL'],
                        required=False,
                        help="""Set the logging level, default is 'INFO'.""")

    parser.add_argument('-i', '--input-file', dest='input_file', action='store',
                        required=True,
                        help="""Path to grammar file to read.""")

    parser.add_argument('-r', '--root-rule', dest='root_rule', action='store',
                        required=False,
                        help="""Define the root rule.
Keep only this rule and dependant rules and remove all other rules.""")

    parser.add_argument('-o', '--resolve-optionals', dest='resolve_optionals', action='store_true',
                        default=False, required=False,
                        help="""Replace rules with optional expressions with explicit sequences.
[A Bopt] => [[A], [A B]]""")

    parser.add_argument('-u', '--use-expression', dest='use_expression', action='store',
                        required=False,
                        help="""Define the expression to search for.
Keep only rules using the expression and remove all other rules.""")

    parser.add_argument('-f', '--flatten-rules', dest='flatten_rules', action='store',
                        default=0, required=False, type=int,
                        help="""Expressions of rules are replaced recursively.
FLATTEN_RULES defines the maximum number of items.
""")

    parser.add_argument('-m', '--match-rules', dest='match_rules', action='store',
                        required=False,
                        help="""Searches for rules that match given tokens.""")

    parser.add_argument('-v', '--verify-rules', dest='verify_rules', action='store_true',
                        default=False, required=False,
                        help="""PEG's choice operator is ordered. Verify rule order.""")

    args = parser.parse_args()
    logging.getLogger().setLevel(args.log_level)

    parser = GrammarParser()
    parser.parse_file(args.input_file)
    if args.root_rule: # filter for root rule with dependencies (remove rest)
        parser.root(args.root_rule)
    if args.resolve_optionals: # make optionals explicit
        parser.resolve_optionals()
    if args.use_expression: # filter for rules using the expression
        parser.use(args.use_expression)
    if args.flatten_rules: # flatten rules
        parser.flatten_rules(args.flatten_rules)
    if args.match_rules:
        parser.match(args.match_rules)
    parser.print_grammar()
    if args.verify_rules: # verify rules
        parser.verify_rules()

    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
