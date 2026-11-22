import { spawnSync } from "node:child_process";
import { createHash } from "node:crypto";
import {
  existsSync, lstatSync, mkdirSync, readFileSync, renameSync, rmSync, writeFileSync,
} from "node:fs";
import { homedir, tmpdir } from "node:os";
import { isAbsolute, join, relative, resolve, sep } from "node:path";
import { fileURLToPath } from "node:url";

const root = fileURLToPath(new URL("../..", import.meta.url));
const cacheRoot = process.env.LOCALAPPDATA || process.env.XDG_CACHE_HOME || join(homedir(), ".cache");
const toolCache = join(cacheRoot, "saltui-codex-quality", "tools");
const turnCache = join(tmpdir(), "saltui-codex-quality", "turns");
const tools = [
  {
    name: "ktlint-cli-1.8.0-all.jar",
    url: "https://repo.maven.apache.org/maven2/com/pinterest/ktlint/ktlint-cli/1.8.0/ktlint-cli-1.8.0-all.jar",
    sha256: "369ad2b789f95a011f807e1fcb690ccef80bd7cd014fd139e73ae82dcc0baeab",
  },
  {
    name: "ktlint-compose-0.5.8-all.jar",
    url: "https://github.com/mrmans0n/compose-rules/releases/download/v0.5.8/ktlint-compose-0.5.8-all.jar",
    sha256: "227830c8d4a293471802a166c7625c3bff2615a39f30391bc3840845f7941551",
  },
];
const kotlinFile = /\.(kt|kts)$/u;
const reviewFile = /(?:\.(?:kt|kts|java|swift|c|cpp|h|hpp|m|mm|js|mjs|ts|sh|ps1|py)$|(?:^|\/)\.editorconfig$)/u;

function run(command, args) {
  const result = spawnSync(command, args, {
    cwd: root,
    encoding: "utf8",
    maxBuffer: 4 * 1024 * 1024,
    windowsHide: true,
  });
  if (result.error) throw result.error;
  return result;
}

function gitFiles(...args) {
  const result = run("git", args);
  if (result.status !== 0) throw new Error(result.stderr.trim() || "Git could not list files.");
  return result.stdout.split("\0").filter(Boolean);
}

function digest(value) {
  return createHash("sha256").update(value).digest("hex");
}

function isRegularFile(file) {
  return lstatSync(join(root, file), { throwIfNoEntry: false })?.isFile() === true;
}

function snapshot(observed = []) {
  const paths = new Set([
    ...gitFiles("diff", "--name-only", "--no-renames", "-z", "HEAD", "--"),
    ...gitFiles("ls-files", "--others", "--exclude-standard", "-z"),
    ...observed,
  ]);
  return Object.fromEntries([...paths].filter(file => reviewFile.test(file)).sort().map(file => [
    file,
    isRegularFile(file) ? digest(readFileSync(join(root, file))) : null,
  ]));
}

function toolPath({ name, url, sha256 }) {
  const jar = join(toolCache, name);
  if (existsSync(jar) && digest(readFileSync(jar)) === sha256) return jar;

  mkdirSync(toolCache, { recursive: true });
  const partial = `${jar}.${process.pid}.part`;
  try {
    const download = run("curl", [
      "-fsSL", "--connect-timeout", "15", "--max-time", "120", "--output", partial, url,
    ]);
    if (download.status !== 0) {
      throw new Error(`Could not download ${name}. ${download.stderr.trim()}`);
    }
    if (digest(readFileSync(partial)) !== sha256) {
      throw new Error(`SHA-256 verification failed for ${name}.`);
    }
    renameSync(partial, jar);
    return jar;
  } finally {
    rmSync(partial, { force: true });
  }
}

