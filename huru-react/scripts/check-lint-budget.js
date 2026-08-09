#!/usr/bin/env node
/**
 * check-lint-budget.js
 *
 * Enterprise "ratchet" pattern: instead of either (a) failing the build
 * on every pre-existing warning, or (b) ignoring lint debt forever,
 * this compares the current warning count against a committed budget
 * file. CI fails only if warnings INCREASE. When you clean some up,
 * re-run with --update to lower the budget — the ratchet only tightens.
 *
 * Usage:
 *   npm run lint:report                 # generates lint-report.json
 *   node scripts/check-lint-budget.js           # check against budget
 *   node scripts/check-lint-budget.js --update  # lower the budget to current count
 */

const fs = require('fs');
const path = require('path');

const REPORT_PATH = path.join(process.cwd(), 'lint-report.json');
const BUDGET_PATH = path.join(process.cwd(), '.eslint-warning-budget.json');
const UPDATE = process.argv.includes('--update');

function countIssues(report) {
  let warnings = 0;
  let errors = 0;
  for (const file of report) {
    warnings += file.warningCount || 0;
    errors += file.errorCount || 0;
  }
  return { warnings, errors };
}

function main() {
  if (!fs.existsSync(REPORT_PATH)) {
    console.error('lint-report.json not found. Run `npm run lint:report` first.');
    process.exit(1);
  }

  const report = JSON.parse(fs.readFileSync(REPORT_PATH, 'utf8'));
  const { warnings, errors } = countIssues(report);

  if (errors > 0) {
    console.error(`\n${errors} ESLint ERROR(s) found (e.g. genuinely unused imports). These must be fixed, not budgeted.`);
    process.exit(1);
  }

  let budget = { maxWarnings: warnings };
  if (fs.existsSync(BUDGET_PATH)) {
    budget = JSON.parse(fs.readFileSync(BUDGET_PATH, 'utf8'));
  }

  if (UPDATE) {
    fs.writeFileSync(BUDGET_PATH, JSON.stringify({ maxWarnings: warnings }, null, 2) + '\n');
    console.log(`Budget updated: ${warnings} warnings (was ${budget.maxWarnings}).`);
    return;
  }

  console.log(`Current warnings: ${warnings} | Budget: ${budget.maxWarnings}`);

  if (warnings > budget.maxWarnings) {
    console.error(
      `\nFAIL: warning count increased by ${warnings - budget.maxWarnings}.\n` +
      `New unused vars/imports were added without being addressed or intentionally deferred with a "_" prefix.\n` +
      `If this is expected WIP, run "node scripts/check-lint-budget.js --update" and commit the new budget with justification in the PR description.`
    );
    process.exit(1);
  }

  console.log('OK — no new lint debt introduced.');
}

main();
