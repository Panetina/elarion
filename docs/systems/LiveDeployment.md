# Live Server Deployment

Purpose: promote a locally verified Elarion server-mod set to PebbleHost only
after the owner accepts QA and confirms the live server is stopped.

## Ownership And Boundaries

- `distribution/mods.json` owns third-party pins and `build.gradle` owns the
  canonical `build/export/server` and `build/export/client` install roots.
- `dev/tools/deploy-live-server.ps1` owns SFTP staging, backup, and promotion.
- `.elarion-deploy.local.psd1` owns this workstation's non-committed SFTP
  destination and key path. Passwords and bridge secrets are never stored in
  the repository.
- The deployment command changes only the remote `mods` directory, the files
  in the canonical managed server-config export, and the release
  manifest. It does not replace the remote `config` directory or mutate worlds,
  player state, unrelated configs, logs, whitelist, website data, or launcher
  releases.
- Starting/restarting PebbleHost and validating its startup log remain separate
  explicit operations.

The website, launcher, Discord bot, and Minecraft bridge are separate release
surfaces. Fabric remains canonical for citizens/Embers, Realms, titles,
economy, Chronicles, and addon state. The website stores permission-filtered
read models and website-owned workflow data only.

## One-Time Local Setup

Copy `dev/deploy/live-server.example.psd1` to
`.elarion-deploy.local.psd1`, fill in the PebbleHost SFTP destination, and use
the dedicated SSH key. The local file is ignored by Git. The known host key
must already be trusted; deployment uses strict host-key checking and batch
authentication.

## Release Flow

1. Complete manual/local QA and approve the exact current source state. A safe
   local plan can be generated first without opening an SFTP connection:

   ```text
   powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\deploy-live-server.ps1 -PlanOnly
   ```

   This writes the manifest plus `stage.sftp`, `commit.sftp`, and
   `rollback.sftp` under `build/deploy-live-server/<timestamp>/` without
   changing remote state.
2. Stop the PebbleHost server and confirm no player or save operation remains.
3. Run:

   ```text
   .\gradlew.bat deployLiveServerMods -PliveDeployConfirmed=true -PserverStopped=true
   ```

4. Gradle runs all module builds, tests, `verifyAiContext`, and regenerates the
  canonical exports. Server promotion reads `build/export/server/mods` and the
  individual files under `build/export/server/config`.
5. The script hashes every server jar, uploads into a timestamped staging
   directory, and does not touch live `mods` until staging succeeds.
6. Promotion renames the prior live `mods` directory, managed config files, and
   checksum manifest into `.elarion-backups/release-<UTC timestamp>/`, then
   moves the staged set into place. A failed commit attempts to restore every
   managed artifact.
7. Start PebbleHost manually and inspect the complete startup log for missing
   dependencies, mixin failures, config failures, and Elarion initialization.
8. Test one representative join and bridge/projection cycle before treating the
   release as accepted.

The script writes the local SHA-256 manifest and SFTP batch records under
`build/deploy-live-server/<timestamp>/`. The live manifest is stored as
`.elarion-live-server-mods.sha256`.

## Failure Rules

- Keep the server stopped if staging, promotion, or startup validation fails.
- Restore the timestamped backup before retrying with a corrected export.
- Never upload only one dependency as an undocumented permanent fix; correct
  `build.gradle`, rebuild both exports, and promote a complete set.
- Never deploy website/Worker migrations, launcher channels, Fabric jars, and
  bridge secrets as one irreversible action. Each surface has its own validation
  and rollback boundary.
