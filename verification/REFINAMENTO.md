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

## Heatmaps e detalhes de hábitos

`HeatmapRules` calcula uma matriz de 12 semanas (84 células), de segunda a domingo, incluindo os dias futuros da semana atual como estado próprio. O período individual oferece 3 meses e, quando o histórico permite, 6 meses/1 ano. Datas anteriores à criação, posteriores ao arquivamento e dias fora da programação ficam neutros. Mini mapas da lista mostram quatro semanas (28 células).

Cada check-in é deduplicado por hábito/data. Para hábitos DAILY, o denominador é a quantidade prevista no dia. Hábitos WEEKLY flexíveis só entram no denominador nos dias com check-in; os outros dias elegíveis são neutros, sem obrigação diária. Assim o mapa não transforma a meta “3 vezes por semana” em sete faltas.

Intensidades: 0, 1–25%, 26–50%, 51–75%, 76–99%, 100%. As tonalidades derivam da cor escolhida; a personalidade controla raio e espaçamento. Cada célula tem data/estado acessível. A seleção mostra os valores por texto e também pode ser percorrida pelos botões de dia anterior/seguinte. Símbolos distinguem estados sem depender só da cor.

`HabitDetailsViewModel` combina definições, histórico e período, calculando consistência e sequência DAILY. A tela preserva acesso à edição, arquivamento/exclusão e foco. Os dados vêm das mesmas tabelas de hábitos/check-ins, sem nova tabela ou migration nesta fase.

## Validação por fase

| Fase | Resultado |
| --- | --- |
| Base | assembleDebug passou antes das alterações. |
| A | assembleDebug e três testes de aparência/persistência/contraste passaram. |
| B | assembleDebug e três testes de heatmap passaram, incluindo células neutras, futuras, percentuais e hábitos flexíveis. |
| C | assembleDebug e testes de Pomodoro, armazenamento e preferências passaram. O teste com relógio controlado percorreu dois ciclos, pausas, reabertura do banco e pausa longa, contando só os minutos de foco. |
| D | assembleDebug e testes de regras, cache, favoritas, frases próprias, parser, preferências e Room passaram. |

Logs locais em `verification/local/refinement-*.log`; fontes/licenças são versionadas. O compilador Kotlin roda no processo Gradle para evitar o daemon Kotlin que não conseguia conectar no ambiente Windows; não há alteração global de Java ou de configuração do sistema.

## Pomodoro e hábitos

Focus Flow mantém Fluxo livre e adiciona Pomodoro 25/5, 50/10 ou configuração própria (foco, pausa curta, ciclos, pausa longa). Preferências: `default_focus_mode`, `pomodoro_focus_minutes`, `pomodoro_short_break_minutes`, `pomodoro_cycles`, `pomodoro_long_break_minutes`.

`FocusRules` calcula a etapa atual; `PomodoroRules` faz foco → pausa curta → próximo ciclo e último foco → pausa longa → fim. O início da próxima etapa é explícito, sem somar ciclos inteiros enquanto o app fica fechado. Pausas manuais e pausas do Pomodoro não entram nos minutos de foco. O estado, as durações e o progresso ficam no Room, independentes de alterações futuras nas preferências.

Migration **3→4** adiciona vínculo opcional `habitId` com FK SET_NULL, modo, fase, ciclo, durações e tempo da fase. Sessões antigas são Fluxo livre e preservam o tempo já registrado. Excluir um hábito mantém o histórico de foco e o título original. A tarefa e o hábito são vínculos alternativos, não simultâneos.

Ao encerrar uma sessão de hábito com tempo registrado e ocorrência prevista hoje, o app pergunta se deseja marcar o hábito. “Agora não” não altera check-ins; confirmar usa uma operação idempotente. O hábito nunca é concluído automaticamente.

## Cadency Quote

`QuoteRemoteDataSource` usa HTTPS nativo do Android, com coroutines/Dispatchers.IO, limites de tamanho e timeouts; não foi adicionada biblioteca de rede. `QuoteRepository` é a única entrada para a UI, com coleção Room, fallback local e rotação persistente. A tela Hoje não aguarda a rede.

Migration **4→5** adiciona `quotes` e índices por categoria/favorito. IDs remotos são hashes estáveis do texto/autor; atualização de cache preserva favoritos. Até 200 frases remotas não favoritas são mantidas, além de favoritas, coleção local e frases próprias. Os oito textos de fallback são conteúdo original do Cadency, em `assets/quotes_pt_br.json`, com duas opções por categoria.

O refresh lê `/categorias` e `/todas` em páginas de 50, no máximo quatro páginas. Atualizações bem-sucedidas têm intervalo mínimo de 24 horas; falhas têm espera de uma hora. “A cada abertura” e “Outra frase” rotacionam o cache sem request obrigatório. A sequência percorre as frases elegíveis e evita repetir a atual quando existe alternativa.

Mapeamento local: Paz/Saúde/Felicidade → Calma; Reflexão/Provérbios → Reflexiva; Motivação/Inspiração/Sonhos/Sucesso/Mudança/Oportunidade/Trabalho → Motivacional. Textos desses dois últimos grupos com termos de constância, persistência, disciplina, hábito, prática ou esforço podem compor Disciplina. Categorias não mapeadas só participam de Aleatória. A categoria original continua registrada/exibida; não há endpoint fictício de Disciplina.

Frequências: a cada abertura, 6 horas, diariamente (mudança de data no fuso do aparelho), 3 dias, semanal e somente manual. Trocar a categoria explicitamente escolhe uma frase compatível, inclusive no modo manual. Preferências: `preferred_quote_category`, `quote_refresh_mode`, `current_quote_id`, `last_quote_rotation_at`, `last_quote_remote_attempt_at`, `last_quote_remote_success_at`.

Favoritas permanecem no banco offline. A tela também mantém frases próprias, com texto obrigatório e autor opcional. Frases pessoais ficam locais e entram como favoritas inicialmente; retirar o favorito não apaga a frase própria. O card segue a tipografia/superfície da personalidade; em Focus fica no fim da página e recolhido, com ação para ler. O conteúdo não muda conforme a personalidade.
