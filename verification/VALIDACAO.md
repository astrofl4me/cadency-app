# Registro de validação — PlannerApp

Verificação realizada em **3 de outubro de 2026**, no Windows, com JDK 17 e SDK Android 36 preparados localmente. O projeto usa o package `br.edu.fsa.planner`, versão 1.0.0, e requer Android 8.0 / API 26 ou superior.

## Resultado final

| Verificação | Resultado |
| --- | --- |
| `assembleDebug` | Sucesso; APK gerado |
| `testDebugUnitTest` | 21 testes, nenhuma falha ou teste ignorado |
| `test` | Sucesso: 21 testes em debug e 20 em release |
| `lintDebug` | Sucesso: nenhum erro; 14 avisos de versões mais recentes disponíveis |
| `assembleDebugAndroidTest` | Sucesso; APK instrumentado compilado |
| Assinatura do APK debug | Verificada com `apksigner`; assinatura v2 válida |
| Execução em Android conectado | Pendente; nenhum dispositivo disponível |

Os 20 testes de regras e persistência são executados nas duas variantes. O teste de interface com Robolectric é específico de debug; utiliza a Activity fornecida por `ui-test-manifest`, sem acrescentar essa Activity ao aplicativo release.

Os avisos do lint sugerem atualizar AGP ou bibliotecas. As versões foram mantidas fixas no conjunto que compilou e passou nos testes. O Gradle também informa que `android.overridePathCheck` é experimental; essa opção atende ao caminho original do projeto, que contém acentos.

## Cobertura dos testes

| Classe | Quantidade em debug | O que verifica |
| --- | --- | --- |
| `PlannerDatesTest` | 6 | Grid mensal, anos bissextos, início no domingo, semana de segunda a domingo, virada de ano e filtros inclusivos |
| `PlannerFormTest` | 7 | Campos obrigatórios, limites, datas e horários inválidos, horário opcional, ordem início/fim e preservação de identidade ao editar |
| `PlannerPersistenceTest` | 5 | Room real em arquivo: CRUD reativo, consultas por data/mês/status, fechamento e reabertura do banco, planos de amanhã e notas/humor |
| `LocalAuthRepositoryTest` | 2 | Login inválido, sessão persistida, reabertura do DataStore e logout |
| `PlannerUiHostTest` | 1 | Login, criação, edição, conclusão, exclusão, navegação e logout pela interface Compose |

O calendário é verificado para diferentes meses nos anos 1900, 2000, 2024, 2025, 2026 e 2100. Os testes de persistência fecham e reabrem arquivos reais; os dados essenciais não dependem de estado em memória.

O teste local de UI usa Robolectric API 35, renderização nativa e uma tela lógica de 360 × 800 dp. Capturas de login, dia vazio, editor, dia com tarefa, semana, mês e perfil foram geradas e inspecionadas. Elas ficam em `verification/local/` e são regeneradas pelo teste; não são versionadas.

### Limites da verificação local

Robolectric não substitui um Android físico. Nesse ambiente, os botões fixos de salvar e logout são acionados por sua ação semântica de acessibilidade; o teste instrumentado utiliza toques. As capturas usam a renderização da View em Canvas porque PixelCopy depende de um display real.

Nos testes do Windows, o DataStore usa `OkioStorage` com o mesmo formato de Preferences, devido à diferença de substituição atômica de arquivos nesse sistema. Essa dependência é exclusiva dos testes. No aplicativo Android, a implementação utiliza o Preferences DataStore padrão.

`PlannerFlowTest`, em `src/androidTest/`, foi compilado e está preparado para percorrer o fluxo com toques e recriar a Activity para conferir restauração. **Ele ainda não foi executado em dispositivo.**

## Artefatos

- APK do aplicativo: `app/build/outputs/apk/debug/app-debug.apk`.
- Tamanho: **21.184.112 bytes**.
- SHA-256: `F8CEEB4353E0E002113F683431D000EF1D4DC5C6A13613CAF7C0A16E886611C7`.
- APK dos testes: `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`.
- Relatórios: `app/build/reports/tests/testDebugUnitTest/`, `app/build/reports/tests/testReleaseUnitTest/` e `app/build/reports/lint-results-debug.html`.
- Resultados XML: `app/build/test-results/`.

Os artefatos de build são locais e ignorados pelo Git. O APK é assinado para desenvolvimento; não é uma publicação em loja.

## Reproduzir neste computador

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1 -Tasks test
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1 -Tasks lintDebug
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1 -Tasks assembleDebugAndroidTest
```

O script cria aliases por junction em `%TEMP%` para contornar o problema de classpaths com acentos do Java 17 no Windows. Todos apontam para o projeto e o cache originais.

## Validação Android que falta concluir

O Android SDK, adb, emulator e a imagem API 35 foram instalados. Não há hipervisor disponível no computador. As tentativas sem aceleração encerraram antes de disponibilizar um dispositivo ao adb; não houve instalação ou execução do app no emulador. Nenhum driver ou configuração global de virtualização foi alterado.

Com um celular autorizado por depuração USB, ou um AVD inicializado:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/device.ps1 devices -l
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/device.ps1 install -r app/build/outputs/apk/debug/app-debug.apk
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/device.ps1 shell am start -n br.edu.fsa.planner/.MainActivity
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1 -Tasks connectedDebugAndroidTest
```

Depois, siga o roteiro de aceitação do README. Confirme especialmente persistência após forçar parada e reabrir o aplicativo, login após logout, planos de amanhã, navegação entre períodos, TalkBack, teclado e fontes ampliadas. Esses checks finais em Android continuam pendentes.
