package com.example.mypianoapp.data

// ── Modèles ────────────────────────────────────────────────────────────────

data class LessonStep(
    val type: StepType,
    val title: String,
    val body: String,                  // texte principal
    val highlight: String? = null,     // encadré "À retenir" ou "Conseil"
    val exercise: Exercise? = null     // exercice pratique associé
)

enum class StepType { THEORY, EXERCISE, TIP }

data class Exercise(
    val instruction: String,
    val notes: List<String> = emptyList(),   // notes à jouer (ex: ["Do", "Ré", "Mi"])
    val duration: String = "",               // ex: "30 secondes"
    val goal: String = ""                    // ce qu'on valide
)

data class LessonData(
    val id: Int,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val duration: String,
    val xpReward: Int,
    val objective: String,              // objectif en une phrase
    val steps: List<LessonStep>,
    val recap: String,                   // résumé de fin de leçon
    val quiz: LessonQuiz? = null         // QCM optionnel (leçons 3, 6, 9)
)

// ── Parcours complet : 10 leçons "Zéro absolu" ────────────────────────────

object LessonCatalog {

    val lessons: List<LessonData> = listOf(

        // ── LEÇON 1 ──────────────────────────────────────────────────────
        LessonData(
            id = 1,
            title = "Découverte du clavier",
            subtitle = "Comprendre la disposition des touches",
            emoji = "🎹",
            duration = "8 min",
            xpReward = 20,
            objective = "Identifier les touches blanches et noires, et comprendre leur organisation en groupes.",
            steps = listOf(
                LessonStep(
                    type = StepType.THEORY,
                    title = "Les touches blanches et noires",
                    body = "Un clavier de piano est composé de touches blanches et de touches noires. Les touches noires sont toujours groupées en blocs de 2 et de 3, et ce motif se répète tout le long du clavier.\n\nCette alternance est ta boussole : elle te permet de te repérer où que tu sois sur le clavier.",
                    highlight = "À retenir : cherche les groupes de 2 touches noires — la touche blanche juste à gauche du groupe de 2 est toujours un Do."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "Les 7 notes",
                    body = "Les touches blanches correspondent aux 7 notes de base de la musique occidentale :\n\nDo — Ré — Mi — Fa — Sol — La — Si\n\nAprès le Si, on recommence avec un nouveau Do, plus aigu. Cette répétition s'appelle une octave.",
                    highlight = "L'oreille perçoit un Do aigu comme le \"même\" Do mais plus haut. Ce n'est pas une nouvelle note, c'est la même note à une octave de distance."
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : trouver tous les Do",
                    body = "Regarde le clavier de l'application. Repère tous les groupes de 2 touches noires, puis identifie la touche blanche immédiatement à leur gauche.",
                    exercise = Exercise(
                        instruction = "Appuie sur chaque Do du clavier, du plus grave au plus aigu.",
                        notes = listOf("Do"),
                        duration = "1 minute",
                        goal = "Localiser les Do sans hésiter"
                    )
                ),
                LessonStep(
                    type = StepType.TIP,
                    title = "Conseil de pratique",
                    body = "Au début, prononce le nom des touches à voix haute pendant que tu les joues. Cette association son-nom s'ancre beaucoup plus vite dans ta mémoire.",
                    highlight = "Dis \"Do\" en appuyant, \"Ré\" en appuyant... Cette technique s'appelle la verbalisation active."
                )
            ),
            recap = "Tu connais maintenant la logique du clavier : 7 notes blanches qui se répètent, des groupes de touches noires comme repères visuels, et le Do toujours à gauche du groupe de 2."
        ),

        // ── LEÇON 2 ──────────────────────────────────────────────────────
        LessonData(
            id = 2,
            title = "Position des mains",
            subtitle = "Posture et placement correct",
            emoji = "🤲",
            duration = "7 min",
            xpReward = 20,
            objective = "Adopter une posture saine qui évite les tensions et permet de jouer longtemps sans douleur.",
            steps = listOf(
                LessonStep(
                    type = StepType.THEORY,
                    title = "La posture assise",
                    body = "Assieds-toi face au piano, dos droit mais sans rigidité. Tes coudes doivent être légèrement plus hauts que le clavier — règle ton siège en conséquence.\n\nTes pieds sont à plat sur le sol. Les épaules sont relâchées, pas remontées vers les oreilles.",
                    highlight = "Teste toi : si tu peux laisser tes bras tomber le long du corps et qu'ils arrivent naturellement au niveau du clavier, ta hauteur de siège est bonne."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "La forme de la main",
                    body = "Imagine que tu tiens une balle de tennis dans ta main. Cette courbure naturelle des doigts est exactement la position qu'on cherche au piano.\n\nLes doigts sont arrondis, le poignet est dans le prolongement de l'avant-bras — ni trop haut, ni trop bas. Les ongles courts sont indispensables : un ongle long force le doigt à se coucher, ce qui crée des tensions.",
                    highlight = "Main = balle de tennis. Répète cette image mentale chaque fois que tu t'installes au clavier."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "La numérotation des doigts",
                    body = "En piano, chaque doigt porte un numéro :\n\n1 = pouce\n2 = index\n3 = majeur\n4 = annulaire\n5 = auriculaire\n\nCette numérotation est universelle — elle apparaît dans toutes les partitions du monde, au-dessus ou en dessous des notes.",
                    highlight = "Les partitions écrivent souvent \"1\" au-dessus d'une note pour dire : joue cette note avec le pouce."
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : lever les doigts un par un",
                    body = "Pose les deux mains sur tes genoux dans la position \"balle de tennis\". Sans bouger le reste de la main, lève lentement le doigt 1 (pouce), puis repose-le. Puis le 2, le 3, le 4, le 5. Répète pour les deux mains.",
                    exercise = Exercise(
                        instruction = "Lève chaque doigt indépendamment en gardant les autres posés.",
                        duration = "2 minutes",
                        goal = "Bouger chaque doigt de façon isolée"
                    )
                )
            ),
            recap = "Dos droit, coudes au niveau du clavier, mains en forme de balle de tennis, doigts numérotés de 1 (pouce) à 5 (auriculaire). Cette posture est ton point de départ à chaque session."
        ),

        // ── LEÇON 3 ──────────────────────────────────────────────────────
        LessonData(
            id = 3,
            title = "Les 5 premières notes",
            subtitle = "Do Ré Mi Fa Sol — main droite",
            emoji = "✋",
            duration = "10 min",
            xpReward = 25,
            objective = "Jouer Do Ré Mi Fa Sol avec la main droite, un doigt par note, de façon fluide.",
            steps = listOf(
                LessonStep(
                    type = StepType.THEORY,
                    title = "Le Do central et le placement",
                    body = "Le Do central est le Do situé au milieu du clavier — c'est ton point de référence universel. Place le pouce (doigt 1) dessus.\n\nEnsuite, pose naturellement tes 4 autres doigts sur les 4 touches qui suivent : index (2) sur Ré, majeur (3) sur Mi, annulaire (4) sur Fa, auriculaire (5) sur Sol.\n\nChaque doigt repose sur sa touche sans appuyer — juste posé.",
                    highlight = "Cette position s'appelle la \"position de 5 doigts\". C'est la base de 90 % des exercices débutants."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "Jouer une note correctement",
                    body = "Pour produire un beau son, le doigt doit s'appuyer sur la touche avec le bout du doigt (la pulpe), pas avec la tranche. La touche doit descendre complètement, d'un mouvement ferme mais décontracté.\n\nRelève le doigt aussitôt après — ne laisse pas le doigt appuyé. Le son continue grâce au mécanisme du piano.",
                    highlight = "Un son étouffé vient souvent d'un doigt qui reste posé sur la touche au lieu de se relever."
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : Do Ré Mi Fa Sol en montant",
                    body = "Joue Do → Ré → Mi → Fa → Sol avec les doigts 1 → 2 → 3 → 4 → 5. Très lentement. Prononce chaque note à voix haute en la jouant.\n\nPuis descends : Sol → Fa → Mi → Ré → Do avec les doigts 5 → 4 → 3 → 2 → 1.",
                    exercise = Exercise(
                        instruction = "Monte et descends 5 fois de suite sans s'arrêter, en gardant le même tempo.",
                        notes = listOf("Do", "Ré", "Mi", "Fa", "Sol"),
                        duration = "3 minutes",
                        goal = "Enchaîner les 5 notes sans regarder ses doigts"
                    )
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : note aléatoire",
                    body = "Ferme les yeux. Pense à une note parmi les 5 (Do, Ré, Mi, Fa, Sol). Pose la main en position, puis joue uniquement cette note. Ouvre les yeux pour vérifier.",
                    exercise = Exercise(
                        instruction = "Répète 10 fois avec des notes différentes.",
                        notes = listOf("Do", "Ré", "Mi", "Fa", "Sol"),
                        duration = "2 minutes",
                        goal = "Trouver chaque note instinctivement"
                    )
                )
            ),
            recap = "Tu sais placer ta main droite en position de 5 doigts sur Do-Ré-Mi-Fa-Sol, et jouer chaque note proprement. C'est la fondation de tout ce qui suit.",
            quiz  = quiz3
        ),

        // ── LEÇON 4 ──────────────────────────────────────────────────────
        LessonData(
            id = 4,
            title = "Le rythme et la pulsation",
            subtitle = "Noires, blanches et rondes",
            emoji = "🥁",
            duration = "10 min",
            xpReward = 25,
            objective = "Comprendre les valeurs rythmiques de base et jouer avec un tempo régulier.",
            steps = listOf(
                LessonStep(
                    type = StepType.THEORY,
                    title = "La pulsation : le cœur de la musique",
                    body = "La pulsation, c'est le battement régulier qui soutient toute la musique. Comme un métronome, comme un cœur qui bat. Tout musicien développe ce sens intérieur de la pulsation.\n\nLe tempo est la vitesse de cette pulsation. On le mesure en BPM (battements par minute). 60 BPM = 1 battement par seconde.",
                    highlight = "Tapote la table avec un doigt de façon régulière. Ce battement régulier, c'est la pulsation. Tout part de là."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "Les 3 valeurs de base",
                    body = "En musique, chaque note a une durée. Les 3 valeurs fondamentales sont :\n\n🎵 La noire — dure 1 temps (1 battement)\n🎵 La blanche — dure 2 temps (2 battements)\n🎵 La ronde — dure 4 temps (4 battements)\n\nConcrètement : si tu joues Do en noire, tu le tiens 1 battement. En blanche, 2 battements. En ronde, tu tiens la touche pendant 4 battements.",
                    highlight = "Moyen mnémotechnique : Noire = 1, Blanche = 2 (le double), Ronde = 4 (le double encore)."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "La mesure à 4 temps",
                    body = "La musique est organisée en mesures. La mesure à 4 temps (la plus courante) contient 4 noires, ou l'équivalent : 2 blanches, ou 1 ronde.\n\nOn compte : \"1 — 2 — 3 — 4 — 1 — 2 — 3 — 4...\"\n\nLe temps 1 est légèrement accentué — c'est le temps fort.",
                    highlight = "Frappe la table en comptant 1-2-3-4 régulièrement, en appuyant un peu plus fort sur le 1."
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : jouer Do en différentes valeurs",
                    body = "Compte à voix haute : \"1 — 2 — 3 — 4\". Joue Do :\n• En noires : une frappe par temps (4 Do en une mesure)\n• En blanches : une frappe tenue 2 temps (2 Do en une mesure)\n• En ronde : une frappe tenue 4 temps (1 Do en une mesure)",
                    exercise = Exercise(
                        instruction = "Répète chaque valeur 4 mesures de suite, en comptant à voix haute.",
                        notes = listOf("Do"),
                        duration = "3 minutes",
                        goal = "Tenir chaque note exactement la bonne durée"
                    )
                )
            ),
            recap = "La pulsation est le cœur de la musique. Noire = 1 temps, Blanche = 2 temps, Ronde = 4 temps. Une mesure à 4/4 contient 4 temps. Toujours compter !"
        ),

        // ── LEÇON 5 ──────────────────────────────────────────────────────
        LessonData(
            id = 5,
            title = "La gamme de Do",
            subtitle = "Les 8 notes à la main droite",
            emoji = "🎼",
            duration = "12 min",
            xpReward = 30,
            objective = "Jouer la gamme de Do complète (Do à Do) à la main droite avec le bon doigté.",
            steps = listOf(
                LessonStep(
                    type = StepType.THEORY,
                    title = "Qu'est-ce qu'une gamme ?",
                    body = "Une gamme est une série de 8 notes jouées dans l'ordre, du plus grave au plus aigu, en suivant un patron d'intervalles précis.\n\nLa gamme de Do majeur est la plus simple : elle utilise uniquement les touches blanches.\n\nDo — Ré — Mi — Fa — Sol — La — Si — Do",
                    highlight = "La gamme de Do est la seule gamme qui n'utilise aucune touche noire. C'est pourquoi on commence toujours par elle."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "Le passage du pouce",
                    body = "On a seulement 5 doigts pour 8 notes — il faut donc passer le pouce sous la main.\n\nDoigté main droite :\nDo(1) — Ré(2) — Mi(3) — Fa(1) — Sol(2) — La(3) — Si(4) — Do(5)\n\nLe moment clé : après Mi (doigt 3), le pouce passe sous la main pour atterrir sur Fa. Ce geste s'appelle le \"passage de pouce\" — il doit être invisible, sans que le poignet se soulève.",
                    highlight = "Le pouce passe SOUS les doigts, pas par-dessus. Commence à préparer le mouvement dès que tu joues le Mi."
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : préparer le passage du pouce",
                    body = "Pose la main sur Do-Ré-Mi (doigts 1-2-3). Joue Do-Ré-Mi lentement. Au moment où le doigt 3 joue Mi, glisse le pouce sous la main pour le placer au-dessus de Fa — sans jouer Fa encore. Répète ce seul mouvement 20 fois.",
                    exercise = Exercise(
                        instruction = "Joue uniquement Do-Ré-Mi + passage de pouce, sans aller plus loin.",
                        notes = listOf("Do", "Ré", "Mi"),
                        duration = "3 minutes",
                        goal = "Passage de pouce fluide et invisible"
                    )
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : gamme complète",
                    body = "Joue la gamme entière très lentement : Do-Ré-Mi-Fa-Sol-La-Si-Do, puis redescends. Compte un temps par note. Ne t'arrête pas si tu fais une erreur — continue.",
                    exercise = Exercise(
                        instruction = "Monte et descends la gamme 5 fois de suite.",
                        notes = listOf("Do", "Ré", "Mi", "Fa", "Sol", "La", "Si", "Do"),
                        duration = "4 minutes",
                        goal = "Gamme complète sans pause au passage de pouce"
                    )
                )
            ),
            recap = "La gamme de Do : Do-Ré-Mi-Fa-Sol-La-Si-Do, uniquement des touches blanches. Le doigté 1-2-3-1-2-3-4-5 avec passage de pouce après Mi. À travailler chaque jour !"
        ),

        // ── LEÇON 6 ──────────────────────────────────────────────────────
        LessonData(
            id = 6,
            title = "La main gauche",
            subtitle = "Do Ré Mi Fa Sol — main gauche",
            emoji = "🤚",
            duration = "10 min",
            xpReward = 25,
            objective = "Jouer Do-Ré-Mi-Fa-Sol avec la main gauche avec le bon doigté.",
            steps = listOf(
                LessonStep(
                    type = StepType.THEORY,
                    title = "La main gauche et la clé de Fa",
                    body = "La main gauche joue généralement dans le registre grave du piano — les notes à gauche du Do central.\n\nDans la notation musicale, la main gauche utilise la clé de Fa (contrairement à la clé de Sol pour la main droite). Mais pour l'instant, on joue à l'oreille et par repères visuels.",
                    highlight = "La main gauche a tendance à être négligée par les débutants. Accorde-lui autant d'attention qu'à la main droite — c'est elle qui donne la puissance harmonique."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "Le doigté main gauche",
                    body = "Pour jouer Do-Ré-Mi-Fa-Sol avec la main gauche, on utilise l'ordre inverse des doigts :\n\nDo(5) — Ré(4) — Mi(3) — Fa(2) — Sol(1)\n\nLe pouce (1) est sur Sol, l'auriculaire (5) est sur Do. C'est l'image miroir de la main droite.",
                    highlight = "Logique : les deux mains se font face. Là où la main droite commence par le pouce (1) sur Do, la main gauche termine par le pouce (1) sur Sol."
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : main gauche Do → Sol",
                    body = "Place l'auriculaire gauche (doigt 5) sur le Do situé à gauche du Do central. Pose les 4 autres doigts : 4 sur Ré, 3 sur Mi, 2 sur Fa, 1 sur Sol.\n\nJoue Do-Ré-Mi-Fa-Sol (5-4-3-2-1) puis redescends Sol-Fa-Mi-Ré-Do (1-2-3-4-5).",
                    exercise = Exercise(
                        instruction = "Monte et descends 5 fois, lentement, en comptant.",
                        notes = listOf("Do", "Ré", "Mi", "Fa", "Sol"),
                        duration = "3 minutes",
                        goal = "Enchaîner les 5 notes main gauche sans hésitation"
                    )
                ),
                LessonStep(
                    type = StepType.TIP,
                    title = "Astuce : alterner les mains",
                    body = "Joue Do-Ré-Mi-Fa-Sol avec la main droite, puis immédiatement avec la main gauche, en alternant. Les deux mains jouent les mêmes notes mais pas en même temps.\n\nCet exercice révèle souvent que la main gauche est moins précise — c'est normal ! Elle se développe avec la pratique.",
                    highlight = "La main non-dominante a besoin de 2 à 3 fois plus de répétitions pour atteindre le même niveau de précision."
                )
            ),
            recap = "Main gauche : Do(5)-Ré(4)-Mi(3)-Fa(2)-Sol(1). La logique est symétrique à la main droite. Travailler les deux mains séparément avant de les combiner.",
            quiz  = quiz6
        ),

        // ── LEÇON 7 ──────────────────────────────────────────────────────
        LessonData(
            id = 7,
            title = "Lire une partition simple",
            subtitle = "Portée, clé de Sol et notes",
            emoji = "📄",
            duration = "12 min",
            xpReward = 30,
            objective = "Déchiffrer les notes sur une portée en clé de Sol.",
            steps = listOf(
                LessonStep(
                    type = StepType.THEORY,
                    title = "La portée et la clé de Sol",
                    body = "La notation musicale s'écrit sur une portée : 5 lignes horizontales parallèles. Les notes se placent sur les lignes ou dans les espaces entre elles.\n\nLa clé de Sol est le symbole en forme de spirale placé au début. Elle indique que la 2e ligne (en partant du bas) correspond à la note Sol.",
                    highlight = "Toute la notation musicale découle de cette convention : une fois qu'on sait où est le Sol, on peut déduire toutes les autres notes."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "Les notes sur la portée",
                    body = "En clé de Sol, de bas en haut :\n\nLignes (bas → haut) : Mi — Sol — Si — Ré — Fa\nEspaces (bas → haut) : Fa — La — Do — Mi\n\nMoyen mnémotechnique pour les lignes : \"Ma Grand-Mère Se Rase Facilement\" (Mi-Sol-Si-Ré-Fa).\n\nPour les espaces : \"FACE\" en anglais (Fa-La-Do-Mi en français).",
                    highlight = "\"Ma Grand-Mère Se Rase Facilement\" — un peu bizarre, mais ça reste !"
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "Les figures de notes",
                    body = "Chaque note est représentée par un symbole qui indique à la fois sa hauteur (où elle est placée sur la portée) et sa durée :\n\n○ Ronde = tête blanche sans queue = 4 temps\n𝅗𝅥 Blanche = tête blanche avec queue = 2 temps\n♩ Noire = tête noire avec queue = 1 temps",
                    highlight = "La forme de la tête (blanche ou noire) et la présence d'une queue indiquent la durée. La position sur la portée indique la hauteur."
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : nommer des notes",
                    body = "Sur une portée imaginaire, place mentalement ces notes et nomme-les :\n• Une note sur la 1ère ligne → Mi\n• Une note dans le 1er espace → Fa\n• Une note sur la 2ème ligne → Sol\n• Une note dans le 2ème espace → La\n• Une note sur la 3ème ligne → Si",
                    exercise = Exercise(
                        instruction = "Répète l'exercice en sens inverse : on te dit la note, tu trouves sa position.",
                        duration = "3 minutes",
                        goal = "Nommer une note sur la portée en moins de 3 secondes"
                    )
                )
            ),
            recap = "La portée = 5 lignes. En clé de Sol : lignes = Mi-Sol-Si-Ré-Fa (\"Ma Grand-Mère...\"), espaces = Fa-La-Do-Mi. Les figures de notes indiquent la durée."
        ),

        // ── LEÇON 8 ──────────────────────────────────────────────────────
        LessonData(
            id = 8,
            title = "Mon premier morceau",
            subtitle = "Frère Jacques — main droite",
            emoji = "🎵",
            duration = "15 min",
            xpReward = 40,
            objective = "Jouer la mélodie de Frère Jacques avec la main droite.",
            steps = listOf(
                LessonStep(
                    type = StepType.THEORY,
                    title = "Présentation du morceau",
                    body = "Frère Jacques est un canon français du 17e siècle — l'une des mélodies les plus connues au monde. Elle utilise uniquement Do-Ré-Mi-Fa-Sol, les 5 notes que tu as déjà apprises !\n\nLa mélodie se divise en 4 phrases de 4 temps chacune.",
                    highlight = "Chanter la mélodie avant de la jouer est une technique professionnelle. Ton oreille guidera tes doigts."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "La partition simplifiée",
                    body = "Phrase 1 : Do — Ré — Mi — Do (1-2-3-1)\nPhrase 2 : Do — Ré — Mi — Do (1-2-3-1)\nPhrase 3 : Mi — Fa — Sol (tenu) (3-4-5)\nPhrase 4 : Mi — Fa — Sol (tenu) (3-4-5)\n\nLe Sol de la phrase 3 et 4 est une blanche (2 temps). Toutes les autres notes sont des noires (1 temps).",
                    highlight = "Repère les répétitions : la phrase 1 = phrase 2, la phrase 3 = phrase 4. Tu n'as en réalité que 2 phrases à apprendre !"
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : phrase par phrase",
                    body = "Apprends d'abord la phrase 1 seule : Do-Ré-Mi-Do. Répète jusqu'à ce qu'elle soit automatique (sans réfléchir aux doigts).\n\nPuis la phrase 3 : Mi-Fa-Sol(tenu).\n\nEnfin, enchaîne les 4 phrases d'affilée.",
                    exercise = Exercise(
                        instruction = "Joue le morceau entier 3 fois de suite sans pause.",
                        notes = listOf("Do", "Ré", "Mi", "Do", "Mi", "Fa", "Sol"),
                        duration = "5 minutes",
                        goal = "Jouer Frère Jacques du début à la fin"
                    )
                ),
                LessonStep(
                    type = StepType.TIP,
                    title = "Aller plus loin",
                    body = "Une fois la main droite fluide, essaie d'ajouter la main gauche : joue simplement Do (main gauche, doigt 5) sur le temps 1 de chaque mesure, comme un point d'ancrage.",
                    highlight = "Un seul Do de main gauche à chaque mesure est déjà une forme d'accompagnement. La musique naît de la coordination des deux mains."
                )
            ),
            recap = "Tu as joué ton premier morceau ! Frère Jacques = 4 phrases, notes Do à Sol, rythme noires et une blanche. Apprendre phrase par phrase est la méthode de tous les pianistes professionnels."
        ),

        // ── LEÇON 9 ──────────────────────────────────────────────────────
        LessonData(
            id = 9,
            title = "Les touches noires",
            subtitle = "Dièses, bémols et demi-tons",
            emoji = "🖤",
            duration = "10 min",
            xpReward = 30,
            objective = "Comprendre ce que sont les dièses et bémols, et les jouer.",
            steps = listOf(
                LessonStep(
                    type = StepType.THEORY,
                    title = "Les demi-tons",
                    body = "Entre deux notes voisines (une touche blanche et la noire à côté), il y a un demi-ton. C'est le plus petit intervalle dans la musique occidentale.\n\nUn ton = 2 demi-tons. Entre Do et Ré, il y a un ton (avec la touche noire Do# entre les deux).\n\nException : entre Mi et Fa, et entre Si et Do, il n'y a pas de touche noire — ces intervalles sont des demi-tons naturels.",
                    highlight = "Mi-Fa et Si-Do sont les deux seuls endroits du clavier sans touche noire entre eux. C'est une propriété unique de la gamme majeure."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "Dièse et bémol",
                    body = "# (dièse) = hausser d'un demi-ton. Do# est la touche noire juste à droite de Do.\n♭ (bémol) = baisser d'un demi-ton. Ré♭ est la touche noire juste à gauche de Ré.\n\nDo# et Ré♭ sont la MÊME touche physique, vue depuis deux angles différents. On appelle ça des enharmoniques.",
                    highlight = "Do# = Ré♭ : même touche, deux noms différents selon le contexte musical."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "Les 5 touches noires",
                    body = "Dans une octave, il y a 5 touches noires :\n\nGroupe de 2 : Do# (=Ré♭) — Ré# (=Mi♭)\nGroupe de 3 : Fa# (=Sol♭) — Sol# (=La♭) — La# (=Si♭)\n\nTu peux les voir comme des touches \"entre\" les blanches correspondantes.",
                    highlight = "Note bien l'absence de Mi# et de Si# : entre Mi-Fa et Si-Do, il n'y a naturellement pas de demi-ton supérieur."
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : jouer les touches noires",
                    body = "Joue les 5 touches noires d'une octave, de gauche à droite : Do#, Ré#, Fa#, Sol#, La#. Prononce leur nom. Puis descends. Répète en utilisant aussi le nom bémol : Ré♭, Mi♭, Sol♭, La♭, Si♭.",
                    exercise = Exercise(
                        instruction = "Joue chaque touche noire en nommant son nom dièse ET son nom bémol.",
                        notes = listOf("Do#", "Ré#", "Fa#", "Sol#", "La#"),
                        duration = "3 minutes",
                        goal = "Localiser les 5 touches noires et les nommer"
                    )
                )
            ),
            recap = "Les touches noires sont des dièses ou des bémols selon le contexte. Un demi-ton = l'écart le plus petit. Mi-Fa et Si-Do n'ont pas de touche noire entre eux. Do# = Ré♭ (enharmoniques).",
            quiz  = quiz9
        ),

        // ── LEÇON 10 ──────────────────────────────────────────────────────
        LessonData(
            id = 10,
            title = "Premiers accords",
            subtitle = "Do majeur, Sol majeur, Fa majeur",
            emoji = "🎶",
            duration = "15 min",
            xpReward = 50,
            objective = "Jouer les 3 accords fondamentaux de Do majeur, Sol majeur et Fa majeur.",
            steps = listOf(
                LessonStep(
                    type = StepType.THEORY,
                    title = "Qu'est-ce qu'un accord ?",
                    body = "Un accord, c'est plusieurs notes jouées simultanément. En musique tonal (pop, rock, classique, jazz...), la grande majorité des morceaux repose sur des enchaînements d'accords.\n\nL'accord le plus simple est la triade : 3 notes jouées en même temps. Un accord majeur donne une sensation lumineuse et joyeuse.",
                    highlight = "3 accords suffisent pour accompagner des centaines de chansons : Do majeur, Fa majeur et Sol majeur."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "Do majeur",
                    body = "L'accord de Do majeur est composé de 3 notes : Do — Mi — Sol.\n\nDoigté main droite : 1 (Do) — 3 (Mi) — 5 (Sol)\nDoigté main gauche : 5 (Do) — 3 (Mi) — 1 (Sol)\n\nToutes les 3 notes sont des touches blanches. Pose les 3 doigts simultanément et appuie.",
                    highlight = "C'est l'accord le plus naturel du piano : 3 touches blanches en sautant une à chaque fois (Do → skip Ré → Mi → skip Fa → Sol)."
                ),
                LessonStep(
                    type = StepType.THEORY,
                    title = "Fa majeur et Sol majeur",
                    body = "Fa majeur = Fa — La — Do (doigts 1-2-5 main droite)\nSol majeur = Sol — Si — Ré (doigts 1-2-5 main droite)\n\nCes 3 accords (Do, Fa, Sol) forment les degrés I, IV et V en Do majeur — la progression harmonique la plus courante de toute la musique occidentale.",
                    highlight = "I — IV — V en Do majeur : centaines de chansons utilisent exactement cette suite. Tu peux déjà accompagner \"La Bamba\", \"Twist and Shout\" et bien d'autres."
                ),
                LessonStep(
                    type = StepType.EXERCISE,
                    title = "Exercice : enchaîner les 3 accords",
                    body = "Joue Do majeur, tenu 2 temps. Puis Fa majeur, 2 temps. Puis Sol majeur, 2 temps. Puis reviens à Do, 2 temps.\n\nFais la transition lentement — les doigts glissent d'un accord à l'autre sans lever la main.",
                    exercise = Exercise(
                        instruction = "Enchaîne Do — Fa — Sol — Do 4 fois de suite.",
                        notes = listOf("Do+Mi+Sol", "Fa+La+Do", "Sol+Si+Ré"),
                        duration = "5 minutes",
                        goal = "Changer d'accord sans interruption du tempo"
                    )
                ),
                LessonStep(
                    type = StepType.TIP,
                    title = "Tu as terminé le parcours débutant !",
                    body = "Tu connais maintenant le clavier, la posture, les 5 premières notes de chaque main, le rythme, la gamme de Do, la lecture de partition, Frère Jacques, les touches noires et 3 accords fondamentaux.\n\nLa prochaine étape : jouer des morceaux complets main droite + main gauche, et explorer d'autres gammes.",
                    highlight = "🎉 Félicitations ! Tu as les fondations d'un vrai pianiste. La régularité (même 10 minutes par jour) est plus efficace que de longues sessions rares."
                )
            ),
            recap = "Do majeur (Do-Mi-Sol), Fa majeur (Fa-La-Do), Sol majeur (Sol-Si-Ré). Ces 3 accords = la base harmonique de toute la musique populaire. Tu as terminé le parcours débutant !"
        )
    )
}

