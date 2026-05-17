# Traveling_Esteban_kylian

Application Android autour du voyage, divisée en deux volets principaux :
- `TravelShare` : partage et découverte de photos de voyage.
- `TravelPath` : autre partie du projet.

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

## Clés API

### Google Places

La fonctionnalité d'autocomplétion de lieu utilise Google Places.

- L'application peut se compiler et se lancer sans clé API.
- Si la clé est absente, la publication reste accessible mais l'autocomplétion de lieu est désactivée.

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

## Comptes de démonstration

Des comptes de test sont préchargés dans l'application :

- `kylian` / `kylian123`
- `esteban` / `esteban123`
- `maya` / `maya123`

Il est aussi possible d'utiliser le mode anonyme depuis l'écran principal.

## Rapports et documents

- Rapport / document existant : [Google Docs](https://docs.google.com/document/d/1o7mm7aCbfqujxYhBLlrR-QNlFFKafq1opXZrzC9ERCA/edit?usp=sharing)
- Rapport complémentaire : [Google Docs](https://docs.google.com/document/d/1setkHzDUsPspcDI7uroq2E83XlOIPxHL6HPTQDQ_DJs/edit?tab=t.0)

## Remarques

- Le dépôt est public et contient ce qu'il faut pour importer le projet dans Android Studio.
- Pour une évaluation, le plus simple est d'utiliser Android Studio avec un émulateur déjà configuré.
