# Evolução do Cadency

## Base e decisões

A base existente compilou antes das alterações (`assembleDebug`, 40 tarefas atualizadas). O Git estava limpo em `ea91f50`. O package `br.edu.fsa.planner`, o módulo `app`, o login e as visões diária/semanal/mensal foram preservados. O nome exibido passou a Cadency sem criar outro projeto.

A arquitetura continua UI → ViewModel → Repository → Room/DataStore. Hábitos, foco e preferências têm repositories específicos porque possuem históricos e regras próprias. O `PlannerViewModel` permanece responsável pelas visões existentes.

## Banco v3

A migration explícita 2→3 adiciona `habits`, `habit_check_ins`, `subtasks` e `focus_sessions`, além de campos de origem, adiamentos, Inbox, descarte e conclusão em `planner_items`. A cadeia 1→2→3 preserva instalações antigas. Subtarefas usam exclusão em cascata; sessões de foco preservam o histórico com referência à tarefa anulada quando ela é excluída.

Os hábitos semanais legados continuam na tabela original e são copiados para hábitos com início/fim limitados à semana original, incluindo cada marcação. Assim, a migração não inventa recorrência para registros antigos. Novos hábitos podem continuar entre semanas. As marcações da visão semanal foram integradas à mesma fonte de dados da nova aba Hábitos.

Preferências de aparência e revisão diária continuam no DataStore; não há preferências visuais no Room. Não existe migração destrutiva.

## Entrega funcional

As dez fases foram implementadas no projeto existente, mantendo uma MainActivity e a arquitetura Compose → ViewModel/StateFlow → Repository → Room/DataStore. Não foram adicionadas dependências. A conta demo, CRUD, diário, água, humor emocional, gratidão, autocuidado, semana, notas e calendário continuam disponíveis. O APK mantém o applicationId para atualizar instalações antigas.

O inventário completo dos [arquivos criados e alterados](CADENCY_ARQUIVOS.md) acompanha esta entrega.

### Modelos e armazenamento

| Modelo/tabela | Comportamento |
| --- | --- |
| `HabitEntity` / `habits` | Frequência DAILY/WEEKLY, dias em bitmask, meta, início/fim, ícone, categoria e arquivamento. |
| `HabitCheckInEntity` / `habit_check_ins` | Histórico separado por hábito/data, índice único, FK CASCADE. Desfazer remove a marcação da data. |
| `SubtaskEntity` / `subtasks` | Título, estado e posição, FK CASCADE para a tarefa. |
| `FocusSessionEntity` / `focus_sessions` | Título preservado, tarefa opcional, início/fim, duração planejada, tempo real, âncora de retomada e status. FK SET_NULL. |
| `PlannerItemEntity` | Novos `originalDate`, `rolloverCount`, `isInbox`, `isDiscarded`, `completedOn`. |

`CadencyMigration.kt` contém a migration 2→3. `PlannerDatabase` registra as duas migrations explicitamente, com schemas 1/2/3 versionados. A migration copia cada marcação dos hábitos semanais antigos, mantendo seu intervalo original. A tabela legada permanece preservada; a interface semanal passa a consultar o histórico canônico. Nenhuma senha é persistida.

### Navegação e estado

Abas principais: `daily`, `weekly`, `monthly`, `habits`, `progress`. A engrenagem abre `settings`.

Rotas internas: `taskEditor?itemId&date&type`, `taskDetails/{itemId}`, `habitEditor?habitId`, `nowMode`, `focusFlow?itemId`, `inbox`, além de `splash` e `login`. Sem navegação inferior em detalhes, editor, Modo Agora ou foco.

Novos ViewModels: `SettingsViewModel`, `HabitsViewModel`, `HabitEditorViewModel`, `TaskDetailsViewModel`, `RolloverViewModel`, `NowModeViewModel`, `FocusViewModel`, `ProgressViewModel`. Os rascunhos e seleções relevantes usam SavedStateHandle; dados persistentes são observados do banco. Os ViewModels existentes de autenticação, planner e edição foram adaptados.

DataStore: `theme_mode`, `planner_mood`, `gentle_rollover_enabled`, `last_rollover_review_date`, junto à sessão já existente no arquivo `planner_preferences`. Defaults: SYSTEM, CLEAN e rollover habilitado. Logout mantém preferências e dados do único usuário demo.

