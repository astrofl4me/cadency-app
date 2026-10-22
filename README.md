# Cadency — PlannerApp

Planner pessoal Android para um projeto acadêmico de Engenharia da Computação. Organize seus dias respeitando o seu ritmo: agenda diária, semanal e mensal, hábitos, subtarefas, foco e informações locais sobre sua rotina, em uma interface em português inspirada em um planner de papel.

**Package:** `br.edu.fsa.planner` · **Android mínimo:** 8.0 / API 26 · **Versão:** 1.0.0.

Repositório: [astrofl4me/cadency-app](https://github.com/astrofl4me/cadency-app). O nome interno do projeto continua `PlannerApp`; o aplicativo aparece como **Cadency**. O package permanece igual para atualizar instalações existentes.

## Teste rápido no celular

1. Neste computador, o APK já gerado fica em `app/build/outputs/apk/debug/app-debug.apk`. Transfira esse arquivo para um celular Android 8.0 ou superior.
2. Abra o APK pelo gerenciador de arquivos. Se o Android solicitar, permita a instalação de aplicativos por essa fonte e conclua a instalação.
3. Abra **Cadency** e toque em **Experimentar com conta demo**, ou entre com `demo@planner.app` e senha `123456`.
4. Crie um plano, edite, conclua e confira no dia, semana e mês. Feche o aplicativo e abra novamente para conferir a persistência. Depois teste exclusão e logout.

Os passos com Android Studio, emulador e depuração USB estão nas seções abaixo. O APK também pode ser obtido nos artifacts de uma execução bem-sucedida do GitHub Actions.

## Status atual

O MVP existente foi ampliado em dez fases, preservando as páginas baseadas nas referências visuais. Hábitos recorrentes, revisão de pendências, subtarefas, Modo Agora, Focus Flow, Meu Ritmo e aparência persistente são recursos funcionais com dados locais reais. Os builds e os testes de cada etapa estão registrados em [verification/CADENCY_EVOLUCAO.md](verification/CADENCY_EVOLUCAO.md), junto às decisões, migrations e limitações. A aceitação em celular físico continua recomendada.

O registro original fica em [verification/VALIDACAO.md](verification/VALIDACAO.md). A validação atual está em [verification/CADENCY_EVOLUCAO.md](verification/CADENCY_EVOLUCAO.md); os relatórios gerados pelo Gradle ficam em `app/build/reports/`.

## Funcionalidades

- Login local, validação, mostrar/ocultar senha, acesso demo, sessão persistente e logout.
- Navegação Compose com Hoje, Semana, Mês, Hábitos e Progresso em uma única Activity. Configurações pela engrenagem superior.
- Criar, editar, concluir, reabrir e excluir tarefas ou compromissos.
- Data, título, descrição, horários opcionais, tipo, prioridade e categoria.
- Visão diária com prioridades, checklist, humor, controle de água, autocuidado, gratidão, anotações e planos de amanhã.
- Visão semanal de segunda a domingo, prioridades, lista de tarefas, hábitos com marcações por dia, intenção e notas da semana.
- Calendário mensal de domingo a sábado, seleção de datas, indicadores de categorias e abertura da visão diária.
- Navegação entre dias, semanas e meses, com retorno ao período atual.
- Habit Tracker diário ou semanal, dias selecionados, meta semanal, histórico, edição, arquivamento e exclusão.
- Gentle Rollover: revisão das pendências, escolhas de data/hoje/Inbox/descarte e controle diário sem mover tarefas automaticamente.
- Etapas manuais com conclusão, exclusão e reordenação; confirmação antes de concluir a tarefa principal.
- Modo Agora com seleção local de tarefa relevante e opção de pular temporariamente.
- Focus Flow de 15/25/45/60 minutos ou duração personalizada; pausa, retomada e histórico do tempo real.
- Meu Ritmo: períodos de 7/30 dias, tarefas concluídas, hábitos realizados, minutos de foco, dias planejados e gráficos acessíveis.
- Ritmo da semana: carga das tarefas pendentes ponderada por prioridade.
- Aparência Sistema/Claro/Escuro e estilos Cozy/Soft/Clean/Focus, combináveis e persistentes.
- Persistência Room reativa; cadeia de migrations 1→2→3 mantém dados existentes, inclusive as marcações dos hábitos legados.
- Estados de carregamento, listas vazias, erros e feedback discreto por snackbar.
- Páginas diária e semanal com papel claro, blocos cinza, linhas finas e títulos serifados; áreas de toque e descrições acessíveis.

### Layout baseado na referência

Em áreas úteis a partir de 600 dp, as páginas usam duas colunas, como os modelos: agenda à esquerda e seções auxiliares à direita. Em celulares, as seções são empilhadas e a página rola verticalmente. A tabela de hábitos permite rolagem horizontal quando necessário para preservar o tamanho dos alvos de toque.

As fontes são provisórias. `PlannerFonts` e `PlannerPaper`, em `ui/theme/PlannerTheme.kt`, centralizam famílias, estilos, cores e formas. As fontes definitivas poderão ser adicionadas em `app/src/main/res/font/` e associadas ali, sem refazer as telas.

O controle de água registra de zero a oito copos por data. Os hábitos criados pelo acompanhamento semanal continuam limitados à semana escolhida; os criados na aba Hábitos podem se repetir entre semanas. As duas interfaces compartilham o mesmo histórico. Atividades/anotações, autocuidado e gratidão são campos independentes. O registro da referência original fica em [verification/REFERENCIA_VISUAL.md](verification/REFERENCIA_VISUAL.md).

## Stack

| Componente | Versão |
| --- | --- |
| Kotlin / plugin Compose | 2.2.21 |
| Android Gradle Plugin | 8.13.2 |
| Gradle Wrapper | 8.13 |
| JDK / bytecode | 17 |
| compileSdk / targetSdk | 36 / 36 |
| Compose BOM / Material 3 | 2025.12.01 / 1.4.0 |
| Navigation Compose | 2.9.6 |
| Lifecycle | 2.10.0 |
| Room | 2.8.4 |
| KSP | 2.2.21-2.0.4 |
| Preferences DataStore | 1.2.0 |
| Coroutines | 1.10.2 |
| Testes | JUnit 4, Robolectric 4.16.1 e Compose Test |

Foram fixadas versões estáveis compatíveis para tornar o projeto reproduzível. Não é necessário instalar Gradle globalmente: o Wrapper está incluído.

## Arquitetura e pastas

O fluxo é **Compose → ViewModel / StateFlow → Repository → Room ou DataStore**. `PlannerApplication` cria um `AppContainer` simples, que fornece os repositórios sem um framework de injeção de dependências.

```text
app/
  schemas/                         # schema versionado do Room
  src/main/
    AndroidManifest.xml
    java/br/edu/fsa/planner/
      MainActivity.kt
      PlannerApplication.kt
      data/
        local/database/            # Room e converters
        local/dao/                 # consultas e operações SQL
        local/entity/              # entidades e mapeamentos
        repository/                # planner, auth, hábitos, foco, progresso e preferências
      domain/model/                # modelos, enums, datas e validação
      ui/
        navigation/
        screens/auth/
        screens/daily/
        screens/weekly/
        screens/monthly/
        screens/editor/
        screens/settings/
        screens/habits/
        screens/details/
        screens/now/
        screens/focus/
        screens/progress/
        components/
        theme/                     # cores, tipografia, dimensões e shapes
      viewmodel/
    res/values/                    # textos em português e tema do sistema
  src/test/                        # regras, persistência e testes locais
  src/testDebug/                   # fluxo Compose com Robolectric
  src/androidTest/                 # fluxo essencial em dispositivo
gradle/wrapper/
scripts/                           # preparação e builds no Windows
verification/                      # registro de validação
```

Não há telas construídas em XML. Os XML existentes são manifest, resources e ícone vetorial.

## Abrir no Android Studio

Para baixar o projeto em outro computador:

```powershell
git clone https://github.com/astrofl4me/cadency-app.git
cd cadency-app
```

1. Instale uma versão estável do [Android Studio](https://developer.android.com/studio) compatível com AGP 8.13.2, ou mais recente.
2. Escolha **Open** e selecione a pasta deste projeto, onde está `settings.gradle.kts`. Não abra apenas o módulo interno `app/`.
3. Aguarde **Gradle Sync**. Em **Settings → Build, Execution, Deployment → Build Tools → Gradle**, selecione um JDK 17 compatível. Neste computador ele foi preparado em `.tooling/jdk`; o JDK compatível incluído no Studio também pode ser utilizado.
4. Em **SDK Manager**, instale Android SDK Platform 36, Build-Tools 36.0.0 e Android SDK Platform-Tools. `local.properties` aponta para o SDK desta máquina e não é versionado; ajuste pelo Studio se utilizar outro SDK.
5. Selecione a configuração **app**, um emulador ou dispositivo, e clique em **Run**.

Android Studio não foi instalado automaticamente. O projeto e o build por linha de comando não dependem da instalação da IDE.

### Ferramentas locais no Windows

O ambiente original possuía Java 8 e Git. O script abaixo prepara JDK 17 e o SDK sem alterar Java, PATH ou configurações globais permanentes:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/bootstrap.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1
```

Os downloads ficam em `.tooling/` e o SDK em `%TEMP%\PlannerApp-android-sdk`. A preparação exige rede e aceita as licenças do SDK para instalar os pacotes solicitados. O Wrapper é distribuído com verificação SHA-256 do Gradle.

O caminho original contém acentos. O script de build cria junctions locais em `%TEMP%\PlannerApp-project` e `%TEMP%\PlannerApp-gradle-cache`, apontando para os arquivos originais. Isso evita problemas de classpath do executor de testes no Java 17 para Windows. Não há uma segunda cópia do projeto. Se o Studio apresentar o mesmo problema de testes, abra pelo alias ou execute os testes pelo script. A compilação incremental Kotlin/KSP está desativada neste MVP para evitar mapeamentos antigos entre esses caminhos.

Se os arquivos temporários do SDK forem removidos pelo Windows, execute o bootstrap novamente ou configure o SDK instalado pelo Android Studio.

## Login demo

```text
E-mail: demo@planner.app
Senha: 123456
```

Também existe o botão **Experimentar com conta demo**. As credenciais de produção do MVP estão centralizadas em `LocalAuthRepository`. A sessão guarda somente o e-mail; a senha digitada não é persistida. Esta conta pública é uma conveniência acadêmica local, sem backend ou proteção de acesso real.

## Testar em emulador

1. Abra **Tools → Device Manager → Create Device**.
2. Escolha um telefone e uma imagem Android API 26 ou superior; API 35 é uma opção para testar esta versão.
3. Configure a aceleração de acordo com a [documentação do Android Emulator](https://developer.android.com/studio/run/emulator-acceleration).
4. Inicie o dispositivo, aguarde a tela inicial do Android e execute **app**.

`scripts/prepare-emulator.ps1` prepara ou registra o AVD `PlannerApp_API_35` na localização padrão do Android. Se ele já existir na pasta antiga do SDK local, somente o registro é copiado; os dados do dispositivo permanecem no lugar. Pacotes já instalados não são baixados novamente.

### Visualizar dentro do VS Code

1. Instale [EmbeDroid](https://marketplace.visualstudio.com/items?itemName=UtpalBarman.embedroid) pela aba Extensions.
2. Nas configurações da extensão, indique em **EmbeDroid: SDK Path** o SDK apontado por `local.properties`. O SDK local do bootstrap fica em `%TEMP%\PlannerApp-android-sdk`; informe o caminho absoluto expandido, não o texto `%TEMP%`.
3. Execute a preparação do AVD:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/prepare-emulator.ps1
```

O emulador precisa de aceleração. No Windows, o projeto utiliza **Windows Hypervisor Platform**, conforme a [orientação oficial do Android](https://developer.android.com/studio/run/emulator-acceleration). A virtualização VT-x/SVM deve estar habilitada na BIOS. Para habilitar o recurso do Windows:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/enable-emulator-acceleration.ps1
```

Esse script solicita autorização UAC de administrador, habilita somente `HypervisorPlatform` e registra o resultado em `verification/local/acceleration-result.json`. Ele não reinicia o computador. Se o resultado indicar `RestartNeeded: true`, salve seu trabalho e reinicie o Windows antes de continuar. Nenhuma proteção do Windows é desativada e a política global de execução do PowerShell é preservada.

Depois do reinício, na raiz do projeto:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-emulator.ps1 -InstallApp
```

O comando utiliza aceleração, inicia o AVD sem janela, aguarda `sys.boot_completed`, instala o APK debug já compilado e abre o planner. Se o APK ainda não existir, execute `scripts/build.ps1` primeiro. Ele também pode reutilizar o mesmo AVD que já esteja iniciado. Não apaga dados do dispositivo.

No EmbeDroid, clique em **Refresh** e em **Open Embedded View** para o dispositivo em **Running Devices**. No computador configurado nesta sessão, também foi criada a tarefa local **Planner: abrir no emulador**, acessível por **Terminal → Run Task**. As configurações `.vscode/` contêm caminhos pessoais e são ignoradas pelo Git.

Para utilizar a janela tradicional do emulador, acrescente `-Window` ao comando. Falhas de inicialização ficam em `verification/local/emulator-out.log` e `emulator-error.log`.

## Testar em celular físico

1. Use um celular com Android 8.0 ou superior.
2. Ative **Opções do desenvolvedor** e **Depuração USB**.
3. Conecte um cabo USB com transmissão de dados e aceite a autorização RSA no celular.
4. No Studio, selecione o aparelho e clique em **Run**. No Windows, alguns fabricantes exigem seu driver USB.

Com `adb` no PATH, também é possível instalar o APK compilado:

```powershell
adb devices -l
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n br.edu.fsa.planner/.MainActivity
```

Se usar apenas as ferramentas locais deste projeto:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/device.ps1 devices -l
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/device.ps1 install -r app/build/outputs/apk/debug/app-debug.apk
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/device.ps1 shell am start -n br.edu.fsa.planner/.MainActivity
```

## Build, testes e APK

Com JDK e SDK configurados:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebugAndroidTest
.\gradlew.bat connectedDebugAndroidTest
```

O último comando exige um Android conectado e inicializado. Em Linux/macOS, use `./gradlew` em vez de `gradlew.bat` e configure o SDK local. Caso seja necessário, habilite a execução com `chmod +x gradlew`.

### Build pelo GitHub Actions

O workflow `.github/workflows/android.yml` executa build, testes locais, lint e compilação dos testes instrumentados em pushes e pull requests para `main`. Utiliza JDK 17 e prepara o SDK 36, sem credenciais externas.

Para obter um APK gerado por ele:

1. Abra [Actions → Android CI](https://github.com/astrofl4me/cadency-app/actions/workflows/android.yml).
2. Selecione uma execução bem-sucedida.
3. Em **Artifacts**, baixe `cadency-debug` estando conectado ao GitHub.
4. Extraia o ZIP e instale `app-debug.apk` no celular.

O artifact `cadency-reports` contém relatórios e capturas locais para revisão. Os artifacts ficam disponíveis por 14 dias; uma nova execução gera novos arquivos. O workflow compila os testes instrumentados, mas a execução deles exige um Android conectado.

Para o caminho com acentos deste computador, prefira:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1 -Tasks lintDebug
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1 -Tasks assembleDebugAndroidTest
```

`scripts/build.ps1` executa `assembleDebug` e `testDebugUnitTest` por padrão. `-Clean` executa uma limpeza dos outputs de build antes de compilar.

**APK principal:** `app/build/outputs/apk/debug/app-debug.apk`.

Os testes cobrem calendário, intervalos semanais, filtros, validação, queries reais do Room, CRUD reativo, banco reaberto, notas/humor e sessão/logout. O teste de interface local fica em `testDebug`, pois utiliza a Activity disponibilizada por `ui-test-manifest` nessa variante. O teste instrumentado percorre o fluxo essencial e recria a Activity para verificar restauração.

Na evolução Cadency, passaram **43 testes em debug e 40 em release**. O lint não apresentou erros; os avisos restantes incluem sugestões de atualização das dependências, pluralização e recursos antigos sem uso. Os resultados da interface e os detalhes da versão atual ficam em [verification/CADENCY_EVOLUCAO.md](verification/CADENCY_EVOLUCAO.md).

### Roteiro de aceitação no aparelho

Entre com a conta demo; crie um plano; confira a data no dia, semana e mês; edite; conclua e reabra; force a parada pelo Android e abra novamente; confirme os dados; exclua; navegue entre períodos; faça logout e login novamente. Verifique também uma prioridade e um plano adicionado pelo atalho **Para amanhã**.

### Testar os novos recursos

| Recurso | Roteiro |
| --- | --- |
| Hábitos | Abra Hábitos → Adicionar hábito. Escolha frequência e dias (ou meta semanal), salve, conclua hoje e desfaça. Toque no nome para editar, ver Histórico, arquivar ou excluir. Navegue entre semanas para conferir as datas reais. |
| Etapas | Toque em uma tarefa → Próxima etapa → botão +. Marque, use o menu para mover ou remover e conclua todas. O app pergunta antes de concluir a tarefa. |
| Gentle Rollover | Crie uma tarefa com data anterior. Volte a Hoje: a revisão aparece uma vez no dia; o botão “Vamos reorganizar?” permite abrir manualmente. Experimente Hoje, outra data, Inbox e descarte. A Caixa de entrada permite agendar o item novamente. |
| Modo Agora | Em Hoje, toque Entrar no Modo Agora. Confira a tarefa escolhida, pule temporariamente, conclua ou inicie foco. Pular não altera o item no banco. |
| Focus Flow | Abra por Hoje, Modo Agora ou detalhes da tarefa. Escolha duração ou Personalizado, inicie, pause e retome. Saia e volte, inclusive após reiniciar o processo. Ao encerrar antecipadamente, confirme o diálogo. Uma sessão de pelo menos um minuto aparece nas métricas de foco. |
| Meu Ritmo | Abra Progresso e alterne 7/30 dias. Confira tarefas, hábitos, foco e dias planejados. Toque nas barras para ver a data/valor; role para consistência e insights. Sem registros, os valores são zero e há uma mensagem de acolhimento. |
| Aparência | Abra a engrenagem → Aparência. Combine os quatro estilos com Claro/Escuro. Em Sistema, altere o modo do dispositivo. Feche/reabra o app para conferir persistência. |
| Ritmo semanal | Compare dias com tarefas LOW/MEDIUM/HIGH ainda abertas. Os pontos diminuem ao concluir. Não contam eventos, Inbox ou itens descartados. |

As sessões de foco registram milissegundos reais e excluem pausas. A interface de Progresso soma as sessões encerradas e mostra minutos inteiros; por isso, testes de poucos segundos podem ainda mostrar zero minutos.

## Decisões de projeto

- **Compose:** permite construir telas declarativas e componentes reutilizáveis, acompanhando o estado observado sem telas XML ou várias Activities.
- **Room:** valida consultas em compilação e emite Flows quando os dados mudam. Os schemas 1/2/3 e migrations são versionados. A migration 2→3 adiciona hábitos recorrentes/check-ins, subtarefas, sessões de foco e campos de rollover/conclusão. Subtarefas usam CASCADE; excluir uma tarefa anula sua referência na sessão de foco e preserva a sessão.
- **DataStore:** mantém sessão, aparência, opção de rollover e última revisão diária de maneira assíncrona. Dados estruturados ficam no Room.
- **Autenticação local:** permite estudar e executar o MVP offline sem configurar serviços externos. `AuthRepository` define a sessão e as operações de login/logout, isolando as credenciais da interface.
- **Firebase no futuro:** implemente `AuthRepository` com Firebase Authentication e forneça a implementação pelo `AppContainer`. ViewModel e UI continuam consumindo o mesmo contrato. Contas reais também exigirão vincular os dados do planner ao ID do usuário e definir a estratégia de sincronização.
- **Datas:** `java.time` calcula os períodos. Datas são persistidas como epoch day e horários como minutos desde meia-noite. A semana começa na segunda; o calendário mensal começa no domingo. Dias vizinhos completam as linhas do calendário.
- **Para amanhã:** o atalho diário cria um item com a data real do dia seguinte. A seção mostra os planos ainda pendentes dessa data; quando o dia chega, eles aparecem normalmente na agenda correspondente.
- **Tema:** oito paletas centralizadas em `CadencyPalette`, com dimensões, shapes e tipografia em `ui/theme`. `PlannerTheme` combina o mood visual com SYSTEM/LIGHT/DARK; SYSTEM usa `isSystemInDarkTheme`. Humor emocional do diário é um dado independente.
- **Gráficos:** Canvas/Compose desenha duas séries simples sem dependência extra. Inclui dias zerados, seleção por toque, descrições acessíveis e cores do tema.
- **Estado:** seleções de datas e rascunho do formulário usam `SavedStateHandle`; dados salvos são observados pelo Room. Logout preserva os planos da única conta local demo.

## Limitações e próximos passos

O MVP tem uma conta local e categorias fixas. Não possui sincronização, cadastro real, recorrência de tarefas, alarmes, sons ou notificações. Hábitos têm recorrência própria. O timer usa timestamps persistidos: continua contando ao sair e é reconciliado ao retornar, sem serviço de primeiro plano nem aviso em segundo plano. As sessões são atribuídas ao dia de início; minutos exibidos são a soma do tempo real encerrado, arredondada para baixo por dia. Horários de compromissos pertencem ao mesmo dia. Desinstalar ou limpar os dados remove a base local; não há backup nesta versão.

Alterar a frequência de um hábito aplica a definição atual ao cálculo histórico; versionamento de agendas é uma evolução futura. Metas semanais são proporcionais aos dias elegíveis no período, arredondadas para cima; porcentagens são limitadas a 100%. Registros antigos sem data de conclusão usam a data planejada. Não há IA nem pontuação de produtividade.

Próximos passos: aceitação em celular físico, TalkBack e fonte ampliada, exportação/backup, notificações de foco e, depois, autenticação real com dados por usuário e sincronização.

O repositório Git preserva os commits por etapa e o commit inicial do GitHub. A branch principal é `main`, com remoto `origin` em `https://github.com/astrofl4me/cadency-app.git`. Build, SDK, cache, `local.properties`, arquivos de IDE e chaves não são versionados; APKs são entregues por artifacts, fora do histórico Git.

Para enviar alterações futuras, na pasta raiz:

```powershell
git status
git add .
git commit -m "describe your change"
git push
```

Confira os arquivos mostrados por `git status` antes de criar cada commit. `.gitignore` não remove arquivos que já estejam versionados; os arquivos gerados deste projeto foram conferidos e não fazem parte do histórico.
