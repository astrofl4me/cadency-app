# Refinamento do Cadency — 22/10/2026

## Base preservada

Git limpo em `b8da8f8`; o build da versão existente passou antes das alterações (`assembleDebug`, 59 s). Mantidos package `br.edu.fsa.planner`, módulo `app`, Room, DataStore e navegação em uma Activity. Hábitos, etapas, rollover, Modo Agora, foco, Meu Ritmo e planner diário/semanal/mensal permanecem no mesmo projeto.

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
