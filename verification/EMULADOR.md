# Configuração do emulador no VS Code

Atualização de **3 de outubro de 2026**.

## Diagnóstico e correções

- EmbeDroid 1.0.2 já estava instalado no VS Code.
- O SDK estava preparado em `%TEMP%\PlannerApp-android-sdk`, fora dos caminhos que a extensão detecta automaticamente. `embedroid.sdkPath` foi configurado em `.vscode/settings.json` local.
- O AVD existente estava registrado somente na pasta própria do SDK. Seu arquivo de registro foi copiado para `%USERPROFILE%\.android\avd\PlannerApp_API_35.ini`, sem mover ou apagar dados.
- O Intel Core i5-12400F apresentou VT-x e SLAT disponíveis, com virtualização já habilitada na BIOS.
- `HypervisorPlatform` estava desabilitado. A ativação com autorização administrativa concluiu com sucesso, com **`RestartNeeded: true`**.
- O script antigo forçava `-accel off`. O script corrigido utiliza aceleração, confere o AVD, aguarda o Android concluir o boot e permite instalar/abrir o APK.

O Windows não foi reiniciado automaticamente. **A execução do aplicativo no emulador ainda depende do reinício e da verificação posterior do boot.**

## Após reiniciar o Windows

Na pasta raiz do projeto:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-emulator.ps1 -InstallApp
```

No VS Code, abra o painel Android do EmbeDroid, atualize a lista e escolha **Open Embedded View** para `PlannerApp_API_35` em **Running Devices**. A tarefa local **Planner: abrir no emulador** executa o mesmo comando.

Depois de um dispositivo aparecer em `adb devices`, o teste de interface pode ser executado com:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1 -Tasks connectedDebugAndroidTest
```

## Evidências locais

O resultado da ativação fica em `verification/local/acceleration-result.json`. Os logs de inicialização ficam em `verification/local/emulator-out.log` e `emulator-error.log`. Esses arquivos e as configurações de VS Code são locais e não são versionados.

Os scripts PowerShell foram conferidos pelo parser e a listagem do AVD foi verificada. A configuração utiliza o [Windows Hypervisor Platform recomendado pelo Android](https://developer.android.com/studio/run/emulator-acceleration); nenhuma proteção do sistema foi desativada.
