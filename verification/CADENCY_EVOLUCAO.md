# Evolução do Cadency

## Base e decisões

A base existente compilou antes das alterações (`assembleDebug`, 40 tarefas atualizadas). O Git estava limpo em `ea91f50`. O package `br.edu.fsa.planner`, o módulo `app`, o login e as visões diária/semanal/mensal são preservados. O nome exibido passará a Cadency sem criar outro projeto.

A arquitetura continua UI → ViewModel → Repository → Room/DataStore. Os repositories de hábitos, foco e preferências serão específicos porque têm históricos e regras próprias. O `PlannerViewModel` permanece responsável pelas visões existentes.

## Banco v3

A migration explícita 2→3 adiciona `habits`, `habit_check_ins`, `subtasks` e `focus_sessions`, além de campos de origem, adiamentos, Inbox, descarte e conclusão em `planner_items`. A cadeia 1→2→3 preserva instalações antigas. Subtarefas usam exclusão em cascata; sessões de foco preservam o histórico com referência à tarefa anulada quando ela é excluída.

Os hábitos semanais legados continuam na tabela original e são copiados para hábitos com início/fim limitados à semana original, incluindo cada marcação. Assim, a migração não inventa recorrência para registros antigos. Novos hábitos poderão continuar entre semanas. As marcações da visão semanal serão integradas à mesma fonte de dados da nova aba Hábitos.

Preferências de aparência e revisão diária continuam no DataStore; não há preferências visuais no Room. Não será usada migração destrutiva.