// ── QCM ───────────────────────────────────────────────────────────────────

data class QuizQuestion(
    val question: String,
    val answers: List<String>,      // 4 réponses
    val correctIndex: Int           // index de la bonne réponse (0-3)
)

data class LessonQuiz(
    val questions: List<QuizQuestion>
)

// QCM après leçon 3 — notes et gamme de Do
val quiz3 = LessonQuiz(listOf(
    QuizQuestion(
        question     = "Quelle est la première note jouée avec le pouce (doigt 1) en position de 5 doigts ?",
        answers      = listOf("Sol", "Do", "Fa", "Ré"),
        correctIndex = 1
    ),
    QuizQuestion(
        question     = "Combien y a-t-il de notes dans la gamme de Do ?",
        answers      = listOf("5", "6", "7", "8"),
        correctIndex = 2
    ),
    QuizQuestion(
        question     = "Quel doigt joue la note Mi en position de 5 doigts (main droite) ?",
        answers      = listOf("Doigt 1 (pouce)", "Doigt 2 (index)", "Doigt 3 (majeur)", "Doigt 4 (annulaire)"),
        correctIndex = 2
    ),
    QuizQuestion(
        question     = "Dans la gamme de Do, quelle note suit Mi ?",
        answers      = listOf("Sol", "Ré", "La", "Fa"),
        correctIndex = 3
    )
))

