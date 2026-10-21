# TOSS Control

Companion Android do ecossistema TOSS para tablet/celular.

## Fase 1 implementada

O companion agora possui APK próprio e conversa diretamente com o TOSS Home pela rede local, sem expor ADB ou root. O projetor mostra IP, porta e um PIN de seis dígitos em **Ajustes > Controle remoto**.

Controles atuais:
- D-pad, OK, Home, Menu e Voltar;
- YouTube, HDMI, Apps e Ajustes;
- Projetor, Cinema, Arquivos e Espelhar;
- foco +/− e volume +/−.

O protocolo v0 usa TCP local na porta `47990`, mensagem `TOSS1|PIN|COMMAND`. O servidor rejeita clientes fora de endereços locais/link-local. O foco continua dependendo do bridge privilegiado do HCS350; quando o backend negar a ação, o tablet mostra o erro em vez de simular sucesso.

## Arquitetura

```text
TOSS Control (tablet)
    |
TCP LAN + PIN
    |
TOSS Projector / RemoteControlServer
    |
TOSS Core
    |
ProjectorControl / Android capability fallbacks
    |
HCS350 Device Adapter
```

Na Fase 2 o protocolo será evoluído para descoberta, estado sincronizado e controles TOSS nativos, mantendo a UI do tablet independente do modelo de projetor.
