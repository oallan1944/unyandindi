#!/usr/bin/env node
/**
 * fix-unused-react-imports.js
 *
 * SAFE, MECHANICAL codemod. It only touches ONE pattern:
 *
 *   import React from 'react'
 *   import React, { useState } from 'react'
 *
 * and only when the identifier `React` is not referenced anywhere
 * else in the file (as React.Fragment, React.FC, React.useState,
 * JSX.Element via React namespace, etc). CRA5 + React 17+'s automatic
 * JSX runtime does not require this import, so removing it is a
 * behavior-neutral change — never a business-logic change.
 *
 * It will NOT:
 *  - touch any other unused variable, state, or parameter
 *  - delete or rename anything you might be planning to use later
 *  - modify files where `React` is genuinely referenced
 *
 * Usage:
 *   node scripts/fix-unused-react-imports.js            # dry run, prints report
 *   node scripts/fix-unused-react-imports.js --write     # applies changes
 */

const fs = require('fs');
const path = require('path');

const SRC_DIR = path.join(process.cwd(), 'src');
const WRITE = process.argv.includes('--write');

const REACT_DEFAULT_IMPORT = /^import\s+React\s*,\s*\{([^}]*)\}\s*from\s*['"]react['"];?\s*$/m;
const REACT_ONLY_IMPORT = /^import\s+React\s*from\s*['"]react['"];?\s*$/m;

function walk(dir, out = []) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (entry.name === 'node_modules' || entry.name.startsWith('.')) continue;
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(full, out);
    else if (/\.(tsx?|jsx?)$/.test(entry.name)) out.push(full);
  }
  return out;
}

function reactIsReferencedOutsideImport(content, importLine) {
  const withoutImport = content.replace(importLine, '');
  // Matches React.something usage, or bare `React` as a type (React.FC etc
  // already caught by React.), or JSX namespace usage like React.ReactNode
  return /\bReact\./.test(withoutImport) || /:\s*React\b/.test(withoutImport);
}

function processFile(file) {
  const content = fs.readFileSync(file, 'utf8');

  const comboMatch = content.match(REACT_DEFAULT_IMPORT);
  const soloMatch = content.match(REACT_ONLY_IMPORT);

  if (!comboMatch && !soloMatch) return null;

  const importLine = (comboMatch || soloMatch)[0];
  if (reactIsReferencedOutsideImport(content, importLine)) {
    return { file, action: 'skipped (React is referenced elsewhere)' };
  }

  let newContent;
  if (comboMatch) {
    // import React, { useState } from 'react'  ->  import { useState } from 'react'
    const namedImports = comboMatch[1].trim();
    newContent = content.replace(
      importLine,
      `import { ${namedImports} } from 'react';`
    );
  } else {
    // import React from 'react'  ->  (removed entirely)
    newContent = content.replace(importLine + '\n', '');
  }

  if (WRITE) {
    fs.writeFileSync(file, newContent, 'utf8');
  }
  return { file, action: WRITE ? 'fixed' : 'would fix (dry run)' };
}

function main() {
  if (!fs.existsSync(SRC_DIR)) {
    console.error('No src/ directory found. Run this from your project root.');
    process.exit(1);
  }

  const files = walk(SRC_DIR);
  const results = files.map(processFile).filter(Boolean);

  const fixed = results.filter(r => r.action.startsWith('fixed') || r.action.startsWith('would fix'));
  const skipped = results.filter(r => r.action.startsWith('skipped'));

  console.log(`\nScanned ${files.length} files.\n`);

  if (fixed.length) {
    console.log(`${WRITE ? 'Fixed' : 'Would fix'} (${fixed.length}):`);
    fixed.forEach(r => console.log(`  - ${path.relative(process.cwd(), r.file)}`));
  }
  if (skipped.length) {
    console.log(`\nSkipped — React is genuinely used, left untouched (${skipped.length}):`);
    skipped.forEach(r => console.log(`  - ${path.relative(process.cwd(), r.file)}`));
  }
  if (!WRITE && fixed.length) {
    console.log('\nThis was a dry run. Re-run with --write to apply, then review the diff before committing.');
  }
}

main();
