#!/usr/bin/env bash

# vim: set tabstop=2 shiftwidth=2 expandtab:

__script_name=${0##*/}
__name="${__script_name//.*}"
__log_file="${__name}.log"
__script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

set -euo pipefail
IFS=$'\n\t '

exec 2>&1
mkdir -p "$(dirname "${__log_file}")" || {
  >&2 echo "Failed to create log directory: $(dirname "${__log_file}")"; return 1;
}
exec &> >(tee "${__log_file}")

__llvm_dir="(none)"

function __usage_main() {
  echo "${__script_name}: Build the Sonar rules from Clang Tidy documentation and Clang diagnostics."
  echo "[ OPTIONS ]"
  echo "  -h|--help   " "Print this helper."
  echo "  --llvm-dir  " "The LLVM source directory. Default: ${__llvm_dir}."
  return 0
}

function __print_args() {
  local args
  for arg in "${@:-}"; do args="${args:-} ${arg:-}"; done
  echo "[$(pwd)] ${BASH_SOURCE[0]}${args:-}"
  return 0
}

function __parse_args() {
  __print_args "${@}"|| return 1
  while :
  do
    case "${1:-}" in
      -h|--help)
        __usage_main
        exit 0
        ;;
      --llvm-dir)
        __llvm_dir="${2:?Missing LLVM source directory path as parameter value.}"
        shift 2
        ;;
      *) # Unknown or no more options
        if [[ -n ${1+x} ]] ; then
          >&2 "Unknown option: ${1}"; return 1;
          shift
        else
          break
        fi
        ;;
    esac
  done
  if [[ ! -d "${__llvm_dir}" ]]; then
     >&2 echo "The LLVM source directory does not exist: ${__llvm_dir}"; return 1;
  fi
  return 0
}

function __has_python3() {
  command -v python3 >/dev/null 2>&1 || {
    >&2 echo "[ERROR] python3 is not available in the PATH!"
    return 1
  }
  python3 --version
  return 0
}

function __has_llvm_tblgen() {
  command -v llvm-tblgen >/dev/null 2>&1 || {
    >&2 echo "[ERROR] llvm-tblgen is not available in the PATH!"
    return 1
  }
  llvm-tblgen --version
  return 0
}

function __has_pandoc() {
  command -v pandoc >/dev/null 2>&1 || {
    >&2 echo "[ERROR] pandoc is not available in the PATH!"
    return 1
  }
  pandoc --version
  return 0
}

function __check_requirements() {
  __has_python3 || return 1
  __has_llvm_tblgen || return 1
  __has_pandoc || return 1
  return 0
}

function __generate_rules_from_clang_tidy_docs() {
  local rules_new rules_old rules_comparison
  local clang_tidy_checks_dir
  rules_new="${__script_dir}/clangtidy_new.xml"
  rules_old="${__script_dir}/../main/resources/clangtidy.xml"
  rules_comparison="${__script_dir}/clangtidy-comparison.md"
  clang_tidy_checks_dir="${__llvm_dir}/clang-tools-extra/docs/clang-tidy/checks"
  (
    cd "${__script_dir}" || {
      >&2 echo "[ERROR] Failed to change directory: ${__script_dir}"; return 1;
    }
    echo "[INFO] generate the new version of the rules file..."
    python3 clangtidy_createrules.py \
      rules "${clang_tidy_checks_dir}" \
      > "${rules_new}" || {
        >&2 echo "[ERROR] Failed to generate the new version of the rules file: ${rules_new} !"; return 1;
      }
    echo "[INFO] compare the new version with the old one, extend the old XML..."
    python3 utils_createrules.py \
      comparerules \
      "${rules_old}" "${rules_new}" \
      > "${rules_comparison}" || {
        >&2 echo "[ERROR] Failed to compare the new version with the old one !"; return 1;
      }
  ) || return 1
  return 0
}

function __generate_rules_from_clang_diagnostics() {
  local rules_new rules_old rules_comparison
  local diagnostic_json
  local clang_basic_dir
  rules_new="${__script_dir}/diagnostic_new.xml"
  rules_old="${__script_dir}/../main/resources/clangtidy.xml"
  rules_comparison="${__script_dir}/diagnostic-comparison.md"
  diagnostic_json="${__script_dir}/diagnostic.json"
  clang_basic_dir="${__llvm_dir}/clang/include/clang/Basic"
  (
    cd "${__script_dir}" || {
      >&2 echo "[ERROR] Failed to change directory: ${__script_dir}"; return 1;
    }
    echo "[INFO] generate the list of diagnostics..."
    (
      cd "${clang_basic_dir}" || {
        >&2 echo "[ERROR] Failed to change directory: ${clang_basic_dir}"; return 1;
      }
      llvm-tblgen \
        -dump-json Diagnostic.td \
        > "${diagnostic_json}" || {
          >&2 echo "[ERROR] Failed to generate the list of diagnostics !"; return 1;
        }
    ) || return 1
    echo "[INFO] generate the new version of the diagnostics file..."
    python3 clangtidy_createrules.py \
      diagnostics "${diagnostic_json}" \
      > "${rules_new}" || {
        >&2 echo "[ERROR] Failed to generate the new version of the diagnostics file !"; return 1;
      }
    echo "[INFO] compare the new version with the old one, extend the old XML..."
    python3 utils_createrules.py \
      comparerules \
      "${rules_old}" "${rules_new}" \
      > "${rules_comparison}" || {
        >&2 echo "[ERROR] Failed to compare the new version with the old one !"; return 1;
      }
  ) || return 1
  return 0
}

function __main() {
  __parse_args "${@}" || return 1
  __check_requirements || {
    >&2 echo "[ERROR] Some required tools are missing."; return 1;
  }
  __generate_rules_from_clang_tidy_docs || return 1
  __generate_rules_from_clang_diagnostics || return 1
  return 0
}

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

__main "${@}"