### Regras e interfaces

- **Habit Tracker:** DAILY com dias selecionados ou WEEKLY com meta N por semana. Marcações reais por data, edição, histórico, desfazer, arquivamento e exclusão. Datas anteriores à criação ou posteriores ao encerramento não entram no planejamento. A tela nova impede marcações futuras. O tracker semanal legado mantém sua interação anterior, inclusive planejamento antecipado.
- **Consistência:** DAILY usa dias elegíveis; WEEKLY usa `ceil(dias elegíveis × meta / dias selecionados)`. Check-ins são únicos por data e a porcentagem é limitada a 100%. Um hábito arquivado conserva o histórico. Não há mensagens de punição.
- **Gentle Rollover:** consulta pendências anteriores a hoje, excluindo concluídos, eventos, Inbox e descartados. A oferta diária grava a revisão em DataStore; abrir manualmente continua disponível. Hoje/outra data/Inbox/descarte atualizam o mesmo registro, preservam a origem e contam adiamentos. Não há movimentação automática.
- **Modo Agora:** primeiro itens em andamento, depois os que começam em até 30 minutos, prioridade HIGH/tipo PRIORITY e demais itens de hoje. Ignora concluídos, Inbox, descartados e eventos encerrados. Empates usam horário, prioridade e ID. Pular dura apenas nessa experiência e não altera a tarefa.
- **Focus Flow:** presets 15/25/45/60 ou 1–180 minutos personalizados. Só uma sessão ativa; início, pausa, retomada, encerramento confirmado e conclusão da tarefa. O tempo real exclui pausas e é limitado à duração planejada. Timestamps no Room permitem restaurar após matar o processo. Sem som, alarme ou serviço de primeiro plano; ao retornar, o prazo é reconciliado.
- **Subtarefas:** adicionar, marcar, remover e mover para cima/baixo. A posição é atualizada em transação. O progresso aparece no dia e nos detalhes. Marcar a última etapa oferece concluir a tarefa; a decisão é explícita.
- **Meu Ritmo:** janelas móveis de 7 ou 30 dias até hoje. Tarefas pela data de conclusão, hábitos realizados, minutos reais de foco encerrado e quantidade de datas planejadas. Dois gráficos Canvas/Compose incluem zeros, toque para selecionar e descrição textual acessível. Consistência por hábito e insights usam apenas esses dados, sem IA ou pontuação.
- **Ritmo da semana:** tarefas abertas LOW=1, MEDIUM=2, HIGH=3. O nível é `ceil(soma/3)`, limitado a cinco; nenhum item significa zero. Exclui eventos, concluídos, Inbox e descartados. Cinco pontos e uma descrição numérica acessível orientam a carga.
- **Aparência:** quatro moods × claro/escuro = oito paletas centralizadas. SYSTEM acompanha `isSystemInDarkTheme`; LIGHT/DARK forçam a escolha. O app aguarda a leitura das preferências antes de desenhar as telas. Cores, formas, tipografia e dimensões permanecem centralizadas. Humor emocional do diário é independente do mood visual.

## Validação incremental

| Fase | Verificação realizada |
| --- | --- |
| Base | `assembleDebug` antes de alterar, sucesso. |
| 1 — Banco | Build e testes de persistência/migration 1→2→3. |
| 2 — Aparência | Build, reabertura do DataStore e contraste das oito paletas. |
| 3 — Hábitos | Build, dias previstos, metas e integração do tracker semanal. |
| 4 — Etapas | Build, reordenação, progresso, CASCADE e SET_NULL. |
| 5 — Rollover | Build, identidade/origem, filtros de agenda/Inbox e revisão diária. |
| 6 — Agora | Build, ordenação, conclusão e exclusão temporária. |
| 7 — Foco | Build, pausa/retomada, tempo real e recuperação do prazo ao reabrir o banco. |
| 8 — Progresso | Build e teste com tarefas, hábitos e sessões reais no Room. |
| 9 — Semana | Build e teste dos pesos e limites da escala. |
| 10 — Final | Testes locais, lint, APK e fluxos no emulador; resultados finais abaixo. |

