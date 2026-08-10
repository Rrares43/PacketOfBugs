
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

COPY ["ImageProcessor.csproj", "./"]
RUN dotnet restore "./ImageProcessor.csproj"


COPY . .
RUN dotnet publish "ImageProcessor.csproj" -c Release -o /app/publish /p:UseAppHost=false

FROM mcr.microsoft.com/dotnet/aspnet:9.0
WORKDIR /app

COPY --from=build /app/publish .

EXPOSE 5157
ENV ASPNETCORE_URLS=http://+:5157

ENTRYPOINT ["dotnet", "ImageProcessor.dll"]