# Refinamento do Cadency — 22/10/2026

## Base preservada

Git limpo em `b8da8f8`; o build da versão existente passou antes das alterações (`assembleDebug`, 59 s). Mantidos package `br.edu.fsa.planner`, módulo `app`, Room, DataStore e navegação em uma Activity. Hábitos, etapas, rollover, Modo Agora, foco, Meu Ritmo e planner diário/semanal/mensal permanecem no mesmo projeto.

O [inventário completo](REFINAMENTO_ARQUIVOS.md) lista os **54 arquivos criados e 41 modificados**, incluindo fontes/licenças. Não foi adicionada dependência de runtime: o projeto reutiliza Compose/Material 3, Room 2.8.4, DataStore 1.2.0, Navigation Compose 2.9.6 e coroutines, com HTTPS/JSON nativos.

Novos arquivos principais: `CadencyPersonality.kt`, `Appearance.kt`, `CadencyHeatmap.kt`, `HeatmapRules.kt`, `HabitDetailsViewModel.kt`, `HabitDetailsScreen.kt`, `PomodoroConfiguration.kt`, `QuoteRemoteDataSource.kt`, `QuoteRepository.kt`, `QuoteEntity.kt`, `QuoteDao.kt`, `QuoteViewModel.kt`, `QuotesScreen.kt` e `RefinementMigrations.kt`. A navegação adiciona detalhes do hábito, favoritas e vínculo de hábito na rota de foco. As telas continuam consumindo ViewModels e repositórios; não acessam Room ou HTTP diretamente.

## Personalidade, cor e aparência

`PlannerPersonality` substitui o conceito antigo de Mood visual. `ThemeMode` e `AccentColor` são independentes. DataStore lê `planner_personality`, com fallback para o valor antigo de `planner_mood`; a migração da preferência não apaga o estilo escolhido. A cor usa `accent_color` em hexadecimal RGB e tem presets ou seleção HSV/hexadecimal.

`CadencyPersonalityTokens` centraliza Typography, Shapes, espaçamento, elevação, bordas, formas de controles, checkbox, linhas de papel, prioridade de composição, navegação, duração de animação, células de heatmap e estilo do timer. `LocalCadencyPersonality` fornece os tokens aos componentes, sem condicionais por enum espalhadas pelas telas. `PlannerDimens` e `PlannerPaper` mantêm compatibilidade com o layout anterior, agora consumindo tokens.

| Personalidade | Fontes | Tratamento |
| --- | --- | --- |
| Cozy | Fraunces / Nunito Sans | Folhas/blocos, checkbox arredondado, linhas, títulos expressivos e pequena elevação. |
| Soft | Quicksand / Nunito Sans | Cantos e controles arredondados, maior espaço e animações suaves. |
| Clean | Manrope | Hierarquia compacta, superfícies planas, borda fina e marcador de seção. |
| Focus | IBM Plex Sans / IBM Plex Mono | Agora em destaque, navegação discreta, mais respiro e timer mono grande. |

Um arquivo romano por família, com pesos utilizados 400/600; Mono somente Regular. Arquivos em `res/font`, licenças completas em `third_party/fonts` e assets do APK; ver [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md). Fontes funcionam offline.

A cor de destaque é ajustada em direção ao claro/escuro até obter contraste mínimo 4,5:1 com a superfície. O texto sobre a cor usa branco ou preto conforme o melhor contraste. Superfícies e textos permanecem neutros. A configuração contém previews reais de tipografia, cartão, botão e checkbox.

## Verificação da API

