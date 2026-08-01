import os
import re
from pathlib import Path

docs_dir = Path('docs')
all_md_files = list(docs_dir.rglob('*.md')) + [Path('.agents/AGENTS.md'), Path('README.md')]
all_md_names = {f.name: f for f in all_md_files}
all_adr_names = [f.name for f in all_md_files if f.name.startswith('ADR-')]

# Regex for .md files referenced in text
md_ref_pattern = re.compile(r'([a-zA-Z0-9_-]+\.md)')
adr_ref_pattern = re.compile(r'(ADR-\d{3}[a-zA-Z0-9_-]*)')

broken_links = []

for file in all_md_files:
    content = file.read_text()
    
    # Check .md references
    for match in md_ref_pattern.finditer(content):
        ref = match.group(1)
        if ref not in all_md_names and ref != 'CHANGELOG.md': # CHANGELOG is known missing
            # Also check if it's a valid relative path
            # Just simple check for now
            broken_links.append(f"{file}: Broken md ref '{ref}'")

    # Check ADR references
    for match in adr_ref_pattern.finditer(content):
        ref = match.group(1)
        if not ref.endswith('.md'):
            ref += '.md'
        if ref not in all_adr_names:
            broken_links.append(f"{file}: Broken ADR ref '{ref}'")

for b in set(broken_links):
    print(b)
