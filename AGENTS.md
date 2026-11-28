# AGENTS.md

**Project overview:** DlangDT Eclipse plugin for D language development (editor support, LSP server `serve-d`, dub building and launching).

**Current state:** Common module has DToolChain for command execution via a queued worker, IProcessExecutor interface and ProcessExecutor impl; Editor module has lsp4e integration. DToolChainConsole shows output and provides hyperlink navigation for `source/app.d(6)` etc. pattern.

**Build system:** Maven + Tycho for Eclipse plug-in packaging (JavaSE-21 target platform).
**Test system:** JUnit 5 (JUnit Jupiter 6.1.3) via Maven test scope.
