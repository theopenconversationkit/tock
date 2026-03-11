// scripts/hooks/install-hooks.js
// Installs git hooks into .git/hooks/.
// Runs automatically via the npm "prepare" lifecycle script (npm install).
// Monorepo-compatible: walks up the directory tree to locate .git,
// regardless of how deeply nested the package is.

const fs = require('fs');
const path = require('path');

const HOOKS_SRC = __dirname;

// Skip silently in CI environments (GitHub Actions, Jenkins, GitLab CI, etc.)
// CI systems set the CI environment variable by convention.
if (process.env.CI) {
  console.log('[hooks] CI environment detected — skipping hook installation.');
  process.exit(0);
}

// Guard: if this script is not running from its expected location within the
// source tree (e.g. frontend-maven-plugin copies package.json into a temp
// target/ directory but not the scripts/ folder), exit silently.
// We detect this by checking that the pre-commit hook source file actually exists.
const HOOK_GUARD = path.join(HOOKS_SRC, 'pre-commit');
if (!fs.existsSync(HOOK_GUARD)) {
  console.log('[hooks] Hook sources not found — skipping installation (build tool temp directory?).');
  process.exit(0);
}

// Walk up the directory tree from a given folder until .git is found
function findGitRoot(dir) {
  if (fs.existsSync(path.join(dir, '.git'))) return dir;
  const parent = path.dirname(dir);
  if (parent === dir) return null; // reached filesystem root without finding .git
  return findGitRoot(parent);
}

const ROOT = findGitRoot(__dirname);

if (!ROOT) {
  console.log('[hooks] No .git directory found — skipping hook installation.');
  process.exit(0);
}

const HOOKS_DST = path.join(ROOT, '.git', 'hooks');

const hooks = ['pre-commit'];

hooks.forEach((hook) => {
  const src = path.join(HOOKS_SRC, hook);
  const dest = path.join(HOOKS_DST, hook);

  if (!fs.existsSync(src)) {
    console.error(`[hooks] Source file not found: ${src}`);
    process.exit(1);
  }

  fs.copyFileSync(src, dest);
  fs.chmodSync(dest, '755');
  console.log(`[hooks] ✔ ${hook} installed into .git/hooks/ (root: ${ROOT})`);
});