// QCM après leçon 6 — main gauche, rythme et gamme complète
val quiz6 = LessonQuiz(listOf(
    QuizQuestion(
        question     = "Quel doigt joue le Do avec la main gauche en position de 5 doigts ?",
        answers      = listOf("Doigt 1 (pouce)", "Doigt 3 (majeur)", "Doigt 4 (annulaire)", "Doigt 5 (auriculaire)"),
        correctIndex = 3
    ),
    QuizQuestion(
        question     = "Combien de temps dure une blanche ?",
        answers      = listOf("1 temps", "2 temps", "3 temps", "4 temps"),
        correctIndex = 1
    ),
    QuizQuestion(
        question     = "Comment s'appelle le passage du pouce sous la main dans la gamme ?",
        answers      = listOf("Croisement", "Pivot", "Passage de pouce", "Glissé"),
        correctIndex = 2
    ),
    QuizQuestion(
        question     = "Combien y a-t-il de temps dans une mesure à 4/4 ?",
        answers      = listOf("2", "3", "4", "6"),
        correctIndex = 2
    )
))

// QCM après leçon 9 — partition, Frère Jacques, touches noires
val quiz9 = LessonQuiz(listOf(
    QuizQuestion(
        question     = "Sur la portée en clé de Sol, sur quelle ligne se trouve le Sol ?",
        answers      = listOf("1ère ligne", "2ème ligne", "3ème ligne", "4ème ligne"),
        correctIndex = 1
    ),
    QuizQuestion(
        question     = "Do# et Ré♭ désignent-ils la même touche sur le piano ?",
        answers      = listOf("Non, ce sont deux touches différentes", "Oui, c'est la même touche", "Ça dépend de l'octave", "Seulement en Do majeur"),
        correctIndex = 1
    ),
    QuizQuestion(
        question     = "Combien y a-t-il de touches noires dans une octave ?",
        answers      = listOf("3", "4", "5", "6"),
        correctIndex = 2
    ),
    QuizQuestion(
        question     = "Quelle est la première phrase de Frère Jacques ?",
        answers      = listOf("Mi Fa Sol Mi", "Do Ré Mi Do", "Sol Fa Mi Do", "Do Mi Sol Do"),
        correctIndex = 1
    )
))

