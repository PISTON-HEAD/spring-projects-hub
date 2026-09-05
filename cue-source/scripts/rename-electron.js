// postinstall (disabled): this project previously renamed the Electron binary to
// "MicrosoftEdgeUpdate.exe" and patched its PE version info to impersonate a
// Microsoft system process ("Microsoft Corporation") so it would blend into Task
// Manager. That process-masquerade / anti-detection behavior has been removed —
// the app now runs honestly under its own name.
//
// This file is kept only so any lingering reference resolves to a safe no-op; it
// is no longer wired into package.json and does nothing.
process.exit(0);