Os logs de execução ficam em `verification/local/cadency-*.log` (ignorados no Git), e os relatórios gerados em `app/build/reports/`.

### Resultado final — 21/10/2026 (horário de São Paulo)

Comando: `scripts/build.ps1 -Tasks @('test','assembleDebug','lintDebug','connectedDebugAndroidTest')`.

- **BUILD SUCCESSFUL**, 125 tarefas, 2 min 22 s.
- **43 testes debug e 40 release**, zero falhas. Cobrem datas/calendário, filtros, formulários, hábitos, foco, Modo Agora, rollover, Room real, migrations, FKs, progresso, DataStore e Compose em celular/tablet. O teste de configuração confirma SYSTEM acompanhando o dispositivo e os modos forçados sobrescrevendo essa escolha.
- **3 testes instrumentados**, zero falhas no AVD `PlannerApp_Paper_API_35`, Android 15/API 35: CRUD/diário/calendário/hábitos legados/logout; hábitos/etapas/Inbox/Agora/foco de um minuto/Progresso; oito paletas e persistência da aparência. A sessão de foco continuou entre telas e o minuto real apareceu no relatório.
- **Lint:** zero erros, 33 avisos. Incluem atualizações sugeridas, pluralização, ortografia e resources antigos sem uso. Não foram atualizadas dependências fora do escopo.
- **Conferência nativa adicional:** instalação APK, login por toque, foco iniciado/pausado/retomado, processo encerrado com `am force-stop`, sessão pausada restaurada e encerramento confirmado por diálogo. SYSTEM respondeu a `cmd uimode night no/yes`, confirmado por pixels das capturas; a configuração original do emulador foi restaurada. Login continuou persistido ao reabrir o processo.
- **APK:** `app/build/outputs/apk/debug/app-debug.apk`, 21.550.441 bytes. Assinatura debug validada com `apksigner verify`.
- **SHA-256:** `958bbccf90c742a8e339c74a8d96022f6eb25bbed99b224f45994a73c180c673`.
- **Dispositivo:** APK instalado e aplicativo deixado na aba Hoje. Não havia celular físico conectado para aceitação nessa execução.

Evidências locais: `cadency-final-validation.log`, `cadency-native-check.log`, `cadency-native-system.log`, capturas `native-*.png` e `device-cadency/`, dentro de `verification/local/`. Algumas capturas rápidas dos testes incluem transições de janelas; as capturas nativas finais foram feitas após estabilização da interface. Outputs de build e capturas permanecem fora do Git.

Os ajustes dos testes antigos acomodam o novo caminho tarefa → detalhes → editor e a restauração da rolagem das abas. Ações semânticas acionam os botões do editor após recriar a Activity; os fluxos principais também foram conferidos por toques reais via ADB/UI Automator.

## Como executar

Abra a raiz no Android Studio, sincronize com JDK 17/SDK 36 e execute `app` em API 26+. Neste Windows, `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1` usa o JDK/SDK locais. APK: `app/build/outputs/apk/debug/app-debug.apk`.

No emulador existente: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-emulator.ps1 -InstallApp`. Em celular, ative Depuração USB, aceite a chave RSA e use `scripts/device.ps1 install -r app/build/outputs/apk/debug/app-debug.apk`. Login: `demo@planner.app` / `123456`, ou botão demo.

O [README](../README.md) contém os passos de instalação e o roteiro de aceitação de cada recurso.

## Limitações e evolução

Uma conta demo local, sem nuvem, backup, notificações ou recorrência de tarefas. Categorias são fixas. Hábitos possuem recorrência; editar a frequência recalcula as métricas com a definição atual, sem versionar agendas antigas. Uma sessão de foco é atribuída ao dia de início, sem divisão à meia-noite; os gráficos mostram minutos inteiros por dia. Conclusões legadas sem timestamp usam a data planejada, indicado na interface.

O foco continua contando fora da tela, mas avisos de término em segundo plano exigirão notificações/serviço apropriado em etapa futura. Arquivar e reativar um hábito conserva as marcações e não modela vários intervalos de pausa. Aceitação em aparelho físico, TalkBack e fontes ampliadas continuam recomendadas. Fontes definitivas da identidade visual poderão substituir `PlannerFonts` sem refazer telas.
