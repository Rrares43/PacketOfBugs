$ErrorActionPreference = "Stop"

$DOCKER_USER = "stun14"
$IMAGE_NAME = "image-service"
$IMAGE_VERSION = "latest"

Write-Host "### Pasul 1: Building image... ###" -ForegroundColor Blue
docker build -t "${IMAGE_NAME}:${IMAGE_VERSION}" .
if ($LASTEXITCODE -ne 0) { throw "Error while building image" }

Write-Host "`n### Pasul 2: Tag image for Docker Hub ###" -ForegroundColor Blue
docker tag "${IMAGE_NAME}:${IMAGE_VERSION}" "${DOCKER_USER}/${IMAGE_NAME}:${IMAGE_VERSION}"
if ($LASTEXITCODE -ne 0) { throw "Error while adding tag!" }

Write-Host "`n### Pasul 3: Push image on Docker Hub ###" -ForegroundColor Blue
docker push "${DOCKER_USER}/${IMAGE_NAME}:${IMAGE_VERSION}"
if ($LASTEXITCODE -ne 0) { throw "Error while pushing image" }

Write-Host "`n### Done! ###" -ForegroundColor Green