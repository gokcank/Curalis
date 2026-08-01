import os
import re
from pathlib import Path

docs_dir = Path('docs')
all_md_files = list(docs_dir.rglob('*.md')) + [Path('.agents/AGENTS.md'), Path('README.md')]
all_md_names = {f.name: f for f in all_md_files}

# Regex for .md files referenced in text
md_ref_pattern = re.compile(r'([a-zA-Z0-9_-]+\.md)')

broken_links = []

for file in all_md_files:
    content = file.read_text()
    
    # Check .md references
    for match in md_ref_pattern.finditer(content):
        ref = match.group(1)
        if ref not in all_md_names and ref != 'CHANGELOG.md':
            broken_links.append(f"{file}: Broken md ref '{ref}'")

for b in set(broken_links):
    print(b)
