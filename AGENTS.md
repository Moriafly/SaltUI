# Project Instructions

- Every commit message must include a concise title and a non-empty detailed body explaining what changed, why it changed, and relevant validation. Do not create title-only commits.
- Public API additions, modifications, and removals must be made on a separate branch and merged through a pull request. Never commit or push public API changes directly to the main branch.
- Every pull request that changes public APIs must include an explicit, itemized list of all public API additions, modifications, and removals in its description.
- Tests must verify real, user-observable behavior rather than only implementation details such as constants, formulas, or helper functions.
- Interaction changes involving windows, pointers, keyboards, focus, hit regions, or platform-native components must verify the complete interaction path using a real platform window and real input events.
- Tests for interaction-region fixes must cover the boundary as well as points inside and outside it, and must assert the final behavioral outcome rather than only coordinate-conversion results.
- Align multiline call chains, split complex expressions into named steps, and keep simple calls concise.
- A `@Composable` function may declare at most one `Modifier` parameter, named `modifier`. Use content lambdas or style parameters to customize internal parts.
- Check edited Kotlin files with ktlint and Compose Rules using the repository's `.editorconfig` before finishing. Use `node .codex/hooks/quality.mjs --check` to check the current Kotlin changes manually.
- Review the changed behavior against any applicable skills available in the session. Follow each skill's invocation rules and report checks that could not be completed.
