# Páginas diária e semanal — 21 de outubro de 2026

## Implementação

As duas páginas foram redesenhadas a partir dos modelos de planner fornecidos pelo autor do projeto. O layout usa fundo de papel claro, retângulos cinza, divisórias finas, títulos serifados e controles discretos. A interface continua em português.

- **Diária:** prioridades, planos do dia, humor, oito marcadores de água, autocuidado, gratidão, atividades/anotações e planos de amanhã.
- **Semanal:** segunda a domingo, número e intervalo da semana, prioridades, lista de tarefas, acompanhamento de hábitos, intenção e notas.
- **Responsividade:** duas colunas a partir de 600 dp de largura útil; seções empilhadas em celulares. Água e hábitos preservam áreas de toque confortáveis. A tabela de hábitos permite rolagem horizontal quando necessário.
- **Fontes:** famílias provisórias em `PlannerFonts`; estilos visuais em `PlannerPaper`, ambos no tema. Nenhum download de fonte é necessário para executar o aplicativo. As fontes definitivas poderão ser incluídas em `res/font`.
- **Persistência:** Room v2 acrescenta os campos do diário e `weekly_habits`. A migration 1→2 mantém tarefas, notas, intenção e humor da versão anterior. Os campos novos são independentes; cada hábito pertence à semana em que foi criado.

## Verificação visual

O teste local utiliza renderização nativa do Robolectric. Capturas em `verification/local/`, ignoradas pelo Git:

- `daily-empty.png`, `daily-task.png`, `daily-journal.png`: celular de 360 × 800 dp.
- `weekly.png`: visão semanal em celular.
- `daily-tablet.png`, `weekly-tablet.png`: páginas em 840 × 1180 dp, com duas colunas.

O teste de tablet verifica que as seções auxiliares estão à direita das prioridades e que os sete dias da semana permanecem visíveis. O teste de persistência abre o schema v1 publicado, aplica a migração e verifica os registros existentes. Outros testes fecham e reabrem o banco para conferir água, gratidão, autocuidado e hábitos, incluindo marcações de segunda e domingo, edição, exclusão e isolamento entre semanas.

## Ambiente local

Arquivos do SDK temporário haviam sido removidos desde a última validação. Foram recuperados os componentes do SDK, suas licenças e o emulador. O bootstrap foi corrigido para restaurar as ferramentas em uma pasta já existente.

Foi criado o AVD `PlannerApp_Paper_API_35`, Android 15 / API 35. Os arquivos do AVD anterior foram preservados. A tarefa local do VS Code **Planner: abrir no emulador** aponta para o novo AVD. Para abrir manualmente:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-emulator.ps1 -AvdName PlannerApp_Paper_API_35 -InstallApp
```

No EmbeDroid, atualize a lista de dispositivos e abra a visualização de `PlannerApp_Paper_API_35`. O aplicativo aparece como **Meu Planner**; o login oferece **Experimentar com conta demo**.

Uma falha de navegação ao recriar a Activity foi encontrada no teste instrumentado. O redirecionamento de sessão agora espera o NavHost disponibilizar uma entrada antes de acessar seu grafo.

## Resultados finais

| Verificação | Resultado |
| --- | --- |
| `assembleDebug` | APK gerado com sucesso |
| `test` | 26 testes em debug e 24 em release, sem falhas |
| `lintDebug` | Nenhum erro; 14 avisos de atualização de dependências |
| `connectedDebugAndroidTest` | 1 cenário completo passou no Android 15 / API 35 |
| Toque nativo via ADB/UI Automator | Editor de anotação abriu; logout retornou ao login; novo login demo funcionou |
| Assinatura do APK | Assinatura v2 verificada com `apksigner` |

O cenário instrumentado verifica CRUD de tarefas, conclusão, dia/semana/mês, água, gratidão, criação/edição/marcação/exclusão de hábitos, restauração após recriar a Activity, logout e novo login. A abertura da anotação e o logout usam a ação semântica de acessibilidade no teste Compose; seus toques foram conferidos separadamente no Android com UI Automator e eventos nativos do ADB.

O app foi reinstalado ao final e deixado aberto no emulador para inspeção. As capturas nativas incluem `emulator-note-dialog.png`, `emulator-daily-final.png` e `emulator-weekly-final.png`, em `verification/local/`.

APK: `app/build/outputs/apk/debug/app-debug.apk`.

SHA-256: `EE26A3AF39B57C04710D3834B6BC63EC3985475A2667F1C485CB92E5C382AF4F`.

Não houve teste em celular físico nesta etapa. As fontes definitivas ainda serão fornecidas pelo autor. Hábitos não têm recorrência automática ou sincronização.
