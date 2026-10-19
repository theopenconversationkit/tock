#!/usr/bin/env python3
#   Copyright (C) 2026 Credit Mutuel Arkea
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
"""
Lists the configuration properties read by the Tock Kotlin code, to maintain docs/docs/*/operate/configuration.md.

A property is read with one of the helpers of shared/src/main/kotlin/Properties.kt (property, intProperty,
booleanProperty...): its value comes from a JVM system property or an environment variable of the same name.

Usage:
  etc/list-doc-properties.py                  # Markdown tables grouped by module
  etc/list-doc-properties.py --check PAGE...  # properties missing from the given pages, and properties documented
                                              # in their tables but no longer read by the code (exit code 1 if any)
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCES = ['bot', 'nlp', 'shared', 'gen-ai', 'stt', 'translator', 'util']
HELPERS = (
    'property|intProperty|longProperty|booleanProperty|listProperty|mapProperty|mapListProperty'
    '|propertyOrNull|propertyExists'
)
CALL = re.compile(r'(?<![\w.])(' + HELPERS + r')\(\s*(?:name\s*=\s*)?"([A-Za-z0-9_.]+)"')
# Properties of a verticle (WebVerticle.verticleProperty...), read as "<verticle name>_<name>"
VERTICLE_CALL = re.compile(r'(?<![\w.])verticle(?:Int|Long|Boolean)?Property\(\s*"([A-Za-z0-9_.]+)"')
# Settings of the Python Gen AI orchestrator (pydantic BaseSettings), read as "<env_prefix><field>"
PYTHON_SETTINGS = ['gen-ai/orchestrator-server/src/main/python/server/src/gen_ai_orchestrator/'
                   'configurations/environment/settings.py']
# A row of a documentation table: | `property` | ...
DOC_ROW = re.compile(r'^\| `([A-Za-z0-9_.]+)` \|', re.MULTILINE)


def split_args(text, start):
    """Returns the arguments of the call whose opening parenthesis is just before `start`."""
    depth, args, current, i, quote = 0, [], '', start, None
    while i < len(text):
        c = text[i]
        if quote:
            current += c
            if c == '\\':
                current += text[i + 1]
                i += 1
            elif c == quote:
                quote = None
        elif c in '"\'':
            quote = c
            current += c
        elif c in '([{':
            depth += 1
            current += c
        elif c in ')]}':
            if depth == 0:
                args.append(current.strip())
                return args
            depth -= 1
            current += c
        elif c == ',' and depth == 0:
            args.append(current.strip())
            current = ''
        else:
            current += c
        i += 1
    return args


def comment_before(text, pos):
    """Returns the KDoc or line comment just above the declaration containing `pos`, if any."""
    line_start = text.rfind('\n', 0, pos) + 1
    before = text[:line_start].rstrip().split('\n')
    lines = []
    while before and before[-1].strip().startswith(('//', '*', '/**')):
        line = before.pop().strip().lstrip('/').lstrip('*').strip()
        if line:
            lines.insert(0, line)
    return ' '.join(lines)


def module_of(path):
    parts = path.relative_to(ROOT).parts
    index = parts.index('src')
    return '/'.join(parts[:index])


def collect():
    properties = {}
    for source in SOURCES:
        for path in sorted((ROOT / source).rglob('*.kt')):
            if '/src/main/' not in path.as_posix() or '/target/' in path.as_posix():
                continue
            text = path.read_text(encoding='utf-8')
            for match in CALL.finditer(text):
                helper, name = match.groups()
                args = split_args(text, text.index('(', match.start()) + 1)
                default = args[1] if len(args) > 1 else ''
                default = re.sub(r'^defaultValue\s*=\s*', '', default)
                entry = properties.setdefault(name, {'modules': set(), 'default': default, 'comment': '',
                                                     'helper': helper, 'file': path.relative_to(ROOT).as_posix()})
                entry['modules'].add(module_of(path))
                entry['comment'] = entry['comment'] or comment_before(text, match.start())
                if not entry['default'] and default:
                    entry['default'] = default
            # Database names, read by Mongos.kt with the constant as property name
            for match in re.finditer(r'const val \w+: String = "(\w+_mongo_db)"', text):
                name = match.group(1)
                properties.setdefault(name, {'modules': {module_of(path)}, 'default': f'"{name[:-len("_mongo_db")]}"',
                                             'comment': 'MongoDB database name', 'helper': 'property',
                                             'file': path.relative_to(ROOT).as_posix()})
    return properties


def print_tables(properties):
    by_module = {}
    for name, entry in properties.items():
        by_module.setdefault(sorted(entry['modules'])[0], []).append(name)
    for module in sorted(by_module):
        print(f'\n### `{module}`\n')
        print('| Property | Default | Description |')
        print('|----------|---------|-------------|')
        for name in sorted(by_module[module]):
            entry = properties[name]
            default = entry['default'].replace('|', '\\|').replace('\n', ' ')
            default = f'`{default}`' if default else ''
            print(f"| `{name}` | {default} | {entry['comment']} |")


def collect_other_names():
    """Returns the names read by the verticles and the Python orchestrator, only used to find obsolete properties."""
    verticle_names, python_names = set(), set()
    for source in SOURCES:
        for path in (ROOT / source).rglob('*.kt'):
            if '/src/main/' in path.as_posix() and '/target/' not in path.as_posix():
                verticle_names |= set(VERTICLE_CALL.findall(path.read_text(encoding='utf-8')))
    for settings in PYTHON_SETTINGS:
        text = (ROOT / settings).read_text(encoding='utf-8')
        prefix = re.search(r"env_prefix='([^']*)'", text).group(1)
        python_names |= {prefix + field for field in re.findall(r'^    (\w+):', text, re.MULTILINE)}
        python_names |= set(re.findall(r"alias='([^']+)'", text))
    return verticle_names, python_names


def check(properties, pages):
    documented, rows = set(), set()
    for page in pages:
        text = pathlib.Path(page).read_text(encoding='utf-8')
        documented |= set(re.findall(r'`([A-Za-z0-9_.]+)`', text))
        rows |= set(DOC_ROW.findall(text))
    missing = sorted(set(properties) - documented)
    for name in missing:
        print(f"{name}\t{properties[name]['file']}")
    print(f'{len(missing)} of {len(properties)} properties are not documented', file=sys.stderr)

    verticle_names, python_names = collect_other_names()
    obsolete = sorted(
        name for name in rows - set(properties) - python_names
        if not any(name.endswith('_' + suffix) for suffix in verticle_names)
    )
    for name in obsolete:
        print(f'{name}\tdocumented but not read by the code')
    print(f'{len(obsolete)} documented properties are not read by the code', file=sys.stderr)
    return 1 if missing or obsolete else 0


if __name__ == '__main__':
    found = collect()
    if len(sys.argv) > 1 and sys.argv[1] == '--check':
        sys.exit(check(found, sys.argv[2:]))
    print_tables(found)
