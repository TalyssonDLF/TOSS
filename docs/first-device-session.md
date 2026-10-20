# Primeira sessão com o projetor — TOSS Phase 0

Objetivo: identificar o hardware e coletar evidências sem alterar firmware, partições ou segurança.

## Antes de conectar

No PC:
1. instalar Android Platform Tools (ADB);
2. ter um cabo USB de dados disponível;
3. clonar este repositório;
4. abrir PowerShell na raiz do TOSS.

No projetor:
1. não restaurar, atualizar firmware ou desbloquear bootloader;
2. ativar opções do desenvolvedor/USB debugging apenas se o firmware oferecer a opção;
3. não instalar ferramentas de root.

## Primeiro teste

```powershell
adb devices -l
```

Se aparecer `unauthorized`, aceitar a chave RSA na tela do projetor e repetir. Se não aparecer dispositivo, não forçar drivers/flash: investigar primeiro USB vs ADB por rede.

Quando o estado for `device`:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\toss-device-survey\survey.ps1
```

O script é intencionalmente passivo/read-only. Ele não executa root, remount, unlock, flash, package disable, alteração de SELinux ou escrita em sysfs.

## Depois do survey automático

Preservar a pasta gerada. A sessão interativa seguinte deve mapear, nesta ordem:

1. identidade: SoC, board, Android, kernel, RAM/storage;
2. partições, AVB/boot state e opções reais de recovery/backup;
3. packages/services OEM;
4. teclas do controle com `getevent -l`;
5. `logcat` enquanto foco, keystone e troca para HDMI são acionados no sistema original;
6. MediaCodec/Codec2/OMX e aceleração de vídeo;
7. thermal/cooling;
8. interfaces candidatas: Intent, Binder, JNI, propriedades, sysfs e vendor services.

## Regra de parada

Durante Phase 0, não executar `fastboot flashing unlock`, `adb root`, `adb remount`, `dd` de escrita, `setenforce 0`, escrita em `/sys`, remoção/desativação de packages OEM ou qualquer ferramenta de flash.

A primeira meta não é modificar o HY320. É produzir evidência suficiente para criar o primeiro `DeviceAdapter` correto e um plano de recuperação antes de qualquer modificação de baixo nível.
