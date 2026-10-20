# TOSS Control

Companion do ecossistema TOSS para tablet/celular.

## v0.1 planejado

A primeira interface terá:
- seleção/estado do projetor conectado;
- Now Playing;
- D-pad remoto;
- touchpad e teclado;
- volume;
- atalhos Cinema, HDMI, Apps e Arquivos;
- foco, keystone, brilho e perfil de imagem;
- envio de arquivos via TOSS Share;
- navegação inferior: Controle, Projetor, Biblioteca e Ajustes.

## Arquitetura

```text
TOSS Control
    |
Control Protocol
    |
Wi-Fi/LAN (Bluetooth opcional)
    |
TOSS Projector
    |
TOSS Core
    |
ProjectorControl / SystemControl / MediaControl
    |
Device Adapter
```

A UI do tablet não deve conhecer detalhes do HY320 ou de qualquer outro projetor. Isso permite que o mesmo TOSS Control controle futuros TOSS Projector, TOSS Box e outros dispositivos.

A implementação Android do companion será iniciada após estabilizarmos o contrato do protocolo e a navegação visual. O preview pode ser desenvolvido independentemente do hardware.
