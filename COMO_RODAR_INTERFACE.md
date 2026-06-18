# Como rodar o painel de threads

Essa versão mostra somente o resumo da simulação. Ela não tem formulário para adicionar, editar ou excluir dados.

## O que aparece na interface

- Resumo geral da simulação
- Status de cada Thread
- Artesãos e pedidos pendentes
- Recursos compartilhados
- Console com os logs do backend Java

## Como rodar no Windows

Dê dois cliques no arquivo:

```txt
rodar_interface.bat
```

Depois abra no navegador:

```txt
http://localhost:8081
```

## Como rodar pelo terminal

```bat
javac -encoding UTF-8 -d target\classes src\main\java\mercado\*.java
xcopy src\main\resources\* target\classes\ /E /I /Y
java -cp target\classes mercado.ServidorWeb
```

## Importante

Essa versão roda sem Maven. Precisa apenas do JDK instalado, porque ela usa `javac`.

Para testar:

```bat
java -version
javac -version
```