function checkKotlin(files) {
  if (files.length === 0) return { ok: true, message: "" };
  try {
    const [ktlint, composeRules] = tools.map(toolPath);
    const result = run("java", ["-jar", ktlint, "--relative", "--ruleset=" + composeRules,
      ...files.map(file => "./" + file)]);
    if (result.status !== 0) {
      const diagnostics = [result.stdout, result.stderr].filter(Boolean).join("\n").trim();
      return { ok: false, message: `ktlint failed (exit ${result.status}).\n${diagnostics.slice(0, 6000)}` };
    }
    return {
      ok: true,
      message: `ktlint and Compose Rules passed for ${files.length} Kotlin file(s) using .editorconfig.`,
    };
  } catch (error) {
    return { ok: false, message: `Kotlin checks could not run. ${error.message}` };
  }
}

function respond(value = {}) {
  process.stdout.write(JSON.stringify(value) + "\n");
}

function handleHook(event) {
  if (!event.session_id || !event.turn_id) return respond();

  const turnKey = digest(JSON.stringify([root, event.session_id, event.turn_id]));
  const statePath = join(turnCache, turnKey + ".json");
  if (event.hook_event_name === "UserPromptSubmit") {
    mkdirSync(turnCache, { recursive: true });
    writeFileSync(statePath, JSON.stringify({ before: snapshot() }));
    return respond();
  }
  if (!existsSync(statePath)) return respond();

  const state = JSON.parse(readFileSync(statePath, "utf8"));
  // Keep reviewing edits already seen in this turn if they have since been committed.
  const current = snapshot(state.changed);
  const changed = Object.keys(current).filter(file => current[file] !== state.before[file]);
  const kotlin = changed.filter(file => kotlinFile.test(file) && current[file] !== null);
  const checkKey = digest(JSON.stringify(changed.map(file => [file, current[file]])));
  const checkedEarlier = state.checkKey === checkKey;
  const shouldCheck = !checkedEarlier || (event.hook_event_name === "Stop" && !state.result.ok);
  const result = shouldCheck ? checkKotlin(kotlin) : state.result;

  if (event.hook_event_name === "PostToolUse") {
    if (checkedEarlier || changed.length === 0) return respond();
    writeFileSync(statePath, JSON.stringify({ before: state.before, changed, checkKey, result }));
    if (kotlin.length === 0) return respond();
    return respond({
      hookSpecificOutput: { hookEventName: "PostToolUse", additionalContext: result.message },
    });
  }

  if (event.hook_event_name !== "Stop") return respond();
  if (changed.length === 0 || event.stop_hook_active) {
    rmSync(statePath, { force: true });
    return respond(result.ok ? {} : { systemMessage: result.message });
  }

  writeFileSync(statePath, JSON.stringify({ before: state.before, changed, checkKey, result }));
  return respond({
    decision: "block",
    reason: [
      "Complete a final review of this turn's code changes before replying.",
      "Follow AGENTS.md. Check which available skills apply to the changed behavior, read their instructions, and carry out their relevant checks. Honor each skill's invocation rules.",
      "Resolve findings within the task scope and report anything you could not verify. Preserve unrelated work.",
      result.message,
      "Files changed in this turn are " + changed.join(", ") + ".",
    ].filter(Boolean).join("\n"),
  });
}

if (process.argv[2] === "--check") {
  const requested = process.argv.slice(3)
    .map(file => relative(root, resolve(file)).split(sep).join("/"));
  const isKotlinSource = file =>
    !isAbsolute(file) && !file.startsWith("../") && kotlinFile.test(file) && isRegularFile(file);
  const invalid = requested.filter(file => !isKotlinSource(file));
  if (invalid.length) {
    process.stderr.write("Expected existing Kotlin files inside SaltUI. Invalid paths are " +
      invalid.join(", ") + ".\n");
    process.exitCode = 1;
  } else {
    const files = requested.length ? requested : Object.keys(snapshot()).filter(isKotlinSource);
    const result = checkKotlin(files);
    process.stdout.write((result.message || "No changed Kotlin files to check.") + "\n");
    process.exitCode = result.ok ? 0 : 1;
  }
} else {
  try {
    handleHook(JSON.parse(readFileSync(0, "utf8")));
  } catch (error) {
    respond({ systemMessage: `SaltUI quality hook could not run. ${error.message}` });
  }
}
