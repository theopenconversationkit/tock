// scripts/hooks/install-hooks.js
// Installs git hooks into .git/hooks/.
// Runs automatically via the npm "prepare" lifecycle script (npm install).
// Monorepo-compatible: walks up the directory tree to locate .git,
// regardless of how deeply nested the package is.

const fs = require('fs');
const path = require('path');

const HOOKS_SRC = __dirname;

// Walk up the directory tree from a given folder until .git is found
function findGitRoot(dir) {
  if (fs.existsSync(path.join(dir, '.git'))) return dir;
  const parent = path.dirname(dir);
  if (parent === dir) return null; // reached filesystem root without finding .git
  return findGitRoot(parent);
}

const ROOT = findGitRoot(__dirname);

if (!ROOT) {
  console.log('[hooks] No .git directory found — skipping hook installation (CI?).');
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