Documentação oficial consultada: [Frases Motivacionais API](https://www.ferramentasdaweb.com.br/mensagens-motivacionais/api/docs/). Endpoints `/categorias`, `/aleatoria` e `/todas?pagina=1&limite=10` responderam HTTP 200 em 22/10/2026. Base: `https://ferramentasdaweb.com.br/mensagens-motivacionais/api/v1/frases.php`.

A documentação limita `/todas` a 50 itens por página e informa 60 requisições/minuto. O JSON atual tem `success`, `data` e `timestamp`; a coleção usa `data.frases`, com `frase`, `autor`, `categoria` e `data`.

Categorias reais verificadas incluem **Motivação, Reflexão, Paz, Inspiração, Mudança, Oportunidade, Sonhos, Sucesso, Trabalho**, além de outras. “Disciplina” não é uma categoria oferecida; não será enviada como filtro fictício. O conteúdo é independente da personalidade visual.

## Heatmaps e detalhes de hábitos

`HeatmapRules` calcula uma matriz de 12 semanas (84 células), de segunda a domingo, incluindo os dias futuros da semana atual como estado próprio. O período individual oferece 3 meses e, quando o histórico permite, 6 meses/1 ano. Datas anteriores à criação, posteriores ao arquivamento e dias fora da programação ficam neutros. Mini mapas da lista mostram quatro semanas (28 células).

Cada check-in é deduplicado por hábito/data. Para hábitos DAILY, o denominador é a quantidade prevista no dia. Hábitos WEEKLY flexíveis só entram no denominador nos dias com check-in; os outros dias elegíveis são neutros, sem obrigação diária. Assim o mapa não transforma a meta “3 vezes por semana” em sete faltas.

Intensidades: 0, 1–25%, 26–50%, 51–75%, 76–99%, 100%. As tonalidades derivam da cor escolhida; a personalidade controla raio e espaçamento. Cada célula tem data/estado acessível. A seleção mostra os valores por texto e também pode ser percorrida pelos botões de dia anterior/seguinte. Símbolos distinguem estados sem depender só da cor.

`HabitDetailsViewModel` combina definições, histórico e período, calculando consistência e sequência DAILY. A tela preserva acesso à edição, arquivamento/exclusão e foco. Os dados vêm das mesmas tabelas de hábitos/check-ins, sem nova tabela ou migration nesta fase.

## Validação por fase

| Fase | Resultado |
| --- | --- |
| Base | assembleDebug passou antes das alterações. |
| A | assembleDebug e três testes de aparência/persistência/contraste passaram. |
| B | assembleDebug e três testes de heatmap passaram, incluindo células neutras, futuras, percentuais e hábitos flexíveis. |
| C | assembleDebug e testes de Pomodoro, armazenamento e preferências passaram. O teste com relógio controlado percorreu dois ciclos, pausas, reabertura do banco e pausa longa, contando só os minutos de foco. |
| D | assembleDebug e testes de regras, cache, favoritas, frases próprias, parser, preferências e Room passaram. |
| E | Polimento responsivo, combinações de tema, rolagem, contraste das barras do Android e regressão dos fluxos anteriores verificados. |

Logs locais em `verification/local/refinement-*.log`; fontes/licenças são versionadas. O compilador Kotlin roda no processo Gradle para evitar o daemon Kotlin que não conseguia conectar no ambiente Windows; não há alteração global de Java ou de configuração do sistema.

## Pomodoro e hábitos

Focus Flow mantém Fluxo livre e adiciona Pomodoro 25/5, 50/10 ou configuração própria (foco, pausa curta, ciclos, pausa longa). Preferências: `default_focus_mode`, `pomodoro_focus_minutes`, `pomodoro_short_break_minutes`, `pomodoro_cycles`, `pomodoro_long_break_minutes`.

`FocusRules` calcula a etapa atual; `PomodoroRules` faz foco → pausa curta → próximo ciclo e último foco → pausa longa → fim. O início da próxima etapa é explícito, sem somar ciclos inteiros enquanto o app fica fechado. Pausas manuais e pausas do Pomodoro não entram nos minutos de foco. O estado, as durações e o progresso ficam no Room, independentes de alterações futuras nas preferências.

Migration **3→4** adiciona vínculo opcional `habitId` com FK SET_NULL, modo, fase, ciclo, durações e tempo da fase. Sessões antigas são Fluxo livre e preservam o tempo já registrado. Excluir um hábito mantém o histórico de foco e o título original. A tarefa e o hábito são vínculos alternativos, não simultâneos.

Ao encerrar uma sessão de hábito com tempo registrado e ocorrência prevista hoje, o app pergunta se deseja marcar o hábito. “Agora não” não altera check-ins; confirmar usa uma operação idempotente. O hábito nunca é concluído automaticamente.

## Cadency Quote

`QuoteRemoteDataSource` usa HTTPS nativo do Android, com coroutines/Dispatchers.IO, limites de tamanho e timeouts; não foi adicionada biblioteca de rede. `QuoteRepository` é a única entrada para a UI, com coleção Room, fallback local e rotação persistente. A tela Hoje não aguarda a rede.

Migration **4→5** adiciona `quotes` e índices por categoria/favorito. IDs remotos são hashes estáveis do texto/autor; atualização de cache preserva favoritos. Até 200 frases remotas não favoritas são mantidas, além de favoritas, coleção local e frases próprias. Os oito textos de fallback são conteúdo original do Cadency, em `assets/quotes_pt_br.json`, com duas opções por categoria.

O refresh lê `/categorias` e `/todas` em páginas de 50, no máximo quatro páginas. Atualizações bem-sucedidas têm intervalo mínimo de 24 horas; falhas têm espera de uma hora. “A cada abertura” e “Outra frase” rotacionam o cache sem request obrigatório. A sequência percorre as frases elegíveis e evita repetir a atual quando existe alternativa.

Mapeamento local: Paz/Saúde/Felicidade → Calma; Reflexão/Provérbios → Reflexiva; Motivação/Inspiração/Sonhos/Sucesso/Mudança/Oportunidade/Trabalho → Motivacional. Textos desses dois últimos grupos com termos de constância, persistência, disciplina, hábito, prática ou esforço podem compor Disciplina. Categorias não mapeadas só participam de Aleatória. A categoria original continua registrada/exibida; não há endpoint fictício de Disciplina.

Frequências: a cada abertura, 6 horas, diariamente (mudança de data no fuso do aparelho), 3 dias, semanal e somente manual. Trocar a categoria explicitamente escolhe uma frase compatível, inclusive no modo manual. Preferências: `preferred_quote_category`, `quote_refresh_mode`, `current_quote_id`, `last_quote_rotation_at`, `last_quote_remote_attempt_at`, `last_quote_remote_success_at`.

Favoritas permanecem no banco offline. A tela também mantém frases próprias, com texto obrigatório e autor opcional. Frases pessoais ficam locais e entram como favoritas inicialmente; retirar o favorito não apaga a frase própria. O card segue a tipografia/superfície da personalidade; em Focus fica no fim da página e recolhido, com ação para ler. O conteúdo não muda conforme a personalidade.

## Polimento e compatibilidade

A prévia de uma frase longa fica limitada a três linhas com ação para ler o texto inteiro. Nos layouts compartilhados, aparece depois dos planos (na coluna auxiliar em tablet); em Focus fica no fim. Assim uma frase extensa não ocupa a primeira página inteira antes das tarefas. O heatmap inicia mostrando as semanas recentes quando a largura exige rolagem. A seleção também tem controles de dia anterior/seguinte.

Cozy, Soft, Clean e Focus foram comparados com a mesma cor vinho. As capturas mostram mudanças reais de famílias tipográficas, formas, hierarquia, densidade e composição. Também foi conferida a combinação Cozy + azul personalizado + escuro e sua persistência após recriar a Activity. O conteúdo remoto apareceu no Android usando a fonte da API, além dos testes determinísticos de falha/cache.

Diálogos de cor, Pomodoro e frase própria permitem rolagem em áreas pequenas. Sliders de cor têm rótulos acessíveis. O seletor HSV preserva o matiz ao ajustar preto/branco. Células do heatmap têm descrição e símbolos, e o contraste dos marcadores é calculado. Contagens de zero/um/vários usam resources de plural.

As barras de status/navegação acompanham fundo e ícones do tema escolhido no app, inclusive quando ele difere do tema do Android. Isso usa `SystemBarStyle`/edge-to-edge da versão AndroidX existente; o teste captura e verifica os pixels de ambas as barras nas oito combinações claro/escuro. A integração segue a [implementação oficial AndroidX](https://android.googlesource.com/platform/frameworks/support/+/HEAD/activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt). O botão para expandir uma frase usa o resultado real do layout do texto, para funcionar com fontes e larguras diferentes.

Uma correção na edição de hábitos mantém o término dos hábitos semanais legados ao mudar apenas o nome; reativação passa a ser uma decisão explícita. Os testes antigos foram adaptados aos detalhes do hábito e à rolagem restaurada, sem alterar o fluxo real do produto. No Robolectric, cliques usam ações semânticas; a suíte no emulador verifica os controles e a navegação no Android.

## Cobertura adicionada

- `AppearancePreferencesTest`: combinação persistente de personalidade/cor/modo, tokens distintos e contraste em claro/escuro, incluindo cores extremas.
- `HeatmapRulesTest`: percentuais e intensidades, futuro, dias neutros, período anterior à criação, programação diária, duplicatas e metas semanais flexíveis.
- `PomodoroRulesTest` e `CadencyStorageTest`: presets/configuração, foco → pausa curta → próximo ciclo → pausa longa, pausa manual, vínculo a hábito, check-in idempotente, exclusão com SET_NULL e restauração do banco.
- `QuoteRulesTest` e `QuoteRepositoryTest`: categorias reais/classificação local, todas as frequências, seleção sem repetição imediata, parser da API, cache, fallback offline, intervalo de atualização, favoritas e frases próprias após reabrir Room.
- `RefinementMigrationTest`: migração real v3 → v5 preservando tarefa, etapas, hábito/check-in e sessão pausada; leitura das preferências antigas de Mood sem acoplar a cor.
- `RefinementFlowTest`: quatro personalidades com a mesma cor, hexadecimal inválido/válido, escuro, recriação da Activity, frase própria/favorita, categoria/manual, seleção de heatmap, detalhes, Pomodoro com etapa real de um minuto e consentimento do hábito.

Os testes anteriores de CRUD, navegação, logout, hábitos semanais, água/notas, rollover, etapas, Modo Agora, foco e Meu Ritmo continuam incluídos. Configuração SYSTEM e layout de tablet são verificados em `PlannerUiHostTest`.

## Execução e artefato

| Verificação final | Resultado |
| --- | --- |
| Gradle `test` | **60 debug + 57 release**, zero falhas. 57 casos compartilhados e três adicionais de UI local em debug; 17 casos compartilhados novos em relação à base. |
| Gradle `assembleDebug` | **BUILD SUCCESSFUL**. |
| Gradle `lintDebug` | **0 erros, 28 avisos**: atualizações disponíveis, recursos sem uso, corretor em inglês e sugestões de estilo/pluralização. Nenhum bloqueio de build; versões existentes preservadas. |
| `connectedDebugAndroidTest` | **5 testes, zero falhas**, Android 15 / API 35, AVD `PlannerApp_Paper_API_35`; BUILD SUCCESSFUL em 3 min 27 s. |
| Morte real do processo | Pomodoro 25/5 pausado, ciclo 1/4, mesma sessão/fase/configuração e **3.409 ms** antes/depois de `am force-stop` e nova abertura. Login permaneceu ativo. Sessão encerrada ao fim da conferência. |
| API/cache no Android | Room v5 continha **73 frases API + oito fallback**. Com Wi-Fi/dados desligados e `Active default network: none`, nova abertura mostrou frase remota do cache e “Outra frase” funcionou. Conexão restaurada ao terminar. |

O comando final independente de build/test/lint terminou com **BUILD SUCCESSFUL em 13 s**, reutilizando os resultados atualizados do Wrapper. Logs: `verification/local/refinement-delivery-build.log` e `refinement-delivery-device.log`. Relatórios detalhados estão em `app/build/reports/tests/`, `app/build/reports/androidTests/` e `app/build/reports/lint-results-debug.html`.

As capturas finais ficam em `verification/local/refinement-reviewed/`; provas de cold start/offline e cópias de leitura do banco de teste ficam em `verification/local/cold-start-acceptance/`. Esses arquivos são locais e ignorados pelo Git. O emulador foi conferido com dados preservados, sem apagar Room nem sessão para repetir os testes. Não houve aparelho físico conectado nesta validação.

Neste Windows, o helper executa o mesmo Gradle Wrapper por um alias sem acentos para evitar falhas de bootstrap do Java. Nenhum ajuste global é necessário:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -Command "& ./scripts/build.ps1 -Tasks @('test','assembleDebug','lintDebug','--no-daemon')"
powershell -NoProfile -ExecutionPolicy Bypass -Command "& ./scripts/build.ps1 -Tasks @('connectedDebugAndroidTest','-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true','--no-daemon')"
```

Em um ambiente normal com JDK/SDK configurados, os comandos equivalentes são `./gradlew test assembleDebug lintDebug` e `./gradlew connectedDebugAndroidTest` (`gradlew.bat` no Windows). A propriedade do segundo comando conserva os APKs no emulador após a instrumentação, permitindo abrir o app e conferir as capturas; não altera o aplicativo distribuído.

APK debug: `app/build/outputs/apk/debug/app-debug.apk`, **23.003.169 bytes / 21,94 MiB**, assinatura validada com `apksigner verify`. SHA-256 local:

```text
7a4cc1db18c39c11afa15e24afdae32fa85aa5e84944e146321cfb2dbaa5d44f
```

Abra a pasta raiz no Android Studio e selecione o módulo `app`. No emulador, o aplicativo chama-se **Cadency**. Para instalar esse APK em um celular Android 8.0+ com depuração USB autorizada: `adb install -r app/build/outputs/apk/debug/app-debug.apk`, depois `adb shell am start -n br.edu.fsa.planner/.MainActivity`. Login demo: `demo@planner.app` / `123456`, ou botão **Experimentar com conta demo**.

## Limitações restantes

- Sem serviço de primeiro plano, alarme ou notificação de foco. A etapa atual é reconciliada ao voltar; a próxima etapa espera confirmação. A pergunta para marcar o hábito pertence à tela da sessão.
- Alterações na frequência do hábito recalculam o histórico com a definição atual; não há versões de programação nem múltiplos intervalos de arquivamento.
- Períodos do heatmap são normalizados em semanas; mapas grandes rolam horizontalmente. Células compactas têm seleção por texto/botões como alternativa.
- Frases dependem de um serviço público, com cache e fallback; atribuições são as fornecidas pela API. Não há verificação editorial, IA ou envio de frases pessoais.
- Conta única local, sem backup/sincronização. Aceitação em smartphone físico e revisão completa com TalkBack/fonte ampliada permanecem recomendadas.
- Na imagem AOSP API 35 utilizada, os botões de navegação do próprio Android continuam cinzentos em algumas capturas de tema escuro. A janela e o SystemUI registram aparência clara dos ícones (`mNavigationLight=false`, `mDarkIntensity=0`); os fundos e os ícones da barra de status acompanham o tema. A renderização dos três botões do sistema precisa ser conferida em uma imagem Google APIs ou aparelho físico.