// ── Banque de questions de révision (pour les 2 questions aléatoires) ─────

val reviewQuestions = listOf(
    // Bases clavier
    QuizQuestion(
        question     = "Combien y a-t-il de notes dans une octave (touches blanches) ?",
        answers      = listOf("5", "6", "7", "8"),
        correctIndex = 2
    ),
    QuizQuestion(
        question     = "Quelle touche blanche se trouve toujours à gauche d'un groupe de 2 touches noires ?",
        answers      = listOf("Ré", "Mi", "Do", "Fa"),
        correctIndex = 2
    ),
    QuizQuestion(
        question     = "Comment s'appelle la répétition des 7 notes à une hauteur différente ?",
        answers      = listOf("Une mesure", "Une octave", "Une gamme", "Un accord"),
        correctIndex = 1
    ),
    // Posture
    QuizQuestion(
        question     = "Quel objet imaginer pour trouver la bonne forme de la main au piano ?",
        answers      = listOf("Une orange", "Une balle de tennis", "Un livre", "Une pomme"),
        correctIndex = 1
    ),
    QuizQuestion(
        question     = "Quel numéro de doigt correspond au pouce ?",
        answers      = listOf("0", "1", "2", "5"),
        correctIndex = 1
    ),
    // Rythme
    QuizQuestion(
        question     = "Quelle figure de note dure 4 temps ?",
        answers      = listOf("La noire", "La blanche", "La croche", "La ronde"),
        correctIndex = 3
    ),
    QuizQuestion(
        question     = "BPM signifie :",
        answers      = listOf("Battements par mesure", "Battements par minute", "Basses par mesure", "Blanches par minute"),
        correctIndex = 1
    ),
    // Gamme
    QuizQuestion(
        question     = "La gamme de Do utilise des touches noires ?",
        answers      = listOf("Oui, toutes", "Oui, quelques-unes", "Non, aucune", "Seulement Do#"),
        correctIndex = 2
    ),
    QuizQuestion(
        question     = "Quel est l'ordre correct des 5 premières notes de la gamme de Do ?",
        answers      = listOf("Do Mi Ré Fa Sol", "Do Ré Mi Fa Sol", "Ré Mi Fa Sol La", "Do Ré Mi Sol Fa"),
        correctIndex = 1
    ),
    // Main gauche
    QuizQuestion(
        question     = "Sur quel doigt de la main gauche repose Sol en position de 5 doigts ?",
        answers      = listOf("Doigt 5 (auriculaire)", "Doigt 3 (majeur)", "Doigt 2 (index)", "Doigt 1 (pouce)"),
        correctIndex = 3
    ),
    // Accords
    QuizQuestion(
        question     = "Combien de notes compose un accord triade ?",
        answers      = listOf("2", "3", "4", "5"),
        correctIndex = 1
    ),
    QuizQuestion(
        question     = "Quelles notes forment l'accord de Do majeur ?",
        answers      = listOf("Do Ré Mi", "Do Mi Sol", "Do Fa La", "Ré Fa La"),
        correctIndex = 1
    ),
)
