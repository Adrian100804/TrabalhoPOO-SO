@echo off
chcp 65001 > nul

echo ==========================================
echo  Interface Mercado Medieval - Sem Maven
echo ==========================================
echo.

where java > nul 2> nul
if errorlevel 1 (
    echo ERRO: Java nao encontrado.
    echo Instale o JDK 17 ou superior e tente de novo.
    echo.
    pause
    exit /b 1
)

where javac > nul 2> nul
if errorlevel 1 (
    echo ERRO: javac nao encontrado.
    echo Isso normalmente acontece quando tem apenas o Java comum instalado,
    echo mas nao tem o JDK completo.
    echo.
    echo Instale o JDK 17 ou superior, ou abra o projeto no IntelliJ
    echo e rode a classe mercado.ServidorWeb.
    echo.
    pause
    exit /b 1
)

echo Limpando compilacao antiga...
if exist target rmdir /s /q target
mkdir target\classes

echo Compilando arquivos Java...
javac -encoding UTF-8 -d target\classes src\main\java\mercado\*.java
if errorlevel 1 (
    echo.
    echo Erro ao compilar. Confira se o JDK esta instalado corretamente.
    echo.
    pause
    exit /b 1
)

echo Copiando arquivos da interface...
xcopy src\main\resources\* target\classes\ /E /I /Y > nul

echo.
echo Servidor iniciado em: http://localhost:8081
echo Se o navegador nao abrir sozinho, copie esse link e cole no Chrome.
echo Para parar o servidor, aperte CTRL + C neste terminal.
echo.
start http://localhost:8081
java -cp target\classes mercado.ServidorWeb

pause
