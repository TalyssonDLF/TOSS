# TOSS Control Protocol v0.1

Contrato local entre interfaces remotas (tablet/celular/web) e o TOSS Projector.

## Princípios
- local-first; nenhuma nuvem é necessária para controlar o projetor;
- transporte primário planejado: Wi-Fi/LAN com HTTP + WebSocket;
- descoberta planejada: mDNS;
- pareamento autenticado por QR/PIN e token por dispositivo;
- a interface remota nunca acessa `DeviceAdapter` diretamente;
- toda ação passa por `TOSS Core -> ProjectorControl/SystemControl/MediaControl`;
- Bluetooth poderá ser um backend secundário para comandos básicos se o hardware permitir.

## Envelope
```json
{
  "version": 1,
  "id": "cmd-123",
  "type": "command",
  "action": "projector.brightness.set",
  "payload": { "value": 72 }
}
```

Resposta:
```json
{
  "version": 1,
  "id": "cmd-123",
  "type": "result",
  "ok": true,
  "state": { "projector.brightness": 72 }
}
```

## Ações iniciais
- `input.key` — up/down/left/right/ok/back/home
- `input.text` — entrada de texto remoto
- `system.volume.set`
- `system.power.sleep`
- `projector.focus.set`
- `projector.keystone.set`
- `projector.brightness.set`
- `projector.pictureMode.set`
- `projector.source.set`
- `media.play`, `media.pause`, `media.seek`
- `media.open`

## Estado sincronizado
O host publica snapshots/deltas com: conexão, volume, fonte, brilho, foco, keystone, modo de imagem e `nowPlaying`.

## Segurança
A implementação real deve vincular tokens ao projetor pareado, limitar acesso à rede local, validar payloads e oferecer revogação de dispositivos. Segredos nunca entram no repositório.
