
## Backend (uff-devweb-backend/)
- pom.xml — added spring-boot-devtools (runtime/optional, excluded from prod jar).
- Dockerfile.dev — Maven image that runs mvn spring-boot:run; source isn't baked in (bind-mounted).
- dev-run.sh — polls src/ for host edits and recompiles; devtools sees the new classes in target/classes and fast-restarts the app context.

## Frontend (uff-webdev-frontend/)
- Dockerfile.dev — Node image running the Vite dev server with HMR on port 5173.
- vite.config.ts — added server.host/watch.usePolling/hmr.clientPort, controlled by env vars (no effect on npm run build).
Root
- docker-compose.override.yml — auto-merged by docker compose up. Bind-mounts both source trees, keeps a Maven cache volume, sets HMR polling + clientPort=8081, maps 8081:5173.
Usage:
docker compose up --build      # dev mode (override auto-merged). Could be slow first time for deps.

# edit files on host → backend hot-restarts, frontend HMRs in the browser at http://localhost:8081

To go back to the prod-style build images, just rename/remove docker-compose.override.yml.

One caveat: the backend's first docker compose up will download Maven deps into the maven_cache volume (the build cache can't prepopulate a separate runtime volume), so give it ~90s. Subsequent boots are fast.
