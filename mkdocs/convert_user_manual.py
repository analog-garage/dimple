#  Copyright 2024 Analog Devices Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
"""
Convert DimpleUserManual from LaTeX to markdown with KaTeX
using pandoc.
"""

import os
from pathlib import Path
from subprocess import check_call

this_dir = Path(__file__).parent
root_dir = this_dir.parent
old_user_manual_dir = root_dir / "doc" / "DimpleUserManual"

mkdocs_dir = root_dir / "mkdocs"
new_user_manual_dir = mkdocs_dir / "user-manual"

def main():
    assert old_user_manual_dir.is_dir()
    new_user_manual_dir.mkdir(parents=True, exist_ok=True)
    os.chdir(old_user_manual_dir)
    for tex_file in old_user_manual_dir.glob("*.tex"):
        print(f"Converting {tex_file}")
        md_file = new_user_manual_dir / tex_file.with_suffix(".md").name
        cmd = [
            "pandoc",
            "-f", "latex",
            "-t", "markdown",
            "-o", str(md_file),
            str(tex_file)
        ]

        check_call(cmd)

if __name__ == "__main__":
    main()
