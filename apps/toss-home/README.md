# TOSS Home — v0.1 visual baseline

Primeiro protótipo executável da Home do TOSS para projetores Android.

## Direção visual

- interface cinematográfica escura;
- roxo/violeta como cor principal;
- sem branding TOSS no canto superior esquerdo;
- sem saudação na Home;
- status mínimo no canto superior direito;
- hero `Continuar assistindo`;
- atalhos principais: Cinema, YouTube, HDMI, Espelhar, Arquivos, Apps, Música e Navegador;
- foco D-pad com borda e glow violeta;
- ações `Projetor` e `Ajustes` no rodapé.

## Implementação atual

A v0.1 usa um `View`/`Canvas` Android nativo e não possui dependências de UI externas. Isso cria uma baseline extremamente leve para o hardware do projetor.

**Isso não fecha a decisão Views vs. Compose.** Quando o HY320 estiver disponível, criaremos uma implementação equivalente em Compose e mediremos cold start, PSS/RAM, frame time, jank e resposta ao D-pad.

O layout usa uma área lógica de 1280×720 e escala proporcionalmente para a tela real.

## Navegação

A Home já responde a:

- D-pad ↑ ↓ ← →
- DPAD_CENTER / Enter

Os destinos ainda são placeholders e exibem uma mensagem curta. A integração real será feita através do TOSS Core/Capability Layer, nunca chamando APIs específicas do HY320 diretamente da UI.

## Build

Abra a raiz do repositório no Android Studio e execute o módulo `apps:toss-home`.

O manifesto registra o app como launcher normal, Leanback launcher e candidato a `HOME`, mas durante desenvolvimento não desabilite o launcher OEM do projetor.
