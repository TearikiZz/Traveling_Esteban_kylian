# Traveling_Esteban_kylian

Application Android autour du voyage, divisée en deux volets principaux :
- `TravelShare` : partage et découverte de photos de voyage.
- `TravelPath` : autre partie du projet.

Le travail documenté ici concerne principalement `TravelShare`.

## Ouvrir et lancer l'application

### Prérequis
- Android Studio installé
- SDK Android disponible
- Un émulateur Android ou un téléphone Android branché

### Lancement rapide
1. Cloner le dépôt :
```bash
git clone https://github.com/TearikiZz/Traveling_Esteban_kylian.git
```
2. Ouvrir le dossier du projet dans Android Studio.
3. Laisser Android Studio faire le `Gradle Sync`.
4. Sélectionner un émulateur ou un appareil.
5. Lancer l'application avec `Run`.

### Configuration technique
- `minSdk` : 24
- `targetSdk` : 35
- Wrapper Gradle inclus dans le dépôt
- Base locale : `Room`
- UI : `AppCompat` + `Material`

## Paramètres avant lancement

### Fichier `local.properties`

L'application peut être lancée sans clés API, mais certaines fonctionnalités deviennent limitées.

Exemple minimal :

```properties
PLACES_API_KEY=VOTRE_CLE_PLACES
GEMINI_API_KEY=VOTRE_CLE_GEMINI
GEMINI_MODEL=gemini-2.5-flash
```

### Permissions à prévoir

- `INTERNET` : nécessaire pour Google Places et Gemini
- `RECORD_AUDIO` : nécessaire pour enregistrer un message vocal
- `POST_NOTIFICATIONS` : nécessaire pour les notifications système Android

Au premier lancement :
- autoriser le micro si vous voulez tester le message vocal
- autoriser les notifications si vous voulez tester les alertes locales

## Clés API et services

### Google Places

La fonctionnalité d'autocomplétion de lieu utilise Google Places.

- L'application peut se compiler et se lancer sans clé API.
- Si la clé est absente ou si l'autocomplétion n'est pas disponible, la publication reste accessible grâce à une saisie manuelle du lieu.
- Aucun `Maps SDK` supplémentaire n'est requis dans la version actuelle car il n'y a pas de vue carte embarquée.

Pour activer cette fonctionnalité, ajouter dans `local.properties` :

```properties
PLACES_API_KEY=VOTRE_CLE_API
```

### Gemini API

La suggestion IA du résumé et des tags utilise Gemini.

- Le site officiel pour créer une clé est [Google AI Studio](https://aistudio.google.com/apikey).
- La documentation officielle explique que les clés Gemini se créent et se gèrent depuis la page API Keys de Google AI Studio : [Using Gemini API keys](https://ai.google.dev/gemini-api/docs/api-key).
- La documentation officielle indique aussi qu'il existe un `Free tier` pour les utilisateurs éligibles, avec des limites par modèle et par jour : [Gemini API rate limits](https://ai.google.dev/gemini-api/docs/rate-limits).

Pour activer l'annotation IA, ajouter dans `local.properties` :

```properties
GEMINI_API_KEY=VOTRE_CLE_GEMINI
GEMINI_MODEL=gemini-2.5-flash
```

Étapes rapides :
1. Ouvrir [Google AI Studio](https://aistudio.google.com/apikey).
2. Se connecter avec un compte Google.
3. Ouvrir la page `API Keys`.
4. Créer une clé Gemini ou utiliser la clé du projet par défaut si elle est proposée.
5. Copier la clé dans `local.properties`.

Attention :
- ne committez jamais `local.properties`
- pour une vraie application de production, il ne faut pas exposer la clé directement dans l'application mobile

## Fonctionnalités TravelShare disponibles

- authentification : connexion, inscription, déconnexion et mode anonyme
- flux de publications avec affichage `liste` et `grille`
- recherche texte et recherche vocale
- filtres par type de lieu, auteur et période
- consultation du détail d'une publication
- likes, signalement, commentaires
- création de publication avec :
  - photo
  - description
  - tags
  - message vocal
  - choix d'un groupe
  - choix d'un type de lieu
  - lieu via Google Places
  - fallback de saisie manuelle du lieu
  - suggestion IA de résumé et tags via Gemini
- gestion des groupes : créer, rejoindre, quitter, consulter
- notifications locales liées à l'activité TravelShare
- profil utilisateur avec :
  - photo
  - statistiques
  - préférences
  - thème clair / sombre / système
  - langue français / anglais

## Limites connues

- pas de vue carte embarquée avec pins dans cette version
- pas de traduction espagnole complète dans cette version
- sans `PLACES_API_KEY`, l'autocomplétion Google est désactivée mais la saisie manuelle du lieu reste disponible
- sans `GEMINI_API_KEY`, l'annotation IA est désactivée mais la création de publication reste disponible

## Comptes de démonstration

Des comptes de test sont préchargés dans l'application :

- `kylian` / `kylian123`
- `esteban` / `esteban123`
- `maya` / `maya123`

Il est aussi possible d'utiliser le mode anonyme depuis l'écran principal ou de se créer un compte 

## Rapports et documents

- Rapport dépot 1 : [Google Docs](https://docs.google.com/document/d/1o7mm7aCbfqujxYhBLlrR-QNlFFKafq1opXZrzC9ERCA/edit?usp=sharing)
- Rapport dépot 2 : [Google Docs](https://docs.google.com/document/d/1setkHzDUsPspcDI7uroq2E83XlOIPxHL6HPTQDQ_DJs/edit?tab=t.0)

## Remarques

- Le dépôt est public et contient ce qu'il faut pour importer le projet dans Android Studio.
- Pour une évaluation, le plus simple est d'utiliser Android Studio avec un émulateur déjà configuré.
- Pour une démonstration complète de `TravelShare`, il est recommandé de renseigner `PLACES_API_KEY` et `GEMINI_API_KEY`.
