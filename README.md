# ParkEase

Simple parking lot app for Windows. The CLI uses MySQL. The web frontend is optional.

Open **PowerShell** in the project folder for every step:

```powershell
cd C:\Users\techn\Documents\GitHub\ParkEase
```

## Requirements

- Java 11 or newer
- MySQL 8 or newer
- Bun (only for the frontend)

## 1. Start MySQL

Check that the MySQL service is running:

```powershell
Get-Service MySQL80
```

If it is stopped, start it (PowerShell may need to be opened as Administrator):

```powershell
Start-Service MySQL80
```

## 2. Create the database

Run this once:

```powershell
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
& $mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS parkease CHARACTER SET utf8mb4;"
```

Enter your MySQL root password when asked.

## 3. Configure `.env`

Create `.env` from the example (only if it does not exist yet):

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
notepad .env
```

Set your MySQL password:

```text
PARKEASE_DB_HOST=localhost
PARKEASE_DB_PORT=3306
PARKEASE_DB_NAME=parkease
PARKEASE_DB_USER=root
PARKEASE_DB_PASSWORD=your-mysql-password
```

The tables are created automatically on the first run.

## 4. Run the CLI

```powershell
.\build.bat
.\run-cli.bat
```

`run-cli.bat` builds and starts the app. You can also run the jar directly:

```powershell
java -jar target\parkease.jar
```

## 5. Run the frontend (optional)

Open a **second** PowerShell window:

```powershell
cd C:\Users\techn\Documents\GitHub\ParkEase\frontend
bun install
bun run dev
```

Open `http://localhost:5173` in your browser.

The frontend keeps its data in the browser (`localStorage`). It does not read the MySQL database.

To build the frontend:

```powershell
cd C:\Users\techn\Documents\GitHub\ParkEase\frontend
bun run build
```

## Reset all CLI data

This deletes all vehicles and tickets:

```powershell
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
& $mysql -u root -p -e "DROP DATABASE parkease; CREATE DATABASE parkease CHARACTER SET utf8mb4;"
```

## Troubleshooting

| Problem | Fix |
| --- | --- |
| `Access denied for user` | Check `PARKEASE_DB_PASSWORD` in `.env` |
| `Unknown database 'parkease'` | Run the step 2 command again |
| `Could not connect to MySQL` | Run `Start-Service MySQL80` |
| `'bun' is not recognized` | Install Bun, then reopen PowerShell |
