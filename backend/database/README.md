# Quest Builder database migration

Run `quest_builder_upgrade.sql` against the `emotion_debugging` database before
starting the backend version that contains Quest Builder.

From the `backend` directory, the recommended command is:

```bash
npm run db:quest-upgrade
```

The migration is non-destructive and idempotent:

- it does not drop or truncate Quest, assignment, run, or event data;
- it creates missing Quest Builder tables;
- it creates and seeds the three-level `problems` taxonomy used by AI matching;
- it adds `quests.problem_id` and keeps legacy `error_type_id` nullable so old
  quest rows remain readable;
- it updates the engine catalog with `ON DUPLICATE KEY UPDATE`;
- it adds only missing hierarchy columns and constraints;
- it can be run more than once safely.

Always take a database backup before applying a schema migration to a shared
or production environment.

New Quest Builder drafts must reference a level-3 leaf problem.
