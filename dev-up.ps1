#Requires -Version 5.1
<#
.SYNOPSIS
    Sobe o projeto completo (backend + frontend + MySQL + RabbitMQ) com hot-reload.

.DESCRIPTION
    Roda o docker compose a partir da pasta-pai (onde os dois repos ficam lado a
    lado), usando --project-directory para resolver os caminhos relativos. Assim
    nao precisa mover nenhum arquivo do repositorio.

    O docker-compose.override.yml entra junto automaticamente e liga o hot-reload:
      - Backend: dev-run.sh observa src/, recompila e o DevTools reinicia.
      - Frontend: Vite HMR atualiza no navegador ao salvar.

.EXAMPLE
    .\dev-up.ps1
    Sobe tudo em foreground com build (Ctrl+C para parar).

.EXAMPLE
    .\dev-up.ps1 up -d
    Sobe em background (detached).

.EXAMPLE
    .\dev-up.ps1 down
    Derruba os containers.

.EXAMPLE
    .\dev-up.ps1 logs -f backend
    Acompanha os logs de um servico.
#>

# Sem 'Stop' de proposito: comandos nativos (docker) escrevem warnings inofensivos
# em stderr que, sob ErrorActionPreference=Stop, virariam erro fatal no PS 5.1.
# Em vez disso, conferimos $LASTEXITCODE.

$backendDir = $PSScriptRoot
$rootDir    = Split-Path $backendDir -Parent

$composeBase     = Join-Path $backendDir 'docker-compose.yml'
$composeOverride = Join-Path $backendDir 'docker-compose.override.yml'

# Confere que o Docker esta no ar antes de tentar subir.
docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker nao esta acessivel. Abra o Docker Desktop e tente de novo." -ForegroundColor Red
    exit 1
}

# Sem argumentos = "up --build". Com argumentos, repassa exatamente o que voce digitar.
$composeArgs = if ($args.Count -gt 0) { $args } else { @('up', '--build') }

Write-Host "==> Projeto:  $rootDir"                     -ForegroundColor Cyan
Write-Host "==> Comando:  docker compose $composeArgs"  -ForegroundColor Cyan
Write-Host ""
Write-Host "Depois que subir:" -ForegroundColor DarkGray
Write-Host "  Frontend         http://localhost:8081"   -ForegroundColor DarkGray
Write-Host "  API              http://localhost:8080"   -ForegroundColor DarkGray
Write-Host "  RabbitMQ painel  http://localhost:15672 (guest/guest)" -ForegroundColor DarkGray
Write-Host "  MySQL            localhost:3306 (root/password, db desweb)" -ForegroundColor DarkGray
Write-Host ""

# Broker RabbitMQ sempre limpo ao subir. Removemos o container do rabbit (e seu
# volume anônimo, via -v) antes do "up". Como o broker é efêmero (sem volume
# nomeado — ver docker-compose.override.yml), ele sobe sem a topologia durável
# antiga e o backend redeclara filas/exchange no startup. Assim, mudar a
# estrutura de filas/exchange não exige mais resetar volume na mão. Só roda
# quando o comando é "up" (não em down/logs/etc.).
if ($composeArgs[0] -eq 'up') {
    Write-Host "==> Resetando RabbitMQ (broker limpo)..." -ForegroundColor Cyan
    docker compose `
        -f $composeBase `
        -f $composeOverride `
        --project-directory $rootDir `
        rm -sfv rabbitmq *> $null
}

docker compose `
    -f $composeBase `
    -f $composeOverride `
    --project-directory $rootDir `
    $composeArgs

exit $LASTEXITCODE
